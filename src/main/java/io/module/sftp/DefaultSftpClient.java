package io.module.sftp;

import com.jcraft.jsch.*;
import io.module.sftp.properties.SftpProperties;
import java.awt.geom.IllegalPathStateException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.UUID;

import static java.util.Arrays.stream;

public final class DefaultSftpClient implements SftpClient {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSftpClient.class);
    
    private static final String STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";
    
    private final SftpProperties properties;
    
    public DefaultSftpClient(SftpProperties properties) {
        this.properties = properties;
    }
    
    private ChannelSftp connectByPassword() throws JSchException {
        JSch jsch = new JSch();
        log.info("Try to connect sftp[{}@{}], use password[{}]",
                 properties.getUsername(),
                 properties.getHost(),
                 properties.getPassword());
        
        Session session = createSession(jsch, properties.getHost(), properties.getUsername(), properties.getPort());
        session.setPassword(properties.getPassword());
        return getChannelSftp(session);
    }
    
    private ChannelSftp connectByPrivateKey() throws JSchException {
        JSch jsch = new JSch();
        
        if(StringUtils.isNotBlank(properties.getPrivateKey())) {
            if(StringUtils.isNotBlank(properties.getPassphrase())) {
                jsch.addIdentity(properties.getPrivateKey(), properties.getPassphrase());
            }
            else {
                jsch.addIdentity(properties.getPrivateKey());
            }
        }
        log.info("Try to connect sftp[{}@{}], use private key[{}] with passphrase[{}]",
                 properties.getUsername(),
                 properties.getHost(),
                 properties.getPrivateKey(),
                 properties.getPassphrase());
        
        Session session = createSession(jsch, properties.getHost(), properties.getUsername(), properties.getPort());
        return getChannelSftp(session);
    }
    
    private Session createSession(JSch jsch, String host, String username, Integer port) throws JSchException {
        Session session = null;
        if(port <= 0) {
            session = jsch.getSession(username, host);
        }
        else {
            session = jsch.getSession(username, host, port);
        }
        if(session == null) {
            throw new JSchException(host + " session is null");
        }
        session.setConfig(STRICT_HOST_KEY_CHECKING, properties.getSessionStrictHostKeyChecking());
        return session;
    }
    
    private void disconnect(ChannelSftp sftp) {
        try {
            if(sftp != null) {
                if(sftp.isConnected()) {
                    sftp.disconnect();
                }
                else if(sftp.isClosed()) {
                    log.info("Sftp is closed already");
                }
                if(null != sftp.getSession()) {
                    sftp.getSession().disconnect();
                }
            }
        }
        catch(JSchException e) {
            log.error(e.getMessage());
        }
    }
    
    private ChannelSftp getChannelSftp(Session session) throws JSchException {
        session.connect(properties.getSessionConnectTimeout());
        log.info("Session connected to {}", properties.getHost());
        
        Channel channel = session.openChannel(properties.getProtocol());
        channel.connect(properties.getChannelConnectedTimeout());
        log.info("Channel created to {}", properties.getHost());
        return (ChannelSftp) channel;
    }
    
    @Override
    public File read(String targetPath) throws FileNotFoundException, JSchException {
        ChannelSftp sftp = null;
        if(!properties.getKeyMode()) {
            sftp = this.connectByPassword();
        }
        else {
            sftp = this.connectByPrivateKey();
        }
        try {
            sftp.cd(properties.getRoot());
            log.info("Change directory to {}", properties.getRoot());
            
            try(InputStream inputStream = sftp.get(targetPath)) {
                return convertInputStreamToFile(inputStream);
            }
        }
        catch(Exception e) {
            log.error("Download file failure. target path: {}", targetPath);
            throw new FileNotFoundException("Download file failure");
        }
        finally {
            this.disconnect(sftp);
        }
    }
    
    private File convertInputStreamToFile(InputStream inputStream) throws IOException {
        File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
        IOUtils.copy(inputStream, new FileOutputStream(tempFile));
        tempFile.deleteOnExit();
        return tempFile;
    }
    
    @Override
    public boolean upload(String targetPath, File file) throws IOException, JSchException {
        return this.upload(targetPath, new FileInputStream(file));
    }
    
    @Override
    public boolean upload(String targetPath, InputStream inputStream) throws JSchException, IOException {
        ChannelSftp sftp = null;
        if(!properties.getKeyMode()) {
            sftp = this.connectByPassword();
        }
        else {
            sftp = this.connectByPrivateKey();
        }
        try {
            sftp.cd(properties.getRoot());
            log.info("Change path to {}", properties.getRoot());
            
            int index = targetPath.lastIndexOf("/");
            String dirName;
            String fileName;
            boolean dirs;
            if(index != -1) {
                dirName = targetPath.substring(0, index);
                fileName = targetPath.substring(index + 1);
                dirs = this.createUpstreamDirs(dirName, sftp);
                if(!dirs) {
                    log.error("Remote path error. path:{}", targetPath);
                    throw new Exception("Upload File failure");
                }
            }
            else {
                fileName = targetPath;
            }
            sftp.put(inputStream, fileName);
            return true;
            
        }
        catch(SftpException e) {
            log.error("Found not root directory. path: {}", e.getMessage());
            throw new IllegalPathStateException("Found not root directory");
        }
        catch(Exception e) {
            log.error("Upload file failure. path: {}", targetPath);
            throw new IllegalPathStateException("Upload file failure");
        }
        finally {
            inputStream.close();
            this.disconnect(sftp);
        }
    }
    
    private boolean createUpstreamDirs(String dirPath, ChannelSftp sftp) {
        if(dirPath != null && !dirPath.isEmpty() && sftp != null) {
            String[] dirs = stream(dirPath.split("/"))
                    .filter(StringUtils::isNotBlank).toArray(String[]::new);
            
            for(String dir : dirs) {
                try {
                    sftp.cd(dir);
                    log.info("Change directory {}", dir);
                }
                catch(Exception e) {
                    try {
                        sftp.mkdir(dir);
                        log.info("Create directory {}", dir);
                    }
                    catch(SftpException e1) {
                        log.error("Create directory failure, directory:{}", dir);
                    }
                    try {
                        sftp.cd(dir);
                        log.info("Change directory {}", dir);
                    }
                    catch(SftpException e1) {
                        log.error("Change directory failure, directory:{}", dir);
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    @Override
    public boolean remove(String targetPath) throws IllegalPathStateException, JSchException {
        ChannelSftp sftp = null;
        try {
            if(!properties.getKeyMode()) {
                sftp = this.connectByPassword();
            }
            else {
                sftp = this.connectByPrivateKey();
            }
            sftp.cd(properties.getRoot());
            sftp.rm(targetPath);
            return true;
        }
        catch(SftpException e) {
            log.error("Delete file failure. path: {}", targetPath);
            throw new IllegalPathStateException("Delete file failure");
        }
        finally {
            this.disconnect(sftp);
        }
    }
    
    @Override
    public boolean download(String targetPath, Path downloadPath) throws JSchException {
        String path = downloadPath.toString();
        int index = path.lastIndexOf(File.separator);
        String location = null;
        try {
            location = path.substring(0, index);
        }
        catch(IndexOutOfBoundsException e) {
            log.error("Invalid download path: {}", path);
            throw new IndexOutOfBoundsException("Invalid download path. please check '/' or '\\'");
        }
        createDownstreamDirs(location);
        
        ChannelSftp sftp = null;
        if(!properties.getKeyMode()) {
            sftp = this.connectByPassword();
        }
        else {
            sftp = this.connectByPrivateKey();
        }
        
        File outputFile = new File(path);
        try(OutputStream outputStream = new FileOutputStream(outputFile)) {
            sftp.cd(properties.getRoot());
            log.info("Change directory to {}", properties.getRoot());
            
            sftp.get(targetPath, outputStream);
            log.info("Download file success. download path: {}", path);
            return true;
        }
        catch(Exception e) {
            log.error("Download file failure. download path: {}", path);
            return false;
        }
        finally {
            this.disconnect(sftp);
        }
    }
    
    private void createDownstreamDirs(String location) {
        File folder = new File(location);
        if(!folder.exists()) {
            try {
                folder.mkdirs();
                log.info("Create folder: {}", folder.getPath());
            }
            catch(Exception e) {
                log.error("Can't create folder: {}", folder.getPath());
            }
        }
        else {
            log.info("Tried to create a folder but failed. because folder already exists!");
        }
    }
}
