# 😎 SFTP-Module

- Need Java8+

## 🏁 API

```java
public interface SftpFileService {
    File read(String targetPath) throws Exception;
    File download(String targetPath, String downloadPath) throws Exception;
    boolean upload(String targetPath, File file) throws Exception;
    boolean upload(String targetPath, InputStream inputStream) throws Exception;
    boolean delete(String fileName) throws Exception;
}
```

---

## 🚗 Download
- [1.0](https://github.com/shirohoo/sftp-client/releases/tag/1.0)
- [1.1](https://github.com/shirohoo/sftp-client/releases/tag/1.1)

---

## 😝 Install

1. Create `rootProject/libs`
2. Add `sftp-client.jar`

![1](https://user-images.githubusercontent.com/71188307/121863160-84826c00-cd36-11eb-8e6e-bb815dca0256.JPG)

3. Set `build.gradle`

![2](https://user-images.githubusercontent.com/78329064/121989977-fe1e6680-cdd7-11eb-9244-9858996191a5.png)

---

## How to use?

### 🤗 Set `SftpProperties` and Create `@Bean`

```java
// for example:
// use username, password     
// required property
@Bean
public SftpFileSystemService sftpService(){
    SftpProperties properties = new SftpProperties();
    properties.setHost("127.0.0.1");
    properties.setUsername("username");
    properties.setPassword("password");
    properties.setRoot("/home");
    return new DefaultSftpFileSystemService(properties);
}

// for example:
// use private key, pass phrase
// required property
@Bean
public SftpFileSystemService sftpService(){
    SftpProperties properties = new SftpProperties();
    properties.setKeyMode(true);
    properties.setHost("127.0.0.1");
    properties.setPrivateKey("key");
    properties.setPassphrase("passphrase");
    properties.setRoot("/home");
    return new DefaultSftpFileSystemService(properties);
}
```

### 🙄 Properties
```java
public class SftpProperties {
    private Boolean keyMode = false;
    private String protocol = "sftp";
    private Integer port = 22;
    private String sessionStrictHostKeyChecking = "no";
    private Integer sessionConnectTimeout = 15000;
    private Integer channelConnectedTimeout = 15000;
    
    //--- required property ---//
    //↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
    private String host;
    private String username;
    private String password;
    private String root;
}
```

---
