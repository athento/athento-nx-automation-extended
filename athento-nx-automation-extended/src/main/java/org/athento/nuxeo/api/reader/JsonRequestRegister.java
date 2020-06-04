package org.athento.nuxeo.api.reader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.utils.RegisterHelper;
import org.nuxeo.ecm.automation.jaxrs.io.operations.ExecutionRequest;
import org.nuxeo.ecm.automation.jaxrs.io.operations.JsonRequestReader;
import org.nuxeo.ecm.core.api.CoreSession;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Json request interceptor.
 */
/*
FIX: REGISTER ANY REQUEST using JAX-RS annotations
@Provider
@Consumes({ "application/json", "application/json+nxrequest" })*/
public class JsonRequestRegister extends JsonRequestReader {

    /**
     * Log.
     */
    private static final Log LOG = LogFactory.getLog(JsonRequestRegister.class);

    @Context
    protected HttpServletRequest request;


    @Override
    public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
        return ((targetMediaTypeNXReq.isCompatible(arg3) || targetMediaType.isCompatible(arg3)) && ExecutionRequest.class.isAssignableFrom(arg0));
    }

    public ExecutionRequest readRequest(InputStream in, MultivaluedMap<String, String> headers, CoreSession session)
            throws IOException, WebApplicationException {
        String content = IOUtils.toString(in, "UTF-8");
        if (content.isEmpty()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        LOG.info("Reading and registering request...");
        RegisterHelper.registerRequest(content, session.getPrincipal().getName(), request.getRemoteAddr());
        return super.readRequest(content, headers, session);
    }

}
