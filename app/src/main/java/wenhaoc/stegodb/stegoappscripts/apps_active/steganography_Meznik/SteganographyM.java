package wenhaoc.stegodb.stegoappscripts.apps_active.steganography_Meznik;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import wenhaoc.stegodb.stegoappscripts.StegoStats;
import wenhaoc.stegodb.stegoappscripts.apps_inactive.steganography_akseltorgard.Steganography;
import wenhaoc.stegodb.stegoappscripts.database.DBStego;
import wenhaoc.stegodb.stegoappscripts.util.P;


public class SteganographyM {

    public static final String FullName = "SteganographyM";
    public static final String AbbrName = "SM";

    public static void makeStegos(File input, List<DBStego> appStegos)
    {
        SteganographyM sm = new SteganographyM(input);
        P.savePNG(sm.cover, appStegos.get(0).stegoImage);

        String deviceName = input.getName().substring(0, input.getName().indexOf("_"));
        String inputPath = input.getAbsolutePath();
        inputPath = inputPath.substring(inputPath.indexOf(deviceName));
        String coverPath = appStegos.get(0).stegoImage.getAbsolutePath();
        coverPath = coverPath.substring(coverPath.indexOf(deviceName));

        for (int i = 1; i < appStegos.size(); i++)
        {
            DBStego stego = appStegos.get(i);

            int messageLength = sm.getPayloadLength(stego.embeddingRate)/8;
            Object[] messageInfo = P.randomMessage(messageLength);
            String message = (String) messageInfo[0];
            String dictionary = (String) messageInfo[1];
            Integer dictLine = (Integer) messageInfo[2];

            int passwordLength = P.randomPositiveInt(SteganographyM.MaxPasswordLength);
            String password = P.randomPassword(passwordLength);

            long time = System.currentTimeMillis();
            sm.embed(message, password, stego.stegoImage.getAbsolutePath());
            time = System.currentTimeMillis()-time;

            StegoStats stats = new StegoStats();
            stats.inputImageName = inputPath;
            stats.coverImageName = coverPath;
            stats.stegoApp = FullName;
            stats.capacity = sm.capacity;
            stats.embedded = sm.embedded;
            stats.embeddingRate = (float)stats.embedded/(float)stats.capacity;
            stats.changed = sm.changed;
            stats.dictionary = dictionary;
            stats.dictStartLine = dictLine;
            stats.messageLength = messageLength;
            stats.password = password;
            stats.time = time;
            stats.saveToFile(stego.statsFile.getAbsolutePath());
        }
    }


    public Bitmap cover, stego;

    private Random rng;
    private BitSet bitSet;

    private static final byte[] prefix = new byte[] {0x52, 0x48, 0x43, 0x50};
    private static final byte[] channelLSBs = new byte[] {0,8,16};
    public static final int MaxPasswordLength = 8;

    public int capacity, embedded, changed;

    public SteganographyM(File inputF)
    {
        this(BitmapFactory.decodeFile(inputF.getAbsolutePath()));
    }

    public SteganographyM(Bitmap input)
    {
        cover = input;
        //NOTE: Although only one channel per pixel can be changed, we still count
        // all 3 channels into the capacity
        capacity = cover.getWidth()*cover.getHeight()*3;
        bitSet = new BitSet(capacity);
    }

    // payload bits, rate: 0-100
    public int getPayloadLength(int rate)
    {
        return (int) (capacity*rate/100f-80);
    }

    // payload bits, rate: 0-1.0
    public int getPayloadLength(float rate)
    {
        return (int) (capacity*rate-80);
    }

    public void embed(String message, String password, String outPath)
    {
        stego = cover.copy(cover.getConfig(), true);
        embedded = 0;
        changed = 0;
        bitSet.clear();

        initRNG(password);
        // first embed 4 bytes of signature
        embed(prefix);
        // then embed 6 bytes of length information
        embed(getLengthBytes(message.length()));
        // then message bytes
        embed(message.getBytes());
        P.savePNG(stego, outPath);
    }

    private void embed(byte[] bytes)
    {
        for (byte b : bytes)
        {
            for (int i = 0; i < 8; i++)
            {
                int payloadBit = b>>i&1;
                int x, y, pixelIndex;
                do
                {
                    x = rng.nextInt(stego.getWidth());    // a/g/a
                    y = rng.nextInt(stego.getHeight());      // a/g/b
                    pixelIndex = x * stego.getWidth() + y;    // the original code might be faulty.
                }
                while (bitSet.get(pixelIndex));
                bitSet.set(pixelIndex);
                int oldColor = stego.getPixel(x, y);
                int channelLSB = channelLSBs[rng.nextInt(3)];

                int newColor = payloadBit==1? (1<<channelLSB)|oldColor : ((1<<channelLSB)^-0x1)&oldColor;
                stego.setPixel(x, y, newColor);
                embedded++;
                if (newColor != oldColor)
                    changed++;
            }
        }
    }

    private byte[] getLengthBytes(int length)
    {
        byte[] bytes = new byte[] {0,0,0,0,0,0};
        for (int i = 5; i > 1; i--)
        {
            bytes[i] = (byte)(length>>((5-i)*8)&255);
        }

        return bytes;
    }


    private void initRNG(String password)
    {
        byte[] pwBytes = password.getBytes();
        byte[] bytes = new byte[] {0,0,0,0,0,0,0,0};
        for (int i = 0; i < pwBytes.length && i < bytes.length; i++)
        {
            bytes[i] = pwBytes[i];
        }
        int[] bitsToShift = new int[] {0x38, 0x30, 0x28, 0x20, 0x18, 0x10, 0x8, 0};
        long seed = 0;
        for (int i = 0; i < 8; i++)
        {
            long l = (long)(bytes[i] & 0xff);
            seed |= (l<<bitsToShift[i]);
        }
        rng = new Random(seed);
    }


}
