package org.athento.nuxeo.operations.utils;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;

import java.io.IOException;

/**
 * Created by victorsanchez on 19/7/16.
 */
@Operation(id = ClearInputOperation.ID, category = "Athento", label = "Clear input context", description = "Clear input context of automation chain")
public class ClearInputOperation {

    public static final String ID = "Athento.InputClear";


    /**
     * Run operation.
     *
     * @return
     */
    @OperationMethod
    public Object run() throws IOException {
        return null;
    }


}
