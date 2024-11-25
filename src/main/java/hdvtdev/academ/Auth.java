package hdvtdev.academ;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import java.util.Base64;
import java.util.regex.Pattern;

public class Auth {

    private static final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public static boolean isCodeValid(String secretKey, int code) {
        return gAuth.authorize(secretKey, code);
    }

    public static boolean isSecretKeyValid(String secretKey) {

        if (secretKey == null || secretKey.trim().isEmpty()) {
            return false;
        }

        if (secretKey.length() < 16) {
            return false;
        }

        Pattern base32Pattern = Pattern.compile("^[A-Z2-7]+=*$");
        if (!base32Pattern.matcher(secretKey).matches()) {
            return false;
        }

        try {
            Base64.getDecoder().decode(secretKey);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


}
