package wenhaoc.stegodb.stegoappscripts;

public class TEMP {


    public static void tempTest() {
        int[][] intArr = new int[6][7];
        Integer[][][] integerArr = new Integer[4][6][7];
        float[][] floatArr = new float[5][3];
        Float[][] floatBigArr = new Float[5][3];

        for (int i=0; i<10; i++)
            floatBigArr[i][2] = floatArr[i][1] = intArr[i][0] = integerArr[i][0][0] = i*123;
    }

}
