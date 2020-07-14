package wenhaoc.stegodb.stegoappscripts.apps_active.pixelknot.encode;

/**
 * Created by C03223-Stego2 on 7/21/2017.
 */
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class JpegEncoder {

    private static int[] jpegNaturalOrder = {
            0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5, 12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13, 6, 7,
            14, 21, 28, 35, 42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37, 44, 51, 58, 59, 52, 45, 38, 31, 39, 46,
            53, 60, 61, 54, 47, 55, 62, 63, };


    public static void compressJpeg(String outPath, ImageData image, DCT dct)
    {
        try
        {
            Huffman huf = new Huffman();
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outPath));
            JpegEncoder.WriteHeaders(out, image, dct, huf);
            JpegEncoder.writeBody(out, image, huf);
            JpegEncoder.WriteEOI(out);
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void writeBody(BufferedOutputStream out, ImageData image, Huffman huf)
    {
        try
        {
            int coeff_index = 0;
            int blockRows = image.cb.length/8;
            int blockColumns = image.cb[0].length/8;
            int[] dctArray3 = new int[64];
            int lastDCValue_y = 0, lastDCValue_cb = 0, lastDCValue_cr = 0;
            // DC table number: DC(Y,Cb,Cr) = {0,1,1}
            // AC table number: AC(Y,Cb,Cr) = {0,1,1}
            for (int r = 0; r < blockRows; r++)
            {
                for (int c = 0; c < blockColumns; c++)
                {
                    // read coefficients in the same order they were written:
                    // 4 times Y, then 1 Cb, then 1 Cr.
                    //  - 4 times Y
                    for (int i = 0; i < 4; i++)
                    {
                        System.arraycopy(image.coefficients, coeff_index, dctArray3, 0, 64);
                        huf.HuffmanBlockEncoder(out, dctArray3, lastDCValue_y, 0, 0);
                        lastDCValue_y = dctArray3[0];
                        coeff_index += 64;
                    }
                    //  - 1 time Cb
                    System.arraycopy(image.coefficients, coeff_index, dctArray3, 0, 64);
                    huf.HuffmanBlockEncoder(out, dctArray3, lastDCValue_cb, 1, 1);
                    lastDCValue_cb = dctArray3[0];
                    coeff_index += 64;
                    //  - 1 time Cr
                    System.arraycopy(image.coefficients, coeff_index, dctArray3, 0, 64);
                    huf.HuffmanBlockEncoder(out, dctArray3, lastDCValue_cr, 1, 1);
                    lastDCValue_cr = dctArray3[0];
                    coeff_index += 64;
                }
            }
            huf.flushBuffer(out);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void WriteEOI(BufferedOutputStream out)
    {
        byte[] EOI = {
                (byte) 0xFF, (byte) 0xD9 };
        WriteMarker(EOI, out);
    }

    private static void WriteHeaders(BufferedOutputStream out, ImageData image, DCT dct, Huffman huf)
    {
        int i, j, index, offset;
        int tempArray[];

        // the SOI marker
        final byte[] SOI = {
                (byte) 0xFF, (byte) 0xD8 };
        WriteMarker(SOI, out);

        // The order of the following headers is quiet inconsequential.
        // the JFIF header

        final byte JFIF[] = new byte[18];
        JFIF[0] = (byte) 0xff; // app0 marker
        JFIF[1] = (byte) 0xe0;
        JFIF[2] = (byte) 0x00; // length
        JFIF[3] = (byte) 0x10;
        JFIF[4] = (byte) 0x4a; // "JFIF"
        JFIF[5] = (byte) 0x46;
        JFIF[6] = (byte) 0x49;
        JFIF[7] = (byte) 0x46;
        JFIF[8] = (byte) 0x00;
        JFIF[9] = (byte) 0x01; // 1.01
        JFIF[10] = (byte) 0x01;
        JFIF[11] = (byte) 0x00;
        JFIF[12] = (byte) 0x00;
        JFIF[13] = (byte) 0x01;
        JFIF[14] = (byte) 0x00;
        JFIF[15] = (byte) 0x01;
        JFIF[16] = (byte) 0x00;
        JFIF[17] = (byte) 0x00;

        // the github source code has commented this out, but the APK version still writes the JFIF.
        // Either way, the image data remains the same.
        WriteArray(JFIF, out);

		/*
        if (this.JpegObj.getComment().equals("JPEG Encoder Copyright 1998, James R. Weeks and BioElectroMech.  ")) {
            JFIF[10] = (byte) 0x00; // 1.00
        }
		 */

        // The DQT header
        // 0 is the luminance index and 1 is the chrominance index
        final byte DQT[] = new byte[134];
        DQT[0] = (byte) 0xFF;
        DQT[1] = (byte) 0xDB;
        DQT[2] = (byte) 0x00;
        DQT[3] = (byte) 0x84;
        offset = 4;
        for (i = 0; i < 2; i++) {
            DQT[offset++] = (byte) ((0 << 4) + i);
            tempArray = (int[]) dct.quantum[i];
            for (j = 0; j < 64; j++) {
                DQT[offset++] = (byte) tempArray[jpegNaturalOrder[j]];
            }
        }
        WriteArray(DQT, out);

        // Start of Frame Header
        final byte SOF[] = new byte[19];
        SOF[0] = (byte) 0xFF;
        SOF[1] = (byte) 0xC0;
        SOF[2] = (byte) 0x00;
        SOF[3] = (byte) 17;
        SOF[4] = (byte) 8; // precision
        SOF[5] = (byte) (image.height >> 8 & 0xFF);
        SOF[6] = (byte) (image.height & 0xFF);
        SOF[7] = (byte) (image.width >> 8 & 0xFF);
        SOF[8] = (byte) (image.width & 0xFF);
        SOF[9] = (byte) 3; // number of components
        // Y
        SOF[10] = (byte) 1; // component number
        SOF[11] = (byte) ((2 << 4) + 2); // sample factor
        SOF[12] = (byte) 0; // q table number
        // Cb
        SOF[13] = (byte) 2; // component number
        SOF[14] = (byte) ((1 << 4) + 1); // sample factor
        SOF[15] = (byte) 1; // q table number
        // Cr
        SOF[16] = (byte) 3; // component number
        SOF[17] = (byte) ((1 << 4) + 1); // sample factor
        SOF[18] = (byte) 1; // q table number
        WriteArray(SOF, out);

        // The DHT Header
        byte DHT1[], DHT2[], DHT3[], DHT4[];
        int bytes, temp, oldindex, intermediateindex;
        index = 4;
        oldindex = 4;
        DHT1 = new byte[17];
        DHT4 = new byte[4];
        DHT4[0] = (byte) 0xFF;
        DHT4[1] = (byte) 0xC4;
        for (i = 0; i < 4; i++) {
            bytes = 0;
            DHT1[index++ - oldindex] = (byte) huf.bits.elementAt(i)[0];
            for (j = 1; j < 17; j++) {
                temp = huf.bits.elementAt(i)[j];
                DHT1[index++ - oldindex] = (byte) temp;
                bytes += temp;
            }
            intermediateindex = index;
            DHT2 = new byte[bytes];
            for (j = 0; j < bytes; j++) {
                DHT2[index++ - intermediateindex] = (byte) huf.val.elementAt(i)[j];
            }
            DHT3 = new byte[index];
            java.lang.System.arraycopy(DHT4, 0, DHT3, 0, oldindex);
            java.lang.System.arraycopy(DHT1, 0, DHT3, oldindex, 17);
            java.lang.System.arraycopy(DHT2, 0, DHT3, oldindex + 17, bytes);
            DHT4 = DHT3;
            oldindex = index;
        }
        DHT4[2] = (byte) (index - 2 >> 8 & 0xFF);
        DHT4[3] = (byte) (index - 2 & 0xFF);
        WriteArray(DHT4, out);

        // Start of Scan Header
        final byte SOS[] = new byte[14];
        SOS[0] = (byte) 0xFF;
        SOS[1] = (byte) 0xDA;
        SOS[2] = (byte) 0x00;
        SOS[3] = (byte) 12;
        SOS[4] = (byte) 3; // number of components
        // Y
        SOS[5] = (byte) 1; // id
        SOS[6] = (byte) ((0 << 4) + 0); // dc and ac table number
        // Cb
        SOS[7] = (byte) 2; // id
        SOS[8] = (byte) ((1 << 4) + 1); // dc and ac table number
        // Cr
        SOS[9] = (byte) 3; // id
        SOS[10] = (byte) ((1 << 4) + 1); // dc and ac table number

        SOS[11] = (byte) 0; // Ss
        SOS[12] = (byte) 63; // Se
        SOS[13] = (byte) ((0 << 4) + 0); // Ah and Al
        WriteArray(SOS, out);

    }

    private static void WriteMarker(final byte[] data, final BufferedOutputStream out)
    {
        try
        {
            out.write(data, 0, 2);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void WriteArray(final byte[] data, final BufferedOutputStream out)
    {
        try
        {
            int length = ((data[2] & 0xFF) << 8) + (data[3] & 0xFF) + 2;
            out.write(data, 0, length);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



}