package org.athento.nuxeo.operations;

import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.runtime.api.Framework;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by victorsanchez on 19/7/16.
 */
@Operation(id = CompareVocabulariesOperation.ID, category = "Athento", label = "Compare vocabularies", description = "Compare vocabularies and result diff between them")
public class CompareVocabulariesOperation {

    private static final Log LOG = LogFactory.getLog(CompareVocabulariesOperation.class);

    public static final String ID = "Athento.CompareVocabularies";

    @Param(name = "vocabularies", description = "List of comma-separated values with vocabulary names to compare")
    protected String vocabularies;

    private DirectoryService directoryService;

    /**
     * Run method.
     *
     * @return
     */
    @OperationMethod
    public List<JSONObject> run() throws Exception {
        // Get vocabularies to compare
        String[] vocabulayNames = vocabularies.split(",");
        List<JSONObject> diff = new ArrayList<>();
        for (String vocabularyName : vocabulayNames) {
            Session directorySession = getDirectoryService().open(vocabularyName.trim());
            try {
                DocumentModelList entries =
                        directorySession.query(new HashMap<String, Serializable>());
                for (DocumentModel entry : entries) {
                    boolean xvocabulary = entry.hasSchema("xvocabulary");
                    JSONObject val = new JSONObject();
                    val.put("name", vocabularyName);
                    val.put("id", entry.getPropertyValue((xvocabulary ? "x" : "") + "vocabulary:id"));
                    val.put("label", entry.getPropertyValue((xvocabulary ? "x" : "") + "vocabulary:label"));
                    if (xvocabulary) {
                        val.put("parent", entry.getPropertyValue("xvocabulary:parent"));
                    }
                    int pos = getEntryPosition(val, diff);
                    if (pos == -1) {
                        diff.add(val);
                    } else {
                        diff.remove(pos);
                    }
                }
            } finally {
                directorySession.close();
            }
        }
        return diff;
    }

    /**
     * Get entry position into list.
     *
     * @param obj
     * @param diff
     * @return position
     */
    private int getEntryPosition(JSONObject obj, List<JSONObject> diff) {
        int i = 0;
        for (JSONObject jsonObj : diff) {
            String jsonObjId = (String) jsonObj.get("id");
            String id = (String) obj.get("id");
            if (id != null && id.equals(jsonObjId)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * Get directory service.
     *
     * @return
     */
    private DirectoryService getDirectoryService() {
        if (directoryService == null) {
            directoryService = Framework.getService(DirectoryService.class);
        }
        return directoryService;
    }

}
