package wenhaoc.stegodb.stegoappscripts.database;

import java.io.File;
import java.util.List;
import java.util.Map;

import wenhaoc.stegodb.stegoappscripts.util.F;
import wenhaoc.stegodb.stegoappscripts.util.P;

public class DBScene {

    public String id;
    public int index;
    public DBDevice device;
    public DBImage[] raw_images, jpeg_images;

    DBScene(String id, DBDevice device, int index)
    {
        this.id = id;
        this.device = device;
        this.index = index;
        raw_images = new DBImage[10];
        jpeg_images = new DBImage[10];
    }

    public void addImage(File original, int imageIndex)
    {
        String ext = F.getFileExt(original).toLowerCase();
        if (ext.equals("jpg"))
            jpeg_images[imageIndex] = new DBImage(original, this, imageIndex);
        else if (ext.equals("dng"))
            raw_images[imageIndex] = new DBImage(original, this, imageIndex);
        else
            P.e("trying to add this file as original image: " + original.getAbsolutePath());
    }

    public void makeStegos()
    {
        for (DBImage image : jpeg_images)
        {
            image.makeStegos();
        }
        for (DBImage image : raw_images)
        {
            image.makeStegos();
        }
    }
}
