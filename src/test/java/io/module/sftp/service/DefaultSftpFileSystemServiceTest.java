package io.module.sftp.service;

import io.module.sftp.properties.SftpProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("SftpProperties_미설정상태이므로_비활성화")
@SpringBootTest(classes = {SftpProperties.class, DefaultSftpFileSystemService.class})
class DefaultSftpFileSystemServiceTest {
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
        
        fileSystemService = new DefaultSftpFileSystemService(properties);
        
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
