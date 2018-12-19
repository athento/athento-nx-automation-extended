package org.athento.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.impl.ACLImpl;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.runtime.api.Framework;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

/**
 * Security util class.
 */
public final class SecurityUtil {

    /** Log. */
    private static final Log LOG = LogFactory.getLog(SecurityUtil.class);

    private static final String DEFAULT_IV = "Z8guyTT7clad3vVV";

    private static Cipher cipher;
    private static IvParameterSpec parameterSpec;
    private static SecretKeySpec skeySpec;

    static {
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOG.error("Unable to init ciper instance");
        }
    }

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

    public static String encrypt(String key, String src) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, makeKey(key), makeIv());
            return new String(Base64.encodeBase64(cipher.doFinal(src.getBytes())));
        } catch (Exception e) {
            LOG.error("Encrypt problem occurred: " + e.getMessage());
            return src;
        }
    }

    public static String decrypt(String key, String src) {
        String decrypted = "";
        try {
            cipher.init(Cipher.DECRYPT_MODE, makeKey(key), makeIv());
            decrypted = new String(cipher.doFinal(Base64.decodeBase64(src)));
        } catch (Exception e) {
            LOG.error("Decrypt problem occurred: " + e.getMessage());
            decrypted = src;
        }
        return decrypted;
    }

    static AlgorithmParameterSpec makeIv() {
        try {
            String iv;
            if (Framework.isInitialized()) {
                iv = Framework.getProperty("athento.cipher.iv", null);
            } else {
                iv = DEFAULT_IV;
            }
            if (parameterSpec == null) {
                parameterSpec = new IvParameterSpec(iv.getBytes("UTF-8"));
            }
            return parameterSpec;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    static Key makeKey(String secret) throws UnsupportedEncodingException {
        return new SecretKeySpec(secret.getBytes("UTF-8"), "AES");
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
