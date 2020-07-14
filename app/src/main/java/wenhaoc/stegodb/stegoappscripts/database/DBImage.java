package wenhaoc.stegodb.stegoappscripts.database;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wenhaoc.stegodb.stegoappscripts.MainScript;
import wenhaoc.stegodb.stegoappscripts.apps_active.mobistego.MobiStego;
import wenhaoc.stegodb.stegoappscripts.apps_active.pixelknot.PixelKnot;
import wenhaoc.stegodb.stegoappscripts.apps_active.pocketstego.PocketStego;
import wenhaoc.stegodb.stegoappscripts.apps_active.steganography_Meznik.SteganographyM;
import wenhaoc.stegodb.stegoappscripts.util.F;
import wenhaoc.stegodb.stegoappscripts.util.P;

public class DBImage {


    public File original, png;
    public Map<String, List<DBStego>> stegos;
    private DBScene scene;
    public int index;

    DBImage(File original, DBScene scene, int index)
    {
        this.original = original;
        this.scene = scene;
        this.index = index;
        String leftPart = original.getName().substring(0, original.getName().lastIndexOf("_o."));
        png = new File(scene.device.pngDir, leftPart+"_cc.png");

        stegos = new HashMap<>();
        for (File appDir : scene.device.stegosDir.listFiles())
        {
            String appName = appDir.getName();
            File input = appName.equals(PixelKnot.FullName)? original : png;
            String stegoNamePrefix = input.getName()+"_s_"+MainScript.getAbbrAppName(appName)+"_rate-";
            String stegoNameSuffix = MainScript.spatialApps.contains(appName)?".png":".jpg";
            List<DBStego> appStegos = new ArrayList<>();
            for (int rate = 0; rate <= 25; rate += 5)
            {
                String rateS = String.format("%02d", rate);
                DBStego stego = new DBStego();
                stego.stegoImage = new File(appDir, stegoNamePrefix + rateS + stegoNameSuffix);
                stego.statsFile = new File(appDir, stegoNamePrefix + rateS + ".csv");
                stego.isCover = (rate==0);
                stego.embeddingRate = (float)rate/100.0f;
                appStegos.add(stego);
            }
            stegos.put(appName, appStegos);
        }
    }

    static final Set<String> appsToSkip = new HashSet<String>(){{
        //add(MobiStego.FullName);
        //add(PocketStego.FullName);
        //add(SteganographyM.FullName);
    }};


    public void makeStegos()
    {
        String message = String.format("Doing Scene %03d/%03d %s No.%d ",
                scene.index, scene.device.scenes.size(),
                isJPEG()?"JPG":"DNG", index);
        for (Map.Entry<String, List<DBStego>> entry : stegos.entrySet())
        {
            String appName = entry.getKey();

            if (appsToSkip.contains(appName))
                continue;

            List<DBStego> appStegos = entry.getValue();

            logProgress(message + MainScript.getAbbrAppName(appName)+". Space = "+P.getRemainingStorage());

            if (allExist(appStegos))
                continue;


            File input = appName.equals(PixelKnot.FullName)? original : png;
            if (!input.exists())
            {
                P.e("no input for" + appName+": "+input.getAbsolutePath());
                continue;
            }

            // appStegos have 6 members, first 1 is the cover, next 5 are stegos
            long time = System.currentTimeMillis();
            switch (appName)
            {
                case PixelKnot.FullName:
                    PixelKnot.makeStegos(input, appStegos);
                    break;
                case MobiStego.FullName:
                    MobiStego.makeStegos(input, appStegos);
                    break;
                case PocketStego.FullName:
                    PocketStego.makeStegos(input, appStegos);
                    break;
                case SteganographyM.FullName:
                    SteganographyM.makeStegos(input, appStegos);
                    break;
            }
            time = (System.currentTimeMillis()-time)/1000;
            P.i("Embedding time: "+time+" seconds.");
        }
    }

    public boolean isJPEG()
    {
        return F.getFileExt(original).toLowerCase().equals("jpg");
    }

    public boolean isDNG()
    {
        return F.getFileExt(original).toLowerCase().equals("dng");
    }

    private void logProgress(String message)
    {
        Log.i("wenhaoc_progress", message);
        P.i(message);
    }
    private boolean allExist(List<DBStego> appStegos)
    {
        for (DBStego stego : appStegos)
            if (!stego.exists())
                return false;
        return true;
    }
}
