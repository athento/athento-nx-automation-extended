package org.athento.nuxeo.service;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.utils.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyNotFoundException;
import org.nuxeo.ecm.directory.BaseSession;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.runtime.api.Framework;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Default implementation for {@link AutomationRegistryService}.
 */
public class AutomationRegistryServiceImpl implements AutomationRegistryService {

    private static final long serialVersionUID = -584592734659278346L;

    /** Log. */
    private static final Log LOG = LogFactory.getLog(AutomationRegistryServiceImpl.class);

    /** Directory name. */
    protected static final String DIRECTORY_NAME = "queryRequests";


    /**
     * Register a new query request.
     *
     * @param query
     * @param user
     * @param pageSize
     * @param firstPage
     * @param startDateMillis
     * @param endDateMillis
     */
    @Override
    public void registerQueryRequest(String query, String user, int pageSize, int firstPage, long startDateMillis, long endDateMillis) {
        if (query == null) {
            LOG.warn("Unable to register query with no query");
            return;
        }
        // Log in as system user
        LoginContext lc;
        try {
            lc = Framework.login();
            } catch (LoginException e) {
                throw new ClientException("Cannot log in as system user", e);
            }
            try {
                // Create entry into session for directory {@link AutomationRegistryServiceImpl.DIRECTORY_NAME}
                final Session session = Framework.getService(DirectoryService.class).open(DIRECTORY_NAME);
                try {
                    final DocumentModel entry = getEntryFromQueryRequest(Framework.getService(DirectoryService.class));
                    entry.setPropertyValue("aqr:user", user);
                    entry.setPropertyValue("aqr:name", UUID.randomUUID().toString());
                    entry.setPropertyValue("aqr:query", query);
                    entry.setPropertyValue("aqr:pageSize", pageSize);
                    entry.setPropertyValue("aqr:firstPage", firstPage);
                    entry.setPropertyValue("aqr:start_date", startDateMillis);
                    entry.setPropertyValue("aqr:end_date", endDateMillis);
                    entry.setPropertyValue("aqr:hash", new String(Base64.encodeBase64(StringUtils.md5(query.getBytes("UTF-8")))));
                    session.createEntry(entry);
                } catch (UnsupportedEncodingException | NoSuchAlgorithmException | PropertyNotFoundException e) {
                    LOG.error("Unable to save request", e);
                } finally {
                    session.close();
                }
        } finally {
            try {
                // Login context may be null in tests
                if (lc != null) {
                    lc.logout();
                }
            } catch (LoginException e) {
                throw new ClientException("Cannot log out system user", e);
            }
        }
    }

    /**
     * Get information about query.
     *
     * @param query
     */
    @Override
    public void getInformationAboutQuery(String query) {
        // FIXME: Complete
    }

    /**
     * Get entry from query request.
     *
     * @param directoryService
     * @return
     * @throws ClientException
     */
    protected DocumentModel getEntryFromQueryRequest(DirectoryService directoryService) throws ClientException {
        String directorySchema = directoryService.getDirectorySchema(DIRECTORY_NAME);
        return BaseSession.createEntryModel(null, directorySchema, null, null);
    }
}
