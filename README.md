# 😎 SFTP-Module

- Need Java8+

## 🏁 API

```java
public interface SftpFileService {
    File read(String targetPath) throws Exception;
    boolean upload(String targetPath, File file) throws Exception;
    boolean upload(String targetPath, InputStream inputStream) throws Exception;
    boolean remove(String targetPath) throws Exception;
    boolean download(String targetPath, Path downloadPath) throws Exception;
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

```groovy
dependencies {
    implementation files('libs/sftp-client-{version}.jar')
    // for example:
    // implementation files('libs/sftp-client-1.1.jar') 
}
```

---

## How to use?

### 🤗 Set `SftpProperties` and Create `@Bean`

```java
// for example:
// use username, password     
// required property

import java.nio.file.Paths;

@Bean
public SftpFileService sftpService(){
        SftpProperties properties=new SftpProperties();
        properties.setHost("127.0.0.1");
        properties.setUsername("username");
        properties.setPassword("password");
        properties.setRoot("/home");
        return new DefaultSftpFileService(properties);
        }

// for example:
// use private key, pass phrase
// required property
@Bean
public SftpFileService sftpService(){
        SftpProperties properties=new SftpProperties();
        properties.setKeyMode(true);
        properties.setHost("127.0.0.1");
        properties.setPrivateKey("key");
        properties.setPassphrase("passphrase");
        properties.setRoot("/home");
        return new DefaultSftpFileService(properties);
        }

// usage
public class FileService {
    private SftpFileService sftpFileService;
    
    // Constructor DI
    public FileService(SftpFileService sftpFileService) {
        this.sftpFileService = sftpFileService;
    }
    
    public void example() throws Exception {
        // API
        File read = sftpFileService.read("targetPath");
        boolean upload_1 = sftpFileService.upload("targetPath", new File("uploadFile"));
        boolean upload_2 = sftpFileService.upload("targetPath", new FileInputStream(new File("uploadFile")));
        boolean remove = sftpFileService.remove("targetPath");
        boolean download = sftpFileService.download("targetPath", Paths.get("downloadPath"));
    }
}
```

### 🙄 Properties
```java
public class SftpProperties {
    //--- default property ---//
    //↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
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
