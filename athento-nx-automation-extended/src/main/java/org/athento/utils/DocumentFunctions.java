package org.athento.utils;

import org.apache.commons.httpclient.util.DateUtil;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.query.sql.NXQL;

import java.util.GregorianCalendar;

/**
 * Created by victorsanchez on 18/7/16.
 */
public final class DocumentFunctions {

    /** Date format to export. */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Get current lifecycle.
     *
     * @param doc to get lifecycle
     * @return lifecycle
     */
    public static String currentLifecycle(DocumentModel doc) {
        return doc.getCurrentLifeCycleState();
    }

    /**
     * Get value of document given the column.
     *
     * @param doc
     * @param column
     * @return
     */
    public static Object getValue(DocumentModel doc, String column) {
        if (NXQL.ECM_UUID.equals(column)) {
            return doc.getId();
        } else if (NXQL.ECM_NAME.equals(column)) {
            return doc.getName();
        } else if (NXQL.ECM_PARENTID.equals(column)) {
            return doc.getParentRef().reference();
        } else if (NXQL.ECM_LIFECYCLESTATE.equals(column) || "lifecycle".equals(column)) {
            return currentLifecycle(doc);
        } else if (NXQL.ECM_VERSIONLABEL.equals(column)) {
            return doc.getVersionLabel();
        } else if (NXQL.ECM_ISVERSION.equals(column)) {
            return doc.isVersion();
        } else if (NXQL.ECM_ISPROXY.equals(column)) {
            return doc.isProxy();
        } else if (NXQL.ECM_LOCK_OWNER.equals(column)) {
            if (doc.isLocked()) {
                return doc.getLockInfo().getOwner();
            } else {
                return "Unlocked";
            }
        } else if (NXQL.ECM_LOCK_CREATED.equals(column)) {
            if (doc.isLocked()) {
                return DateUtil.formatDate(doc.getLockInfo().getCreated().getTime(), DATE_FORMAT);
            } else {
                return "Unlocked";
            }
        } else if (column.contains(":")) {
            Object value = doc.getPropertyValue(column);
            if (value instanceof GregorianCalendar) {
                return DateUtil.formatDate(((GregorianCalendar) value).getTime(), DATE_FORMAT);
            } else {
                return value;
            }
        } else {
            return "";
        }
    }
}
