package io.github.shirohoo.sftp;

import com.jcraft.jsch.JSchException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.List;

public interface SftpClient {

    /**
     * Pass the path of the file you want to read as an argument. The starting path is the root of SftpProperties. For example, if root is ~/ and the path passed as an argument is user/temp/someFile.txt , SftpClient reads ~/user/temp/someFile.txt and
     * returns it as a File object. At this time, the transferred file is not saved on the client hard disk, but only in client memory.
     *
     * @param targetPath String
     * @return File
     */
    File read(final String targetPath) throws JSchException, NotDirectoryException;

    /**
     * Pass the path of the file you want to read as an argument. The starting path is the root of SftpProperties. For example, if root is ~/ and the path passed as an argument is user/temp/someDir (last args is directory name) , SftpClient reads
     * ~/user/temp/someDir and returns it as a List {@literal <}File{@literal >} object.
     *
     * @param targetDirPath String
     * @return List {@literal <}File{@literal >}
     */
    List<File> listFiles(final String targetDirPath) throws JSchException, NoSuchFileException;

    /**
     * The location where you want to upload the file is passed as the first argument, and the file you want to upload as the second argument. In this case, the first argument must also include the name of the file.
     *
     * @param targetPath String
     * @param uploadFile       File
     * @return boolean
     */
    boolean upload(final String targetPath, final File uploadFile) throws JSchException;

    /**
     * The location where you want to upload the file is passed as the first argument, and the InputStream of file you want to upload as the second argument. In this case, the first argument must also include the name of the file.
     *
     * @param targetPath  String
     * @param uploadFileStream InputStream
     * @return boolean
     */
    boolean upload(final String targetPath, final InputStream uploadFileStream) throws IOException, JSchException;

    /**
     * If you pass the path to the file you want to remove as an argument, it will try to remove the file and return whether the removal succeeded or failed.
     *
     * @param targetPath String
     * @return boolean
     */
    boolean remove(final String targetPath) throws JSchException;

    /**
     * Pass the path to the file you want to download from the remote server as the first argument. In this case, the all argument must include a file name. Enter the location where you want to save the downloaded file as the second argument. Returns
     * true if the download is successful, false if it fails
     *
     * @param targetPath   String
     * @param downloadPath Path
     * @return boolean
     */
    boolean download(final String targetPath, Path downloadPath) throws JSchException;

}
