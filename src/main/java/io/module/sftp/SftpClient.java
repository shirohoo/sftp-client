package io.module.sftp;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

public interface SftpClient {
    /**
     * Pass the path of the file you want to read as an argument. The starting path is the root of SftpProperties.
     * For example, if root is ~/ and the path passed as an argument is user/temp/test.txt , SftpClient reads ~/user/temp/test.txt and returns it as a File object.
     * @param targetPath String
     * @return File
     */
    File read(String targetPath);

    /**
     * The location where you want to upload the file is passed as the first argument, and the file you want to upload as the second argument.
     * In this case, the first argument must also include the name of the file.
     * @param targetPath String
     * @param file File
     * @return boolean
     */
    boolean upload(String targetPath, File file);

    /**
     * The location where you want to upload the file is passed as the first argument, and the InputStream of file you want to upload as the second argument.
     * In this case, the first argument must also include the name of the file.
     * @param targetPath String
     * @param inputStream InputStream
     * @return boolean
     */
    boolean upload(String targetPath, InputStream inputStream);

    /**
     * If you pass the path to the file you want to remove as an argument, it will try to remove the file and return whether the removal succeeded or failed.
     * @param targetPath String
     * @return boolean
     */
    boolean remove(String targetPath);

    /**
     * Pass the path to the file you want to download from the remote server as the first argument. In this case, the first argument must include a file name.
     * Enter the location where you want to save the downloaded file as the second argument.
     * Returns true if the download is successful, false if it fails
     * @param targetPath String
     * @param downloadPath Path
     * @return boolean
     */
    boolean download(String targetPath, Path downloadPath);
}
