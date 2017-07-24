package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.impl.DocumentPipeImpl;
import org.nuxeo.ecm.core.io.impl.plugins.NuxeoArchiveWriter;
import org.nuxeo.ecm.platform.io.selectionReader.DocumentModelListReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Export a document list to a ZIP file.
 */
@Operation(id = ExportDocumentsToZIPOperation.ID, category = "Athento", label = "Export documents to ZIP", description = "Export documents into a ZIP file")
public class ExportDocumentsToZIPOperation {

    /**
     * Log.
     */
    private static final Log LOG = LogFactory.getLog(ExportDocumentsToZIPOperation.class);

    /** Operation ID. */
    public static final String ID = "Athento.ExportDocsToZIP";

    @Param(name = "destinyPath", required = false)
    private String destinyPath = null;

    /** Run. */
    @OperationMethod
    public Blob run(DocumentModel doc) throws Exception {
        DocumentModelList docs = new DocumentModelListImpl();
        docs.add(doc);
        return export(docs);
    }

    /** Run. */
    @OperationMethod
    public Blob run(DocumentModelList docs) throws Exception {
        return export(docs);
    }

    /**
     * Export.
     *
     * @param docs
     */
    public Blob export(List<DocumentModel> docs) {
        DocumentReader reader = null;
        DocumentWriter writer = null;
        FileOutputStream fos = null;
        try {
            File tmpFile;
            if (destinyPath == null) {
                tmpFile = File.createTempFile("athento-zip-export-", ".zip");
            } else {
                tmpFile = new File(destinyPath);
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("ZIP File exported is in: " + tmpFile.getAbsolutePath());
            }
            fos = new FileOutputStream(tmpFile);
            reader = new DocumentModelListReader(docs);
            writer = new NuxeoArchiveWriter(fos);
            DocumentPipe pipe = new DocumentPipeImpl(10);
            pipe.setReader(reader);
            pipe.setWriter(writer);
            pipe.run();
            return new FileBlob(tmpFile);
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
        return null;
    }

}
