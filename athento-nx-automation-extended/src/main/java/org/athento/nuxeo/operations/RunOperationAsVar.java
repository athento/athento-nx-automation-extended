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
package org.athento.nuxeo.operations;

import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;

import java.util.Map;

/**
 * Run an operation and set result into var.
 */
@Operation(id = RunOperationAsVar.ID, category = Constants.CAT_SUBCHAIN_EXECUTION, label = "Run operation as var", description = "Run an operation and save the result into variable.")
public class RunOperationAsVar {

    public static final String ID = "Athento.RunOperationAsVar";

    @Context
    protected OperationContext ctx;

    @Context
    protected AutomationService service;

    @Param(name = "id")
    protected String chainId;

    @Param(name = "name", description = "Destiny context variable name")
    protected String name;

    @Param(name = "isolate", required = false, values = "false")
    protected boolean isolate = false;

    @Param(name = "parameters", description = "Accessible in the subcontext ChainParameters. For instance, @{ChainParameters['parameterKey']}.", required = false)
    protected Properties chainParameters;

    @OperationMethod
    public void run() throws Exception {
        OperationContext subctx = ctx.getSubContext(isolate, ctx.getInput());
        Object result = service.run(subctx, chainId, (Map) chainParameters);
        ctx.put(name, result);
    }

}
