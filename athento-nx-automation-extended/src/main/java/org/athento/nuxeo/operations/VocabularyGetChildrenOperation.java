package org.athento.nuxeo.operations;


import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.i18n.I18NUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.runtime.api.Framework;

import java.util.*;

/**
 * Get vocabulary children. It is able to get several parents to filter, and order with limit and offset.
 *
 * @author victorsanchez
 */
@Operation(id = VocabularyGetChildrenOperation.ID, category = "Athento", label = "Vocabulary Get Children", description = "Returns a list of children vocabularies of specified vocabulary by param 'name'")
public class VocabularyGetChildrenOperation {

    public static final String ID = "Athento.VocabularyGetChildrenOperation";

    @Context
    protected OperationContext ctx;

    @Param(name = "expanded", widget = Constants.W_CHECK, required = false)
    protected Boolean expanded = false;
    @Param(name = "languageId", widget = Constants.W_TEXT, required = false)
    protected String languageId;
    /**
     * This value is able to get multiple values, separated by comma.
     */
    @Param(name = "parentValue", widget = Constants.W_TEXT, description = "You can including multiple parent values separated by comma")
    protected String parentValue;
    @Param(name = "vocabularyName", widget = Constants.W_TEXT)
    protected String vocabularyName;
    /**
     * Format of orderBy "field ASC|DESC".
     */
    @Param(name = "orderBy", required = false,
            description = "You can use order-by incluiding several columns to order separated by comma. If you set more than one parent value you only can use one column to order.")
    protected String orderBy;
    @Param(name = "groupOrder", required = false, description = "Sort all result entries.")
    protected boolean groupOrder = false;
    @Param(name = "limit", required = false)
    protected int limit = -1;
    @Param(name = "offset", required = false)
    protected int offset = -1;
    /** Default order. */
    private boolean asc = true;

    /**
     * Run operation.
     *
     * @return
     * @throws Exception
     */
    @OperationMethod
    public Object run() throws Exception {
        if (_log.isDebugEnabled()) {
            _log.debug("Getting children of vocabulary: " + vocabularyName + " for value " + parentValue);
            _log.debug("    languageId: " + languageId + " expanded:" + expanded);
        }
        List<JSONObject> children = getValuesForParentValue();
        if (_log.isDebugEnabled()) {
            _log.debug("Children vocabularies of [" + vocabularyName + "]: " + children);
        }

        return children;
    }

    /**
     * Get parent for parent value.
     *
     * @return list of JSON objects
     * @since AT-975
     */
    private List<JSONObject> getValuesForParentValue() {
        List<DocumentModel> result = new ArrayList<DocumentModel>();
        List<JSONObject> jsonResult = new ArrayList<>();
        Session directorySession = getDirectoryService().open(vocabularyName);
        boolean multipleParent = false;
        try {
            if (parentValue.contains(",")) {
                multipleParent = true;
                String[] parentValues = parentValue.split(",");
                for (String parentValue : parentValues) {
                    result.addAll(filterForParentValue(directorySession, parentValue.trim()));
                }
            } else {
                result.addAll(filterForParentValue(directorySession, parentValue));
            }
        } finally {
            directorySession.close();
        }
        if (groupOrder && !orderBy.contains(",")) {
            if (orderBy.contains(" ")) {
                String [] orderInfoTmp = orderBy.split(" ");
                orderBy = orderInfoTmp[0];
                if ("DESC".equals(orderInfoTmp[1])) {
                    asc = false;
                }
            }
            Collections.sort(result, new Comparator<DocumentModel>() {
                @Override
                public int compare(DocumentModel d1, DocumentModel d2) {
                    Object value1 = d1.getPropertyValue(orderBy);
                    if (value1 instanceof Comparable) {
                        Object value2 = d2.getPropertyValue(orderBy);
                        return (asc ? 1 : -1) * ((Comparable) value1).compareTo(value2);
                    } else {
                        return 0;
                    }
                }
            });
        }
        // Set locale
        Locale locale = null;
        if (languageId != null) {
            locale = new Locale(languageId);
        }
        // Complete JSON result
        Iterator<DocumentModel> it = result.iterator();
        while (it.hasNext()) {
            DocumentModel entry = it.next();
            String label = (String) entry.getPropertyValue("xvocabulary:label");
            if (_log.isDebugEnabled()) {
                _log.debug("- entry Id: " + entry.getId());
                _log.debug("- entry Title: " + entry.getTitle());
                _log.debug("- entry Label: " + label);
            }

            if (locale != null) {
                if (label != null) {
                    label = I18NUtils.getMessageString(
                            "messages", label, new String[0], locale);
                }
            }
            JSONObject val = new JSONObject();
            if (!expanded) {
                val.put(entry.getId(), label);
            } else {
                val.put("id", entry.getId());
                val.put("label", label);
                if (multipleParent) {
                    val.put("parent", entry.getPropertyValue("xvocabulary:parent"));
                }
            }
            if (_log.isDebugEnabled()) {
                _log.debug(">> adding value: " + val);
            }
            jsonResult.add(val);
        }
        return jsonResult;
    }

    /**
     * Add values filtering for a parent value.
     *
     * @param directorySession is the session
     * @param parentValue      is the parent value to filter
     * @return list of JSON objects
     */
    private List<DocumentModel> filterForParentValue(Session directorySession, String parentValue) {
        List<JSONObject> result = new ArrayList<JSONObject>();
        // Prepare filter map
        Map queryFilter = new HashMap();
        queryFilter.put("parent", parentValue);
        if (_log.isInfoEnabled()) {
            _log.info("Querying directory [" + vocabularyName + "] with query: " + queryFilter);
        }
        // Get sort info
        Map<String, String> sortInfo = getSortInfo();
        // Execute query
        return directorySession.query(queryFilter, new HashSet<String>(0), sortInfo
                , false, limit, offset);
    }

    /**
     * Get sort info.
     *
     * @return map of order by's
     */
    private Map<String, String> getSortInfo() {
        Map<String, String> sortInfo = new HashMap<String, String>();
        if (this.orderBy != null) {
            if (this.orderBy.contains(",")) {
                String[] multipleOrderBy = this.orderBy.split(",");
                for (String orderBy : multipleOrderBy) {
                    getOrderByInfo(orderBy.trim(), sortInfo);
                }
            } else {
                getOrderByInfo(orderBy.trim(), sortInfo);
            }
        }
        return sortInfo;
    }

    /**
     * Get order-by info.
     *
     * @param orderBy
     * @param sortInfo
     */
    private void getOrderByInfo(String orderBy, Map<String, String> sortInfo) {
        String[] orderByInfo = orderBy.split(" ");
        if (orderByInfo.length > 1) {
            sortInfo.put(orderByInfo[0].trim(), orderByInfo[1].trim());
        } else {
            sortInfo.put(orderBy, "ASC");
        }
    }

    private DirectoryService getDirectoryService() {
        if (directoryService == null) {
            directoryService = Framework.getService(DirectoryService.class);
        }
        return directoryService;
    }

    private DirectoryService directoryService;


    private static final Log _log = LogFactory.getLog(VocabularyGetChildrenOperation.class);

}