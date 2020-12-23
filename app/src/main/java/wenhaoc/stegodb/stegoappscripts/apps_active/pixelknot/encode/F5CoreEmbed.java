package wenhaoc.stegodb.stegoappscripts.apps_active.pixelknot.encode;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;

import wenhaoc.stegodb.stegoappscripts.apps_active.pixelknot.common.F5Random;
import wenhaoc.stegodb.stegoappscripts.apps_active.pixelknot.common.Permutation;


/**
 * Created by C03223-Stego2 on 7/21/2017.
 */

public class F5CoreEmbed {

    DCT dct;
    //Huffman huf;
    public ImageData image;
    int permutation_index;
    public int[] original_coefficients;

    public int f5_k, f5_codeN, f5_embedded, f5_changed, f5_thrown;
    public int changed_y, changed_cb, changed_cr;
    public int thrown_y, thrown_cb, thrown_cr;

    public static int defaultQuality = 90;

    public F5CoreEmbed(File cover, int quality)
    {
        this(cover.getAbsolutePath(), quality);
    }

    public F5CoreEmbed(String cover_path, int quality)
    {
        dct = new DCT(quality);
        //huf = new Huffman();
        image = new ImageData(cover_path, dct);
        original_coefficients = image.coefficients.clone();
    }

    public F5CoreEmbed(File cover)
    {
        this(cover, defaultQuality);
    }

    public F5CoreEmbed(String input_path)
    {
        this(input_path, defaultQuality);
    }

    public void saveIntermediateCover(String outPath)
    {
        image.coefficients = original_coefficients.clone();
        JpegEncoder.compressJpeg(outPath, image, dct);
    }

    ByteArrayInputStream data;
    F5Random random;
    public int kBits(int k) {
        // Permutation must be initiated before status word calling "random.getBytes()"
        Permutation permutation = new Permutation(image.coefficients.length, random);
        int statusWord = data.available();
        f5_k = k;
        f5_codeN = (1 << f5_k) - 1;
        int availableBits = 0;
        boolean isLastByte = false;
        int currentByte = 0;
        int kBits = 0;
        for (int i = 0; i < f5_k; i++) {
            if (availableBits == 0) {
                if (data.available()==0)
                {
                    isLastByte = true;
                    break;
                }
                currentByte = data.read();
                currentByte ^= random.getNextByte();
                //P.print("need " +(image.f5_k-i) + " more bits. new byte: " + Integer.toBinaryString(currentByte));
                availableBits = 8;
            }
            int nextBit = currentByte & 1;
            currentByte >>= 1;
            availableBits--;
            kBits |= nextBit << i;
            f5_embedded++;
        }
        return kBits;
    }

    public int nCoeffs(int n) {
        f5_codeN = n;
        Permutation permutation = new Permutation(image.coefficients.length, random);
        int[] codeWord = new int[f5_codeN];
        for (int i = 0; i < f5_codeN; permutation_index++)
        {
            int shuffledIndex = permutation.getShuffled(permutation_index);
            if (shuffledIndex % 64 == 0 || image.coefficients[shuffledIndex] == 0)
                continue;
            codeWord[i++] = shuffledIndex;
        }
        int hash = 0;
        for (int i = 0; i < f5_codeN; i++)
        {
            int coeff = image.coefficients[codeWord[i]];
            int extractedBit = coeff>0? coeff&1 : 1-(coeff&1);
            hash ^= i+1;
        }
        return hash;
    }

    public void cellToChange() {
        int c = kBits(2) ^ nCoeffs(3);
    }

    public void tempEmbed(String message, String seed, String outPath) {
        ByteArrayInputStream data = new ByteArrayInputStream(message.getBytes());
        F5Random random = new F5Random(seed.getBytes());
        // Permutation must be initiated before status word calling "random.getBytes()"
        Permutation permutation = new Permutation(image.coefficients.length, random);
        int statusWord = data.available();
        f5_k = getK(image.capacity, statusWord+4);
        f5_codeN = (1 << f5_k) - 1;
        int availableBits = 0;
        boolean isLastByte = false;
        int currentByte = 0;
        embeddingLoop: do
        {
            //steps:
            // 1. get the newest k bits
            int kBits = 0;
            for (int i = 0; i < f5_k; i++) {
                if (availableBits == 0) {
                    if (data.available()==0)
                    {
                        isLastByte = true;
                        break;
                    }
                    currentByte = data.read();
                    currentByte ^= random.getNextByte();
                    //P.print("need " +(image.f5_k-i) + " more bits. new byte: " + Integer.toBinaryString(currentByte));
                    availableBits = 8;
                }
                int nextBit = currentByte & 1;
                currentByte >>= 1;
                availableBits--;
                kBits |= nextBit << i;
                f5_embedded++;
            }
            //P.print("k bits: " + Integer.toBinaryString(kBits));
            // 2. get a code word of n non-zero coefficients
            // 3. encode the k bits to the code word
            // 4. if the action turned a coefficient to 0, go back to step 2; else go to step 1
            int[] codeWord = new int[f5_codeN];
            int cellToChange = 0;
            do {
                //P.print("finding n non-zero coeffs starting from " + permutation_index);
                int startingIndex = permutation_index;
                for (int i = 0; i < f5_codeN; permutation_index++)
                {
                    if (permutation_index >= image.coefficients.length)
                    {
                        Log.d("WENHAOCHEN", "  Capacity exhausted.");
                        break embeddingLoop;
                    }
                    int shuffledIndex = permutation.getShuffled(permutation_index);
                    if (shuffledIndex % 64 == 0 || image.coefficients[shuffledIndex] == 0)
                        continue;
                    codeWord[i++] = shuffledIndex;
                }
                int hash = 0;
                for (int i = 0; i < f5_codeN; i++)
                {
                    int coeff = image.coefficients[codeWord[i]];
                    int extractedBit = coeff>0? coeff&1 : 1-(coeff&1);
                    if (extractedBit == 1)
                        hash ^= i+1;
                }
                //P.print("code word hash = " + Integer.toBinaryString(hash));
                cellToChange = hash ^ kBits;
                if (cellToChange==0) //no changes needed
                    break;
                // make the change
                cellToChange--;
                if (image.coefficients[codeWord[cellToChange]]>0)
                    image.coefficients[codeWord[cellToChange]]--;
                else
                    image.coefficients[codeWord[cellToChange]]++;
                recordChange(codeWord[cellToChange]);
                if (image.coefficients[codeWord[cellToChange]]==0)
                {
                    permutation_index = startingIndex;
                    recordThrow(codeWord[cellToChange]);
                    //P.print("redoing because 0: " + codeWord[cellToChange]);
                }
            }
            while (image.coefficients[codeWord[cellToChange]]==0);
        }
        while (!isLastByte);
    }

    //message and seed are the parameters feed to F5 core, encryption
    // should be done prior to calling this method.
    public void embed(String message, String seed, String outPath)
    {
        image.coefficients = original_coefficients.clone();
        f5_k = f5_changed = f5_embedded = f5_thrown = 0;
        changed_y = changed_cb = changed_cr = 0;
        thrown_y = thrown_cb = thrown_cr = 0;
        image.countCapacities();
        Log.d("WENHAOCHEN", "  Capacity: all/y/c/c = " + image.capacity+"/"+image.capacityYCC[0]+"/"+image.capacityYCC[1]+"/"+image.capacityYCC[2]);
        if (message == null || message.isEmpty())
        {
            Log.d("WENHAOCHEN", "  No message to embed. Compressing unchanged image.");
            JpegEncoder.compressJpeg(outPath, image, dct);
            return;
        }

        ByteArrayInputStream data = new ByteArrayInputStream(message.getBytes());
        F5Random random = new F5Random(seed.getBytes());
        // Permutation must be initiated before status word calling "random.getBytes()"
        Permutation permutation = new Permutation(image.coefficients.length, random);

        // initialize status word with length information(low 23 bits), max length is 0x7fffff
        // the 24th bit of status word is reserved for "future use", hence the "7"
        int statusWord = data.available();
        Log.d("WENHAOCHEN","  Embedding " + (statusWord*8+32)+" bits ("+statusWord+"+4 bytes)");
        if (statusWord > 0x007fffff)
        {
            Log.d("WENHAOCHEN","Embedded data too long. Max length is: 0x7FFFFF.");
            statusWord = 0x007fffff;
        }
        f5_k = getK(image.capacity, statusWord+4);
        f5_codeN = (1 << f5_k) - 1;
        switch (f5_codeN)
        {
            case 0:
                Log.d("WENHAOCHEN","  using default code, file will not fit");
                f5_codeN++;
                break;
            case 1:
                Log.d("WENHAOCHEN","  using default code");
                break;
            default:
                Log.d("WENHAOCHEN","  using (1, " + f5_codeN + ", " + f5_k + ") code");
        }
        // store k in the status word (25-32 bits)
        statusWord ^= f5_k<<24;
        // encrypt status word with first 4 pseudo-random bytes
        statusWord ^= random.getNextByte();
        statusWord ^= random.getNextByte() << 8;
        statusWord ^= random.getNextByte() << 16;
        statusWord ^= random.getNextByte() << 24;

        permutation_index = 0;
        defaultEmbedding(image, permutation, statusWord, 32);
        if (f5_codeN > 1)
        {
            boolean isLastByte = false;
            int currentByte = 0, availableBits = 0;
            embeddingLoop: do
            {
                //steps:
                // 1. get the newest k bits
                int kBits = 0;
                for (int i = 0; i < f5_k; i++)
                {
                    if (availableBits == 0)
                    {
                        if (data.available()==0)
                        {
                            isLastByte = true;
                            break;
                        }
                        currentByte = data.read();
                        currentByte ^= random.getNextByte();
                        //P.print("need " +(image.f5_k-i) + " more bits. new byte: " + Integer.toBinaryString(currentByte));
                        availableBits = 8;
                    }
                    int nextBit = currentByte & 1;
                    currentByte >>= 1;
                    availableBits--;
                    kBits |= nextBit << i;
                    f5_embedded++;
                }
                //P.print("k bits: " + Integer.toBinaryString(kBits));
                // 2. get a code word of n non-zero coefficients
                // 3. encode the k bits to the code word
                // 4. if the action turned a coefficient to 0, go back to step 2; else go to step 1
                int[] codeWord = new int[f5_codeN];
                int cellToChange = 0;
                do
                {
                    //P.print("finding n non-zero coeffs starting from " + permutation_index);
                    int startingIndex = permutation_index;
                    for (int i = 0; i < f5_codeN; permutation_index++)
                    {
                        if (permutation_index >= image.coefficients.length)
                        {
                            Log.d("WENHAOCHEN", "  Capacity exhausted.");
                            break embeddingLoop;
                        }
                        int shuffledIndex = permutation.getShuffled(permutation_index);
                        if (shuffledIndex % 64 == 0 || image.coefficients[shuffledIndex] == 0)
                            continue;
                        codeWord[i++] = shuffledIndex;
                    }
                    int hash = 0;
                    for (int i = 0; i < f5_codeN; i++)
                    {
                        int coeff = image.coefficients[codeWord[i]];
                        int extractedBit = coeff>0? coeff&1 : 1-(coeff&1);
                        if (extractedBit == 1)
                            hash ^= i+1;
                    }
                    //P.print("code word hash = " + Integer.toBinaryString(hash));
                    cellToChange = hash ^ kBits;
                    if (cellToChange==0) //no changes needed
                        break;
                    // make the change
                    cellToChange--;
                    if (image.coefficients[codeWord[cellToChange]]>0)
                        image.coefficients[codeWord[cellToChange]]--;
                    else
                        image.coefficients[codeWord[cellToChange]]++;
                    recordChange(codeWord[cellToChange]);
                    if (image.coefficients[codeWord[cellToChange]]==0)
                    {
                        permutation_index = startingIndex;
                        recordThrow(codeWord[cellToChange]);
                        //P.print("redoing because 0: " + codeWord[cellToChange]);
                    }
                }
                while (image.coefficients[codeWord[cellToChange]]==0);
            }
            while (!isLastByte);
        }
        else
        {
            while (data.available()>0 && permutation_index < image.coefficients.length)
            {
                int byteToEmbed = data.read()^random.getNextByte();
                defaultEmbedding(image, permutation, byteToEmbed, 8);
            }
        }
        Log.d("WENHAOCHEN", "  Embedded: all = " + f5_embedded);
        Log.d("WENHAOCHEN", "  Changed: all/y/cb/cr = " + f5_changed+"/"+changed_y+"/"+changed_cb+"/"+changed_cr);
        Log.d("WENHAOCHEN", "  Thrown: all/y/cb/cr = " + f5_thrown+"/"+thrown_y+"/"+thrown_cb+"/"+thrown_cr);
        JpegEncoder.compressJpeg(outPath, image, dct);
    }



    private void defaultEmbedding(ImageData image, Permutation permutation, int data, int remainingBits)
    {

        while (remainingBits > 0 && permutation_index < image.coefficients.length)
        {
            int currentBit = data & 1;
            int shuffled_index = permutation.getShuffled(permutation_index++);
            int coeff = image.coefficients[shuffled_index];
            // skip DC coefficients and zeros
            if (shuffled_index%64==0 || coeff==0)
                continue;
            // if found a usable coefficient, decrease its absolute value
            if (coeff > 0 && (coeff&1)!=currentBit)
            {
                image.coefficients[shuffled_index]--;
                recordChange(shuffled_index);
            }
            else if (coeff < 0 && (coeff&1)==currentBit)
            {
                image.coefficients[shuffled_index]++;
                recordChange(shuffled_index);
            }
            // if value becomes 0 after change, this change is thrown, need to embed it again
            if (image.coefficients[shuffled_index] != 0)
            {
                f5_embedded++;
                data >>= 1;
                remainingBits--;
            }
            else
            {
                recordThrow(shuffled_index);
            }
        }
        if (permutation_index >= image.coefficients.length)
        {
            Log.d("WENHAOCHEN", "  Capacity exhausted. Couldn't embed full message.");
        }
    }

    private void recordChange(int index)
    {
        f5_changed++;
        int loc = index%384;
        if (loc < 256)
            changed_y++;
        else if (loc <320)
            changed_cb++;
        else
            changed_cr++;
    }


    private void recordThrow(int index)
    {
        f5_thrown++;
        int loc = index%384;
        if (loc < 256)
            thrown_y++;
        else if (loc <320)
            thrown_cb++;
        else
            thrown_cr++;
    }

    private int getK(int capacity, int payload_length)
    {
        int i;
        for (i = 1; i < 8; i++)
        {
            int n = (1 << i) - 1;
            int usable = (capacity * i / n - capacity * i / n % n) / 8;
            if (usable == 0 || usable < payload_length)
                break;
        }
        return i-1;
    }
}
