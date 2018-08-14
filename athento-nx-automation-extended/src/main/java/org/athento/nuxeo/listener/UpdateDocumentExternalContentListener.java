package org.athento.nuxeo.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.utils.FTPException;
import org.athento.utils.FTPUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.api.model.PropertyNotFoundException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.runtime.api.Framework;

import java.io.File;
import java.io.Serializable;

/**
 * Listener to update the file:content metadata from an external uri, using dc:source metadata.
 */
public class UpdateDocumentExternalContentListener implements EventListener {

    /** Log. */
    private static final Log LOG = LogFactory.getLog(UpdateDocumentExternalContentListener.class);

    /**
     * Handle event.
     *
     * @param event
     */
    @Override
    public void handleEvent(Event event) {
        DocumentEventContext ctxt = (DocumentEventContext) event.getContext();
        if (ctxt instanceof DocumentEventContext) {
            DocumentModel doc = ctxt.getSourceDocument();
            if (doc.isVersion()) {
                return;
            }
            if (hasExternalContent(doc)) {
                CoreSession session = ctxt.getCoreSession();
                try {
                    String source = (String) doc.getPropertyValue("dc:source");
                    LOG.info("External content detected for " + doc);
                    boolean removeRemote = FTPUtils.checkRemoveRemoteFile(source);
                    String remoteFilePath = FTPUtils.getRemoteFilePath(source);
                    File remoteFile = FTPUtils.getFile(remoteFilePath, removeRemote);
                    if (remoteFile != null) {
                        LOG.info("Remote file " + remoteFile.getName());
                        Blob blob = new FileBlob(remoteFile);
                        blob.setFilename(remoteFile.getName());
                        String mime;
                        try {
                            MimetypeRegistry mimetypeRegistry = Framework.getService(MimetypeRegistry.class);
                            mime = mimetypeRegistry.getMimetypeFromBlob(blob);
                            LOG.info("Mime is " + mime);
                            if (mime == null) {
                                mime = "application/pdf";
                            }
                        } catch (Exception e) {
                            LOG.warn("Mimetype problems in external content", e);
                            mime = "application/pdf";
                        }
                        blob.setMimeType(mime);
                        // Clear source after update
                        doc.setPropertyValue("dc:source", "Content from " + source);
                        doc.setPropertyValue("file:content", (Serializable) blob);
                        session.saveDocument(doc);
                        session.save();
                    }
                } catch (FTPException e) {
                    LOG.error("Unable to set blob from external content SFTP", e);
                }
            }
        }
    }

    /**
     * Check if the document has an external content.
     *
     * @param doc
     * @return
     */
    private boolean hasExternalContent(DocumentModel doc) {
        boolean hasExternal = false;
        if (doc != null) {
            try {
                String source = (String) doc.getPropertyValue("dc:source");
                if (source != null) {
                    return source.startsWith("sftp:") || source.startsWith("ftp:");
                }
            } catch (PropertyNotFoundException e) {
                LOG.trace("Unable to check external content for document" +
                        " because it doesn't have dublincore source metadata");
            }
        }
        return hasExternal;
    }


}
