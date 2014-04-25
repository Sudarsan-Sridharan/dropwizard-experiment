package bo.gotthardt;

import com.google.common.io.BaseEncoding;
import org.joda.time.DateTime;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * @author Bo Gotthardt
 */
public class TOTPSecret {
    private byte[] key;

    public TOTPSecret() {
        key = new byte[10];
        new SecureRandom().nextBytes(key);
    }

    public String getEncoded() {
        return BaseEncoding.base32().encode(key).toUpperCase();
    }

    public boolean equalsToken(int token, DateTime now) {
        int previousToken = tokenAt(now.minusSeconds(30).getMillis() / 30);
        int currentToken = tokenAt(now.getMillis() / 30);
        int nextToken = tokenAt(now.plusSeconds(30).getMillis() / 30);

        return token == previousToken || token == currentToken || token == nextToken;
    }

    public int tokenAt(long time) {
        byte[] data = ByteBuffer.allocate(8).putLong(time).array();

        // Building the secret key specification for the HmacSHA1 algorithm.
        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        try {
            // Getting an HmacSHA1 algorithm implementation from the JCE.
            Mac mac = Mac.getInstance("HmacSHA1");

            // Initializing the MAC algorithm.
            mac.init(signKey);

            // Processing the instant of time and getting the encrypted data.
            byte[] hash = mac.doFinal(data);

            // Building the validation code.
            int offset = hash[20 - 1] & 0xF;

            // We are using a long because Java hasn't got an unsigned integer type.
            long truncatedHash = 0;

            for (int i = 0; i < 4; ++i) {
                //truncatedHash = (truncatedHash * 256) & 0xFFFFFFFF;
                truncatedHash <<= 8;

                // Java bytes are signed but we need an unsigned one:
                // cleaning off all but the LSB.
                truncatedHash |= (hash[offset + i] & 0xFF);
            }

            // Cleaning bits higher than 32nd and calculating the module with the
            // maximum validation code value.
            truncatedHash &= 0x7FFFFFFF;
            truncatedHash %= 1000 * 1000;

            return (int) truncatedHash;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return 0;
    }
}