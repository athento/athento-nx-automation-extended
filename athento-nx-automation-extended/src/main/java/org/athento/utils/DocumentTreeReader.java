package org.athento.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.automation.core.scripting.DocumentWrapper;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.ExportedDocumentImpl;
import org.nuxeo.ecm.core.io.impl.plugins.DocumentModelReader;

import java.io.IOException;

/**
 * Based on {@link DocumentTreeReader} of Nuxeo DM.
 */
public class DocumentTreeReader extends DocumentModelReader {

    private static final Log LOG = LogFactory.getLog(DocumentTreeReader.class);

    DocumentModel currentDoc;

    protected int pathSegmentsToRemove = 0;
    protected boolean leafDoc = true;

    public DocumentTreeReader(CoreSession session, DocumentModel doc) throws ClientException {
        super(session);
        if (doc == null) {
            throw new ClientException("Document is mandatory");
        }
        LOG.info("Init tree reader ...");
        this.currentDoc = doc;
        pathSegmentsToRemove = 1;
    }

    @Override
    public void close() {
        super.close();
        this.currentDoc = null;
    }

    @Override
    public ExportedDocument read() throws IOException {
        if (this.currentDoc != null) {
            DocumentModel docModel = null;
            try {
                LOG.info("Current doc " + this.currentDoc.getPathAsString());
                try {
                    if (pathSegmentsToRemove > 0) {
                        docModel = this.currentDoc;
                        if (leafDoc) {
                            leafDoc = false;
                            return new ExportedDocumentImpl(docModel, docModel.getPath().removeFirstSegments(
                                    pathSegmentsToRemove), inlineBlobs);
                        } else {
                            if (!docModel.getType().equals("Domain")) {
                                // remove unwanted leading segments
                                return new ExportedDocumentImpl(docModel,
                                        docModel.getPath().removeFirstSegments(
                                                pathSegmentsToRemove), inlineBlobs);
                            }
                        }
                    } else {
                        return new ExportedDocumentImpl(docModel, inlineBlobs);
                    }
                } finally {
                    if (currentDoc.getParentRef() != null) {
                        this.currentDoc = new DocumentWrapper(session, currentDoc).getParent().getDoc();
                    } else {
                        this.currentDoc = null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private boolean hasParent(DocumentModel currentDoc) {
        return currentDoc.getParentRef() != null;
    }

}
