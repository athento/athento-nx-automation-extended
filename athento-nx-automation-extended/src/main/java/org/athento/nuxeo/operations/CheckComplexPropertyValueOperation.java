package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.nuxeo.common.utils.StringUtils;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.Property;

import java.io.Serializable;
import java.util.*;

/**
 * @author <a href="vs@athento.com">Victor Sanchez</a>
 */
@Operation(id = CheckComplexPropertyValueOperation.ID, category = Constants.CAT_DOCUMENT, label = "Check Complex property value", description = "Check for a complex property value")
public class CheckComplexPropertyValueOperation {

    public static final String ID = "Athento.CheckComplexPropertyValue";

    private static final Log LOG = LogFactory.getLog(CheckComplexPropertyValueOperation.class);

    @Context
    protected CoreSession session;

    @Param(name = "xpath")
    protected String xpath;

    @Param(name = "metadata")
    protected String metadata;

    @Param(name = "value")
    protected String value;

    @Param(name = "chainId")
    protected String chainId;

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) throws Exception {
        LOG.info("Checking complex property...");
        Property p = doc.getProperty(xpath);
        List<Serializable> array = null;
        if (p.getValue() != null) {
            if (p.getValue() instanceof Serializable[]) {
                array = Arrays.asList((Serializable[]) p.getValue());
            } else if (p.getValue() instanceof ArrayList) {
                array = (ArrayList) p.getValue();
            } else {
                throw new OperationException("Value must be a list");
            }
        }
        if (!array.isEmpty()) {
            boolean check = true;
            for (Serializable v : array) {
                if (v != null && !v.equals(value)) {
                    check = false;
                    break;
                }
            }
            if (check) {
                // Launch chainId
                AthentoOperationsHelper.runOperation(chainId, doc, new HashMap(0), session);
            }
        }
        return doc;
    }


}
