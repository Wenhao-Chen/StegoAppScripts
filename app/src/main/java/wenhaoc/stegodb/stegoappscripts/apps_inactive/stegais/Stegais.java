package wenhaoc.stegodb.stegoappscripts.apps_inactive.stegais;

/**
 * Created by C03223-Stego2 on 1/17/2018.
 */

public class Stegais {


    public static void embed(String imagePath, String message, String password)
    {
        byte[] data = prepareMessage(message.getBytes());
        if (password == null)
        {
            //:cond_0
            password = "Stegais";
        }
        shuffleArrayOfDCTs();
    }


    private static void shuffleArrayOfDCTs()
    {

    }

    private static byte[] prepareMessage(byte[] message)
    {
        //TODO
        return null;
    }

}
