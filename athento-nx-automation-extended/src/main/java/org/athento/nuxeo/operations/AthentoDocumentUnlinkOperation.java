/**
 *
 */
package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;

/**
 * Unlink a document.
 *
 * @author athento
 */
@Operation(id = AthentoDocumentUnlinkOperation.ID, category = "Athento", label = "Athento Document Unlink", description = "Remove a document link for a source document")
public class AthentoDocumentUnlinkOperation extends AbstractAthentoOperation {

    /**
     * Operation ID.
     */
    public static final String ID = "Athento.Document.Unlink";

    private static final Log LOG = LogFactory
            .getLog(AthentoDocumentUnlinkOperation.class);

    @Context
    protected CoreSession session;

    /**
     * Operation context.
     */
    @Context
    protected OperationContext ctx;

    @Param(name = "properties", required = false)
    protected Properties properties;

    /**
     * Run operation.
     *
     * @param doc
     * @return
     * @throws Exception
     */
    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("Unlinking a document " + doc.getId());
        }
        if (!doc.hasFacet("relationable")) {
            // Update document with properties
            LOG.info("Unlinking the document " + doc.getId()
                    + " with properties because it has not the relationable facet");
            DocumentHelper.setProperties(session, doc, properties);
            session.saveDocument(doc);
        } else {
            // Remove link document between source and destiny
            String sourceDocId = (String) doc.getPropertyValue("athentoRelation:sourceDoc");
            DocumentModel sourceDoc = null;
            try {
                sourceDoc = session.getDocument(new IdRef(sourceDocId));
            } catch (ClientException e) {
                // No source document is found
            }
            // Check if it is a leaf in the relation
            DocumentModel destinyDoc = null;
            String destinyDocId = (String) doc.getPropertyValue("athentoRelation:destinyDoc");
            try {
                destinyDoc = session.getDocument(new IdRef(destinyDocId));
            } catch (ClientException e) {
                // Do nothing
            }
            unlink(doc);
            if (sourceDoc != null) {
                if (destinyDoc == null) {
                    // The document is a leaf in the global relations, then remove the destiny id in his source document
                    sourceDoc.setPropertyValue("athentoRelation:destinyDoc", null);
                } else {
                    // The document is not a leaf in the global relations, then put the destiny id to the source
                    if (sourceDocId.equals(doc.getId())) {
                        sourceDoc.setPropertyValue("athentoRelation:sourceDoc", null);
                    }
                    sourceDoc.setPropertyValue("athentoRelation:destinyDoc", destinyDocId);
                    if (destinyDocId.equals(doc.getId())) {
                        destinyDoc.setPropertyValue("athentoRelation:destinyDoc", null);
                    }
                    destinyDoc.setPropertyValue("athentoRelation:sourceDoc", sourceDocId);
                    session.saveDocument(destinyDoc);
                }
                session.saveDocument(sourceDoc);
            } else {
                if (destinyDoc != null) {
                    // The document is not a leaf in the global relations, then put the destiny id to the source
                    destinyDoc.setPropertyValue("athentoRelation:sourceDoc", null);
                    session.saveDocument(destinyDoc);
                }
            }
        }
        return doc;

    }

    /**
     * Unlink document.
     *
     * @param doc
     */
    private void unlink(DocumentModel doc) {
        // The document is a leaf in the relation
        if ("default".equals(doc.getLifeCyclePolicy())) {
            session.followTransition(doc.getRef(), "delete");
        }
    }


}