package wenhaoc.stegodb.stegoappscripts.apps_inactive.niastego;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by C03223-Stego2 on 1/16/2018.
 */

public class Testing {


    public static void testAES()
    {
        File aes1 = new File("/sdcard/Download/niastego.aes");
        File aes2 = new File("/sdcard/Download/niastego.aes2");
        byte[] b1 = read(aes1);
        byte[] b2 = read(aes2);
        Log.i("wenhaoc", "AES compare: " + compare(b1, b2));
    }

    private static boolean compare(byte[] b1, byte[] b2)
    {
        if (b1.length != b2.length)
            return false;
        for (int i = 0; i < b1.length; i++)
        {
            if (b1[i]!=b2[i])
                return false;
        }
        return true;
    }

    private static byte[] read(File f)
    {
        byte[] result = new byte[(int)f.length()];
        try
        {
            FileInputStream in = new FileInputStream(f);
            in.read(result);
            in.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }


}
