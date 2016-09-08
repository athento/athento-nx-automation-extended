/**
 *
 */
package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.api.model.BatchResult;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.athento.utils.StringUtils;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelListCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.utils.DocumentModelUtils;

import java.util.*;

/**
 * @author athento
 */
@Operation(id = AthentoDocumentMultiCreateOperation.ID, category = "Athento", label = "Athento Multi Document Create", description = "Creates multi-document in Athento's way")
public class AthentoDocumentMultiCreateOperation {

    public static final String ID = "Athento.MultiDocumentCreate";

    private static final Log LOG = LogFactory
            .getLog(AthentoDocumentMultiCreateOperation.class);

    @Context
    protected CoreSession session;

    @Param(name = "destination", required = false)
    protected String destination;

    @Param(name = "name", required = false)
    protected String name;

    @Param(name = "common_properties", required = false)
    protected Properties commonProperties;

    @Param(name = "properties", required = false)
    protected List<Properties> properties;

    @Param(name = "type")
    protected String type;

    /**
     * Run.
     *
     * @return document model list
     * @throws Exception
     */
    @OperationMethod()
    public DocumentModelList run() throws Exception {
        String parentFolderPath = getDestinationPath();
        return run(new PathRef(parentFolderPath));
    }

    /**
     * Run.
     *
     * @param docRef with document ref
     * @return document model list
     * @throws Exception
     */
    @OperationMethod(collector = DocumentModelListCollector.class)
    public DocumentModelList run(DocumentRef docRef) throws Exception {
        return run(session.getDocument(docRef));
    }

    /**
     * Run.
     *
     * @param parentDoc
     * @return document model list
     * @throws Exception
     */
    @OperationMethod(collector = DocumentModelListCollector.class)
    public DocumentModelList run(DocumentModel parentDoc) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug(AthentoDocumentMultiCreateOperation.ID
                    + " BEGIN with params:");
            LOG.debug(" - parentDoc: " + parentDoc);
            LOG.debug(" - type: " + type);
            LOG.debug(" - name: " + name);
            LOG.debug(" - properties: " + properties);
        }
        try {
            List<LinkedHashMap<String, Object>> propertiesList = new ArrayList<>();
            // Complete properties
            for (Iterator it = this.properties.iterator(); it.hasNext();) {
                LinkedHashMap<String, Object> props = new LinkedHashMap();
                LinkedHashMap currentProperties = (LinkedHashMap) it.next();
                // Add name
                props.put("name", name);
                props.put("type", type);
                props.put("destination", destination);
                // Add common properties
                currentProperties.putAll(commonProperties);
                props.put("properties", AthentoOperationsHelper.transformPropertiesAsString(currentProperties));
                propertiesList.add(props);
            }

            // Add properties to param
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("properties", propertiesList);
            params.put("chainId", "Athento.DocumentCreate");
            params.put("class", "org.nuxeo.ecm.core.api.DocumentModel");

            // Execute batch operation
            BatchResult<DocumentModel> result = (BatchResult) AthentoOperationsHelper
                    .runOperation(RunOperationAsBatch.ID, null, params, session);

            return new DocumentModelListImpl(result.getItems());
        } catch (Exception e) {
            LOG.error(
                    "Unable to complete operation: "
                            + AthentoDocumentMultiCreateOperation.ID + " due to: "
                            + e.getMessage(), e);
            if (e instanceof AthentoException) {
                throw e;
            }
            AthentoException exc = new AthentoException(e.getMessage(), e);
            throw exc;
        }
    }

    /**
     * Get destination path.
     *
     * @return destination
     */
    private String getDestinationPath() {
        String val = destination;
        if (StringUtils.isNullOrEmpty(destination)) {
            val = AthentoOperationsHelper.readConfigValue(session,
                    AthentoDocumentCreateOperation.CONFIG_DEFAULT_DESTINATION);
        }
        return val;
    }

}