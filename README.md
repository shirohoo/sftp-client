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

## 🚗 Download
- [1.0](https://github.com/shirohoo/sftp-client/releases/tag/1.0)

---

## 😝 Install

1. Create `rootProject/libs`
2. Add `sftp-client.jar`

![1](https://user-images.githubusercontent.com/71188307/121863160-84826c00-cd36-11eb-8e6e-bb815dca0256.JPG)

3. Set `build.gradle`

![2](https://user-images.githubusercontent.com/71188307/121863162-864c2f80-cd36-11eb-9906-0f63f7e0c928.JPG)

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
        //given
        File file = new File(PATH);
    
        //when
        boolean delete = file.delete();
    
        //then
        assertThat(delete).isTrue();
    }
    
    @Test
    void 파일을_다운로드한다() throws Exception {
        //given
        File upload = new File(PATH);
        fileSystemService.upload(CREATE_FILE, upload);
        
        //when
        File file = fileSystemService.download(CREATE_FILE, DIR + DOWNLOAD_FILE);
        
        //then
        assertThat(file).isNotNull().exists().isFile();
        assertThat(file.getName()).isEqualTo(DOWNLOAD_FILE);
    }
    
    @Test
    void 파일을_업로드한다_인수는_파일() throws Exception {
        //given
        File file = new File(PATH);
        
        //when
        boolean upload = fileSystemService.upload(CREATE_FILE, file);
        
        //then
        assertThat(upload).isTrue();
    }
    
    @Test
    void 파일을_업로드한다_인수는_인풋스트림() throws Exception {
        //given
        File file = new File(PATH);
    
        //when
        boolean upload = fileSystemService.upload(CREATE_FILE, new FileInputStream(file));
    
        //then
        assertThat(upload).isTrue();
    }
    
    @Test
    void 파일을_제거한다() throws Exception {
        //given
        String fileName = CREATE_FILE;
        
        //when
        boolean delete = fileSystemService.delete(fileName);
        
        //then
        assertThat(delete).isTrue();
    }
}
```
