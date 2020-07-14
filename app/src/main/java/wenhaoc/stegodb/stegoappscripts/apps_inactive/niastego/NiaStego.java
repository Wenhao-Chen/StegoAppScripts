package wenhaoc.stegodb.stegoappscripts.apps_inactive.niastego;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.io.File;

/**
 * Created by C03223-Stego2 on 1/15/2018.
 */

public class NiaStego {


    public static void embed(byte[] payload, Bitmap cover)
    {
        int index = 0;
        int value = 0;
        int pixelElementIndex = 0;
        int zeros = 0;
        int R = 0, G = 0, B = 0;
        for (int i = 0; i < cover.getHeight(); i++)
        {
            for (int j = 0; j < cover.getWidth(); j++)
            {
                int pixel = cover.getPixel(j,i);
                R = Color.red(pixel)- Color.red(pixel)%2;
                G = Color.green(pixel)- Color.green(pixel)%2;
                B = Color.blue(pixel)- Color.blue(pixel)%2;
                for (int n = 0; n < 3; n++)
                {
                    int v14 = pixelElementIndex%8;
                    if (v14 != 0)
                    {
                        //:cond_a
                        switch (pixelElementIndex%3)
                        {
                            case 0: //:pswitch_0
                            case 1: //:pswitch_1
                            case 2: //:pswitch_2
                        }
                    }
                    else
                    {
                        //.line 62

                    }
                }
            }
        }
    }


    // the input method has another parameter called "ScalingLogic" which has two values: FIT or CROP.
    // Seems like the CROP was not used, so I am ignoring this parameter for now.
    public static Bitmap createScaledBitmap(Bitmap unscaledBitmap, long dstWidth, long dstHeight)
    {
        Rect srcRect = calculateSrcRect(
                unscaledBitmap.getWidth(), unscaledBitmap.getHeight(),
                dstWidth, dstHeight);
        Rect dstRect = calculateDstRect(
                unscaledBitmap.getWidth(), unscaledBitmap.getHeight(),
                dstWidth, dstHeight);

        Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(), dstRect.height(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.drawBitmap(unscaledBitmap, srcRect, dstRect, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }


    public static Rect calculateSrcRect(int w, int h, long dstW, long dstH)
    {
        return new Rect(0,0,w, h);
    }

    public static Rect calculateDstRect(int w, int h, long dstW, long dstH)
    {
        float srcAspect = (float)w/(float)h;
        float dstAspect = (float)dstW/(float)dstH;
        if (srcAspect <= dstAspect)
        {
            return new Rect(0,0,(int)dstW, (int)dstH);
        }
        else
        {
            return new Rect(0,0,(int)dstW, (int)(dstW/srcAspect));
        }
    }

    public static byte[] combineFileAndName(byte[] payloadBytes, String fileName)
    {
        byte[] nameBytes = ("<fn>"+fileName+"<fn>").getBytes();
        byte[] combinedBytes = new byte[nameBytes.length+payloadBytes.length];
        System.arraycopy(payloadBytes, 0, combinedBytes, 0, payloadBytes.length);
        System.arraycopy(nameBytes, 0, combinedBytes, payloadBytes.length, nameBytes.length);
        return combinedBytes;
    }

    public static byte[] addPart(byte[] payloadPart, int partIndex)
    {
        String partName = "<pt>"+partIndex+"<pt>";
        byte[] partNameBytes = partName.getBytes();
        byte[] combinedBytes = new byte[partNameBytes.length+payloadPart.length];
        System.arraycopy(payloadPart, 0, combinedBytes, 0, payloadPart.length);
        System.arraycopy(partNameBytes, 0, combinedBytes, payloadPart.length, partNameBytes.length);
        return combinedBytes;
    }

    public static int[] requireSize(Bitmap bitmap, long fileSize)
    {
        // number of pixel required for the payload file
        double requiredPixel = 1.1f * ((fileSize*8.0f)/3.0f + fileSize%3);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float ratio = (float)w/(float)h;
        while (w*h < requiredPixel)
        {
            w += 10;
            h = (int)((float)w/ratio);
        }
        return new int[] {w, h};
    }


    public static long fileSizeEstimation(File payloadFile, int coverImageCount)
    {
        long fileSize = payloadFile.length(); // v8
        long afterEncrypted = fileSizeAfterEncrypted(fileSize, payloadFile.getName());
        Log.i("wenhaoc_long", "afterEncrypted: " + afterEncrypted);
        long afterEncoded = fileSizeAfterEncoded(afterEncrypted);
        Log.i("wenhaoc_long", "afterEncoded: " + afterEncoded);
        long imageAfterParted = afterEncoded;
        String imgCount = String.valueOf(coverImageCount);
        if (coverImageCount > 1)
        {
            imageAfterParted = afterEncoded/coverImageCount +
                    afterEncoded%coverImageCount +
                    "<pt>".length()*2 +
                    String.valueOf(coverImageCount).length();
        }
        return imageAfterParted;
    }

    public static long fileSizeAfterEncrypted(long fileSize, String fileName)
    {
        long fileSizeAfterTag = fileName.length()+
                fileSize+
                "<fn>".length()*2;
        long fileAfterEncrypted = (fileSizeAfterTag/16+1)*16;
        return fileAfterEncrypted;
    }

    public static long fileSizeAfterEncoded(long fileSizeAfterEncrpted)
    {
        long v6 = 3;
        long v2 = 4;
        long v4 = fileSizeAfterEncrpted/v6*v2;
        v2 = fileSizeAfterEncrpted%v6;

        return fileSizeAfterEncrpted%3==0? fileSizeAfterEncrpted/3*4: fileSizeAfterEncrpted/3*4+4;
    }




}
