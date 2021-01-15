package wenhaoc.stegodb.stegoappscripts.apps_active.pixelknot;

import java.io.File;
import java.util.List;

import wenhaoc.stegodb.stegoappscripts.StegoStats;
import wenhaoc.stegodb.stegoappscripts.apps_active.pixelknot.common.Aes;
import wenhaoc.stegodb.stegoappscripts.apps_active.pixelknot.common.Constants;
import wenhaoc.stegodb.stegoappscripts.apps_active.pixelknot.encode.F5CoreEmbed;
import wenhaoc.stegodb.stegoappscripts.database.DBStego;
import wenhaoc.stegodb.stegoappscripts.util.F;
import wenhaoc.stegodb.stegoappscripts.util.P;


/**
 * Created by C03223-Stego2 on 7/21/2017.
 */

public class PixelKnot {

    public static final String FullName = "PixelKnot";
    public static final String AbbrName = "PK";


    public static void makeStegos(File input, List<DBStego> appStegos)
    {
        F5CoreEmbed f5 = new F5CoreEmbed(input);
        f5.saveIntermediateCover(appStegos.get(0).stegoImage.getAbsolutePath());


        String deviceName = input.getName().substring(0, input.getName().indexOf("_"));
        String inputPath = input.getAbsolutePath();
        inputPath = inputPath.substring(inputPath.indexOf(deviceName));
        String coverPath = appStegos.get(0).stegoImage.getAbsolutePath();
        coverPath = coverPath.substring(coverPath.indexOf(deviceName));
        for (int i = 1; i < appStegos.size(); i++)
        {
            DBStego stego = appStegos.get(i);
            int minCap = PixelKnot.getMinimumCapacity(stego.embeddingRate);
            if (f5.image.capacity <= minCap)
            {
                P.e("Insufficient capacity (has "+f5.image.capacity+ " need "+minCap+") for "+(stego.embeddingRate*100)+"% embedding PixelKnot: " + input.getAbsolutePath());
                continue;
            }

            // make stego file
            int length_pt = PixelKnot.getPlaintextLength((int) (f5.image.capacity*stego.embeddingRate));
            Object[] messageInfo = P.randomMessage(length_pt);
            String plaintext = (String) messageInfo[0];
            String fullPassword = P.randomPassword(4, 12);
            String aesKey = fullPassword.substring(0, fullPassword.length() / 3);
            String aesSalt = fullPassword.substring(fullPassword.length() / 3, fullPassword.length() / 3 * 2);
            String f5Seed = fullPassword.substring(fullPassword.length() / 3 * 2);
            String[] encrypted = Aes.encryptWithPassword(aesKey, plaintext, aesSalt);
            String iv = encrypted[0];
            String ciphertext = encrypted[1];
            long time = System.currentTimeMillis();

            // this is how PixelKnot assembles the payload
            f5.embed(Constants.PASSWORD_SENTINEL+iv+ciphertext, f5Seed, stego.stegoImage.getAbsolutePath());
            time = System.currentTimeMillis()-time;

            // make stats file
            StegoStats stats = new StegoStats();
            stats.inputImageName = inputPath;
            stats.coverImageName = coverPath;
            stats.stegoApp = FullName;
            stats.capacity = f5.image.capacity;
            stats.embedded = f5.f5_embedded;
            stats.embeddingRate = (float)stats.embedded/(float)stats.capacity;
            stats.changed = f5.f5_changed;
            stats.dictionary = (String)messageInfo[1];
            stats.dictStartLine = (int)messageInfo[2];
            stats.messageLength = length_pt;
            stats.password = fullPassword;
            stats.time = time;
            stats.additionalInfo.put("AES IV", iv);
            stats.saveToFile(stego.statsFile.getAbsolutePath());
        }
    }

    // rate should be float between 0 and 1
    public static int getMinimumCapacity(float rate)
    {
        float minCap = 84*8/rate;
        return (int) minCap;
    }


    // targetLength is in bits
    // return value is in bytes
    public static int getPlaintextLength(int targetLength)
    {
        int result = -1;

        int ciphertext_length = targetLength/8 - 4 - Constants.sentinel_length-Constants.iv_length;
        if (ciphertext_length < Constants.min_ciphertext_length) // not enough capacity
            return 1;
        else if (ciphertext_length <= 77)
        {
            result = (ciphertext_length-25)*3/4;
        }
        else
        {
            result = (ciphertext_length*57-1383)/77;
        }
        return result;
    }


}
