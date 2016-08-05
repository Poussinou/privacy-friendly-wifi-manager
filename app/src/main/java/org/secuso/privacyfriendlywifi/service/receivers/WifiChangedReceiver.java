package org.secuso.privacyfriendlywifi.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.secuso.privacyfriendlywifi.logic.types.WifiLocationEntry;
import org.secuso.privacyfriendlywifi.logic.util.StaticContext;
import org.secuso.privacyfriendlywifi.logic.util.WifiHandler;
import org.secuso.privacyfriendlywifi.logic.util.WifiListHandler;
import org.secuso.privacyfriendlywifi.service.ManagerService;
import org.secuso.privacyfriendlywifi.service.WifiNotification;

/**
 * BroadcastReceiver to listen for Wi-Fi state changes.
 * If connected to a new wireless network, a notification will be shown.
 */
public class WifiChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        StaticContext.setContext(context);
        WifiListHandler wifiListHandler = new WifiListHandler();
        wifiListHandler.sort(); // sort list in order to reflect state changes is Wi-Fi list fragment

        if (ManagerService.isServiceActive()) { // check that the app is actually expected to manage wifi
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            if (info != null && info.isConnected()) { // wifi is connected

                // get current ssid
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = WifiHandler.getCleanSSID(wifiInfo.getSSID());

                if (ssid.equals("<unknown ssid>") || ssid.equals("0x")) { // check if connected correctly
                    return;
                }


                // check whether the wifi is already known
                for (WifiLocationEntry entry : wifiListHandler.getAll()) {
                    if (entry.getSsid().equals(ssid)) {
                        return;
                    }
                }

                // if unknown, show notification
                WifiNotification.show(context, wifiInfo);
            } else {
                AlarmReceiver.fireAndSchedule(true); // not connected -> maybe we can already disable Wi-Fi
            }
        }
    }
}
