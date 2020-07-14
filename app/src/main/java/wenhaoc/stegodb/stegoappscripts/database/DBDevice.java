package wenhaoc.stegodb.stegoappscripts.database;

import android.app.Activity;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import wenhaoc.stegodb.stegoappscripts.util.F;
import wenhaoc.stegodb.stegoappscripts.MainScript;
import wenhaoc.stegodb.stegoappscripts.util.P;

public class DBDevice {

    public static Activity activity;

    public static final File defaultRoot = new File("/sdcard/Download/StegoDB_March2019");
    public static final File goodImagesF = new File(defaultRoot, "GoodOriginals.txt");


    public File deviceDir, originalsDir, pngDir, stegosDir;

    public Map<String, DBScene> scenes;

    public DBDevice()
    {
        this(defaultRoot);
    }

    public DBDevice(File dir)
    {
        this.deviceDir = dir;
        this.originalsDir = new File(deviceDir, "originals");
        this.originalsDir.mkdirs();
        this.pngDir = new File(deviceDir, "cropped");
        this.pngDir.mkdirs();
        this.stegosDir = new File(deviceDir, "stegos");
        this.stegosDir.mkdirs();

        for (String app : MainScript.AvtiveStegoApps)
        {
            File appDir = new File(stegosDir, app);
            appDir.mkdirs();
        }

        init();
    }

    private void init()
    {
        scenes = new TreeMap<>();
        if (!goodImagesF.exists())
        {
            String msg = "ERROR: "+goodImagesF.getAbsolutePath()+" does not exist!";
            P.e(msg);
            if (activity != null)
            {
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            }
            return;
        }

        List<String> list = F.readLines(goodImagesF);
/*        if (list.size()<2000)
        {
            String msg = "ERROR: has only " + list.size()+" images, should be at least 2000";
            P.e(msg);
            if (activity != null)
            {
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            }
            return;
        }*/
        Set<String> goodOriginals = new HashSet<>();
        for (String name : list)
        {
            if (name.isEmpty())
                continue;
            goodOriginals.add(name);
        }

        List<File> toDelete = new ArrayList<>();
        for (File f : originalsDir.listFiles())
        {
            if (f.isDirectory())
            {
                P.e("This folder is in the wrong place: " + f.getAbsolutePath());
                continue;
            }
            String ext = F.getFileExt(f).toLowerCase();
            if (!ext.equals("jpg") && !ext.equals("dng"))
            {
                P.e("This file shouldn't be here: " + f.getAbsolutePath());
                continue;
            }
            String[] parts = f.getName().split("_");
            if (parts.length==7) // remove scene label
            {
                String[] newParts = new String[6];
                System.arraycopy(parts, 0, newParts, 0, 5);
                newParts[5] = parts[6];
                parts = newParts;
            }
            String sceneID = parts[1];
            String[] sceneIDParts = sceneID.split("-");
            if (sceneIDParts.length==4) // remove scene index
            {
                parts[1] = sceneIDParts[0]+"-"+sceneIDParts[1]+"-"+sceneIDParts[2];
            }
            String newName = String.join("_", parts).replace(".JPG", ".jpg").replace(".DNG", ".dng");
            if (!f.getName().equals(newName))
            {
                File newF = new File(f.getParentFile(), newName);
                f.renameTo(newF);
                if (!addImageToScene(newF, goodOriginals))
                    toDelete.add(newF);
            }
            else
            {
                if (!addImageToScene(f, goodOriginals))
                    toDelete.add(f);
            }

        }

        if (goodOriginals.size()>0)
        {
            P.e("There are "+goodOriginals.size()+" original images not found. Fix it first.");
            return;
        }

/*        P.i("Useless original images to delete: " + toDelete.size());
        for (File f : toDelete)
            f.delete();*/
    }



    private boolean addImageToScene(File f, Set<String> goodOriginals)
    {
        P.i("checking "+f.getName());
        if (!goodOriginals.contains(f.getName()))
            return false;
        goodOriginals.remove(f.getName());

        String[] parts = f.getName().split("_");
        String sceneID = parts[1];
        int imageIndex = Integer.parseInt(parts[2].substring(4));

        if (!scenes.containsKey(sceneID))
            scenes.put(sceneID, new DBScene(sceneID, this, scenes.size()+1));

        DBScene scene = scenes.get(sceneID);
        scene.addImage(f, imageIndex);

        return true;
    }

    public void makeStegos()
    {
        List<DBScene> ss = new ArrayList<>(scenes.values());
        Collections.sort(ss, new Comparator<DBScene>() {
            @Override
            public int compare(DBScene s1, DBScene s2)
            {
                return s1.index - s2.index;
            }
        });
        for (DBScene s : ss)
            s.makeStegos();
    }
}
