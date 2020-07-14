package wenhaoc.stegodb.stegoappscripts.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import wenhaoc.stegodb.stegoappscripts.MainActivity;

public class P {

    public static final String dict_dir = "/sdcard/Download/message_dictionary";

    public static void i(String message)
    {
        Log.i("wenhaoc_log", message);
        if (act!=null)
            act.log(message);
    }

    public static void e(String message)
    {
        Log.e("wenhaoc_log", message);if (act!=null)
        if (act!=null)
            act.log(message);
    }

    public static MainActivity act;

    public static String getRemainingStorage()
    {
        File f = new File("/sdcard/Download");
        long bytes = f.getFreeSpace();
        long kbs = bytes/1024;
        int mbs = (int)(kbs/1024);
        int gbs = mbs/1024;

        return String.format("%dG %dMB", gbs, mbs%1024);
    }


    public static void savePNG(Bitmap bitmap, String outPath)
    {
        try
        {
            FileOutputStream out = new FileOutputStream(outPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void savePNG(Bitmap bitmap, File f)
    {
        try
        {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static boolean readable(File f)
    {
        try
        {
            BitmapFactory.decodeFile(f.getAbsolutePath());
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    public static Object[] randomMessage(int bytes)
    {
        File f = getRandomDictionary(bytes);
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(f));
            String line;

            int lineCount = 0;
            long total = f.length(), len = 0;

            while ((line=in.readLine())!=null && len<=total-bytes)
            {
                lineCount++;
                len += line.length()+1; // +1 is for the new line character
            }
            in.close();

            int lineIndex = new Random().nextInt(lineCount)+1;
            Object[] result = new Object[3];
            result[0] = getMessage(f.getName(), lineIndex, bytes);
            result[1] = f.getName();
            result[2] = lineIndex;

            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static File getRandomDictionary(int bytes)
    {
        File dictFolder = new File(dict_dir);
        if (!dictFolder.exists() || !dictFolder.isDirectory() || dictFolder.list().length<1)
        {
            System.err.println("can't find any dictionary files at \"" + dict_dir + "\"");
            System.exit(1);
        }

        File[] dicts = dictFolder.listFiles();
        List<File> largeEnough = new ArrayList<>();
        for (File dict : dicts)
            if (dict.length()>bytes)
                largeEnough.add(dict);
        if (largeEnough.isEmpty())
        {
            System.err.println("Can't find a dictionary large enough that provides " + bytes + " bytes.");
            System.exit(1);
        }

        return largeEnough.get(new Random().nextInt(largeEnough.size()));
    }

    //result:
    // [0]: message string
    // [1]: dictionary name
    // [2]: line index
    public static Object[] randomMessage_old(int bytes)
    {
        //startTimer();
        File dictFolder = new File(dict_dir);
        if (!dictFolder.exists() || !dictFolder.isDirectory() || dictFolder.list().length<=0)
        {
            P.e("can't find any dictionary files at \"" + dict_dir+"\"");
            return null;
        }

        File[] dicts = dictFolder.listFiles();
        List<File> largeEnough = new ArrayList<>();
        for (File dict : dicts)
            if (dict.length()>bytes)
                largeEnough.add(dict);
        if (largeEnough.isEmpty())
        {
            P.e("Can't find a dictionary large enough! Asked for " + bytes + " bytes.");
            return null;
        }

        File f = largeEnough.get(new Random().nextInt(largeEnough.size()));

        try
        {
            BufferedReader in = new BufferedReader(new FileReader(f));

            int startRange = (int)(f.length()-bytes);
            char[] head = new char[startRange];
            in.read(head);

            List<Integer> lineIndices = new ArrayList<>();
            for (int i = 0; i < head.length; i++)
            {
                if (head[i]=='\n')
                {
                    lineIndices.add(i);
                }
            }
            int lineIndex = new Random().nextInt(lineIndices.size());
            int startIndex = lineIndices.get(lineIndex)+1;
            String message = new String(head, startIndex, head.length-startIndex);
            if (message.length()<bytes)
            {
                char[] remainingMessage = new char[bytes - message.length()];
                in.read(remainingMessage);
                message += new String(remainingMessage);
            }
            else if (message.length()>bytes)
            {
                message = message.substring(0, bytes);
            }
            in.close();
            Object[] result = new Object[3];
            result[0] = message;
            result[1] = f.getName();
            result[2] = lineIndex+2;
            //reportElapsedTime("generating message");
            if (message.charAt(0)=='\n')
            {
                P.e("Random message first character is NewLine");
            }
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;

    }

    public static String getMessage(String dictName, int lineIndex, int bytes)
    {
        return getMessage(new File(dict_dir, dictName), lineIndex, bytes);
    }

    public static String getMessage(File dict, int lineIndex, int bytes)
    {
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(dict));
            String line;
            StringBuilder sb = new StringBuilder();

            // reach target line index
            int currLine = 1;
            while (currLine++<lineIndex)
                in.readLine();

            while ((line=in.readLine())!=null && sb.length()<bytes)
            {
                sb.append(line);
                sb.append('\n');
            }
            in.close();
            return sb.substring(0, bytes);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String randomPassword(int lengthLowerBound, int lengthUpperBound)
    {
        int length = randomPositiveInt(lengthLowerBound, lengthUpperBound);
        return randomPassword(length);
    }

    public static String randomPassword(int length)
    {
        String password = UUID.randomUUID().toString();
        while (password.length()<length)
            password += UUID.randomUUID().toString();
        return password.substring(0, length);
    }

    public static int randomPositiveInt(int upperBound)
    {
        return new Random().nextInt(upperBound)+1;
    }

    public static int randomPositiveInt(int lowerBound, int upperBound)
    {
        return new Random().nextInt(upperBound-lowerBound+1)+lowerBound;
    }
}
