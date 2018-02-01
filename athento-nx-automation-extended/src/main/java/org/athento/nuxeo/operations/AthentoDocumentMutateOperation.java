/**
 *
 */
package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.athento.utils.StringUtils;
import org.nuxeo.ecm.automation.ConflictOperationException;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.tag.TagService;

import java.util.HashMap;
import java.util.Map;

/**
 * Athento Mutate operation.
 */
@Operation(id = AthentoDocumentMutateOperation.ID, category = "Athento", label = "Athento Document Mutate", description = "Create a document with documenttype a copy all metadata")
public class AthentoDocumentMutateOperation extends AbstractAthentoOperation {

    /**
     * Log.
     */
    private static final Log _log = LogFactory
            .getLog(AthentoDocumentMutateOperation.class);

    /**
     * Operation ID.
     */
    public static final String ID = "Athento.Document.Mutate";

    @Context
    protected CoreSession session;

    /** Operation context. */
    @Context
    protected OperationContext ctx;

    /** New document type. */
    @Param(name = "type", required = false)
    protected String type;

    @Param(name = "destination", required = false)
    protected String destination;

    /** Properties to add. */
    @Param(name = "properties", required = false)
    protected Properties properties;

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) throws Exception {
        // Check access
        checkAllowedAccess(ctx);

        // Check destination
        if (destination == null) {
            destination = (String) doc.getParentRef().reference();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("properties", properties);
        params.put("type", type);
        params.put("destination", destination);

        // Create new document
        DocumentModel mutatedDoc = (DocumentModel) AthentoOperationsHelper.runOperation(AthentoDocumentCreateOperation.ID, doc.getParentRef(), params, session);

        return mutatedDoc;

    }



}