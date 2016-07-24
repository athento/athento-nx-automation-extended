package org.athento.nuxeo.operations.utils;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by victorsanchez on 19/7/16.
 */
@Operation(id = GetJSONAttributeOperation.ID, category = "Athento", label = "Find values from JSON", description = "Find values of attribute values from JSON")
public class GetJSONAttributeOperation {

    public static final String ID = "Athento.FindValuesFromJSON";

    /**
     * Attribute of
     */
    @Param(name = "attribute", required = true)
    protected String attribute;

    /**
     * Run operation.
     *
     * @param blob
     * @return
     */
    @OperationMethod
    public String run(Blob blob) throws IOException {
        return run(blob.getString());
    }

    /**
     * Run operation.
     *
     * @param json
     * @return
     */
    @OperationMethod
    public String run(String json) throws IOException {
        String result = "";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(json);
        List<JsonNode> nodes = actualObj.findValues(attribute);
        for (Iterator<JsonNode> it = nodes.iterator(); it.hasNext(); ) {
            JsonNode node = it.next();
            if (node.isTextual()) {
                result += "'" + node.getTextValue() + "'";
            } else {
                result += node.getValueAsText();
            }
            if (it.hasNext()) {
                result += ", ";
            }
        }
        return result;
    }


}
