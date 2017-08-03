package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.utils.DocumentTreeReader;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.impl.DocumentPipeImpl;
import org.nuxeo.ecm.core.io.impl.plugins.NuxeoArchiveWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Export a document tree to a ZIP file.
 */
@Operation(id = ExportTreeToZIPOperation.ID, category = "Athento", label = "Export document tree to ZIP", description = "Export document tree into a ZIP file")
public class ExportTreeToZIPOperation {

    /**
     * Log.
     */
    private static final Log LOG = LogFactory.getLog(ExportTreeToZIPOperation.class);

    /** Operation ID. */
    public static final String ID = "Athento.ExportTreeToZIP";

    @Param(name = "destinyDir", required = false)
    protected String destinyDir = null;

    @Context
    protected CoreSession session;

    /** Run. */
    @OperationMethod
    public void run(DocumentModel doc) throws Exception {
        DocumentModelList docs = new DocumentModelListImpl();
        docs.add(doc);
        export(docs);
    }

    /** Run. */
    @OperationMethod
    public void run(DocumentModelList docs) throws Exception {
        export(docs);
    }

    /**
     * Export.
     *
     * @param docs
     */
    public void export(List<DocumentModel> docs) {
        DocumentReader reader = null;
        DocumentWriter writer = null;
        FileOutputStream fos = null;
        int totalZips = 0;
        for (DocumentModel doc : docs) {
            try {
                LOG.info("Making ZIP for document: " + doc.getId());
                File tmpFile;
                if (destinyDir == null) {
                    destinyDir = "/tmp/" + doc.getPath().segment(0);
                }
                File destinyDirFile = new File(destinyDir);
                if (!destinyDirFile.exists()) {
                    destinyDirFile.mkdirs();
                }
                tmpFile = new File(destinyDir + "/" + doc.getPath().segment(0) + "$" + doc.getId() + ".zip");
                if (LOG.isInfoEnabled()) {
                    LOG.info("ZIP File exported is in: " + tmpFile.getAbsolutePath());
                }
                fos = new FileOutputStream(tmpFile);
                reader = new DocumentTreeReader(session,  doc);
                writer = new NuxeoArchiveWriter(fos);
                DocumentPipe pipe = new DocumentPipeImpl(10);
                pipe.setReader(reader);
                pipe.setWriter(writer);
                pipe.run();
                totalZips++;
            } catch (ClientException e) {
                LOG.error("Error during XML export " + e.getMessage());
            } catch (IOException e) {
                LOG.error("Error during XML export " + e.getMessage());
            } catch (Exception e) {
                LOG.error("Error during XML export " + e.getMessage());
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Total ZIPs created: " + totalZips + " in folder: " + destinyDir);
        }
    }

}
