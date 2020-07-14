package wenhaoc.stegodb.stegoappscripts.apps_inactive.steganographymaster;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import wenhaoc.stegodb.stegoappscripts.util.P;


public class SteganographyMasterEncoder {


    private Bitmap cover, stego;
    public int capacity, embedded, changed;

    public SteganographyMasterEncoder(String path)
    {
        cover = BitmapFactory.decodeFile(path);
    }

    public SteganographyMasterEncoder(Bitmap input)
    {
        cover = input;
        capacity = cover.getWidth()*cover.getHeight()*8;
    }

    public int getInputLength(float rate, boolean hasPassword)
    {
        if (hasPassword)
        {
            return (int) (capacity*rate-22*8-6*8);
        }
        else
        {
            return (int) (capacity*rate-22*8);
        }
    }

    public void embed(String message, String outPath)
    {
        embed(message, null, outPath);
    }

    // each pixel holds 8 bits of payload. R/G/B channel holds the hundreds/tens/ones number
    public void embed(String message, String password, String outPath)
    {
        stego = cover.copy(cover.getConfig(), true);
        embedded = changed = 0;
        String passwordString = "";
        if (password != null && !password.isEmpty())
            passwordString = "[!$" + password + "$!]";
        String payload = "(#*CEVAP*#)"+passwordString+message+"(#*BUREK*#)";

        int index = 0;
        for (int x = 0; x < stego.getWidth(); x++)
        {
            for (int y = 0; y < stego.getHeight(); y++)
            {
                int character = payload.charAt(index++);
                int hundreds = character/100;
                int tens = character/10%10;
                int ones = character%10;

                int color = stego.getPixel(x,y);
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                int newRed = correctOverflow(red-red%10+hundreds);
                if (red != newRed)
                    changed++;
                int newGreen = correctOverflow(green-green%10+tens);
                if (green != newGreen)
                    changed++;
                int newBlue = correctOverflow(blue-blue%10+ones);
                if (blue != newBlue)
                    changed++;
                stego.setPixel(x, y, Color.rgb(newRed, newGreen, newBlue));
                embedded += 8;
                if (index>=payload.length())
                {
                    P.savePNG(stego, outPath);
                    stego.recycle();
                    return;
                }
            }
        }
    }

    private int correctOverflow(int value)
    {
        return value>255?value-10:value;
    }



}
