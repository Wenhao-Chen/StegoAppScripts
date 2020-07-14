package wenhaoc.stegodb.stegoappscripts.apps_inactive.davinci;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

/**
 * Created by C03223-Stego2 on 7/11/2017.
 */

public class DavinciExtract {

    public static String extract(String stego_path)
    {
        Bitmap bitmap = BitmapFactory.decodeFile(stego_path);
        CursorXY cur = new CursorXY();
        String first_segment = extractNextSegment(bitmap, cur);
        if (first_segment.equals("t2ip"))
        {
            String password = extractNextSegment(bitmap, cur);
            return extractNextSegment(bitmap, cur);
        }
        else if (first_segment.equals("t2i"))
        {
            return extractNextSegment(bitmap, cur);
        }
        else
        {
            Log.e("WENHAOCHEN", "do not recognize first segment. expected \"t2ip\" or \"t2i\"");
        }
        return "";
    }

    private static String extractNextSegment(Bitmap bitmap, CursorXY cur)
    {
        int length = Integer.parseInt(readBits(bitmap, cur, 32), 2);
        Log.d("WENHAOCHEN","length="+length);
        String message = readBits(bitmap, cur, length*8);
        String converted = "";
        for (int i = 0; i <= message.length()-8; i+=8)
        {
            int k = Integer.parseInt(message.substring(i, i+8), 2);
            converted += (char) k;
        }
        Log.d("WENHAOCHEN", converted);
        return converted;
    }

    private static String readBits(Bitmap bitmap, CursorXY cur, int num_bits)
    {
        String result = "";
        for (int i = 0; i < num_bits; i++)
        {
            int color = bitmap.getPixel(cur.x, cur.y);
            int alpha = Color.alpha(color);
            result += alpha==0xfe?"0":"1";
            cur.x++;
            if(cur.x == bitmap.getWidth())
            {
                cur.x = 0;
                cur.y++;
            }
        }
        return result;
    }


}
