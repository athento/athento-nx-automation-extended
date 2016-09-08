/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package org.athento.nuxeo.api.codec;

import org.athento.nuxeo.api.model.BatchResult;
import org.athento.nuxeo.api.writer.JsonBatchResultWriter;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.nuxeo.ecm.automation.io.services.codec.ObjectCodec;
import org.nuxeo.ecm.automation.jaxrs.io.documents.JSONDocumentModelReader;
import org.nuxeo.ecm.automation.jaxrs.io.documents.JsonDocumentWriter;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import java.io.IOException;

/**
 * Code for BatchResult.
 */
public class BatchResultCodec extends ObjectCodec<BatchResult> {

    @Override
    public String getType() {
        return "batch-result";
    }

    @Override
    public boolean isBuiltin() {
        return true;
    }

    @Override
    public void write(JsonGenerator jg, BatchResult value) throws
            IOException {
        try {
            JsonBatchResultWriter.writeBatchResult(jg, value, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BatchResult read(JsonParser jp, CoreSession session) throws
            IOException {
        // FIXME:
        return null;
    }
}
