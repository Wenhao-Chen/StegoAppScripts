package wenhaoc.stegodb.stegoappscripts.apps_inactive.steganography;

import android.graphics.Bitmap;

import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.Random;

/**
 * Created by C03223-Stego2 on 12/20/2017.
 */

public class Unknown_a_d {

    public static volatile long a, b;
    private static final int[] c = {
            //TODO
    };
    private static final byte[] d = {
            82, 72, 67, 80
    };
    private static long e;  //encoded password
    private static Random f; // rng
    //private static Unknown_a_g g;
    private static BitSet h;

    public static long unknow_d(Bitmap bitmap, String message)
    {
        long v0 = (long)bitmap.getByteCount()/4;
        byte[] bytes = message.getBytes(Charset.forName("UTF-8"));
        return v0-(bytes.length+d.length+1+1+4)*8;
    }

    public static Bitmap embed(byte[] message, Bitmap cover, Unknown_a_f af)
    {
        Bitmap copy = cover.copy(Bitmap.Config.ARGB_8888, true);
        copy.setHasAlpha(false);
        f = new Random(e);
        h = new BitSet(cover.getWidth()*cover.getHeight());

        h.clear();

        if (copy == null || !copy.isMutable())
            return null;

        Unknown_a(copy, af);
        for (byte b : message)
        {
            embed(b, copy);
        }
        return copy;
    }

    //af initial values: (true,0,messageBytes.length,0)
    public static void Unknown_a(Bitmap bitmap, Unknown_a_f af)
    {
        //TODO

    }

    public static void embed(byte b, Bitmap bitmap)
    {

    }


}
