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
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.utils.DocumentModelUtils;
import org.nuxeo.ecm.platform.tag.TagService;

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

    public static final String CONFIG_OPERATION_ID_PRE = "automationExtendedConfig:documentLinkOperationIdPre";
    public static final String CONFIG_OPERATION_ID_POST = "automationExtendedConfig:documentLinkOperationIdPost";

    private static final Log LOG = LogFactory
            .getLog(AthentoDocumentLinkOperation.class);

    @Context
    protected CoreSession session;

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
        LOG.info("Linking a document " + doc.getId());
        DocumentModel linkedDoc = session.copy(doc.getRef(), doc.getParentRef(), doc.getName());
        // FIXME: Complete
        return linkedDoc;
    }


}