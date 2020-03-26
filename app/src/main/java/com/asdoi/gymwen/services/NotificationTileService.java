/*
 * Copyright (c) 2020 Felix Hollederer
 *     This file is part of GymWenApp.
 *
 *     GymWenApp is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GymWenApp is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GymWenApp.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import com.asdoi.gymwen.util.PreferenceUtil;

@TargetApi(24)
public class NotificationTileService extends TileService {

    @Override
    public void onStartListening() {
        Tile tile = getQsTile();
        if (PreferenceUtil.isNotification()) {
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
        boolean showNotifNew = !PreferenceUtil.isNotification();
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
