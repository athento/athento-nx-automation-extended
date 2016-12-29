/**
 * 
 */
package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.client.model.DateUtils;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.relations.api.DocumentRelationManager;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Link a document.
 *
 * @author athento
 *
 */
@Operation(id = AthentoDocumentLinkOperation.ID, category = "Athento", label = "Athento Document Link", description = "Creates a document link from source document")
public class AthentoDocumentLinkOperation extends AbstractAthentoOperation {

    public static final String ID = "Athento.Document.Link";

    private static final Log LOG = LogFactory
            .getLog(AthentoDocumentLinkOperation.class);

    // Ignored system schemas
    private static final String [] IGNORED_SCHEMAS = { "uid", "file", "files", "common", "inherit", "inheritance" };

    @Context
    protected CoreSession session;

    @Context
    protected DocumentRelationManager relations;

    /** Operation context. */
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
            LOG.info("Linking a document " + doc.getId());
        }
        // Prepare create link document params
        Map<String, Object> params = new HashMap<>();
        params.put("type", doc.getType());
        params.put("name", doc.getName());
        // Extract properties from source document and add to properties param
        Properties linkedDocProperties = extractAndAddProperties(doc);
        properties.putAll(linkedDocProperties);
        params.put("properties", properties);
        // Call to Athento.DocumentCreate operation to create the document
        DocumentModel linkedDocument = (DocumentModel) AthentoOperationsHelper
                .runOperation(AthentoDocumentCreateOperation.ID, null, params, session);
        if (linkedDocument != null) {
            // Add content and filename
            linkedDocument.setPropertyValue("file:content", doc.getPropertyValue("file:content"));
            linkedDocument.setPropertyValue("file:filename", doc.getPropertyValue("file:filename"));
            session.saveDocument(linkedDocument);
            // Create relation
            relations.addRelation(session, doc, linkedDocument, "http://purl.org/dc/terms/References", true);
            if (LOG.isInfoEnabled()) {
                LOG.info("Linked document " + linkedDocument.getRef());
            }
        }
        return linkedDocument;
    }

    /**
     * Extract properties to create the linked document.
     *
     * @param doc
     * @return
     */
    private Properties extractAndAddProperties(DocumentModel doc) {
        Properties properties = new Properties();
        for (String schema : doc.getSchemas()) {
            if (!Arrays.asList(IGNORED_SCHEMAS).contains(schema)) {
                for (Map.Entry<String, Object> entry : doc.getProperties(schema).entrySet()) {
                    Object v = entry.getValue();
                    String property = entry.getKey();
                    if (!this.properties.containsKey(property)) {
                        if (v instanceof String) {
                            properties.put(entry.getKey(), v.toString());
                        } else if (v instanceof Boolean) {
                            properties.put(entry.getKey(), String.valueOf(v));
                        } else if (v instanceof Integer) {
                            properties.put(entry.getKey(), String.valueOf(v));
                        } else if (v instanceof Date) {
                            properties.put(entry.getKey(), DateUtils.formatDate((Date) v));
                        } else {
                            LOG.warn("Unsupported scalar value for property " + entry.getKey());
                        }
                    }
                }
            }
        }
        return properties;
    }


}