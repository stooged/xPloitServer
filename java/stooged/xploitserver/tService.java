package stooged.xploitserver;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

@TargetApi(24)
public class tService extends TileService {

    public tService() {
        super();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();

        Intent intent;

        Tile tile = getQsTile();

        if (tile.getState() == Tile.STATE_ACTIVE)
        {
            tile.setIcon(Icon.createWithResource(this, R.drawable.svr_disabled));
            tile.setLabel("xPloit Server");
            tile.setContentDescription("Ps4 Server Stopped");
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();

            intent = new Intent(this, MainActivity.class);
            intent.putExtra("Action", "Stop");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);


        }
        else if (tile.getState() == Tile.STATE_INACTIVE)
        {
            tile.setIcon(Icon.createWithResource(this, R.drawable.svr_enabled));
            tile.setLabel("xPloit Server");
            tile.setContentDescription("Ps4 Server Running");
            tile.setState(Tile.STATE_ACTIVE);
            tile.updateTile();
            Utils.ShowToast(this,"Server enabled",3000);

            intent = new Intent(this, MainActivity.class);
            intent.putExtra("Action", "Start");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);

        }
        else if (tile.getState() == Tile.STATE_UNAVAILABLE)
        {
            Utils.ShowToast(this,"Error: STATE_UNAVAILABLE",3000);
        }

    }

    @Override
    public void onStartListening() {

        Tile tile = getQsTile();
        if (Utils.isSvcRunning(this, xService.class)) {

            tile.setIcon(Icon.createWithResource(this, R.drawable.svr_enabled));
            tile.setLabel("xPloit Server");
            tile.setContentDescription("Ps4 Server Running");
            tile.setState(Tile.STATE_ACTIVE);
            tile.updateTile();
        }
        else
        {
            tile.setIcon(Icon.createWithResource(this, R.drawable.svr_disabled));
            tile.setLabel("xPloit Server");
            tile.setContentDescription("Ps4 Server Stopped");
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();

        }

    }

}
