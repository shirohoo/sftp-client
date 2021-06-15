package io.module.sftp.service;

import com.jcraft.jsch.*;
import io.module.sftp.properties.SftpProperties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.*;
import java.util.UUID;
import java.util.regex.Matcher;

public class DefaultSftpFileService implements SftpFileService {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSftpFileService.class);
    
    private static final String SESSION_properties_STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";
    
    private final SftpProperties properties;
    
    public DefaultSftpFileService(SftpProperties properties) {
        this.properties = properties;
    }
    
    @Override
    public boolean upload(String targetPath, File file) throws Exception {
        return this.upload(targetPath, new FileInputStream(file));
    }
    
    @Override
    public boolean upload(String targetPath, InputStream inputStream) throws Exception {
        ChannelSftp sftp = null;
        if(!properties.getKeyMode()) {
            sftp = this.connectByPassword();
        }
        else {
            sftp = this.connectByKey();
        }
        try {
            sftp.cd(properties.getRoot());
            log.info("Change directory to {}", properties.getRoot());
            
            int index = targetPath.lastIndexOf(File.separator);
            String fileName = targetPath.substring(index + 1);
            
            sftp.put(inputStream, fileName);
            log.info("Upload file success. path: {}", targetPath);
            return true;
        }
        catch(Exception e) {
            log.error("Upload file failure. path: {}", targetPath, e);
            throw new Exception("Upload file failure");
        }
        finally {
            inputStream.close();
            this.disconnect(sftp);
        }
    }
    
    @Override
    public boolean delete(String fileName) throws Exception {
        ChannelSftp sftp = null;
        try {
            if(!properties.getKeyMode()) {
                sftp = this.connectByPassword();
            }
            else {
                sftp = this.connectByKey();
            }
            sftp.cd(properties.getRoot());
            sftp.rm(fileName);
            return true;
        }
        catch(Exception e) {
            log.error("Delete file failure. path: {}", fileName, e);
            throw new Exception("Delete file failure");
        }
        finally {
            this.disconnect(sftp);
        }
    }
    
    @Override
    public File read(String targetPath) throws Exception {
        ChannelSftp sftp = null;
        if(!properties.getKeyMode()) {
            sftp = this.connectByPassword();
        }
        else {
            sftp = this.connectByKey();
        }
        try {
            sftp.cd(properties.getRoot());
            log.info("Change directory to {}", properties.getRoot());
            
            try(InputStream inputStream = sftp.get(targetPath)) {
                return convertInputStreamToFile(inputStream);
            }
        }
        catch(Exception e) {
            log.error("Download file failure. target path: {}", targetPath, e);
            throw new Exception("Download file failure");
        }
        finally {
            this.disconnect(sftp);
        }
    }
    
    @Override
    public File download(String targetPath, String downloadPath) throws Exception {
        int index = downloadPath.lastIndexOf(File.separator);
        String location = null;
        try {
            location = downloadPath.substring(0, index);
        }
        catch(IndexOutOfBoundsException e) {
            log.error("Invalid download path: {}", downloadPath, e);
            throw new Exception("Invalid download path");
        }
        checkFolder(location);
        
        ChannelSftp sftp = null;
        if(!properties.getKeyMode()) {
            sftp = this.connectByPassword();
        }
        else {
            sftp = this.connectByKey();
        }
        
        File outputFile = new File(downloadPath);
        try(OutputStream outputStream = new FileOutputStream(outputFile)) {
            sftp.cd(properties.getRoot());
            log.info("Change directory to {}", properties.getRoot());
            
            sftp.get(targetPath, outputStream);
            log.info("Download file success. download path: {}", downloadPath);
            return outputFile;
        }
        catch(Exception e) {
            log.error("Download file failure. download path: {}", downloadPath, e);
            throw new Exception("Download file failure");
        }
        finally {
            this.disconnect(sftp);
        }
    }
    
    private File convertInputStreamToFile(InputStream inputStream) throws IOException {
        File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
        IOUtils.copy(inputStream, new FileOutputStream(tempFile));
        return tempFile;
    }
    
    private ChannelSftp connectByPassword() throws Exception {
        JSch jsch = new JSch();
        log.info("Try to connect sftp[{}@{}], use password[{}]",
                 properties.getUsername(),
                 properties.getHost(),
                 properties.getPassword());
        
        Session session = createSession(jsch, properties.getHost(), properties.getUsername(), properties.getPort());
        session.setPassword(properties.getPassword());
        return getChannelSftp(session);
    }
    
    private ChannelSftp connectByKey() throws Exception {
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
    
    private void checkFolder(String location) {
        String path = location.replaceAll("/", Matcher.quoteReplacement(File.separator));
        File folder = new File(path);
        if(!folder.exists()) {
            try {
                folder.mkdirs();
                log.info("Create folder: {}", folder.getPath());
            }
            catch(Exception e) {
                log.error("Can't create folder: {}", folder.getPath(), e);
            }
        }
        else {
            log.info("Tried to create a folder but failed. because folder already exists!");
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
    
    private Session createSession(JSch jsch, String host, String username, Integer port) throws Exception {
        Session session = null;
        if(port <= 0) {
            session = jsch.getSession(username, host);
        }
        else {
            session = jsch.getSession(username, host, port);
        }
        
        if(session == null) {
            throw new Exception(host + " session is null");
        }
        session.setConfig(SESSION_properties_STRICT_HOST_KEY_CHECKING, properties.getSessionStrictHostKeyChecking());
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
}
