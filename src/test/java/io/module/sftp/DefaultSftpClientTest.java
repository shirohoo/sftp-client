package io.module.sftp;

import static org.assertj.core.api.Assertions.assertThat;

import io.module.sftp.properties.SftpProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("SftpProperties is not setup")
class DefaultSftpClientTest {
    private SftpClient sftpClient;

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

        sftpClient = new DefaultSftpClient(properties);

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
        final File file = Paths.get(System.getProperty("user.home"), "sftp-module-test", "sftp-test-file.txt").toFile();
        boolean delete = file.delete();
        assertThat(delete).isTrue();
    }

    @Test
    void read() throws Exception {
        final String targetPath = "sftp-test-file.txt";
        final File file = Paths.get(System.getProperty("user.home"), "sftp-module-test", targetPath).toFile();
        boolean upload = sftpClient.upload(targetPath, file);
        File read = sftpClient.read(targetPath);
        assertThat(upload).isTrue();
        assertThat(read).isNotNull().isFile().canRead().canRead();
    }


    @Test
    void listFiles() throws Exception {
        final String targetPath = Paths.get(System.getProperty("user.home"), "sftp-module-test").toString();
        List<File> files = sftpClient.listFiles(targetPath);
        Assertions.assertThat(files).filteredOn(File.class::isInstance);
    }

    @Test
    void download() throws Exception {
        final String targetPath = "sftp-test-file.txt";
        final File file = Paths.get(System.getProperty("user.home"), "sftp-module-test", targetPath).toFile();
        sftpClient.upload(targetPath, file);
        boolean download = sftpClient.download(targetPath, Paths.get("src", "main", "resources", "download", "download-file.txt"));
        assertThat(download).isTrue();
    }

    @Test
    void uploadParamIsFile() throws Exception {
        final File file = Paths.get(System.getProperty("user.home"), "sftp-module-test", "sftp-test-file.txt").toFile();
        boolean upload = sftpClient.upload("sftp/upload-file.txt", file);
        assertThat(upload).isTrue();
    }

    @Test
    void uploadParamIsInputStream() throws Exception {
        final File localFile = Paths.get(System.getProperty("user.home"), "sftp-module-test", "sftp-test-file.txt").toFile();
        boolean upload = sftpClient.upload("sftp/upload-file.txt", new FileInputStream(localFile));
        assertThat(upload).isTrue();
    }

    @Test
    void remove() throws Exception {
        final String targetPath = "sftp-test-file.txt";
        sftpClient.upload(targetPath, Paths.get(System.getProperty("user.home"), "sftp-module-test", targetPath).toFile());
        boolean delete = sftpClient.remove("sftp-test-file.txt");
        assertThat(delete).isTrue();
    }
}
