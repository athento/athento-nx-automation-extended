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
        String key = "ZxT3mzDekMgrmExE";
        String v = SecurityUtil.encrypt(key, "SELECT ecm:uuid, ecm:path, ecm:primaryType, ecm:mixinType, dc:title, dc:description, oportunidad:identificador_crm, oportunidad:documento, oportunidad:cliente FROM Document WHERE ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'");
        System.out.println(v);
        v = SecurityUtil.decrypt(key, v);
        System.out.println(v);
    }

}
