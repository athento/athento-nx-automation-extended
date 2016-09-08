package org.athento.nuxeo.api.util;

import java.util.Map;

/**
 * Created by victorsanchez on 8/9/16.
 */
public interface BatchInterceptor<T> {

    /**
     * Proceed method.
     *
     * @param result
     * @param params
     */
    void proceed(T result, Map<String, Object> params);

}
