package stooged.xploitserver;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;

public class xService extends IntentService {
    NanoHTTPD webServer;

    public xService() {
        super("xService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.ShowToast(this,"Stopped server",Toast.LENGTH_SHORT);
        StopServer();
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        StartServer();
        return START_STICKY;
    }

    private void StopServer() {
        if (webServer != null) {
            if (webServer.isAlive()) {
                webServer.closeAllConnections();
                webServer.stop();
            }
            webServer = null;
        }
    }

    private void StartServer()
    {
        String LocIp = Utils.getIp();
        webServer = new Server(8080);
        try {
            webServer.start();
            Utils.SaveSetting(this,"HOSTADDR" ,"http://" + LocIp + ":8080/index.html");
            updateStatus("http://" + LocIp + ":8080/index.html");
        } catch (IOException e) {
            e.printStackTrace();
            try {
                webServer = null;
                webServer = new Server(9090);
                webServer.start();
                Utils.SaveSetting(this,"HOSTADDR" , "http://" + LocIp + ":9090/index.html");
                updateStatus("http://" + LocIp + ":9090/index.html");
            }
            catch (IOException ignored)
            {
                Utils.SaveSetting(this,"HOSTADDR" , "ERROR");
                updateStatus("Failed to start server");
            }
        }
    }

    private void updateStatus (String msg){
        Intent intent = new Intent ("status");
        intent.putExtra("hostaddr", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
