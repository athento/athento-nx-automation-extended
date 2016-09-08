package org.athento.nuxeo.api.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by victorsanchez on 8/9/16.
 */
public interface ListResult<T> {

    /**
     * Get items.
     *
     * @return
     */
    List<T> getItems();


}
