package org.athento.nuxeo;

import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;
import org.athento.utils.SecurityUtil;
import org.junit.Test;

/**
 * Encrypt/Decrypt test.
 */
public class EncryptDecryptTest {

    private static final Log LOG = LogFactory.getLog(EncryptDecryptTest.class);

    @Test
    public void testEncryptDecrypt() throws Exception {
        String key = "v7vyvy7o76236443"; // 128 bit key
        String initVector = "v7vyvy7o76236443"; // 16 bytes IV
        String v = SecurityUtil.encrypt(key, initVector, "SELECT ecm:uuid, ecm:path, ecm:primaryType, ecm:mixinType, dc:title, dc:source FROM Folder WHERE ecm:currentLifeCycleState != deleted AND npl:contract = '1'");
        SecurityUtil.decrypt(key, initVector, v);

        v = SecurityUtil.encrypt_data(key, "SELECT ecm:uuid, ecm:path, ecm:primaryType, ecm:mixinType, dc:title, dc:source FROM Oportunidad WHERE ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'");
        SecurityUtil.decrypt_data(key, v);
    }

}
