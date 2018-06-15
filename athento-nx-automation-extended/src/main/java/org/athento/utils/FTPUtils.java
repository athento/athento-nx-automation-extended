package org.athento.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.Path;
import org.nuxeo.runtime.api.Framework;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * FTP Utils class.
 */
public class FTPUtils {

    /** Log. */
    private static final Log LOG = LogFactory.getLog(FTPUtils.class);

    /**
     * Get file from FTP.
     *
     * @param sftpString
     * @param remove remove from ftp server
     * @return
     */
    public static final File getFile(String sftpString, boolean remove) throws FTPException {
        if (!sftpString.startsWith("sftp") && !sftpString.startsWith("ftp"))  {
            throw new FTPException("SFTP remote is not valid");
        }
        SftpClient client = null;
        try {
            URI uri = new URI(sftpString);
            String host = uri.getHost();
            int port = uri.getPort();
            if (port == -1) {
                port = 22;
            }
            String username = "";
            String password = "";
            String userinfo = uri.getUserInfo();
            if (userinfo != null && userinfo.contains(":")) {
                username = userinfo.split(":")[0];
                password = userinfo.split(":")[1];
            }
            if (username.isEmpty()) {
                username = Framework.getProperty("sftpserver.username." + host);
            }
            if (password.isEmpty()) {
                password = Framework.getProperty("sftpserver.password." + host);
            }
            String path = uri.getPath();
            client = new SftpClient(host, port, username, password);
            client.connect();

            String basename = FilenameUtils.getBaseName(path);
            String extension = FilenameUtils.getExtension(path);

            // Create temp file
            File file = File.createTempFile(basename, "." + extension);

            // Get file from server
            client.retrieveFile(path, file.getAbsolutePath());
            if (remove) {
                client.removeFile(path);
            }

            return file;

        } catch (Exception e) {
            throw new FTPException(e);
        } finally {
            if (client != null) {
                client.disconnect();
            }
        }
    }

    /**
     * Check if a sftp string connection has password.
     *
     * @param sftpString
     * @return
     */
    public static boolean hasPassword(String sftpString) {
        try {
            URI uri = new URI(sftpString);
            String userinfo = uri.getUserInfo();
            if (userinfo != null && userinfo.contains(":")) {
                return !userinfo.split(":")[1].isEmpty();
            }
            return false;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Check remove file.
     *
     * @param value
     * @return
     */
    public static boolean checkRemoveRemoteFile(String value) {
        boolean remove = true;
        if (value != null) {
            if (value.contains(" --no-remove") || value.contains(" --nr")) {
                remove = false;
            }
        }
        return remove;
    }

    /**
     * Get remote file path.
     *
     * @param remoteString
     * @return
     */
    public static String getRemoteFilePath(String remoteString) {
        String remoteFilePath = new String(remoteString);
        if (remoteFilePath != null) {
            remoteFilePath = remoteFilePath.replace(" --no-remove", "").replace(" --nr", "");
        }
        return remoteFilePath;
    }
}
