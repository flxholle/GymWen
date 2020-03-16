package com.asdoi.gymwen.services;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;
import com.asdoi.gymwen.receivers.NotificationDismissButtonReceiver;

@TargetApi(24)
public class NotificationTileService extends TileService {

    @Override
    public void onStartListening() {
        Tile tile = getQsTile();
        if (ApplicationFeatures.getBooleanSettings("showNotification", true)) {
            tile.setIcon(Icon.createWithResource(this, R.drawable.ic_notifications_black_24dp));
            tile.setState(Tile.STATE_ACTIVE);
            tile.setContentDescription(getString(R.string.on));
        } else {
            tile.setIcon(Icon.createWithResource(this, R.drawable.ic_notifications_off_black_24dp));
            tile.setState(Tile.STATE_INACTIVE);
            tile.setContentDescription(getString(R.string.off));
        }
        tile.setLabel(getString(R.string.tile_name));
        tile.updateTile();
    }

    @Override
    public void onClick() {
        if (!ApplicationFeatures.initSettings(false, true)) {
            makeTileInactive();
            return;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
        boolean showNotifNew = !sharedPreferences.getBoolean("showNotification", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("showNotification", showNotifNew);
        if (showNotifNew) {
            editor.commit();
            ApplicationFeatures.sendNotifications();
        } else {
            editor.apply();
            Intent intent = new Intent(this, NotificationDismissButtonReceiver.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            sendBroadcast(intent);
        }
        onStartListening();
    }

    private void makeTileInactive() {
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_UNAVAILABLE);
        tile.setIcon(Icon.createWithResource(this, R.drawable.ic_notifications_black_24dp));
        tile.updateTile();
    }

    @Override
    public void onTileAdded() {
        if (!ApplicationFeatures.initSettings(false, true)) {
            makeTileInactive();
        } else
            onStartListening();
    }

    @Override
    public IBinder onBind(Intent intent) {
        TileService.requestListeningState(this, new ComponentName(this, NotificationTileService.class));
        return super.onBind(intent);
    }
}
