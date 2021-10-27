package io.module.sftp;

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
import io.module.sftp.properties.SftpProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Collectors;
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

    @Override
    public File read(final String targetPath) throws JSchException, NotDirectoryException {
        return readFile(targetPath, getChannelSftp());
    }

    private File readFile(final String targetPath, final ChannelSftp sftp) throws JSchException, NotDirectoryException {
        try {
            sftp.cd(properties.getRoot());
            log.info("Change directory to {}", properties.getRoot());
            try (InputStream inputStream = sftp.get(targetPath)) {
                return convertInputStreamToFile(inputStream);
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

    private File convertInputStreamToFile(final InputStream inputStream) throws IOException {
        File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
        IOUtils.copy(inputStream, new FileOutputStream(tempFile));
        tempFile.deleteOnExit();
        return tempFile;
    }

    private ChannelSftp getChannelSftp() throws JSchException {
        return properties.getKeyMode() ? connectByPrivateKey() : connectByPassword();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<File> listFiles(final String dirPath) throws JSchException, NoSuchFileException {
        final ChannelSftp sftp = getChannelSftp();
        try {
            sftp.cd(properties.getRoot());
            log.info("Change directory to {}", properties.getRoot());
            return ((Vector<LsEntry>) sftp.ls(dirPath))
                .stream()
                .filter(DefaultSftpClient::isFile)
                .map(LsEntry::getFilename)
                .map(File::new)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Download file list failure. target path: {}", dirPath);
            throw new NoSuchFileException(dirPath);
        } finally {
            disconnect(sftp);
            log.info("Disconnected sftp connection.");
        }
    }

    private static boolean isFile(LsEntry lsEntry) {
        return !lsEntry.getAttrs().isDir();
    }

    @Override
    public boolean upload(final String targetPath, final File file) throws JSchException {
        try {
            return upload(targetPath, new FileInputStream(file));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean upload(final String targetPath, final InputStream inputStream) throws IOException, JSchException {
        final ChannelSftp sftp = getChannelSftp();
        try {
            sftp.cd(properties.getRoot());
            log.info("Change path to {}", properties.getRoot());

            sftp.put(inputStream, getFileName(targetPath, sftp, targetPath.lastIndexOf("/")));
            return true;
        } catch (SftpException e) {
            log.error("Found not root directory. path: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Upload file failure. path: {}", targetPath);
            return false;
        } finally {
            inputStream.close();
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

}
