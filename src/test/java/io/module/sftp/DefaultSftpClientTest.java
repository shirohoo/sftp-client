package io.module.sftp;

import static org.assertj.core.api.Assertions.assertThat;

import io.module.sftp.properties.SftpProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
        File file = new File(Paths.get(Paths.get(System.getProperty("user.home"), "sftp-module-test").toString(), "sftp-test-file.txt").toString());
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
        File file = new File(Paths.get(Paths.get(System.getProperty("user.home"), "sftp-module-test").toString(), "sftp-test-file.txt").toString());
        boolean delete = file.delete();
        assertThat(delete).isTrue();
    }

    @Test
    void read() throws Exception {
        //given
        sftpService.upload("sftp-test-file.txt", new File(Paths.get(Paths.get(System.getProperty("user.home"), "sftp-module-test").toString(), "sftp-test-file.txt").toString()));

        //when
        File read = sftpService.read("sftp-test-file.txt");

        //then
        assertThat(read).isNotNull().isFile().canRead().canRead();
    }

    @Test
    void download() throws Exception {
        //given
        sftpService.upload("sftp-test-file.txt", new File(Paths.get(Paths.get(System.getProperty("user.home"), "sftp-module-test").toString(), "sftp-test-file.txt").toString()));

        //when
        boolean download = sftpService.download("sftp-test-file.txt", Paths.get("src", "main", "resources", "download", "download-file.txt"));

        //then
        assertThat(download).isTrue();
    }

    @Test
    void uploadParamIsFile() throws Exception {
        //given
        File file = new File(Paths.get(Paths.get(System.getProperty("user.home"), "sftp-module-test").toString(), "sftp-test-file.txt").toString());

        //when
        boolean upload = sftpService.upload("sftp/upload-file.txt", file);

        //then
        assertThat(upload).isTrue();
    }

    @Test
    void uploadParamIsInputStream() throws Exception {
        //given
        File localFile = new File(Paths.get(Paths.get(System.getProperty("user.home"), "sftp-module-test").toString(), "sftp-test-file.txt").toString());

        //when
        boolean upload = sftpService.upload("sftp/upload-file.txt", new FileInputStream(localFile));

        //then
        assertThat(upload).isTrue();
    }

    @Test
    void remove() throws Exception {
        //given
        sftpService.upload("sftp-test-file.txt", new File(Paths.get(Paths.get(System.getProperty("user.home"), "sftp-module-test").toString(), "sftp-test-file.txt").toString()));

        //when
        boolean delete = sftpService.remove("sftp-test-file.txt");

        //then
        assertThat(delete).isTrue();
    }
}
