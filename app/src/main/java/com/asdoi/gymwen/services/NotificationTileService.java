package com.asdoi.gymwen.services;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.preference.PreferenceManager;

import com.asdoi.gymwen.ApplicationFeatures;
import com.asdoi.gymwen.R;

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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationFeatures.getContext());
        boolean showNotifNew = !sharedPreferences.getBoolean("showNotification", false);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("showNotification", showNotifNew);
        editor.commit();
        if (showNotifNew) {
            ApplicationFeatures.sendNotification();
        }
        onStartListening();
    }

    @Override
    public void onTileAdded() {

    }
}
