# 😎 SFTP-Module

## 🏁 API

```java
public interface SftpFileSystemService {
    File download(String targetPath, String downloadPath) throws Exception;
    boolean upload(String targetPath, InputStream inputStream) throws Exception;
    boolean upload(String targetPath, File file) throws Exception;
    boolean delete(String fileName) throws Exception;
}
```

---

##😝 Install

1. Create `rootProject/libs`
2. Add `sftp-client.jar`

![1](https://user-images.githubusercontent.com/71188307/121863160-84826c00-cd36-11eb-8e6e-bb815dca0256.JPG)

3. Set `build.gradle`

![2](https://user-images.githubusercontent.com/71188307/121863162-864c2f80-cd36-11eb-9906-0f63f7e0c928.JPG)

---

## How to use?

### 🤗 Set `SftpProperties` and 

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
    private String host = null;
    private String username = null;
    private String password = null;
    private String root = null;
}
```

---


## 😋 Test Code

```java
@Disabled("민감정보제거_비활성화")
@SpringBootTest(classes = {SftpProperties.class, SftpFileSystemServiceImpl.class})
class SftpFileSystemServiceImplTest {
    static final String DIR = (System.getProperty("user.home") + File.separator + "sftp-module-test") + File.separator;
    static final String CREATE_FILE = "sftp-test-file.txt";
    static final String DOWNLOAD_FILE = "download-file.txt";
    static final String PATH = (DIR + CREATE_FILE).replaceAll("/", Matcher.quoteReplacement(File.separator));
    
    SftpFileSystemService fileSystemService;
    
    @BeforeEach
    void setUp() throws Exception {
        SftpProperties properties = new SftpProperties();
    
        //----------------------- VARIABLE  -----------------------//
        properties.setHost("127.0.0.1");
        properties.setUsername("username");
        properties.setPassword("password");
        properties.setRoot("/home");
        //---------------------------------------------------------//
        
        fileSystemService = new SftpFileSystemServiceImpl(properties);
        
        File dir = new File(DIR);
        String path = PATH;
        File file = new File(path);
        FileOutputStream stream = new FileOutputStream(file);
        try(OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            if(!dir.exists()) {
                dir.mkdirs();
            }
            writer.flush();
        }
    }
    
    @AfterEach
    void tearDown() {
        File file = new File(PATH);
        file.delete();
    }
    
    @Test
    void 파일을_다운로드한다() throws Exception {
        File upload = new File(PATH);
        fileSystemService.upload(CREATE_FILE, upload);
        
        File file = fileSystemService.download(CREATE_FILE, DIR + DOWNLOAD_FILE);
        assertThat(file).isNotNull().exists().isFile();
        assertThat(file.getName()).isEqualTo(DOWNLOAD_FILE);
    }
    
    @Test
    void 파일을_업로드한다_인수는_파일() throws Exception {
        File file = new File(PATH);
        boolean upload = fileSystemService.upload(CREATE_FILE, file);
        assertThat(upload).isTrue();
    }
    
    @Test
    void 파일을_업로드한다_인수는_인풋스트림() throws Exception {
        File file = new File(PATH);
        boolean upload = fileSystemService.upload(CREATE_FILE, new FileInputStream(file));
        assertThat(upload).isTrue();
    }
    
    @Test
    void 파일을_제거한다() throws Exception {
        String fileName = CREATE_FILE;
        boolean delete = fileSystemService.delete(fileName);
        assertThat(delete).isTrue();
    }
}
```
