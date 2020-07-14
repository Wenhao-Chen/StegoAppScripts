package wenhaoc.stegodb.stegoappscripts.apps_inactive.steganography_akseltorgard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Created by C03223-Stego2 on 1/16/2018.
 */

public class Steganography {


    public static Bitmap embed2(String imagePath, String message)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

        byte[] data = message.getBytes();
        byte[] dataWithLength = new byte[data.length+4];
        int dataLength = data.length;
        for (int i = 0; i < 4; i++)
        {
            dataWithLength[i] = (byte)(dataLength&0xff);
            dataLength = dataLength >>> 8;
        }
        System.arraycopy(data, 0, dataWithLength, 4, dataLength);
        int x = 0, y = 0;
        for (int i = 0; i < dataWithLength.length; i++)
        {
            byte b = dataWithLength[i];
            for (int j = 0; j < 8; j++)
            {
                int pixel = bitmap.getPixel(x, y);
                Log.i("wenhaoc_int", "old value: " + pixel);
                pixel &= -2;
                pixel |= b&1;
                bitmap.setPixel(x, y, pixel);

                Log.i("wenhaoc_int", "new value: " + pixel);

                b = (byte) (b>>>1);
                y++;
                if (y == bitmap.getWidth())
                {
                    y = 0;
                    x++;
                }
            }
        }
        return bitmap;
    }

    public static void embed(String imagePath, String message)
    {
        Bitmap bitmap = decodeFile(imagePath);
        byte[] data = message.getBytes();

        int numberOfPixels = bitmap.getWidth()*bitmap.getHeight();
        int requiredLength = data.length*8+32;
        if (requiredLength < numberOfPixels)
        {
            int[] encodedPixels = encode(getPixels(bitmap), message);

        }
    }

    private static int[] encode(int[] pixels, String message)
    {
        byte[] data = message.getBytes();
        byte[] dataWithLength = new byte[data.length+4];
        int dataLength = data.length;
        for (int i = 0; i < 4; i++)
        {
            dataWithLength[i] = (byte)(dataLength&0xff);
            dataLength = dataLength >>> 8;
        }
        System.arraycopy(data, 0, dataWithLength, 4, dataLength);
        int pixelIndex = 0;
        for (int i = 0; i < dataWithLength.length; i++)
        {
            byte b = dataWithLength[i];
            for (int j = 0; j < 8; j++)
            {
                pixels[pixelIndex] &= -2;
                pixels[pixelIndex] |= b&1;
                b = (byte) (b>>>1);
                pixelIndex++;
            }
        }
        return pixels;
    }


    private static Bitmap decodeFile(String imagePath)
    {
        return BitmapFactory.decodeFile(imagePath);
    }

    private static int[] getPixels(Bitmap bitmap)
    {
        int[] pixels = new int[bitmap.getWidth()*bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        return pixels;
    }

    private static void saveBitmap(Bitmap bitmap)
    {
        //TODO
    }

}
