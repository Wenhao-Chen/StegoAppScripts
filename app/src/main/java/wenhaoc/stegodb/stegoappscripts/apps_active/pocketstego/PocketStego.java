package wenhaoc.stegodb.stegoappscripts.apps_active.pocketstego;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.util.List;

import wenhaoc.stegodb.stegoappscripts.StegoStats;
import wenhaoc.stegodb.stegoappscripts.database.DBStego;
import wenhaoc.stegodb.stegoappscripts.util.P;


public class PocketStego {

    public static final String FullName = "PocketStego";
    public static final String AbbrName = "PS";

    public static void makeStegos(File input, List<DBStego> appStegos)
    {
        PocketStego ps = new PocketStego(input);
        P.savePNG(ps.cover, appStegos.get(0).stegoImage);

        String deviceName = input.getName().substring(0, input.getName().indexOf("_"));
        String inputPath = input.getAbsolutePath();
        inputPath = inputPath.substring(inputPath.indexOf(deviceName));
        String coverPath = appStegos.get(0).stegoImage.getAbsolutePath();
        coverPath = coverPath.substring(coverPath.indexOf(deviceName));

        for (int i = 1; i < appStegos.size(); i++)
        {
            DBStego stego = appStegos.get(i);
            int messageLength = ps.getPayloadLength(stego.embeddingRate)/8;
            Object[] messageInfo = P.randomMessage(messageLength);
            String message = (String) messageInfo[0];
            String dictionary = (String) messageInfo[1];
            Integer dictLine = (Integer) messageInfo[2];
            long time = System.currentTimeMillis();
            ps.embed(message, stego.stegoImage.getAbsolutePath(), true);
            time = System.currentTimeMillis()-time;

            StegoStats stats = new StegoStats();
            stats.inputImageName = inputPath;
            stats.coverImageName = coverPath;
            stats.stegoApp = FullName;
            stats.capacity = ps.capacity;
            stats.embedded = ps.embedded;
            stats.embeddingRate = (float)stats.embedded/(float)stats.capacity;
            stats.changed = ps.changed;
            stats.dictionary = dictionary;
            stats.dictStartLine = dictLine;
            stats.messageLength = messageLength;
            stats.password = "N/A";
            stats.time = time;
            stats.additionalInfo.put("Embedding path", "Column by column");
            stats.saveToFile(stego.statsFile.getAbsolutePath());
        }
    }

    static class Options {
        boolean columnFirst; // if column first, then (0,0), (0,1), ...; else (0,0), (1,0), ...
        int encodingBits; // if 8 bits, then all bits are embedded; if 7 bits, then only the L7SB are embedded
        boolean useEBCIDIC; // if true, then encode each character to another character
        boolean bitReversal; // if true, then reverse the bit order for each byte
        boolean bitInversion; // if true, then inverse the value of each bit: 0 to 1, 1 to 0
        boolean inverseEveryOther; // if true, then inverse the even position bits
    }

    public Bitmap cover, stego;
    public int capacity, embedded, changed;

    public PocketStego(File f)
    {
        this(f.getAbsolutePath());
    }

    public PocketStego(String input)
    {
        this(BitmapFactory.decodeFile(input));
    }

    public PocketStego(Bitmap original)
    {
        int height = 512*original.getHeight()/original.getWidth();
        cover = Bitmap.createScaledBitmap(original, 512, height, true);
        capacity = cover.getWidth()*cover.getHeight();
    }

    // returns payload length in bits
    public int getPayloadLength(float rate)
    {
        return (int)(capacity*rate)-8;
    }

    public int getPayloadLength(int rate)
    {
        return getPayloadLength((float)rate/100f);
    }


    public void embed(String message, String outPath, boolean columnFirst)
    {
        PocketStego.Options options = new PocketStego.Options();
        options.columnFirst = columnFirst;
        options.bitInversion = false;
        options.inverseEveryOther = false;
        options.bitReversal = false;
        options.useEBCIDIC = false;
        options.encodingBits = 8;
        embed(message, options, outPath);
    }

    public Bitmap embed(String message, Options options, String outPath)
    {
        embedded = 0;
        changed = 0;
        stego = cover.copy(cover.getConfig(), true);
        message += "\u0000";
        int payloadLength = message.length()*options.encodingBits;
        int payloadBitIndex = 0; // v6
        int xDimension = getXDimension(options);
        int yDimension = getYDimension(options);
        for (int x = 0; x < xDimension; x++)
        {
            for (int y = 0; y < yDimension; y++)
            {
                int oldColor = getPixel(options, x, y), color = oldColor;
                if (payloadBitIndex < payloadLength)
                {
                    color = color&0xfffffffe;
                    char bits = message.charAt(payloadBitIndex/options.encodingBits); // v5
                    if (options.useEBCIDIC)
                    {
                        bits = Constants.A2E[bits];
                    }
                    if (options.bitReversal)
                    {
                        bits = reverseBits(bits, options.encodingBits);
                    }
                    int shift = options.encodingBits-1-payloadBitIndex%options.encodingBits;
                    int v4 =  bits>>shift&1;
                    //P.i("embed " + bits+"("+Integer.toBinaryString(bits)+") " + shift+" " + Integer.toBinaryString(v4));
                    if (options.bitInversion)
                    {
                        v4 = v4^0xff&1;
                    }
                    else if (options.inverseEveryOther && payloadBitIndex%2==0)
                    {
                        v4 = v4^0xff&1;
                    }
                    color |= v4;
                    payloadBitIndex++;
                    embedded++;
                    if (oldColor!=color)
                        changed++;
                }
                setPixel(options, x, y, color);
            }
        }
        P.savePNG(stego, outPath);
        return stego;
    }

    static char reverseBits(char bits, int encodingBits)
    {
        char result = 0;
        for (int i = 0; i < encodingBits; i++)
        {
            int bit = (bits>>(encodingBits-1-i))&1;
            result |= bit<<i;
        }
        return result;
    }

    private int getXDimension(Options options)
    {
        return options.columnFirst?cover.getWidth():cover.getHeight();
    }

    private int getYDimension(Options options)
    {
        return options.columnFirst?cover.getHeight():cover.getWidth();
    }

    private int getPixel(Options options, int x, int y)
    {
        return options.columnFirst?cover.getPixel(x,y):cover.getPixel(y,x);
    }

    private void setPixel(Options options, int x, int y, int c)
    {
        if (options.columnFirst)
            stego.setPixel(x,y,c);
        else
            stego.setPixel(y,x,c);
    }


}
