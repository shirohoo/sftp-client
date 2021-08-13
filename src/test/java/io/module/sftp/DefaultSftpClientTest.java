package io.module.sftp;

import io.module.sftp.properties.SftpProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("SftpProperties is not setup")
class DefaultSftpClientTest {
    private SftpClient sftpService;

    @BeforeEach
    void setUp() throws Exception {
        //----------------------- VARIABLE  -----------------------//
        SftpProperties properties = SftpProperties.builder()
                .host("127.0.0.1")
                .username("username")
                .password("password")
                .root("/home") // entry point
                .build();
        //---------------------------------------------------------//

        sftpService = new DefaultSftpClient(properties);

        //create file at local
        File file = Paths.get(System.getProperty("user.home"), "sftp-module-test", "sftp-test-file.txt").toFile();
        FileOutputStream stream = new FileOutputStream(file);

        try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            if (!file.exists()) {
                file.mkdirs();
            }
            writer.flush();
        }
    }

    @AfterEach
    void tearDown() {
        //remove file at local
        File file = Paths.get(System.getProperty("user.home"), "sftp-module-test", "sftp-test-file.txt").toFile();
        boolean delete = file.delete();
        assertThat(delete).isTrue();
    }

    @Test
    void read() throws Exception {
        String targetPath = "sftp-test-file.txt";
        File file = Paths.get(System.getProperty("user.home"), "sftp-module-test", targetPath).toFile();
        boolean upload = sftpService.upload(targetPath, file);
        File read = sftpService.read(targetPath);
        assertThat(upload).isTrue();
        assertThat(read).isNotNull().isFile().canRead().canRead();
    }

    @Test
    void download() throws Exception {
        String targetPath = "sftp-test-file.txt";
        File file = Paths.get(System.getProperty("user.home"), "sftp-module-test", targetPath).toFile();
        sftpService.upload(targetPath, file);
        boolean download = sftpService.download(targetPath, Paths.get("src", "main", "resources", "download", "download-file.txt"));
        assertThat(download).isTrue();
    }

    @Test
    void uploadParamIsFile() throws Exception {
        File file = Paths.get(System.getProperty("user.home"), "sftp-module-test", "sftp-test-file.txt").toFile();
        boolean upload = sftpService.upload("sftp/upload-file.txt", file);
        assertThat(upload).isTrue();
    }

    @Test
    void uploadParamIsInputStream() throws Exception {
        File localFile = Paths.get(System.getProperty("user.home"), "sftp-module-test", "sftp-test-file.txt").toFile();
        boolean upload = sftpService.upload("sftp/upload-file.txt", new FileInputStream(localFile));
        assertThat(upload).isTrue();
    }

    @Test
    void remove() throws Exception {
        String targetPath = "sftp-test-file.txt";
        sftpService.upload(targetPath, Paths.get(System.getProperty("user.home"), "sftp-module-test", targetPath).toFile());
        boolean delete = sftpService.remove("sftp-test-file.txt");
        assertThat(delete).isTrue();
    }
}
