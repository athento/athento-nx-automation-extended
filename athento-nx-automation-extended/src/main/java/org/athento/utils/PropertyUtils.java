package org.athento.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.PropertyNotFoundException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by victorsanchez on 26/1/17.
 */
public final class PropertyUtils {

    /** Log. */
    private static final Log LOG = LogFactory.getLog(PropertyUtils.class);

    /**
     * Get properties as string list.
     *
     * @param properties
     * @param doc
     * @return
     */
    public static final StringList getPropertiesAsStringList(Properties properties, DocumentModel doc) {
        StringList lastUpdatedProperties = new StringList();
        Iterator<String> keyIterator = properties.keySet().iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            try {
                Property p = doc.getProperty(key);
                if (p.isScalar()) {
                    lastUpdatedProperties.add(key);
                } else {
                    LOG.warn("Property not scalar: " + key);
                }
                //
            } catch (PropertyNotFoundException ex) {
                LOG.warn("Property is not found in " + doc.getType()
                        + ": " + key);
            }
        }
        return lastUpdatedProperties;
    }
}
