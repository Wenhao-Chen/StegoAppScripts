package wenhaoc.stegodb.stegoappscripts.apps_inactive.steganography;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by C03223-Stego2 on 12/19/2017.
 */

public class Steganography {

    private static int calculateInSampleSize(int imageWidth, int imageHeight, int areaWidth, int areaHeight)
    {
        int w = imageWidth;
        int h = imageHeight;
        int result = 1;
        if (h > areaHeight || w > areaWidth)
        {
            w = w/2;
            h = h/2;
            while (h/result >= areaHeight && w/result >= areaWidth)
                result*=2;
        }
        return result;
    }

    public static Bitmap loadImage(String path, int areaWidth, int areaHeight)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, areaWidth, areaHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }



}
