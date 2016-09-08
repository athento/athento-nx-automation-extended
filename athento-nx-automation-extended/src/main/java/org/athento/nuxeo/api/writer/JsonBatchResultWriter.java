package org.athento.nuxeo.api.writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.api.model.BatchResult;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.nuxeo.common.utils.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.automation.jaxrs.io.documents.JsonDocumentWriter;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.Lock;
import org.nuxeo.ecm.core.api.model.PropertyNotFoundException;
import org.nuxeo.ecm.core.schema.utils.DateParser;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Writer used as list result.
 */
@Provider
@Produces({"application/json+nxentity", "application/json"})
public class JsonBatchResultWriter implements
        MessageBodyWriter<BatchResult> {

    private static final Log LOG = LogFactory.getLog(JsonBatchResultWriter.class);

    public static final String DOCUMENT_PROPERTIES_HEADER = "X-NXDocumentProperties";

    @Context
    protected HttpHeaders headers;

    @Context
    private HttpServletRequest request;

    @Override
    public void writeTo(BatchResult data, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        try {
            JsonGenerator jg = getGenerator(entityStream);
            writeBatchResult(jg, data, headers);
            jg.close();
            entityStream.flush();
        } catch (Exception e) {
            throw new IOException("Failed to write batch result as JSON", e);
        }
    }

    protected JsonGenerator getGenerator(OutputStream entityStream)
            throws IOException {
        JsonFactory factory = new JsonFactory();
        JsonGenerator jg = factory.createJsonGenerator(entityStream,
                JsonEncoding.UTF8);
        jg.useDefaultPrettyPrinter();
        return jg;
    }

    /**
     * Write batch result.
     *
     * @param jg
     * @param data
     * @param headers
     * @throws Exception
     */
    public static void writeBatchResult(JsonGenerator jg, BatchResult data, HttpHeaders headers)
            throws Exception {
        jg.writeStartObject();
        jg.writeStringField("entity-type", "batch-result");
        jg.writeArrayFieldStart("result");
        Class<?> type = data.getType();
        if (type != null) {
            if (type.equals(DocumentModelList.class)) {
                jg.writeStartObject();
                jg.writeStringField("entity-type", "documents");
                String[] schemas = null;
                if (headers != null) {
                    List<String> props = headers.getRequestHeader(JsonBatchResultWriter.DOCUMENT_PROPERTIES_HEADER);
                    if (props != null && !props.isEmpty()) {
                        schemas = StringUtils.split(props.get(0), ',', true);
                    }
                }
                jg.writeArrayFieldStart("entries");
                for (Object docList : data.getItems()) {
                    DocumentModelList list = (DocumentModelList) docList;
                    for (DocumentModel doc : list) {
                        JsonDocumentWriter.writeDocument(jg, doc, schemas, null);
                    }
                }
                jg.writeEndArray();
                jg.writeEndObject();
            } else if (type.equals(DocumentModel.class)) {
                jg.writeStartObject();
                jg.writeStringField("entity-type", "documents");
                String[] schemas = null;
                if (headers != null) {
                    List<String> props = headers.getRequestHeader(JsonBatchResultWriter.DOCUMENT_PROPERTIES_HEADER);
                    if (props != null && !props.isEmpty()) {
                        schemas = StringUtils.split(props.get(0), ',', true);
                    }
                }
                jg.writeArrayFieldStart("entries");
                for (Object docItem : data.getItems()) {
                    DocumentModel doc = (DocumentModel) docItem;
                    JsonDocumentWriter.writeDocument(jg, doc, schemas, null);
                }
                jg.writeEndArray();
                jg.writeEndObject();
            } else if (type.equals(Blob.class)) {
                List<DocumentModel> documents = (List<DocumentModel>) data.getProperties().get("documents");
                jg.writeStartObject();
                jg.writeStringField("entity-type", "blobs");
                jg.writeArrayFieldStart("entries");
                int idx = 0;
                for (Object blobItem : data.getItems()) {
                    Blob blob = (Blob) blobItem;
                    jg.writeStartObject();
                    jg.writeStringField("entity-type", "blob");
                    jg.writeStringField("filename", blob.getFilename());
                    jg.writeStringField("encoding", blob.getEncoding());
                    jg.writeStringField("digest", blob.getDigest());
                    jg.writeStringField("digestAlgorithm", blob.getDigestAlgorithm());
                    DocumentModel document = documents.get(idx++);
                    jg.writeStringField("uid", document.getId());
                    jg.writeStringField("path", document.getPathAsString());
                    //JsonDocumentWriter.writeDocument(jg, document, null, null);
                    jg.writeEndObject();
                    jg.flush();
                }
                jg.writeEndArray();
                jg.writeEndObject();
            }
        }
        jg.writeEndArray();
        jg.writeEndObject();
        jg.flush();

    }

    @Override
    public long getSize(BatchResult arg0, Class<?> arg1, Type arg2,
                        Annotation[] arg3, MediaType arg4) {
        return -1L;
    }

    @Override
    public boolean isWriteable(Class<?> arg0, Type type, Annotation[] arg2,
                               MediaType arg3) {
        return BatchResult.class.isAssignableFrom(arg0);
    }

}
