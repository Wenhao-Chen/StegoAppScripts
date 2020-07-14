package wenhaoc.stegodb.stegoappscripts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;

import wenhaoc.stegodb.stegoappscripts.database.DBDevice;
import wenhaoc.stegodb.stegoappscripts.util.P;

public class MainActivity extends AppCompatActivity {


    static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    TextView tv;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.textView);
        P.act = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void onClick(View v)
    {
        makeStegos();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!hasPermissions())
        {
            requestPermissions();
            return;
        }
        P.i("onResume");
//        new Thread(new Runnable() {
//            @Override
//            public void run()
//            {
//                checkReadability();
//            }
//        }).start();
        makeStegos();
    }

    private void del(File dd)
    {
        for (File f : dd.listFiles())
        {
            if (f.getName().contains("Scene-20180418-092321"))
            {
                f.delete();
            }
        }
    }


    private void checkReadability()
    {
        File dd = new File("/sdcard/Download/stegodb_March2019/stegos");
        int total = 0;
        for (File dir : dd.listFiles())
            total += dir.list().length;
        int curr = 0;
        int bad = 0;
        for (File dir : dd.listFiles())
        {
            for (File f : dir.listFiles())
            {
                P.i("checking " + (++curr) + "/" + total + ": " + f.getAbsolutePath());
                if (f.getName().endsWith(".jpg") || f.getName().endsWith(".png"))
                {
                    boolean readable = P.readable(f);
                    if (!readable)
                    {
                        bad++;
                    }
                }
            }
        }
        P.i("bad: "+bad);
    }

    private Thread thread;
    void makeStegos()
    {
        if (thread!=null && thread.isAlive())
            return;
        thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
/*
                File dd = new File("/sdcard/Download/stegodb_March2019");
                del(new File(dd, "originals"));
                del(new File(dd, "cropped"));
                File s = new File(dd, "stegos");
                for (File ff : s.listFiles())
                    del(ff);
*/

                //checkReadability();
                P.i("initing device...");
                DBDevice d = new DBDevice();
                P.i("making stegos...");
                d.makeStegos();
                P.i("All done.");
            }
        });
        thread.start();
    }
    public void log(final String text)
    {
        if (tv==null)
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                tv.setText(text);
            }
        });
    }

    boolean hasPermissions()
    {
        for (String permission : PERMISSIONS)
        {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    void requestPermissions()
    {
        requestPermissions(PERMISSIONS, 0);
    }


}
