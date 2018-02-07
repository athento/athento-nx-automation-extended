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

    @Test
    public void testEncryptDecryptSE() throws Exception {
        String key = "kFPdsiT348Yu43jh34876w";
        String v = SecurityUtil.encrypt(key, "metadata.dni_persona=12345678&metadata.nombre_persona=usuario1&metadata.apellidos_persona=apellido1&metadata.telefono_persona=99999999&metadata.email_persona=usuario1@empresa.com&metadata.autor=userid&metadata.oficina=1111&current_datetime=2018-01-30 19:14");
        System.out.println(v);
        v = SecurityUtil.decrypt(key, v);
        System.out.println(v);
    }

}
