package io.module.sftp.service;

import java.io.File;
import java.io.InputStream;

public interface SftpFileService {
    File read(String targetPath) throws Exception;
    File download(String targetPath, String downloadPath) throws Exception;
    boolean upload(String targetPath, File file) throws Exception;
    boolean upload(String targetPath, InputStream inputStream) throws Exception;
    boolean delete(String fileName) throws Exception;
}
