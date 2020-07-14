package wenhaoc.stegodb.stegoappscripts.database;

import java.io.File;

public class DBStego {


    public File stegoImage, statsFile;
    public float embeddingRate;
    public boolean isCover;

    public boolean exists()
    {
        if (isCover)
            return stegoImage.exists();
        return stegoImage.exists()&&statsFile.exists();
    }


}
