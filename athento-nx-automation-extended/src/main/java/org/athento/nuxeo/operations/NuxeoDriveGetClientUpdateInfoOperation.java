package org.athento.nuxeo.operations;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;
import org.nuxeo.runtime.api.Framework;

import java.io.IOException;

/**
 * Get drive information.
 *
 * Based on NX 6.0
 * Fixed security problem for product information for no Administrators.
 */
@Operation(id = NuxeoDriveGetClientUpdateInfoOperation.ID, category = Constants.CAT_SERVICES, label = "Nuxeo Drive: Get client update information")
public class NuxeoDriveGetClientUpdateInfoOperation {

    public static final String ID = "NuxeoDrive.GetClientUpdateInfo";

    @OperationMethod
    public Blob run() throws ClientException, IOException {

        String serverVersion = Framework.getProperty("org.nuxeo.ecm.product.version");
        String updateSiteURL = Framework.getProperty("org.nuxeo.drive.update.site.url");
        String betaUpdateSiteURL = Framework.getProperty("org.nuxeo.drive.beta.update.site.url");
        NuxeoDriveClientUpdateInfo info = new NuxeoDriveClientUpdateInfo(serverVersion, updateSiteURL,
                betaUpdateSiteURL);
        return asJSONBlob(info);
    }

    public static Blob asJSONBlob(Object value) throws JsonGenerationException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(value);
        return StreamingBlob.createFromByteArray(json.getBytes("UTF-8"), "application/json");
    }

    private class NuxeoDriveClientUpdateInfo {

        protected String serverVersion;

        protected String updateSiteURL;

        protected String betaUpdateSiteURL;

        protected NuxeoDriveClientUpdateInfo() {
            // Needed for JSON deserialization
        }

        public NuxeoDriveClientUpdateInfo(String serverVersion, String updateSiteURL, String betaUpdateSiteURL) {
            this.serverVersion = serverVersion;
            this.updateSiteURL = updateSiteURL;
            this.betaUpdateSiteURL = betaUpdateSiteURL;
        }

        public String getServerVersion() {
            return serverVersion;
        }

        public void setServerVersion(String serverVersion) {
            this.serverVersion = serverVersion;
        }

        public String getUpdateSiteURL() {
            return updateSiteURL;
        }

        public void setUpdateSiteURL(String updateSiteURL) {
            this.updateSiteURL = updateSiteURL;
        }

        public String getBetaUpdateSiteURL() {
            return betaUpdateSiteURL;
        }

        public void setBetaUpdateSiteURL(String betaUpdateSiteURL) {
            this.betaUpdateSiteURL = betaUpdateSiteURL;
        }

    }

}