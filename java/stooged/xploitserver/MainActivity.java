package stooged.xploitserver;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends Activity {

    TextView text1, txtlog;
    boolean stopAction = false;
    boolean startAction = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
        finish();
    }

    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("status"));
    }

    protected void onPause (){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);


        if (Utils.GetSetting(this,"HASINIT",false)) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String mAction = extras.getString("Action");

                if (mAction != null && mAction.equals("Stop")) {
                    stopAction = true;
                } else if (mAction != null && mAction.equals("Start")) {
                    startAction = true;
                }
            }
        }


        if (Utils.isSvcRunning(this, xService.class)) {
            if (stopAction) {
                Intent intent = new Intent(this, xService.class);
                stopService(intent);
                finish();
                return;
            }
        } else {
            if (startAction) {
                Intent intent = new Intent(this, xService.class);
                startService(intent);
                finish();
                return;
            }
        }


        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            SetPermissions();
        }
        else {
            initApp();
        }
    }

    @TargetApi(23)
    private void SetPermissions() {
        String[] perms = {"android.permission.INTERNET", "android.permission.ACCESS_WIFI_STATE", "android.permission.ACCESS_NETWORK_STATE", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.FOREGROUND_SERVICE"};
        int permsRequestCode = 200;
        requestPermissions(perms, permsRequestCode);
    }

    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){
            case 200:
                initApp();
                break;
        }
    }

    private void initApp()
    {
        setContentView(R.layout.activity_main);
        text1 = findViewById(R.id.text1);
        txtlog = findViewById(R.id.txtlog);
        txtlog.setMovementMethod(new ScrollingMovementMethod());

        Utils.SaveSetting(getBaseContext(),"HASINIT",true);

        if (Utils.isSvcRunning(this, xService.class)) {
            text1.setText(Utils.GetSetting(this, "HOSTADDR", "Start server"));
        } else {
            text1.setText("Start server");
        }


        PrintLog("WWW Folder: " + Environment.getExternalStorageDirectory().toString() + "/xPloitServer/");


        new Thread(new Runnable() {
            public void run() {
                File PayloadDir = new File(Environment.getExternalStorageDirectory().toString() + "/xPloitServer/");
                if (!PayloadDir.exists()) {
                    if (!PayloadDir.mkdirs()) {
                        ShowToast(getBaseContext(), "Failed to create directory", Toast.LENGTH_SHORT);
                        PrintLog("Failed to create directory");
                        return;
                    }
                }
                File[] contents = PayloadDir.listFiles();
                if (contents == null) {
                    ShowToast(getBaseContext(), "Failed to read directory", Toast.LENGTH_SHORT);
                    PrintLog("Failed to read directory");
                }
                else if (contents.length == 0) {
                    Utils.SaveSetting(getBaseContext(),"VERSION",BuildConfig.VERSION_CODE);
                    PrintLog("Unpacking resources...\n");
                    ShowToast(getBaseContext(), "Unpacking resources...", Toast.LENGTH_SHORT);
                    unPack(getBaseContext(),R.raw.xproject_zip,Environment.getExternalStorageDirectory().toString() + "/xPloitServer/");
                    ShowToast(getBaseContext(), "Done", Toast.LENGTH_SHORT);
                }
                else
                {
                    if (Utils.GetSetting(getBaseContext(),"VERSION",10) < BuildConfig.VERSION_CODE)
                    {
                        PrintLog("Updating resources...\n");
                        ShowToast(getBaseContext(), "Updating resources...", Toast.LENGTH_SHORT);
                        unPack(getBaseContext(),R.raw.xproject_zip,Environment.getExternalStorageDirectory().toString() + "/xPloitServer/");
                        ShowToast(getBaseContext(), "Done", Toast.LENGTH_SHORT);
                    }
                }
            }
        }).start();
    }


    public void btn1_Click(View view) {
        Intent intent = new Intent(this, xService.class);
        startService(intent);
    }

    public void btn2_Click(View view) {
        Intent intent = new Intent(this, xService.class);
        stopService(intent);
        PrintLog("Stopped server");
        text1.setTextColor(0xFF33b5e5);
        text1.setText("Start server");
    }


    private BroadcastReceiver bReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra("hostaddr");
            if (status.equals("Failed to start server"))
            {
                Utils.ShowToast(getBaseContext(),"Failed to start server",Toast.LENGTH_SHORT);
                PrintLog("Failed to start server");
                text1.setTextColor(0xFFD50000);
                text1.setText("ERROR");
            }
            else
            {
                Utils.ShowToast(getBaseContext(),"Server enabled",Toast.LENGTH_SHORT);
                PrintLog("Server enabled\n");
                PrintLog("Connect PS4 to " + status);
                text1.setTextColor(0xFF33b5e5);
                text1.setText(status);
            }
        }
    };

    private void unPack(Context context, int resourceId, String destPath)
    {
        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = context.getResources().openRawResource(resourceId);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;
            while ((ze = zis.getNextEntry()) != null)
            {
                filename = ze.getName();
                if (ze.isDirectory()) {
                    File fmd = new File(destPath + filename);
                    fmd.mkdirs();
                    continue;
                }
                PrintLog(filename);
                FileOutputStream fout = new FileOutputStream(destPath + filename);
                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }
                fout.close();
                zis.closeEntry();
            }
            zis.close();
            PrintLog("\nDone");
        }
        catch(IOException e)
        {
            e.printStackTrace();
            PrintLog("ERROR\n" + e.getMessage());
        }
    }

    public void ShowToast(final Context mContext, final String msg, final int duration )
    {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(mContext, msg, duration).show();
            }
        });
    }

    public void PrintLog(final String msg)
    {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                txtlog.setText(txtlog.getText() + msg + "\n");
            }
        });
    }

}


