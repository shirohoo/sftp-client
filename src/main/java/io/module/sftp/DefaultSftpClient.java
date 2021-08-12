package io.module.sftp;

import com.jcraft.jsch.*;
import io.module.sftp.properties.SftpProperties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

import static java.util.Arrays.stream;

public final class DefaultSftpClient implements SftpClient {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSftpClient.class);
    private static final String STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";

    private final SftpProperties properties;

    public DefaultSftpClient(SftpProperties properties) {
        this.properties = properties;
    }

    private ChannelSftp connectByPassword() {
        log.info("Try to connect sftp[{}@{}], use password[{}]", properties.getUsername(), properties.getHost(), properties.getPassword());
        final Session session = createSession(new JSch(), properties.getHost(), properties.getUsername(), properties.getPort());
        session.setPassword(properties.getPassword());
        return getChannelSftp(session);
    }

    private ChannelSftp connectByPrivateKey() {
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
            }
            else {
                jsch.addIdentity(properties.getPrivateKey());
            }
        }
        catch (JSchException e) {
            log.error(e.getMessage(), e);
        }
    }

    private Session createSession(final JSch jsch, final String host, final String username, final int port) {
        final Session session = getSession(jsch, host, username, port);
        session.setConfig(STRICT_HOST_KEY_CHECKING, properties.getSessionStrictHostKeyChecking());
        return Objects.requireNonNull(session, host + " must not be null!");
    }

    private Session getSession(final JSch jsch, final String host, final String username, final int port) {
        try {
            return port <= 0 ? jsch.getSession(username, host) : jsch.getSession(username, host, port);
        }
        catch (JSchException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private void disconnect(final ChannelSftp sftp) {
        try {
            if (Objects.nonNull(sftp)) {
                close(sftp);
            }
        }
        catch (JSchException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void close(final ChannelSftp sftp) throws JSchException {
        if (sftp.isConnected()) {
            sftp.disconnect();
            return;
        }
        if (sftp.isClosed()) {
            log.info("Sftp is closed already");
            return;
        }
        if (Objects.nonNull(sftp.getSession())) {
            sftp.getSession().disconnect();
            return;
        }
    }

    private ChannelSftp getChannelSftp(Session session) {
        try {
            session.connect(properties.getSessionConnectTimeout());
        }
        catch (JSchException e) {
            log.error(e.getMessage(), e);
        }
        log.info("Session connected to {}", properties.getHost());
        return getChannel(session);
    }

    private ChannelSftp getChannel(final Session session) {
        Channel channel = null;
        try {
            channel = session.openChannel(properties.getProtocol());
        }
        catch (JSchException e) {
            log.error(e.getMessage(), e);
        }
        try {
            channel.connect(properties.getChannelConnectedTimeout());
        }
        catch (JSchException e) {
            log.error(e.getMessage(), e);
        }
        log.info("Channel created to {}", properties.getHost());
        return (ChannelSftp) channel;
    }

    @Override
    public File read(String targetPath) {
        return readFile(targetPath, getChannelSftp());
    }

    private File readFile(final String targetPath, final ChannelSftp sftp) {
        try {
            sftp.cd(properties.getRoot());
            log.info("Change directory to {}", properties.getRoot());
            try (InputStream inputStream = sftp.get(targetPath)) {
                return convertInputStreamToFile(inputStream);
            }
        }
        catch (Exception e) {
            log.error("Download file failure. target path: {}", targetPath);
            return null;
        }
        finally {
            this.disconnect(sftp);
        }
    }

    private File convertInputStreamToFile(final InputStream inputStream) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
            IOUtils.copy(inputStream, new FileOutputStream(tempFile));
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        tempFile.deleteOnExit();
        return tempFile;
    }

    private ChannelSftp getChannelSftp() {
        return properties.getKeyMode() ? this.connectByPrivateKey() : this.connectByPassword();
    }

    @Override
    public boolean upload(String targetPath, File file) {
        try {
            return upload(targetPath, new FileInputStream(file));
        }
        catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean upload(String targetPath, InputStream inputStream) {
        final ChannelSftp sftp = getChannelSftp();
        try {
            sftp.cd(properties.getRoot());
            log.info("Change path to {}", properties.getRoot());

            sftp.put(inputStream, getFileName(targetPath, sftp, targetPath.lastIndexOf("/")));
            return true;
        }
        catch (SftpException e) {
            log.error("Found not root directory. path: {}", e.getMessage());
            return false;
        }
        catch (Exception e) {
            log.error("Upload file failure. path: {}", targetPath);
            return false;
        }
        finally {
            streamClose(inputStream);
            this.disconnect(sftp);
        }
    }

    private void streamClose(final InputStream inputStream) {
        try {
            inputStream.close();
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String getFileName(final String targetPath, final ChannelSftp sftp, final int index) throws Exception {
        if (index != -1) {
            verifyDirs(targetPath, sftp, targetPath.substring(0, index));
            return targetPath.substring(index + 1);
        }
        return targetPath;
    }

    private void verifyDirs(final String targetPath, final ChannelSftp sftp, final String dirName) throws Exception {
        if (!this.createUpstreamDirs(dirName, sftp)) {
            log.error("Remote path error. path:{}", targetPath);
            throw new Exception("Upload File failure");
        }
    }

    private boolean createUpstreamDirs(final String dirPath, final ChannelSftp sftp) {
        if (isNonBlank(dirPath) && Objects.nonNull(sftp)) {
            stream(stream(dirPath.split("/"))
                           .filter(StringUtils::isNotBlank)
                           .toArray(String[]::new))
                    .forEach(dir -> mkdir(sftp, dir));
            return true;
        }
        return false;
    }

    private boolean isNonBlank(final String dirPath) {
        return Objects.nonNull(dirPath) && !dirPath.isEmpty();
    }

    private void mkdir(final ChannelSftp sftp, final String dir) {
        try {
            sftp.cd(dir);
            log.info("Change directory {}", dir);
        }
        catch (Exception e) {
            try {
                sftp.mkdir(dir);
                log.info("Create directory {}", dir);
            }
            catch (SftpException e1) {
                log.error("Create directory failure, directory:{}", dir);
            }
            try {
                sftp.cd(dir);
                log.info("Change directory {}", dir);
            }
            catch (SftpException e1) {
                log.error("Change directory failure, directory:{}", dir);
            }
        }
    }

    @Override
    public boolean remove(final String targetPath) {
        final ChannelSftp sftp = getChannelSftp();
        try {
            sftp.cd(properties.getRoot());
            sftp.rm(targetPath);
            return true;
        }
        catch (SftpException e) {
            log.error("Delete file failure. path: {}", targetPath);
            return false;
        }
        finally {
            this.disconnect(sftp);
        }
    }

    @Override
    public boolean download(final String targetPath, final Path downloadPath) {
        final String path = downloadPath.toString();
        if (!isMkdir(path)) {
            return false;
        }
        return download(targetPath, path);
    }

    private boolean isMkdir(final String path) {
        final String location = getLocation(path);
        if (Objects.isNull(location)) {
            return false;
        }
        return createDownstreamDirs(location);
    }

    private String getLocation(final String path) {
        try {
            return path.substring(0, path.lastIndexOf(File.separator));
        }
        catch (IndexOutOfBoundsException e) {
            log.error("Invalid download path: {}. please check '/' or '\\'", path);
            return null;
        }
    }

    private boolean createDownstreamDirs(final String location) {
        final File folder = new File(location);
        if (!folder.exists()) {
            try {
                folder.mkdirs();
                log.info("Create folder: {}", folder.getPath());
                return true;
            }
            catch (Exception e) {
                log.error("Can't create folder: {}", folder.getPath());
                return false;
            }
        }
        log.info("Tried to create a folder but failed. because folder already exists!");
        return false;
    }

    private boolean download(final String targetPath, final String path) {
        final ChannelSftp sftp = getChannelSftp();
        try (OutputStream outputStream = new FileOutputStream(path)) {
            sftp.cd(properties.getRoot());
            log.info("Change directory to {}", properties.getRoot());

            sftp.get(targetPath, outputStream);
            log.info("Download file success. download path: {}", path);
            return true;
        }
        catch (Exception e) {
            log.error("Download file failure. download path: {}", path);
            return false;
        }
        finally {
            this.disconnect(sftp);
        }
    }
}
