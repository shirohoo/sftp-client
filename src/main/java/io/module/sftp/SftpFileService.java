package io.module.sftp;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

public interface SftpFileService {
    File read(String targetPath) throws Exception;
    boolean upload(String targetPath, File file) throws Exception;
    boolean upload(String targetPath, InputStream inputStream) throws Exception;
    boolean remove(String targetPath) throws Exception;
    boolean download(String targetPath, Path downloadPath) throws Exception;
}
