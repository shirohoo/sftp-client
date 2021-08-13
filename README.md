# 😎 SFTP-Module

`Spring Integration` was too complicated to configure, so it didn't work well for me, who just wanted to do `CRUD`.

This module helps you to connect to a remote server through `SFTP` and do `CRUD` operations very simply.

> This module is need Java8+ and depend on `commons-io-2.8.0`, `commons-lang3-3.12.0`, `jsch-0.1.55`

## 🏁 API

```java
public interface SftpClient {
    /**
     * Pass the path of the file you want to read as an argument. The starting path is the root of SftpProperties.
     * For example, if root is ~/ and the path passed as an argument is user/temp/test.txt , SftpClient reads ~/user/temp/test.txt and returns it as a File object.
     * @param targetPath String
     * @return File
     */
    File read(String targetPath);

    /**
     * The location where you want to upload the file is passed as the first argument, and the file you want to upload as the second argument.
     * In this case, the first argument must also include the name of the file.
     * @param targetPath String
     * @param file File
     * @return boolean
     */
    boolean upload(String targetPath, File file);

    /**
     * The location where you want to upload the file is passed as the first argument, and the InputStream of file you want to upload as the second argument.
     * In this case, the first argument must also include the name of the file.
     * @param targetPath String
     * @param inputStream InputStream
     * @return boolean
     */
    boolean upload(String targetPath, InputStream inputStream);

    /**
     * If you pass the path to the file you want to remove as an argument, it will try to remove the file and return whether the removal succeeded or failed.
     * @param targetPath String
     * @return boolean
     */
    boolean remove(String targetPath);

    /**
     * Pass the path to the file you want to download from the remote server as the first argument. In this case, the first argument must include a file name.
     * Enter the location where you want to save the downloaded file as the second argument.
     * Returns true if the download is successful, false if it fails
     * @param targetPath String
     * @param downloadPath Path
     * @return boolean
     */
    boolean download(String targetPath, Path downloadPath);
}
```

---

## 🚗 Download
- [1.0](https://github.com/shirohoo/sftp-client/releases/tag/1.0)
- [1.1](https://github.com/shirohoo/sftp-client/releases/tag/1.1)
- [1.2](https://github.com/shirohoo/sftp-client/releases/tag/1.2)
- [1.3](https://github.com/shirohoo/sftp-client/releases/tag/1.3)
- [1.4](https://github.com/shirohoo/sftp-client/releases/tag/1.4)
- [1.5](https://github.com/shirohoo/sftp-client/releases/tag/1.5)

---

## 😝 Install

### 📜 Maven
```xml
<!--pom.xml-->
<dependency>
  <groupId>io.github.shirohoo</groupId>
  <artifactId>sftp-client</artifactId>
  <version>1.5</version>
</dependency>
```

### 📜 Gradle
```groovy
// file: build.gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation 'io.github.shirohoo:sftp-client:1.5'
}
```

---

## How to use?

### 🤗 Set `SftpProperties` and Create `@Bean`

required property is `username`, `password` or `privateKey`, `passphrase`.

You must make the following essential settings:

```java
import java.nio.file.Paths;

@Bean
public SftpClient sftpClient1() {
    return new DefaultSftpClient(SftpProperties.builder()
        .host("127.0.0.1")
        .username("username") // use username and password
        .password("password") 
        .root("/home") // base path folder after sftp connection
        .build());
}

@Bean
public SftpClient sftpClient() {
    return new DefaultSftpClient(SftpProperties.builder()
        .keyMode(true)
        .host("127.0.0.1")
        .privateKey("key") // use privateKey and passphrase
        .passphrase("passphrase")
        .root("/home") // base path folder after sftp connection
        .build());
}
```

You can use `SftpClient` by DI.

```java
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
    private Boolean keyMode = false;
    private String protocol = "sftp";
    private Integer port = 22;
    private String sessionStrictHostKeyChecking = "no";
    private Integer sessionConnectTimeout = 15000;
    private Integer channelConnectedTimeout = 15000;
    
    //--- required property ---//
    private String host;
    private String username;
    private String password;
    private String root;
}
```
