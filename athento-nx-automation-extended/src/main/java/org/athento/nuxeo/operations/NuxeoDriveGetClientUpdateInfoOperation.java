package org.athento.nuxeo.operations;


import org.codehaus.jackson.map.ObjectMapper;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.runtime.api.Framework;

import java.io.ByteArrayInputStream;
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

    @Context
    protected CoreSession session;

    @OperationMethod
    public Blob run() throws ClientException, IOException {
        NuxeoDriveClientUpdateInfo info = new NuxeoDriveClientUpdateInfo();
        if (((NuxeoPrincipal) session.getPrincipal()).isAdministrator()) {
            String serverVersion = Framework.getProperty("org.nuxeo.ecm.product.version");
            String updateSiteURL = Framework.getProperty("org.nuxeo.drive.update.site.url");
            String betaUpdateSiteURL = Framework.getProperty("org.nuxeo.drive.beta.update.site.url");
            info.setBetaUpdateSiteURL(betaUpdateSiteURL);
            info.setServerVersion(serverVersion);
            info.setUpdateSiteURL(updateSiteURL);
        }
        return asJSONBlob(info);
    }

    public static Blob asJSONBlob(Object value) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(value);;
        return new FileBlob(new ByteArrayInputStream(json.getBytes("UTF-8")), "application/json");
    }

    private class NuxeoDriveClientUpdateInfo {

        protected String serverVersion;

        protected String updateSiteURL;

        protected String betaUpdateSiteURL;

        protected NuxeoDriveClientUpdateInfo() {
            // Needed for JSON deserialization
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