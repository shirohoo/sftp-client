package io.github.shirohoo.sftp;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public final class DefaultSftpClient implements SftpClient {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSftpClient.class);

    private static final String STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";

    private final SftpProperties properties;

    public DefaultSftpClient(SftpProperties properties) {
        this.properties = properties;
    }

    private ChannelSftp connectByPassword() throws JSchException {
        log.info("Try to connect sftp[{}@{}], use password[{}]", properties.getUsername(), properties.getHost(), properties.getPassword());
        final Session session = createSession(new JSch(), properties.getHost(), properties.getUsername(), properties.getPort());
        session.setPassword(properties.getPassword());
        return getChannelSftp(session);
    }

    private ChannelSftp connectByPrivateKey() throws JSchException {
        return getChannelSftp(createSession(getJsch(), properties.getHost(), properties.getUsername(), properties.getPort()));
    }

    private JSch getJsch() {
        final JSch jsch = new JSch();
        if (StringUtils.isNotBlank(properties.getPrivateKey())) {
            addIdentity(jsch);
        }
        log.info("Try to connect sftp[{}@{}], use private key[{}] with passphrase[{}]", properties.getUsername(), properties.getHost(), properties.getPrivateKey(), properties.getPassphrase());
        return jsch;
    }

    private void addIdentity(final JSch jsch) {
        try {
            if (StringUtils.isNotBlank(properties.getPassphrase())) {
                jsch.addIdentity(properties.getPrivateKey(), properties.getPassphrase());
            } else {
                jsch.addIdentity(properties.getPrivateKey());
            }
        } catch (JSchException e) {
            log.error(e.getMessage(), e);
        }
    }

    private Session createSession(final JSch jsch, final String host, final String username, final int port) throws JSchException {
        final Session session = getSession(jsch, host, username, port);
        session.setConfig(STRICT_HOST_KEY_CHECKING, properties.getSessionStrictHostKeyChecking());
        return session;
    }

    private Session getSession(final JSch jsch, final String host, final String username, final int port) throws JSchException {
        return port <= 0 ? jsch.getSession(username, host) : jsch.getSession(username, host, port);
    }

    private void disconnect(final ChannelSftp sftp) throws JSchException {
        if (isNull(sftp)) {
            return;
        }
        if (sftp.isConnected()) {
            sftp.disconnect();
        }
        if (sftp.isClosed()) {
            log.info("Sftp is closed already");
        }
        if (nonNull(sftp.getSession())) {
            sftp.getSession().disconnect();
        }
    }

    private ChannelSftp getChannelSftp(final Session session) throws JSchException {
        try {
            session.connect(properties.getSessionConnectTimeout());
        } catch (JSchException e) {
            log.error(e.getMessage(), e);
        }
        log.info("Session connected to {}", properties.getHost());
        return getChannel(session);
    }

    private ChannelSftp getChannel(final Session session) throws JSchException {
        Channel channel = session.openChannel(properties.getProtocol());
        channel.connect(properties.getChannelConnectedTimeout());
        log.info("Channel created to {}", properties.getHost());
        return (ChannelSftp) channel;
    }

    /**
     * Pass the path of the file you want to read as an argument. The starting path is the root of SftpProperties. For example, if root is ~/ and the path passed as an argument is user/temp/someFile.txt , SftpClient reads ~/user/temp/someFile.txt and
     * returns it as a File object. At this time, the transferred file is not saved on the client hard disk, but only in client memory.
     *
     * @param targetPath String
     * @return File
     */
    @Override
    public File read(final String targetPath) throws JSchException, NotDirectoryException {
        return readFile(targetPath, getChannelSftp());
    }

    private File readFile(final String targetPath, final ChannelSftp sftp) throws JSchException, NotDirectoryException {
        try {
            sftp.cd(properties.getRoot());
            log.info("Change directory to {}", properties.getRoot());
            try (InputStream inputStream = sftp.get(targetPath)) {
                return convertInputStreamToFile(inputStream, getFileName(targetPath));
            }
        } catch (Exception e) {
            throw new NotDirectoryException(
                String.format("Download file failure. target path: %s", targetPath)
            );
        } finally {
            disconnect(sftp);
            log.info("Disconnected sftp connection.");
        }
    }

    private File convertInputStreamToFile(final InputStream inputStream, final String fileName) throws IOException {
        File file = new File(fileName);
        IOUtils.copy(inputStream, new FileOutputStream(file));
        file.deleteOnExit();
        return file;
    }

    private ChannelSftp getChannelSftp() throws JSchException {
        return properties.getKeyMode() ? connectByPrivateKey() : connectByPassword();
    }

    /**
     * Pass the path of the file you want to read as an argument. The starting path is the root of SftpProperties. For example, if root is ~/ and the path passed as an argument is user/temp/someDir (last args is directory name) , SftpClient reads
     * ~/user/temp/someDir and returns it as a List {@literal <}File{@literal >} object.
     *
     * @param targetDirPath String
     * @return List {@literal <}File{@literal >}
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<File> listFiles(final String targetDirPath) throws JSchException, NoSuchFileException {
        final ChannelSftp sftp = getChannelSftp();
        List<File> files = new ArrayList<>();
        try {
            Vector<ChannelSftp.LsEntry> list = sftp.ls(targetDirPath);
            for (ChannelSftp.LsEntry entry : list) {
                if (isFile(entry)) {
                    String filePath = targetDirPath + "/" + entry.getFilename();
                    files.add(convertInputStreamToFile(sftp.get(filePath), getFileName(filePath)));
                }
            }
            return files;
        } catch (Exception e) {
            log.error("Download file list failure. target path: {}", targetDirPath);
            throw new NoSuchFileException(targetDirPath);
        } finally {
            disconnect(sftp);
            log.info("Disconnected sftp connection.");
        }
    }

    private static boolean isFile(LsEntry lsEntry) {
        return !lsEntry.getAttrs().isDir();
    }

    /**
     * The location where you want to upload the file is passed as the first argument, and the file you want to upload as the second argument. In this case, the first argument must also include the name of the file.
     *
     * @param targetPath String
     * @param uploadFile File
     * @return boolean
     */
    @Override
    public boolean upload(final String targetPath, final File uploadFile) throws JSchException {
        try {
            return upload(targetPath, new FileInputStream(uploadFile));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * The location where you want to upload the file is passed as the first argument, and the InputStream of file you want to upload as the second argument. In this case, the first argument must also include the name of the file.
     *
     * @param targetPath       String
     * @param uploadFileStream InputStream
     * @return boolean
     */
    @Override
    public boolean upload(final String targetPath, final InputStream uploadFileStream) throws IOException, JSchException {
        final ChannelSftp sftp = getChannelSftp();
        try {
            sftp.cd(properties.getRoot());
            log.info("Change path to {}", properties.getRoot());

            sftp.put(uploadFileStream, getFileName(targetPath, sftp, targetPath.lastIndexOf("/")));
            return true;
        } catch (SftpException e) {
            log.error("Found not root directory. path: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Upload file failure. path: {}", targetPath);
            return false;
        } finally {
            uploadFileStream.close();
            log.info("Closed input stream.");
            disconnect(sftp);
            log.info("Disconnected sftp connection.");
        }
    }

    private String getFileName(final String targetPath, final ChannelSftp sftp, final int index) {
        if (index != -1) {
            final String dirName = targetPath.substring(0, index);
            createUpstreamDirs(dirName, sftp);
            return targetPath.substring(index + 1);
        }
        return targetPath;
    }

    private void createUpstreamDirs(final String dirPath, final ChannelSftp sftp) {
        if (isNonBlank(dirPath) && nonNull(sftp)) {
            stream(stream(dirPath.split("/"))
                .filter(StringUtils::isNotBlank)
                .toArray(String[]::new))
                .forEach(dir -> mkdir(sftp, dir));
        }
    }

    private boolean isNonBlank(final String dirPath) {
        return nonNull(dirPath) && !dirPath.isEmpty();
    }

    private void mkdir(final ChannelSftp sftp, final String dir) {
        try {
            sftp.cd(dir);
            log.info("Change directory {}", dir);
        } catch (Exception e) {
            try {
                sftp.mkdir(dir);
                log.info("Create directory {}", dir);
            } catch (SftpException e1) {
                log.error("Create directory failure, directory:{}", dir);
            }
            try {
                sftp.cd(dir);
                log.info("Change directory {}", dir);
            } catch (SftpException e1) {
                log.error("Change directory failure, directory:{}", dir);
            }
        }
    }

    /**
     * If you pass the path to the file you want to remove as an argument, it will try to remove the file and return whether the removal succeeded or failed.
     *
     * @param targetPath String
     * @return boolean
     */
    @Override
    public boolean remove(final String targetPath) throws JSchException {
        final ChannelSftp sftp = getChannelSftp();
        try {
            sftp.cd(properties.getRoot());
            sftp.rm(targetPath);
            return true;
        } catch (SftpException e) {
            log.error("Delete file failure. path: {}", targetPath);
            return false;
        } finally {
            disconnect(sftp);
            log.info("Disconnected sftp connection.");
        }
    }

    /**
     * Pass the path to the file you want to download from the remote server as the first argument. In this case, the all argument must include a file name. Enter the location where you want to save the downloaded file as the second argument. Returns
     * true if the download is successful, false if it fails
     *
     * @param targetPath   String
     * @param downloadPath Path
     * @return boolean
     */
    @Override
    public boolean download(final String targetPath, final Path downloadPath) throws JSchException {
        final String path = downloadPath.toString();
        if (!isMkdir(path)) {
            return false;
        }
        return download(targetPath, path);
    }

    private boolean isMkdir(final String path) {
        return createDownstreamDirs(getLocation(path));
    }

    private String getLocation(final String path) {
        try {
            return path.substring(0, path.lastIndexOf(File.separator));
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(
                String.format("Invalid download path: %s. please check '/' or '\\'", path)
            );
        }
    }

    private boolean createDownstreamDirs(final String location) {
        final File folder = new File(location);
        if (!folder.exists()) {
            try {
                folder.mkdirs();
                log.info("Create folder: {}", folder.getPath());
                return true;
            } catch (Exception e) {
                log.error("{}. Can't create folder: {}", e.getMessage(), folder.getPath());
                return false;
            }
        }
        log.info("Tried to create a folder but failed. because folder already exists!");
        return false;
    }

    private boolean download(final String targetPath, final String path) throws JSchException {
        final ChannelSftp sftp = getChannelSftp();
        try (OutputStream outputStream = new FileOutputStream(path)) {
            sftp.cd(properties.getRoot());
            log.info("Change directory to {}", properties.getRoot());

            sftp.get(targetPath, outputStream);
            log.info("Download file success. download path: {}", path);
            return true;
        } catch (Exception e) {
            log.error("Download file failure. download path: {}", path);
            return false;
        } finally {
            disconnect(sftp);
            log.info("Disconnected sftp connection.");
        }
    }

    private String getFileName(final String filePath) {
        int index = filePath.lastIndexOf("/");
        return filePath.substring(index + 1);
    }

}
