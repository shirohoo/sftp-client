package io.module.sftp;

import com.jcraft.jsch.JSchException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface SftpClient {
    File read(String targetPath) throws FileNotFoundException, JSchException;
    boolean upload(String targetPath, File file) throws IOException, JSchException;
    boolean upload(String targetPath, InputStream inputStream) throws JSchException, IOException;
    boolean remove(String targetPath) throws JSchException;
    boolean download(String targetPath, Path downloadPath) throws JSchException;
}
