# 😎 SFTP-Module

---

`Spring Integration` was too complicated to configure, so it didn't work well for me, who just wanted to do `CRUD`.

This module helps you to connect to a remote server through `SFTP` and do `CRUD` operations very simply.

> This module is need Java8+ and depend on `commons-io-2.8.0`, `commons-lang3-3.12.0`, `jsch-0.1.55`

<br />

## 🏁 API

---

```java
public interface SftpClient {

    /**
     * Pass the path of the file you want to read as an argument. The starting path is the root of SftpProperties. For example, if root is ~/ and the path passed as an argument is user/temp/someFile.txt , SftpClient reads ~/user/temp/someFile.txt and
     * returns it as a File object. At this time, the transferred file is not saved on the client hard disk, but only in client memory.
     *
     * @param targetPath String
     * @return File
     */
    File read(final String targetPath) throws JSchException, NotDirectoryException;

    /**
     * Pass the path of the file you want to read as an argument. The starting path is the root of SftpProperties. For example, if root is ~/ and the path passed as an argument is user/temp/someDir (last args is directory name) , SftpClient reads
     * ~/user/temp/someDir and returns it as a List {@literal <}File{@literal >} object.
     *
     * @param targetDirPath String
     * @return List {@literal <}File{@literal >}
     */
    List<File> listFiles(final String targetDirPath) throws JSchException, NoSuchFileException;

    /**
     * The location where you want to upload the file is passed as the first argument, and the file you want to upload as the second argument. In this case, the first argument must also include the name of the file.
     *
     * @param targetPath String
     * @param uploadFile       File
     * @return boolean
     */
    boolean upload(final String targetPath, final File uploadFile) throws JSchException;

    /**
     * The location where you want to upload the file is passed as the first argument, and the InputStream of file you want to upload as the second argument. In this case, the first argument must also include the name of the file.
     *
     * @param targetPath  String
     * @param uploadFileStream InputStream
     * @return boolean
     */
    boolean upload(final String targetPath, final InputStream uploadFileStream) throws IOException, JSchException;

    /**
     * If you pass the path to the file you want to remove as an argument, it will try to remove the file and return whether the removal succeeded or failed.
     *
     * @param targetPath String
     * @return boolean
     */
    boolean remove(final String targetPath) throws JSchException;

    /**
     * Pass the path to the file you want to download from the remote server as the first argument. In this case, the all argument must include a file name. Enter the location where you want to save the downloaded file as the second argument. Returns
     * true if the download is successful, false if it fails
     *
     * @param targetPath   String
     * @param downloadPath Path
     * @return boolean
     */
    boolean download(final String targetPath, Path downloadPath) throws JSchException;

}
```

<br />

## 🚗 Direct Download Jar

---

- [2.4](https://github.com/shirohoo/sftp-client/releases/tag/2.4)

<br />

## 😝 Install with build tool

---

### 📜 Maven

```xml
<!--pom.xml-->
<dependency>
  <groupId>io.github.shirohoo</groupId>
  <artifactId>sftp-client</artifactId>
  <version>2.4</version>
</dependency>
```

### 📜 Gradle

```groovy
// build.gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation 'io.github.shirohoo:sftp-client:2.4'
}
```

<br />

## 🤔 How to use?

---

### Setup `SftpProperties` and Create `@Bean`

required property is `username`, `password` or `privateKey`, `passphrase`.

You must make the following essential settings:

```java
// use username and password
@Bean
public SftpClient sftpClient(){
    return new DefaultSftpClient(SftpProperties.builder()
        .host("127.0.0.1") // host ip address of remote server you want to connect to
        .username("username") // username of remote server you want to connect to
        .password("password") // password of remote server you want to connect to
        .root("/home") // base path folder after sftp connection
        .build());
    }

// use privateKey and passphrase
@Bean
public SftpClient sftpClient(){
    return new DefaultSftpClient(SftpProperties.builder()
        .keyMode(true) // you should set this option to true if you want to access using private key
        .host("127.0.0.1") // host ip address of remote server you want to connect to
        .privateKey("privateKey") // privateKey of remote server you want to connect to
        .passphrase("passphrase") // passphrase of remote server you want to connect to
        .root("/home") // base path folder after sftp connection
        .build());
    }
```

<br />

### 🙄 Properties Detail

The detailed default implementation of `SftpProperties` is as follows.

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

<br />

You can use `SftpClient` by `DI`.

Assuming that the server you want to connect to via SFTP is Unix-like, use "/" as the path separator, and it is recommended to use `java.nio.Paths` to identify your server operating system at runtime.

<br />

```java
public class ExampleFileService {

    private final SftpClient sftpClient;

    public ExampleFileService(SftpClient sftpClient) {
        this.sftpClient = sftpClient;
    }

    public void example() throws Exception {
        // APIs
        File read = sftpClient.read("targetPath");
        List<File> files = sftpClient.listFiles("targetDirPath");
        boolean upload_1 = sftpClient.upload("targetPath", new File("uploadFile"));
        boolean upload_2 = sftpClient.upload("targetPath", new FileInputStream(new File("uploadFile")));
        boolean remove = sftpClient.remove("targetPath");
        boolean download = sftpClient.download("targetPath", Paths.get("downloadPath"));
    }

}
```

<br />

### 💡 for example `read`:

If you want to read `~/someDir/someFile.txt` from a remote server, you can use it like this:

Assume that `SftpProperties.root` is `~`.

```java
public File read(){
    String wantReadFilePath = Paths.get("someDir","someFile.txt").toString();
    return sftpClient.read(wantReadFilePath);
}
```

<br />

### 💡 for example `listFiles`:

If you want to read all files in `~/someDir1/someDir2` from a remote server, you can use it like this:

Assume that `SftpProperties.root` is `~`.

```java
public List<File> listFiles(){
    String wantReadDirPath = Paths.get("someDir1","someDir2").toString();
    return sftpClient.listFiles(wantReadDirPath);
}
```

<br />

### 💡 for example `upload`:

`upload()` is the location where you want to upload the file is passed as the first argument, and the file you want to upload as the second argument.

If you want to uplad `~/someDir/someFile.txt` to remote server, you can use it like this:

Assume that `SftpProperties.root` is `~`.

```java
public boolean upload(){
    String wantUploadFilePath = Paths.get("someDir","someFile.txt").toString();
    File uploadFile = new File("someFile");
    return sftpClient.upload(wantUploadFilePath,uploadFile);
}

public boolean upload(){
    String wantUploadFilePath = Paths.get("someDir","someFile.txt").toString();
    FileInputStream uploadFileInputStream = new FileInputStream(new File("someFile"));
    return sftpClient.upload(wantUploadFilePath,uploadFileInputStream);
}
```

<br />

### 💡 for example `remove`:

If you want to remove `~/someDir/someFile.txt` on a remote server, you can use something like this:

Assume that `SftpProperties.root` is `~`.

```java
public boolean remove(){
    String wantRemoveFilePath = Paths.get("someDir","someFile.txt").toString();
    return sftpClient.remove(wantRemoveFilePath);
}
```

<br />

### 💡 for example `download`:

Pass the path to the file you want to download from the remote server as the first argument. In this case, the all argument must include a file name.

And enter the location where you want to save the downloaded file as the second argument.

If the path of the file to be downloaded from the remote server is `~/someDir/someFile.txt` and the path where this file will be downloaded is local `~/download/someFile.txt`, you can use it as follows.

Also assume that `SftpProperties.root` is `~`.

```java
public boolean download(){
    String targetFilePath = Paths.get("someDir","someFile.txt").toString();
    String downloadLocalPath = Paths.get(System.getProperty("user.home"),"download","someFile.txt").toString();
    return sftpClient.downlad(Paths.get(targetFilePath,downloadLocalPath));
}
```

<br />

# 📜 License

This module is open source software released under the Apache 2.0 license.
