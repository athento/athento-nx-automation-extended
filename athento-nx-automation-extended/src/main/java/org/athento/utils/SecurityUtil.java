package org.athento.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.impl.ACLImpl;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Security util class.
 */
public final class SecurityUtil {

    /** Log. */
    private static final Log LOG = LogFactory.getLog(SecurityUtil.class);

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

    /**
     * Encrypt a value.
     *
     * @param secret
     * @param init
     * @param value
     * @return
     */
    public static String encrypt(String secret, String init, String value) {
        if (secret == null || init == null) {
            return value;
        }
        try {
            IvParameterSpec iv = new IvParameterSpec(init.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(secret.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            LOG.trace("Encrypt error query: " + e.getMessage());
        }

        return null;
    }

    /**
     * Decrypt a value.
     *
     * @param secret
     * @param init
     * @param value
     * @return
     */
    public static String decrypt(String secret, String init, String value) {
        if (secret == null || init == null) {
            return value;
        }
        try {
            String initVector = init;
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(secret.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decodeBase64(value));
            value = new String(original);
        } catch (Exception e) {
            LOG.trace("Decrypt error query: " + e.getMessage());
        }
        return value;
    }

    public static String decrypt_data(String secret, String encData)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec skeySpec = new SecretKeySpec(secret.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] original = cipher
                .doFinal(Base64.decodeBase64(encData.getBytes()));
        String decrypt = new String(original).trim();
        return decrypt;
    }

    public static String encrypt_data(String secret, String data)
            throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(secret.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] original = Base64.encodeBase64(cipher.doFinal(data.getBytes()));
        String encrypt = new String(original);
        return encrypt;
    }

    /**
     * Check if login as is enabled.
     *
     * @return
     */
    public static boolean isLoginAsEnabled() {
        return Boolean.valueOf(Framework.getProperty("loginas.enabled", "false"));
    }
}
