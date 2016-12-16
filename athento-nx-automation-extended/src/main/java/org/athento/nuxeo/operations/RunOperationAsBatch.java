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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.api.model.BatchResult;
import org.athento.nuxeo.api.util.BatchInterceptor;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;

import java.util.*;

/**
 * Run an operation with a batch properties for a DocumentModel or Blob.
 *
 * @author <a href="vs@athento.com">Victor Sanchez</a>
 */
@Operation(id = RunOperationAsBatch.ID, category = "Athento", label = "Run operation as batch", description = "Run an operation as batch.")
public class RunOperationAsBatch {

    /** Log. */
    private static final Log LOG = LogFactory.getLog(RunOperationAsBatch.class);

    /** Operation ID. */
    public static final String ID = "Athento.RunOperationAsBatch";

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    /**
     * Chain id.
     */
    @Param(name = "chainId", required = true, description = "Chain ID or operation ID to execute")
    protected String chainId;

    @Param(name = "class", required = false)
    protected String className;

    /**
     * Properties.
     */
    @Param(name = "properties", required = false, description = "List of properties to distribute them in each OperationChain execution.")
    protected List<Properties> properties;

    /**
     * Run with no-args.

     * @return result
     * @throws Exception on error
     */
    @OperationMethod
    public BatchResult run() throws Exception {
        Class c = RunOperationAsBatch.class.getClassLoader().loadClass(className);
        return localRun(null, c);
    }

    /**
     * Run with document as argument.
     *
     * @param doc is the document
     * @return result
     * @throws Exception on error
     */
    @OperationMethod
    public BatchResult run(DocumentModel doc) throws Exception {
        return localRun(doc, DocumentModel.class);
    }

    /**
     * Run with blob as argument.
     *
     * @param blob is the blob
     * @return result
     * @throws Exception on error
     */
    @OperationMethod
    public BatchResult run(Blob blob) throws Exception {
        final BatchResult batchResult = new BatchResult(Blob.class);
        localRun(blob, Blob.class, new BatchInterceptor<Blob>() {
            @Override
            public void proceed(Blob result, Map<String, Object> params) {
                List<DocumentModel> propDocs = (List<DocumentModel>) batchResult.getProperties().get("documents");
                if (propDocs == null) {
                    propDocs = new ArrayList<>();
                }
                propDocs.add(session.getDocument(new IdRef((String) params.get("document"))));
                batchResult.getProperties().put("documents", propDocs);
                batchResult.getItems().add(result);
            }
        });
        return batchResult;
    }

    /**
     * Local run of any input: empty, Document or Blob.
     *
     * @param input is the input
     * @param clazz is the result class
     * @throws OperationException on operation error
     */
    private <T> BatchResult localRun(Object input, Class<T> clazz) throws OperationException {
        return localRun(input, clazz, null);
    }

    /**
     * Local run of any input: empty, Document or Blob.
     *
     * @param input is the input
     * @param clazz is the result class
     * @param interceptor is the interceptor to manage result and params of each operation call
     * @throws OperationException on operation error
     */
    private <T> BatchResult localRun(Object input, Class<T> clazz, BatchInterceptor<T> interceptor) throws OperationException {
        BatchResult result = new BatchResult(clazz);
        for (Iterator it = this.properties.iterator(); it.hasNext();) {
            LinkedHashMap properties = (LinkedHashMap) it.next();
            Map<String, Object> params = AthentoOperationsHelper.transformPropertiesToParams(properties);
            Object opResult = AthentoOperationsHelper.runOperation(chainId, input, params, session);
            if (interceptor != null) {
                interceptor.proceed((T) opResult, params);
            }
            if (!clazz.isInstance(opResult)) {
                LOG.warn("Operation result was not a " + clazz + " instance. It was " + opResult.getClass());
                throw new OperationException("Unable to execute batch operation: " + opResult);
            }
            result.getItems().add(opResult);
        }
        return result;
    }
}
