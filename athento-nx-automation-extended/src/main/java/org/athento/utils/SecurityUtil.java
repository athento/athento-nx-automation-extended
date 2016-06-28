package org.athento.utils;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.impl.ACLImpl;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;

/**
 * Security util class.
 */
public final class SecurityUtil {

    /**
     * Add permission to user.
     *
     * @param documentManager
     * @param ref
     * @param user
     * @param permission
     * @throws Exception
     */
    public static void addPermission(CoreSession documentManager, String aclName, DocumentRef ref, String user, String permission)
            throws Exception {
        ACPImpl acp = new ACPImpl();
        ACLImpl aclImpl = new ACLImpl(aclName);
        acp.addACL(aclImpl);
        ACE ace = new ACE(user, permission, true);
        aclImpl.add(ace);
        documentManager.setACP(ref, acp, false);
    }

}
