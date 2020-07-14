package wenhaoc.stegodb.stegoappscripts.apps_active.pixelknot.common;

import android.content.Context;
import android.graphics.Bitmap;


import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.InputStream;


/**
 * Created by Wenhao on 6/26/2017.
 */

public class Util
{
    public static Bitmap loadImageForPixelKnot(File f)
    {
        try
        {
            return
                    Picasso.get()
                    .load(f)
                    .resize(Constants.MAX_IMAGE_PIXEL_SIZE, Constants.MAX_IMAGE_PIXEL_SIZE)
                    .onlyScaleDown()
                    .centerInside()
                    .get();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static int plaintext_source;


}
