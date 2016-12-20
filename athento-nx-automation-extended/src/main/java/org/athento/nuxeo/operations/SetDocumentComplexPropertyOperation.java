package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.nuxeo.ecm.core.api.model.Property;

import java.io.Serializable;
import java.util.*;

/**
 * @author <a href="vs@athento.com">Victor Sanchez</a>
 */
@Operation(id = SetDocumentComplexPropertyOperation.ID, category = Constants.CAT_DOCUMENT, label = "Update Complex property", description = "Set a complex property value on the input document. ")
public class SetDocumentComplexPropertyOperation {

    public static final String ID = "Athento.SetComplexProperty";

    private static final Log LOG = LogFactory.getLog(SetDocumentComplexPropertyOperation.class);

    @Context
    protected CoreSession session;

    @Param(name = "xpath")
    protected String xpath;

    @Param(name = "properties", required = false)
    protected Properties properties;

    @Param(name = "save", required = false, values = "true")
    protected boolean save = true;

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) throws Exception {
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

        Serializable newValue = addComplexIntoList(array, this.properties);
        p.setValue(newValue);

        if (save) {
            doc = session.saveDocument(doc);
        }

        return doc;
    }

    /**
     * Add complex into list.
     *
     * @param array
     * @param properties
     * @return
     */
    private Serializable addComplexIntoList(List<Serializable> array, Properties properties) {

        List<Object> list = new ArrayList<Object>();

        if (array != null) {
            list.addAll(array);
        }

        Map<String, String> newComplexItem = new HashMap<String, String>();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String metadata = entry.getKey();
            String value = entry.getValue();
            LOG.info(metadata + "=" + value);
            newComplexItem.put(metadata, value);
        }

        // TODO: Check if item exists with a parameter
        list.add(newComplexItem);

        LOG.info("List =" + list);

        return (Serializable) list;

    }

}
