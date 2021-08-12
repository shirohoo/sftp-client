package io.module.sftp;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

public interface SftpClient {
    File read(String targetPath);
    boolean upload(String targetPath, File file);
    boolean upload(String targetPath, InputStream inputStream);
    boolean remove(String targetPath);
    boolean download(String targetPath, Path downloadPath);
}
