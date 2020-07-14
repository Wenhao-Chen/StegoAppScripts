package wenhaoc.stegodb.stegoappscripts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wenhaoc.stegodb.stegoappscripts.apps_active.mobistego.MobiStego;
import wenhaoc.stegodb.stegoappscripts.apps_active.pixelknot.PixelKnot;
import wenhaoc.stegodb.stegoappscripts.apps_active.pocketstego.PocketStego;
import wenhaoc.stegodb.stegoappscripts.apps_active.steganography_Meznik.SteganographyM;
import wenhaoc.stegodb.stegoappscripts.database.DBDevice;
import wenhaoc.stegodb.stegoappscripts.database.DBImage;
import wenhaoc.stegodb.stegoappscripts.database.DBScene;
import wenhaoc.stegodb.stegoappscripts.database.DBStego;

public class MainScript {

    public static final String[] AvtiveStegoApps;
    private static final Map<String, String> AppAbbrNames;
    public static final Set<String> spatialApps;
    public static final Set<String> jpegApps;

    static
    {
        AvtiveStegoApps = new String[]{MobiStego.FullName, PixelKnot.FullName, PocketStego.FullName, SteganographyM.FullName};
        AppAbbrNames = new HashMap<String, String>() {{
            put(MobiStego.FullName, MobiStego.AbbrName);
            put(PixelKnot.FullName, PixelKnot.AbbrName);
            put(PocketStego.FullName, PocketStego.AbbrName);
            put(SteganographyM.FullName, SteganographyM.AbbrName);
        }};
        spatialApps = new HashSet<String>() {{
            add(MobiStego.FullName);
            add(PocketStego.FullName);
            add(SteganographyM.FullName);
        }};
        jpegApps = new HashSet<String>() {{
            add(PixelKnot.FullName);
        }};
    }

    public static String getAbbrAppName(String fullName)
    {
        if (AppAbbrNames.containsKey(fullName))
            return AppAbbrNames.get(fullName);
        return fullName;
    }




}
