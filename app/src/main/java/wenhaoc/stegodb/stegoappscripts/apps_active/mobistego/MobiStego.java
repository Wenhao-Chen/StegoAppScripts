package wenhaoc.stegodb.stegoappscripts.apps_active.mobistego;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.List;

import wenhaoc.stegodb.stegoappscripts.StegoStats;
import wenhaoc.stegodb.stegoappscripts.database.DBStego;
import wenhaoc.stegodb.stegoappscripts.util.P;

public class MobiStego {

    public static final String FullName = "MobiStego";
    public static final String AbbrName = "MS";

    public static void makeStegos(File input, List<DBStego> appStegos)
    {
        MobiStego ms = new MobiStego(input);
        P.savePNG(ms.cover, appStegos.get(0).stegoImage);

        String deviceName = input.getName().substring(0, input.getName().indexOf("_"));
        String inputPath = input.getAbsolutePath();
        inputPath = inputPath.substring(inputPath.indexOf(deviceName));
        String coverPath = appStegos.get(0).stegoImage.getAbsolutePath();
        coverPath = coverPath.substring(coverPath.indexOf(deviceName));

        for (int i = 1; i < appStegos.size(); i++)
        {
            DBStego stego = appStegos.get(i);
            int messageLength = ms.getMessageLength(stego.embeddingRate)/8;
            Object[] messageInfo = P.randomMessage(messageLength);
            String message = (String) messageInfo[0];
            String dictionary = (String) messageInfo[1];
            Integer dictLine = (Integer) messageInfo[2];
            long time = System.currentTimeMillis();
            ms.embed(message, stego.stegoImage.getAbsolutePath());
            time = System.currentTimeMillis()-time;

            StegoStats stats = new StegoStats();
            stats.inputImageName = inputPath;
            stats.coverImageName = coverPath;
            stats.stegoApp = FullName;
            stats.capacity = ms.capacity;
            stats.embedded = ms.embedded;
            stats.embeddingRate = (float)stats.embedded/(float)stats.capacity;
            stats.changed = ms.changed;
            stats.dictionary = dictionary;
            stats.dictStartLine = dictLine;
            stats.messageLength = messageLength;
            stats.password = "N/A";
            stats.time = time;
            stats.saveToFile(stego.statsFile.getAbsolutePath());
        }
    }

    Bitmap cover;

    public MobiStego(File f)
    {
        this(BitmapFactory.decodeFile(f.getAbsolutePath()));
    }

    public MobiStego(Bitmap cover) {this.cover = cover; capacity = cover.getWidth()*cover.getHeight()*6;}

    private byte[] messageBytes;
    private int mIndex, shiftIndex;
    public int capacity, embedded, changed;


    // rate : 1-100
    public int getMessageLength(int rate)
    {
        return (int) (capacity*rate/100f - 48);
    }

    // rate: 0-1.0
    public int getMessageLength(float rate)
    {
        return (int) (capacity*rate-48);
    }

    // this one is for: no splitting, no password encryption
    public void embed(String message, String outPath)
    {
        changed = 0;
        messageBytes = ("@!#"+message+"#!@").getBytes(Charset.forName("UTF-8"));
        mIndex = 0;
        shiftIndex = 0;
        int[] pixels = new int[cover.getWidth()*cover.getHeight()];
        cover.getPixels(pixels, 0, cover.getWidth(), 0,0,cover.getWidth(), cover.getHeight());
        int[] embeddedPixels = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++)
        {
            int pixel = pixels[i];
            int oldRed = Color.red(pixel), red = oldRed;
            int oldGreen = Color.green(pixel), green = oldGreen;
            int oldBlue = Color.blue(pixel), blue = oldBlue;
            if (!allEmbedded())
            {
                byte bits = getNext2bits();
                red &= 0xfc;
                red |= bits;
                recordChange(red, oldRed);
            }
            if (!allEmbedded())
            {
                byte bits = getNext2bits();
                green &= 0xfc;
                green |= bits;
                recordChange(green, oldGreen);
            }
            if (!allEmbedded())
            {
                byte bits = getNext2bits();
                blue &= 0xfc;
                blue |= bits;
                recordChange(blue, oldBlue);
            }
            int newColor = Color.argb(0xff, red, green, blue);
            embeddedPixels[i] = newColor;
        }
        embedded = messageBytes.length*8;
        Bitmap bitmap = Bitmap.createBitmap(cover.getWidth(), cover.getHeight(), Bitmap.Config.ARGB_8888);
        bitmap.setDensity(cover.getDensity());
        bitmap.setPixels(embeddedPixels, 0, bitmap.getWidth(), 0,0, bitmap.getWidth(), bitmap.getHeight());
        P.savePNG(bitmap, outPath);
    }


    private void recordChange(int i1, int i2)
    {
        if ((i1&1) != (i2&1))
            changed++;
        if ((i1&2) != (i2&2))
            changed++;
    }

    private byte getNext2bits()
    {
        byte result = messageBytes[mIndex];
        if (shiftIndex==0)
            result = (byte) ((result>>6)&0x3);
        else if (shiftIndex == 1)
            result = (byte) ((result>>4)&0x3);
        else if (shiftIndex == 2)
            result = (byte) ((result>>2)&0x3);
        else if (shiftIndex == 3)
            result = (byte) (result &0x3);
        //P.i("reading message " + Integer.toBinaryString(messageBytes[mIndex])+" " + Integer.toBinaryString(result));
        shiftIndex++;
        if (shiftIndex>3)
        {
            mIndex++;
            shiftIndex = 0;
        }
        return result;
    }

    private boolean allEmbedded()
    {
        return mIndex>=messageBytes.length;
    }

}
