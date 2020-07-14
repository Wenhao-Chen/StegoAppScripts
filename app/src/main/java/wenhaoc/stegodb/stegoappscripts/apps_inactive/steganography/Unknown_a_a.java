package wenhaoc.stegodb.stegoappscripts.apps_inactive.steganography;

import android.util.Log;

/**
 * Created by C03223-Stego2 on 12/20/2017.
 */

public class Unknown_a_a {

/**
 encoded long for v4[0]=3530822107858468864
 encoded long for v4[1]=14073748835532800
 current OR value=3544895856694001664
 encoded long for v4[2]=56075093016576
 current OR value=3544951931787018240
 encoded long for v4[3]=223338299392
 current OR value=3544952155125317632
 encoded long for v4[4]=889192448
 current OR value=3544952156014510080
 encoded long for v4[5]=3538944
 current OR value=3544952156018049024
 encoded long for v4[6]=14080
 current OR value=3544952156018063104
 encoded long for v4[7]=56
 current OR value=3544952156018063160
 a.a.b(bytes) = 3544952156018063160
 *
 *
 * */


    public static long Unkonwn_b(byte[] bytes)
    {
        byte[] b = new byte[]{0,0,0,0,0,0,0,0};
        int length = bytes.length<=8?bytes.length:8;
        for (int index = 0; index < length; index++)
        {
            b[index] = bytes[index];
            //Log.d("carrie", "v4["+index+"]="+b[index]);
        }
        long result = 0;
        int[] ints = new int[8];
        long[] longs = new long[8];
        long[] shiftedLongs = new long[8];
        int[] bitsToShift = new int[] {0x38, 0x30, 0x28, 0x20, 0x18, 0x10, 0x8, 0};
        for (int i = 0; i < 8; i++)
        {
            longs[i] = (long) (b[i] & 0xff);
            shiftedLongs[i] = longs[i] << bitsToShift[i];
            result |= shiftedLongs[i];
            Log.d("carrie", "byte/int/long/shifted/result = " + b[i]+"/"+ints[i]+"/"+ longs[i]+"/"+ shiftedLongs[i]+"/"+result);
        }
/*        long l0 = (long)(0xff+(int)b[0]) << 0x38;
        long l1 = (long)(b[1]+0xff) << 0x30;
        result = l1|l0;
        long l2 = (long)(b[2]+0xff) << 0x28;
        result = result|l2;
        long l3 = (long)(b[3]+0xff) << 0x20;
        result = result|l3;
        long l4 = (long)(b[4]+0xff) << 0x18;
        result = result|l4;
        long l5 = (long)(b[5]+0xff) << 0x10;
        result = result|l5;
        long l6 = (long)(b[6]+0xff) << 0x8;
        result = l6|result;
        long l7 = (long)(b[7]+0xff);
        result = result|l7;*/
        return result;
    }
}
