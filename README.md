# 😎 SFTP-Module

- This module is need Java8+ and depend on `commons-io-2.8.0`, `commons-lang3-3.12.0`, `jsch-0.1.55`

## 🏁 API

```java
public interface SftpClient {
    File read(String targetPath) throws FileNotFoundException, JSchException;
    boolean upload(String targetPath, File file) throws IOException, JSchException;
    boolean upload(String targetPath, InputStream inputStream) throws JSchException, IOException;
    boolean remove(String targetPath) throws JSchException;
    boolean download(String targetPath, Path downloadPath) throws JSchException;
}
```

---

## 🚗 Download
- [1.0](https://github.com/shirohoo/sftp-client/releases/tag/1.0)
- [1.1](https://github.com/shirohoo/sftp-client/releases/tag/1.1)
- [1.2](https://github.com/shirohoo/sftp-client/releases/tag/1.2)
- [1.3](https://github.com/shirohoo/sftp-client/releases/tag/1.3)

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
    // implementation files('libs/sftp-client-1.3.jar') 
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
public SftpClient sftpClient1() {
    return new DefaultSftpClient(SftpProperties.builder()
        .host("127.0.0.1")
        .username("username")
        .password("password")
        .root("/home")
        .build());
}

// for example:
// use private key, pass phrase
// required property
@Bean
public SftpClient sftpClient() {
    return new DefaultSftpClient(SftpProperties.builder()
        .keyMode(true)
        .host("127.0.0.1")
        .privateKey("key")
        .passphrase("passphrase")
        .root("/home")
        .build());
}

// usage
public class ExampleFileService {
    private SftpClient sftpClient;
    
    // Constructor DI
    public ExampleFileService(SftpClient sftpClient) {
        this.sftpClient = sftpClient;
    }
    
    public void example() throws Exception {
        // API
        File read = sftpClient.read("targetPath");
        boolean upload_1 = sftpClient.upload("targetPath", new File("uploadFile"));
        boolean upload_2 = sftpClient.upload("targetPath", new FileInputStream(new File("uploadFile")));
        boolean remove = sftpClient.remove("targetPath");
        boolean download = sftpClient.download("targetPath", Paths.get("downloadPath"));
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
