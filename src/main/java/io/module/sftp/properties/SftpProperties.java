package io.module.sftp.properties;

public class SftpProperties {
    private Boolean keyMode;
    private String protocol;
    private Integer port;
    private String sessionStrictHostKeyChecking;
    private Integer sessionConnectTimeout;
    private Integer channelConnectedTimeout;
    private String host;
    private String username;
    private String password;
    private String privateKey;
    private String passphrase;
    private String root;

    public Boolean getKeyMode() {
        return keyMode;
    }

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

    public SftpProperties(Boolean keyMode, String protocol, Integer port, String sessionStrictHostKeyChecking,
            Integer sessionConnectTimeout, Integer channelConnectedTimeout, String host, String username,
            String password, String privateKey, String passphrase, String root) {
        this.keyMode = keyMode;
        this.protocol = protocol;
        this.port = port;
        this.sessionStrictHostKeyChecking = sessionStrictHostKeyChecking;
        this.sessionConnectTimeout = sessionConnectTimeout;
        this.channelConnectedTimeout = channelConnectedTimeout;
        this.host = host;
        this.username = username;
        this.password = password;
        this.privateKey = privateKey;
        this.passphrase = passphrase;
        this.root = root;
    }

    public static SftpPropertiesBuilder builder() {
        return new SftpPropertiesBuilder();
    }

    public String toString() {
        return "SftpProperties(keyMode=" + this.getKeyMode() + ", protocol=" + this.getProtocol() + ", port=" + this
                .getPort() + ", sessionStrictHostKeyChecking=" + this.getSessionStrictHostKeyChecking()
                + ", sessionConnectTimeout=" + this.getSessionConnectTimeout() + ", channelConnectedTimeout=" + this
                .getChannelConnectedTimeout() + ", host=" + this.getHost() + ", username=" + this.getUsername()
                + ", password=" + this.getPassword() + ", privateKey=" + this.getPrivateKey() + ", passphrase=" + this
                .getPassphrase() + ", root=" + this.getRoot() + ")";
    }

    public static class SftpPropertiesBuilder {
        private Boolean keyMode;
        private String protocol;
        private Integer port;
        private String sessionStrictHostKeyChecking;
        private Integer sessionConnectTimeout;
        private Integer channelConnectedTimeout;
        private String host;
        private String username;
        private String password;
        private String privateKey;
        private String passphrase;
        private String root;

        SftpPropertiesBuilder() {
        }

        public SftpPropertiesBuilder keyMode(Boolean keyMode) {
            this.keyMode = keyMode;
            return this;
        }

        public SftpPropertiesBuilder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public SftpPropertiesBuilder port(Integer port) {
            this.port = port;
            return this;
        }

        public SftpPropertiesBuilder sessionStrictHostKeyChecking(String sessionStrictHostKeyChecking) {
            this.sessionStrictHostKeyChecking = sessionStrictHostKeyChecking;
            return this;
        }

        public SftpPropertiesBuilder sessionConnectTimeout(Integer sessionConnectTimeout) {
            this.sessionConnectTimeout = sessionConnectTimeout;
            return this;
        }

        public SftpPropertiesBuilder channelConnectedTimeout(Integer channelConnectedTimeout) {
            this.channelConnectedTimeout = channelConnectedTimeout;
            return this;
        }

        public SftpPropertiesBuilder host(String host) {
            this.host = host;
            return this;
        }

        public SftpPropertiesBuilder username(String username) {
            this.username = username;
            return this;
        }

        public SftpPropertiesBuilder password(String password) {
            this.password = password;
            return this;
        }

        public SftpPropertiesBuilder privateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public SftpPropertiesBuilder passphrase(String passphrase) {
            this.passphrase = passphrase;
            return this;
        }

        public SftpPropertiesBuilder root(String root) {
            this.root = root;
            return this;
        }

        public SftpProperties build() {
            if(keyMode == null){
                keyMode = false;
            }
            if(protocol == null){
                protocol = "sftp";
            }
            if(port == null){
                port = 22;
            }
            if(sessionStrictHostKeyChecking == null){
                sessionStrictHostKeyChecking = "no";
            }
            if(sessionConnectTimeout == null){
                sessionConnectTimeout = 15000;
            }
            if(channelConnectedTimeout == null){
                channelConnectedTimeout = 15000;
            }
            return new SftpProperties(keyMode, protocol, port, sessionStrictHostKeyChecking, sessionConnectTimeout,
                    channelConnectedTimeout, host, username, password, privateKey, passphrase, root);
        }
    }
}
