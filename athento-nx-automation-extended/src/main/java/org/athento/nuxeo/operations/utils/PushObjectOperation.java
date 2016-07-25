/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     bstefanescu
 */
package org.athento.nuxeo.operations.utils;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;

import java.io.Serializable;

/**
 *
 */
@Operation(id = PushObjectOperation.ID, category = Constants.CAT_EXECUTION_STACK, label = "Push any serializable object", description = "Push an object.")
public class PushObjectOperation {

    public static final String ID = "Athento.Push";

    @Context
    protected OperationContext ctx;

    @Param(name = "object", required = false)
    protected Serializable object;

    @OperationMethod
    public Serializable run(Serializable object) {
        ctx.push("object", object);
        return object;
    }

    @OperationMethod
    public Serializable run() {
        ctx.push("object", object);
        return object;
    }

}
