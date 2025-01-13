package com.example.fyp_authenticator.Utilities;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

public class TOTP_Algo {

    //private static final String HMAC_ALGORITHM = "HmacSHA1"; // 1,256,384,512You can also use "HmacSHA256" or "HmacSHA512"
    private static final int DIGITS = 6; //can change digit
    //private static final long TIME_STEP = 30; // Time step in seconds
    //private static final int HOTP_COUNTER = 0;

    public static String generateTOTP(String secretKey, String SHA_ALGO) {
        try {
            long counter = Instant.now().getEpochSecond() / 30; // Timestep is 30 seconds
            byte[] secretBytes = Base64.getDecoder().decode(secretKey);
            byte[] counterBytes = ByteBuffer.allocate(8).putLong(counter).array();

            SecretKeySpec keySpec = new SecretKeySpec(secretBytes, SHA_ALGO);
            Mac mac = Mac.getInstance(SHA_ALGO);
            mac.init(keySpec);

            byte[] hash = mac.doFinal(counterBytes);

            // Truncate to 6 digits
            int offset = hash[hash.length - 1] & 0xF;
            int binary =
                    ((hash[offset] & 0x7F) << 24) |
                            ((hash[offset + 1] & 0xFF) << 16) |
                            ((hash[offset + 2] & 0xFF) << 8) |
                            (hash[offset + 3] & 0xFF);

            int otp = binary % (int) Math.pow(10, DIGITS);
            return String.format("%0" + DIGITS + "d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateHOTP(String secretKey, long hotp_counter, String SHA_ALGO) {
        try {
            byte[] secretBytes = Base64.getDecoder().decode(secretKey);
            byte[] counterBytes = ByteBuffer.allocate(8).putLong(hotp_counter).array();

            SecretKeySpec keySpec = new SecretKeySpec(secretBytes, SHA_ALGO);
            Mac mac = Mac.getInstance(SHA_ALGO);
            mac.init(keySpec);

            byte[] hash = mac.doFinal(counterBytes);

            // Truncate to 6 digits
            int offset = hash[hash.length - 1] & 0xF;
            int binary =
                    ((hash[offset] & 0x7F) << 24) |
                            ((hash[offset + 1] & 0xFF) << 16) |
                            ((hash[offset + 2] & 0xFF) << 8) |
                            (hash[offset + 3] & 0xFF);

            int otp = binary % (int) Math.pow(10, DIGITS);
            return String.format("%0" + DIGITS + "d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }


}