package wenhaoc.stegodb.stegoappscripts.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class F {


    public static PrintWriter initPrintWriter(String path)
    {
        return initPrintWriter(path, false);
    }


    public static PrintWriter initPrintWriter(String path, boolean append)
    {
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(new FileWriter(path, append));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return writer;
    }

    public static List<String> readLines(File f)
    {
        List<String> result = new ArrayList<>();
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(f));
            String line;
            while ((line=in.readLine())!=null)
            {
                result.add(line);
            }
            in.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public static String readFirstLine(File f)
    {
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(f));
            String line = in.readLine();
            in.close();
            return line;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public static void writeLine(String s, File f, boolean append)
    {
        write(s.endsWith("\n")?s:s+"\n", f, append);
    }

    public static void write(String s, File f, boolean append)
    {
        try
        {
            PrintWriter out = new PrintWriter(new FileWriter(f, append));
            out.write(s);
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void write(Iterable<? extends Object> list, File f, boolean append)
    {
        try
        {
            PrintWriter out = new PrintWriter(new FileWriter(f, append));
            for (Object obj : list)
                out.write(obj.toString()+"\n");
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String getFileExt(File f)
    {
        int index = f.getName().lastIndexOf(".");
        if (index == -1)
            return "";
        return f.getName().substring(index+1);
    }
}
