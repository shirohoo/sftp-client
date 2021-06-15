package io.module.sftp;

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
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("SftpProperties_is_not_setup")
@SpringBootTest(classes = {SftpProperties.class, DefaultSftpFileService.class})
class DefaultSftpFileServiceTest {
    static final String USER_DIR = Paths.get(System.getProperty("user.home"), "sftp-module-test").toString();
    static final String LOCAL_FILE_NAME = "sftp-test-file.txt";
    static final String UPLOAD_FILE_PATH = "sftp/upload-file.txt";
    static final String LOCAL_FILE_PATH = Paths.get(USER_DIR, LOCAL_FILE_NAME).toString();
    static final String REMOTE_FILE_NAME = "download-file.txt";
    static final Path DOWNLOAD_PATH = Paths.get("src", "main", "resources", "download", REMOTE_FILE_NAME);
    
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
        
        //create file at local
        File file = new File(LOCAL_FILE_PATH);
        FileOutputStream stream = new FileOutputStream(file);
        try(OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            if(!file.exists()) {
                file.mkdirs();
            }
            writer.flush();
        }
    }
    
    @AfterEach
    void tearDown() {
        //remove file at local
        File file = new File(LOCAL_FILE_PATH);
        boolean delete = file.delete();
        assertThat(delete).isTrue();
    }
    
    @Test
    void read() throws Exception {
        //given
        sftpService.upload(LOCAL_FILE_NAME, new File(LOCAL_FILE_PATH));
        
        //when
        File read = sftpService.read(LOCAL_FILE_NAME);
        
        //then
        assertThat(read).isNotNull().isFile().canRead().canRead();
    }
    
    @Test
    void download() throws Exception {
        //given
        sftpService.upload(LOCAL_FILE_NAME, new File(LOCAL_FILE_PATH));
        
        //when
        boolean download = sftpService.download(LOCAL_FILE_NAME, DOWNLOAD_PATH);
        
        //then
        assertThat(download).isTrue();
    }
    
    @Test
    void uploadParamIsFile() throws Exception {
        //given
        File file = new File(LOCAL_FILE_PATH);
        
        //when
        boolean upload = sftpService.upload(UPLOAD_FILE_PATH, file);
        
        //then
        assertThat(upload).isTrue();
    }
    
    @Test
    void uploadParamIsInputStream() throws Exception {
        //given
        File localFile = new File(LOCAL_FILE_PATH);
        
        //when
        boolean upload = sftpService.upload(UPLOAD_FILE_PATH, new FileInputStream(localFile));
        
        //then
        assertThat(upload).isTrue();
    }
    
    @Test
    void remove() throws Exception {
        //given
        sftpService.upload(LOCAL_FILE_NAME, new File(LOCAL_FILE_PATH));
        
        //when
        boolean delete = sftpService.remove(LOCAL_FILE_NAME);
        
        //then
        assertThat(delete).isTrue();
    }
}
