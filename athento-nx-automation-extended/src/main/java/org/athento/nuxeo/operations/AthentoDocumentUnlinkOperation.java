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
import org.nuxeo.ecm.core.api.model.Property;

import java.util.Map;

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
            for (Map.Entry<String, String> property : properties.entrySet()) {
                String metadata = property.getKey();
                Property prop;
                if ((prop = doc.getProperty(metadata)) != null) {
                    if (!prop.isComplex() && !prop.isList()) {
                        // check same value only for primitives to clean the value or save the new value
                        // if they are distinct
                        if (prop.getValue() != null && prop.getValue().equals(property.getValue())) {
                            doc.setPropertyValue(metadata, null);
                        } else {
                            doc.setPropertyValue(metadata, property.getValue());
                        }
                    }
                }
            }
            // Save document
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
            if (sourceDoc != null) {
                unlink(doc);
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
                    // Unlink destiny document
                    run(destinyDoc);
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