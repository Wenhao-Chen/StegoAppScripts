package wenhaoc.stegodb.stegoappscripts.apps_inactive.davinci;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.File;
import java.io.FileOutputStream;

import wenhaoc.stegodb.stegoappscripts.util.P;


public class DaVinciEmbed {

    public static final int MaxPasswordLength = 12;

    public static void embedScript(String input, float[] rates, String stegoPath)
    {
        DaVinciEmbed de = new DaVinciEmbed(input);

        for (float rate : rates)
        {
            // 1. determine the length of password and message
            int totalLength = de.calculateInputLengths(rate, true) / 8;
            int passwordLength = P.randomPositiveInt(MaxPasswordLength);
            int messageLength = totalLength - passwordLength;
            P.i("  rate " + rate + " length password/message = " + passwordLength + "/" + messageLength);

            // 2. generate random password and message
            String password = P.randomPassword(passwordLength);
            Object[] messageInfo = P.randomMessage(messageLength);
            String message = (String) messageInfo[0];
            String dictionary = (String) messageInfo[1];
            Integer dictLine = (Integer) messageInfo[2];
            P.i("  message dict/line/length = " + dictionary + "/" + dictLine + "/" + messageLength);
            P.i("  password length/password = " + passwordLength + "/" + password);

            // 3. embed and output stego
            de.embed(message, password, stegoPath);
        }
    }



    public Bitmap input, stego;
    public int capacity, embedded, changed;

    private int x,y;

    public DaVinciEmbed(String inputPath)
    {
        input = BitmapFactory.decodeFile(inputPath);
        x = y = 0;
        capacity = input.getWidth()*input.getHeight();
    }

    public DaVinciEmbed(File input)
    {
        this(input.getAbsolutePath());
    }

    public DaVinciEmbed(Bitmap cover)
    {
        input = cover;
        x = y = 0;
        capacity = input.getWidth()*input.getHeight();
    }

    public void saveCover(String outPath)
    {
        saveImage(input, outPath, false);
    }

    public void embed(String message, String password, String outPath)
    {
        initStego();
        embed("t2ip");
        embed(password);
        embed(message);
        saveImage(stego, outPath, true);
    }

    public void embed(String message, String outPath)
    {
        initStego();
        embed("t2i");
        embed(message);
        saveImage(stego, outPath, true);
    }

    // The returned length is in bits.
    // If has password, then the length is (message+password).length;
    // if no password, then the length is message.length
    public int calculateInputLengths(float rate, boolean hasPassword)
    {
        if (rate<=0 || rate>1)
            return -1;
        if (hasPassword)
        {
            return (int)(capacity*rate)-32*4;
        }
        else
        {
            return (int)(capacity*rate)-32-32-24;
        }
    }

    private void initStego()
    {
        stego = input.copy(Bitmap.Config.ARGB_8888, true);
        stego.setHasAlpha(true);
        embedded = 0;
        changed = 0;
        x = 0;
        y = 0;
    }

    private void embed(String s)
    {
        byte[] msg = s.getBytes();
        byte[] length = bit_conversion(msg.length);
        P.i("embedding {" + length[0]+","+length[1]+","+length[2]+","+length[3]+"} " + (s.length()<=10?s:""));
        embed(length);
        embed(msg);
    }

    private void embed(byte[] bytes)
    {
        for (byte b : bytes)
        {
            for (int bitIndex = 7; bitIndex >= 0; bitIndex--)
            {
                int bit = (b >>> bitIndex)&1;
                int alpha = 254+bit;
                int oldC = input.getPixel(x,y);
                int newC = Color.argb(alpha, Color.red(oldC), Color.green(oldC), Color.blue(oldC));
                stego.setPixel(x,y, newC);
                embedded++;
                if (oldC != newC)
                    changed++;
                x++;
                if (x== input.getWidth())
                {
                    x=0; y++;
                }
            }
        }
    }

    private void saveImage(Bitmap bitmap, String outPath, boolean recycle)
    {
        try
        {
            FileOutputStream outStream = new FileOutputStream(outPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
            if (recycle)
            {
                bitmap.recycle();
                bitmap = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // returns a 32 bit length information
    protected static byte[] bit_conversion(int i)
    {
        byte byte3 = (byte) ((0xff000000 & i) >>> 24);
        byte byte2 = (byte) ((0xff0000 & i) >>> 16);
        byte byte1 = (byte) ((0xff00 & i) >>> 8);
        byte byte0 = (byte) (0xff & i);

        byte[] result = new byte[4];
        result[0] = byte3;
        result[1] = byte2;
        result[2] = byte1;
        result[3] = byte0;

        return result;
    }

}
