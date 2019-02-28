package stooged.xploitserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class nReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals("OPEN_APP")) {
                intent = new Intent(context, MainActivity.class);
                intent.putExtra("Action", "Open");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else if (action.equals("STOP_SERVER")) {
                intent = new Intent(context, MainActivity.class);
                intent.putExtra("Action", "Stop");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }
}


