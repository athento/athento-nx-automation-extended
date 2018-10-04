package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.StringUtils;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelListCollector;
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

    @Param(name = "position", required = false)
    protected Integer position;

    @Param(name = "save", required = false, values = "true")
    protected boolean save = true;

    public DocumentModel run(DocumentModelList docs) throws Exception {
        if (!docs.isEmpty()) {
            return run(docs.get(0));
        }
        return null;
    }

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
        Serializable newValue = addComplexIntoList(array, this.properties, this.position);
        if (LOG.isInfoEnabled()) {
            LOG.info("Complex List to add " + newValue);
        }
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
     * @param position
     * @return
     */
    private Serializable addComplexIntoList(List<Serializable> array, Properties properties, Integer position) {
        List list = new ArrayList<>();
        if (array != null) {
            list.addAll(array);
        }
        Map<String, Object> newComplexItem = new HashMap<>();
        if (position == null || position >= list.size()) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String metadata = entry.getKey();
                String value = entry.getValue();
                if (value.startsWith("[") && value.endsWith("]")) {
                    List<String> li = Arrays.asList(StringUtils.split(value.replace("[","").replace("]", ""), ',', true));
                    newComplexItem.put(metadata, li);
                } else {
                    newComplexItem.put(metadata, value);
                }
            }
            list.add(newComplexItem);
        } else {
            try {
                Map<String, Object> item = (Map) array.get(position);
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    String metadata = entry.getKey();
                    String value = entry.getValue();
                    if (value.startsWith("[") && value.endsWith("]")) {
                        List<String> li = Arrays.asList(StringUtils.split(value.replace("[","").replace("]", ""), ',', true));
                        item.put(metadata, li);
                    } else {
                        item.put(metadata, value);
                    }
                }
                list = array;
            } catch (IndexOutOfBoundsException e) {
                LOG.error("Position " + position + " is not found in complex metadata");
            }
        }
        return (Serializable) list;

    }

}
