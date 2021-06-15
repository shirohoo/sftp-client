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
@SpringBootTest(classes = {SftpProperties.class, DefaultSftpFileService.class})
class DefaultSftpFileServiceTest {
    static final String DIR = (System.getProperty("user.home") + File.separator + "sftp-module-test") + File.separator;
    static final String CREATE_FILE = "sftp-test-file.txt";
    static final String DOWNLOAD_FILE = "download-file.txt";
    static final String PATH = (DIR + CREATE_FILE).replaceAll("/", Matcher.quoteReplacement(File.separator));
    
    SftpFileService sftpService;
    
    @BeforeEach
    void setUp() throws Exception {
        SftpProperties properties = new SftpProperties();
    
        //----------------------- VARIABLE  -----------------------//
        properties.setHost("127.0.0.1");
        properties.setUsername("username");
        properties.setPassword("password");
        properties.setRoot("/home");
        //---------------------------------------------------------//
    
        sftpService = new DefaultSftpFileService(properties);
    
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
    void 파일을_읽는다() throws Exception {
        //given
        File upload = new File(PATH);
        sftpService.upload(CREATE_FILE, upload);
        
        //when
        File read = sftpService.read(CREATE_FILE);
        
        //then
        assertThat(read).isNotNull().isFile().canRead().canRead();
    }
    
    @Test
    void 파일을_다운로드한다() throws Exception {
        //given
        File upload = new File(PATH);
        sftpService.upload(CREATE_FILE, upload);
        
        //when
        File file = sftpService.download(CREATE_FILE, DIR + DOWNLOAD_FILE);
        
        //then
        assertThat(file).isNotNull().exists().isFile();
        assertThat(file.getName()).isEqualTo(DOWNLOAD_FILE);
    }
    
    @Test
    void 파일을_업로드한다_인수는_파일() throws Exception {
        //given
        File file = new File(PATH);
    
        //when
        boolean upload = sftpService.upload(CREATE_FILE, file);
    
        //then
        assertThat(upload).isTrue();
    }
    
    @Test
    void 파일을_업로드한다_인수는_인풋스트림() throws Exception {
        //given
        File file = new File(PATH);
    
        //when
        boolean upload = sftpService.upload(CREATE_FILE, new FileInputStream(file));
    
        //then
        assertThat(upload).isTrue();
    }
    
    @Test
    void 파일을_제거한다() throws Exception {
        //given
        String fileName = CREATE_FILE;
    
        //when
        boolean delete = sftpService.delete(fileName);
    
        //then
        assertThat(delete).isTrue();
    }
}
