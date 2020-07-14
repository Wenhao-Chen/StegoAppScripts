package wenhaoc.stegodb.stegoappscripts.apps_active.pixelknot.encode;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.PrintWriter;

import wenhaoc.stegodb.stegoappscripts.apps_active.pixelknot.common.Util;


/**
 * Created by C03223-Stego2 on 7/21/2017.
 */

public class ImageData {

    public String path;
    public int quality;

    public int[] pixels;
    public int width, height;
    public float[][] y, cb, cr;

    public int[] coefficients;
    private int coeff_index;
    public int ones, zeros, larges, capacity;
    public int[] onesYCC, zerosYCC, largesYCC, capacityYCC;

    private DCT dct;

    public static PrintWriter out;


    public ImageData(String path, DCT dct)
    {
        this.path = path;
        File imageFile = new File(path);
        Bitmap bitmap = Util.loadImageForPixelKnot(imageFile);
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        //Log.i("JPEG-STEGO", "cover image size: " + width+"/"+height);
        pixels = new int[width*height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        this.dct = dct;
        getYCCArray();
        getCoefficients();
        countCapacities();
    }

    void countCapacities()
    {
        ones = zeros = 0;
        onesYCC = new int[] {0,0,0};
        zerosYCC = new int[] {0,0,0};
        largesYCC = new int[3];
        capacityYCC = new int[3];
        int coeffCounts[] = new int[] {coefficients.length*2/3, coefficients.length/6, coefficients.length/6};
        for (int i = 0; i < coefficients.length; i++)
        {
            if (i % 64 == 0)
                continue;
            int YCCindex = -1;
            int loc = i%384;
            if (loc<256)
                YCCindex = 0;
            else if (loc<320)
                YCCindex = 1;
            else
                YCCindex = 2;
            int coeff = coefficients[i];
            if (coeff == 0)
            {
                zerosYCC[YCCindex]++;
                zeros++;
            }
            else if (coeff == 1 || coeff == -1)
            {
                onesYCC[YCCindex]++;
                ones++;
            }
        }
        larges = coefficients.length - ones - zeros - coefficients.length/64;
        capacity = larges + (int) (0.49 * ones);
        for (int i = 0; i < 3; i++)
        {
            largesYCC[i] = coeffCounts[i]-onesYCC[i]-zerosYCC[i]-coeffCounts[i]/64;
            capacityYCC[i] = largesYCC[i]+(int)(0.49*onesYCC[i]);
        }
        //Log.i("JPEG-STEGO", "total/ones/larges/capa = " + coefficients.length+"/"+ones+"/"+larges+"/"+capacity);
/*        if (out != null)
        {
            out.write("End of Coeffs\n"+ones+","+larges+","+capacity);
        }*/
    }

    private void getCoefficients()
    {
        // each block is 8*8
        int blockRows = (int) Math.ceil(cb.length/8.0);
        int blockColumns = (int) Math.ceil(cb[0].length/8.0);
        // Y is sampled 4 times, Cb Cr sampled once
        coefficients = new int[blockRows*blockColumns*64*6];
        //Log.i("JPEG-STEGO", "i/j = " + blockRows+"/"+blockColumns);
        //Log.i("JPEG-STEGO", "Coeff count: " + coefficients.length);
        coeff_index = 0;
        // Y has twice height and width, so 2*2 blocks are sampled each time
        // Cb and Cr sample one block each iteration
        for (int r = 0; r < blockRows; r++)
        {
            for (int c = 0; c < blockColumns; c++)
            {
                int x0 = c*8;
                int y0 = r*8;
                //Q table numbers: Q(Y,Cb,Cr) = {0,1,1}
                addCoefficients(y, y0*2, x0*2, 0);
                addCoefficients(y, y0*2, x0*2+8, 0);
                addCoefficients(y, y0*2+8, x0*2, 0);
                addCoefficients(y, y0*2+8, x0*2+8, 0);
                addCoefficients(cb, y0, x0, 1);
                addCoefficients(cr, y0, x0, 1);
            }
        }

    }

    private int[][] addCoefficients(float[][] input, int y0, int x0, int q)
    {
        float[][] dctArray1 = new float[8][8];
        for (int a = 0; a < 8; a++)
        {
            for (int b = 0; b < 8; b++)
            {
                int ia = y0+a;
                int ib = x0+b;
                if (ia >= input.length)
                    ia = input.length-1;
                if (ib >= input[0].length)
                    ib = input[0].length-1;
                dctArray1[a][b] = input[ia][ib];
            }
        }
        double[][] dctArray2 = dct.forwardDCT(dctArray1);
        int[] dctArray3 = dct.quantizeBlock(dctArray2, q);
        System.arraycopy(dctArray3, 0, coefficients, coeff_index, 64);
/*
        if (out != null)
        {
            for (int co : dctArray3)
                out.write(co+",");
            out.write("\n");
        }
*/

        coeff_index += 64;
        return null;
    }

    private void getYCCArray()
    {
        // sample factors: F(Y,Cb,Cr) = {2,1,1}
        int sampledWidth = (int) Math.ceil(width/8.0)*8;
        int sampledHeight = (int) Math.ceil(height/8.0)*8;
        //Log.i("JPEG-STEGO", "sample w/h = " + sampledWidth+"/"+sampledHeight);
        y = new float[sampledHeight][sampledWidth];
        cb = new float[sampledHeight/2][sampledWidth/2];
        cr = new float[sampledHeight/2][sampledWidth/2];

        float[][] cb_raw = new float[sampledHeight][sampledWidth];
        float[][] cr_raw = new float[sampledHeight][sampledWidth];
        int index = 0;
        for (int h = 0; h < height; h++)
        {
            for (int w = 0; w < width; w++)
            {
                int r = pixels[index] >> 16 & 0xff;
                int g = pixels[index] >> 8 & 0xff;
                int b = pixels[index] & 0xff;
                y[h][w] = (float) (0.299 * r + 0.587 * g + 0.114 * b);
                cb_raw[h][w] = 128 + (float) (-0.16874 * r - 0.33126 * g + 0.5 * b);
                cr_raw[h][w] = 128 + (float) (0.5 * r - 0.41869 * g - 0.08131 * b);
                index++;
            }
        }
        cb = downSample(cb_raw, sampledHeight/2, sampledWidth/2);
        cr = downSample(cr_raw, sampledHeight/2, sampledWidth/2);
/*        try
        {
            File dir = new File("/sdcard/Download/testPixelKnot");
            dir.mkdirs();
            PrintWriter outY = new PrintWriter(new FileWriter(new File(dir, "new_Y.txt")));
            for (float[] ff : y)
            {
                for (float f : ff)
                    outY.write(f + ",");
                outY.write("\n");
            }
            outY.close();
            PrintWriter outCb = new PrintWriter(new FileWriter(new File(dir, "new_Cb.txt")));
            for (float[] ff : cb)
            {
                for (float f : ff)
                    outCb.write(f + ",");
                outCb.write("\n");
            }
            outCb.close();
            PrintWriter outCr = new PrintWriter(new FileWriter(new File(dir, "new_Cr.txt")));
            for (float[] ff : cr)
            {
                for (float f : ff)
                    outCr.write(f + ",");
                outCr.write("\n");
            }
            outCr.close();
            PrintWriter outPixels = new PrintWriter(new FileWriter(new File(dir, "new_Pixel.txt")));
            int count = 0;
            for (int p : pixels)
            {
                outPixels.write(p+",");
                count++;
                if (count%width==0)
                    outPixels.write("\n");
            }
            outPixels.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }*/
    }

    private float[][] downSample(float[][] input, int h, int w)
    {
        float[][] output = new float[h][w];
        int bias;
        int inrow = 0, incol = 0;
        for (int outrow = 0; outrow < h; outrow++)
        {
            bias = 1;
            for (int outcol = 0; outcol < w; outcol++)
            {
                float temp = input[inrow][incol++]; // 00
                temp += input[inrow++][incol--]; // 01
                temp += input[inrow][incol++]; // 10
                temp += input[inrow--][incol++] + bias; // 11 -> 02
                output[outrow][outcol] = temp / (float) 4.0;
                bias ^= 3; // the value of bias toggles from {1,2}
            }
            inrow += 2;
            incol = 0;
        }
        return output;
    }

}
