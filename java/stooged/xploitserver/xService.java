package stooged.xploitserver;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;

public class xService extends IntentService {
    NanoHTTPD webServer;
    String webHost;

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
        stopServer();
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServer();

        int notificationId = 13375;
        String channelId = "xPloit_Server";
        String channelName = "xPloit Server";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN);
            channel.setDescription(channelName);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.svr_enabled)
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.mipmap.ic_launcher))
                    .setContentTitle(channelName)
                    .setContentText(webHost)
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setVisibility(-1);

            Intent openApp=new Intent(this, nReceiver.class);
            openApp.setAction("OPEN_APP");
            PendingIntent pOpen = PendingIntent.getBroadcast(this, 0, openApp, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.mipmap.ic_launcher, "Open App", pOpen);

            Intent stopSvr=new Intent(this, nReceiver.class);
            stopSvr.setAction("STOP_SERVER");
            PendingIntent pStopSvr = PendingIntent.getBroadcast(this, 0, stopSvr, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.svr_disabled, "Stop Server", pStopSvr);

            Notification notification = builder.build();
            notification.flags = Notification.FLAG_NO_CLEAR;
            startForeground(notificationId, notification);

        }
        else {

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.svr_enabled)
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.mipmap.ic_launcher))
                    .setContentIntent(pendingIntent)
                    .setContentTitle(channelName)
                    .setContentText(webHost)
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setVisibility(-1);

            Notification notification = builder.build();
            notification.flags = Notification.FLAG_NO_CLEAR;
            startForeground(notificationId, notification);
        }

        return START_STICKY;
    }

    private void stopServer() {
        if (webServer != null) {
            if (webServer.isAlive()) {
                webServer.closeAllConnections();
                webServer.stop();
            }
            webServer = null;
        }
        Utils.deleteNotification(this);
    }

    private void startServer()
    {
        String LocIp = Utils.getIp();
        webServer = new Server(8080);
        try {
            webServer.start();
            webHost = "http://" + LocIp + ":8080/index.html";
            Utils.SaveSetting(this,"HOSTADDR" ,webHost);
            updateStatus(webHost);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                webServer = null;
                webServer = new Server(9090);
                webServer.start();
                webHost = "http://" + LocIp + ":9090/index.html";
                Utils.SaveSetting(this,"HOSTADDR" , webHost);
                updateStatus(webHost);
            }
            catch (IOException ignored)
            {
                Utils.SaveSetting(this,"HOSTADDR" , "ERROR");
                webHost = "Failed to start server";
                updateStatus(webHost);
            }
        }
    }

    private void updateStatus (String msg){
        Intent intent = new Intent ("status");
        intent.putExtra("hostaddr", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
