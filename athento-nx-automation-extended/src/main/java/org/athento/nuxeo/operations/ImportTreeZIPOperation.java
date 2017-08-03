package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.impl.DocumentPipeImpl;
import org.nuxeo.ecm.core.io.impl.plugins.DocumentTreeReader;
import org.nuxeo.ecm.core.io.impl.plugins.NuxeoArchiveWriter;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Import a documents tree from ZIP files.
 */
@Operation(id = ImportTreeZIPOperation.ID, category = "Athento", label = "Import documents tree from ZIP", description = "Import documents tree from ZIP")
public class ImportTreeZIPOperation {

    /**
     * Log.
     */
    private static final Log LOG = LogFactory.getLog(ImportTreeZIPOperation.class);

    /** Operation ID. */
    public static final String ID = "Athento.ImportTreeZIP";

    @Context
    protected FileManager fileManager;

    @Param(name = "sourceDir")
    protected String sourceDir;

    @Param(name = "overwrite", required = false)
    protected boolean overwrite = false;

    @Context
    protected CoreSession session;

    /** Run. */
    @OperationMethod
    public void run() throws Exception {
        importZips(sourceDir);
    }

    /**
     * Import ZIPs.
     *
     * @param dir
     */
    public void importZips(String dir) {
        // Read Zip files from dir
        File dirFile = new File(dir);
        if (dirFile.exists()) {
            File files [] = dirFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().toLowerCase().endsWith(".zip")) {
                        return true;
                    }
                    return false;
                }
            });
            int importedZip = 0;
            for (File zipFile : files) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Importing ZIP file: " + zipFile.getName() + " ...");
                }
                try {
                    String filename = zipFile.getName();
                    if (filename.contains("$")) {
                        filename = filename.split("\\$")[0]; // Path
                        FileBlob blob = new FileBlob(zipFile);
                        fileManager.createDocumentFromBlob(session, blob, "/" + filename, overwrite,
                                blob.getFilename());
                        importedZip++;
                    }
                } catch (Exception e) {
                    LOG.error("Unable to import file " + zipFile.getName(), e);
                }
            }
            LOG.info("Imported ZIPs: " + importedZip);
        }
    }

}
