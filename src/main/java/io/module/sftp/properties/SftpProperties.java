package io.module.sftp.properties;

public class SftpProperties {
    private Boolean keyMode = false;
    private String protocol = "sftp";
    private Integer port = 22;
    private String sessionStrictHostKeyChecking = "no";
    private Integer sessionConnectTimeout = 15000;
    private Integer channelConnectedTimeout = 15000;
    private String host;
    private String username;
    private String password;
    private String privateKey;
    private String passphrase;
    private String root;
    
    public String getProtocol() {
        return protocol;
    }
    public Integer getPort() {
        return port;
    }
    public String getSessionStrictHostKeyChecking() {
        return sessionStrictHostKeyChecking;
    }
    public Integer getSessionConnectTimeout() {
        return sessionConnectTimeout;
    }
    public Integer getChannelConnectedTimeout() {
        return channelConnectedTimeout;
    }
    public String getHost() {
        return host;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getPrivateKey() {
        return privateKey;
    }
    public String getPassphrase() {
        return passphrase;
    }
    public String getRoot() {
        return root;
    }
    public Boolean getKeyMode() {
        return keyMode;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    public void setPort(Integer port) {
        this.port = port;
    }
    public void setSessionStrictHostKeyChecking(String sessionStrictHostKeyChecking) {
        this.sessionStrictHostKeyChecking = sessionStrictHostKeyChecking;
    }
    public void setSessionConnectTimeout(Integer sessionConnectTimeout) {
        this.sessionConnectTimeout = sessionConnectTimeout;
    }
    public void setChannelConnectedTimeout(Integer channelConnectedTimeout) {
        this.channelConnectedTimeout = channelConnectedTimeout;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }
    public void setRoot(String root) {
        this.root = root;
    }
    public void setKeyMode(Boolean keyMode) {
        this.keyMode = keyMode;
    }
}
