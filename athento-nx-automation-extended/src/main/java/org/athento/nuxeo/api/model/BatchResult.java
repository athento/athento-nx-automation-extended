package org.athento.nuxeo.api.model;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.io.services.codec.ObjectCodecService;
import org.nuxeo.ecm.automation.jaxrs.JsonAdapter;
import org.nuxeo.ecm.automation.server.jaxrs.batch.Batch;
import org.nuxeo.runtime.api.Framework;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by victorsanchez on 8/9/16.
 */
public class BatchResult<T> implements ListResult<T>, JsonAdapter {

    private static final Log LOG = LogFactory.getLog(BatchResult.class);

    private List<T> items;

    private transient Class<?> type;

    private Properties properties;

    public BatchResult() {
        super();
    }

    public BatchResult(Class<?> type) {
        this.type = type;
    }

    @Override
    public List<T> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    @Override
    public void toJSON(OutputStream out) throws IOException {
        ObjectCodecService service = Framework.getLocalService(ObjectCodecService.class);
        service.write(out, this, true);
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }
}

