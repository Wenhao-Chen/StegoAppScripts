package wenhaoc.stegodb.stegoappscripts.apps_active.pixelknot.decode;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by C03223-Stego2 on 7/22/2017.
 */

public class HuffTable {

    // Instance variables
    private final int[] BITS = new int[17];

    private final int[] HUFFVAL = new int[256];

    private final int[] HUFFCODE = new int[257];

    private final int[] HUFFSIZE = new int[257];

    private final int[] EHUFCO = new int[257];

    private final int[] EHUFSI = new int[257];

    private final int[] MINCODE = new int[17];

    private final int[] MAXCODE = new int[18];

    private final int[] VALPTR = new int[17];

    private final int Ln;

    private int SI, I, J, K, LASTK, CODE;

    // Declare input steam
    DataInputStream dis;

    // Constructor Method
    public HuffTable(final DataInputStream d, final int l) {
        this.dis = d;
        // System.out.println("Lï¿½nge="+l);
        // Get table data from input stream
        this.Ln = 19 + getTableData();
        // System.out.println(Ln);
        Generate_size_table(); // Flow Chart C.1
        Generate_code_table(); // Flow Chart C.2
        Order_codes(); // Flow Chart C.3
        Decoder_tables(); // Generate decoder tables Flow Chart F.15
    }

    private void Decoder_tables() {
        // Decoder table generation Flow Chart F.15
        this.I = 0;
        this.J = 0;
        while (true) {
            if (++this.I > 16)
                return;

            if (this.BITS[this.I] == 0) {
                this.MAXCODE[this.I] = -1;
            } else {
                this.VALPTR[this.I] = this.J;
                this.MINCODE[this.I] = this.HUFFCODE[this.J];
                this.J = this.J + this.BITS[this.I] - 1;
                this.MAXCODE[this.I] = this.HUFFCODE[this.J++];
            }
        }
    }

    private void Generate_code_table() {
        // Generate Code table Flow Chart C.2
        this.K = 0;
        this.CODE = 0;
        this.SI = this.HUFFSIZE[0];
        while (true) {
            this.HUFFCODE[this.K++] = this.CODE++;

            if (this.HUFFSIZE[this.K] == this.SI) {
                continue;
            }

            if (this.HUFFSIZE[this.K] == 0) {
                break;
            }

            while (true) {
                this.CODE <<= 1;
                this.SI++;
                if (this.HUFFSIZE[this.K] == this.SI) {
                    break;
                }
            }
        }
    }

    private void Generate_size_table() {
        // Generate HUFFSIZE table Flow Chart C.1
        this.K = 0;
        this.I = 1;
        this.J = 1;
        while (true) {
            if (this.J > this.BITS[this.I]) {
                this.J = 1;
                this.I++;
                if (this.I > 16) {
                    break;
                }
            } else {
                this.HUFFSIZE[this.K++] = this.I;
                this.J++;
            }
        }
        this.HUFFSIZE[this.K] = 0;
        this.LASTK = this.K;
    }

    private int getByte() {
        try {
            return this.dis.readUnsignedByte();
        } catch (final IOException e) {
            return -1;
        }
    }

    // IO MethodS
    public int[] getHUFFVAL() {
        return this.HUFFVAL;
    }

    public int getLen() {
        return this.Ln;
    }

    public int[] getMAXCODE() {
        return this.MAXCODE;
    }

    public int[] getMINCODE() {
        return this.MINCODE;
    }

    private int getTableData() {
        // Get BITS list
        int count = 0;
        for (int x = 1; x < 17; x++) {
            this.BITS[x] = getByte();
            count += this.BITS[x];
        }

        // Read in HUFFVAL
        for (int x = 0; x < count; x++) {
            // System.out.println(Ln);
            this.HUFFVAL[x] = getByte();
        }
        return count;
    }

    public int[] getVALPTR() {
        return this.VALPTR;
    }

    private void Order_codes() {
        // Order Codes Flow Chart C.3
        this.K = 0;

        while (true) {
            this.I = this.HUFFVAL[this.K];
            this.EHUFCO[this.I] = this.HUFFCODE[this.K];
            this.EHUFSI[this.I] = this.HUFFSIZE[this.K++];
            if (this.K >= this.LASTK) {
                break;
            }
        }
    }
}
