package wenhaoc.stegodb.stegoappscripts.apps_active.pixelknot.common;

/**
 * Created by Wenhao on 6/26/2017.
 */

public class Constants {
    public final static String PASSWORD_SENTINEL = "----* PK v 1.0 REQUIRES PASSWORD ----*";
    public final static String PGP_SENTINEL = "-----BEGIN PGP MESSAGE-----";

    public final static byte[] DEFAULT_PASSWORD_SALT = new String("When I say \"make some\", you say \"noise\"!").getBytes();
    public final static byte[] DEFAULT_F5_SEED = new String("Make some [noise!]  Make some [noise!]").getBytes();


    public static final int MAX_IMAGE_PIXEL_SIZE = 1280;
    public static final int OUTPUT_IMAGE_QUALITY = 90;
    public static final int sentinel_length = 38;
    public static final int iv_length = 17;
    public static final int min_ciphertext_length = 25;
}
