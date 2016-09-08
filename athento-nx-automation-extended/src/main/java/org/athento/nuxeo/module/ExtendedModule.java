package org.athento.nuxeo.module;

import org.nuxeo.ecm.webengine.app.WebEngineModule;
import org.athento.nuxeo.api.writer.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Extended Module.
 *
 * @author <a href="vs@athento.com">Victor Sanchez</a>
 */
public class ExtendedModule extends WebEngineModule {

    public Set<Object> getSingletons() {
        Set<Object> result = new HashSet<Object>();
        result.add(new JsonBatchResultWriter());
        return result;
    }

}
