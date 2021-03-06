/*
Copyright 2016-2018 Jan Henzel, Patrick Jauernig, Dennis Werner

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.secuso.privacyfriendlywifimanager.logic.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.ContextCompat;

import org.secuso.privacyfriendlywifimanager.logic.preconditions.CellLocationCondition;
import org.secuso.privacyfriendlywifimanager.logic.types.WifiLocationEntry;

import java.util.List;

/**
 * Helper functions for Wi-Fi.
 */
public class WifiHandler {
    private static WifiManager wifiManager;

    /**
     * Returns the system´s WifiManager.
     *
     * @param context A context to use.
     * @return The WifiManager instance.
     */
    public static WifiManager getWifiManager(Context context) {
        if (WifiHandler.wifiManager == null) {
            WifiHandler.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }

        return WifiHandler.wifiManager;
    }

    /**
     * Checks if Wi-Fi permission is granted.
     *
     * @param context A context to use.
     * @return True if Wi-Fi permission has been granted, false otherwise.
     */
    public static boolean hasWifiPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CHANGE_WIFI_STATE)
                == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_WIFI_STATE)
                == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Checks if Wi-Fi is connected.
     *
     * @param context A context to use.
     * @return True if Wi-Fi is connected, false otherwise.
     */
    public static boolean isWifiConnected(Context context) {
        WifiInfo wifiInfo = getWifiManager(context).getConnectionInfo();
        if (wifiInfo != null) {
            int nid = wifiInfo.getNetworkId();
            return wifiManager.isWifiEnabled() && nid != -1;
        }

        return false;
    }

    /**
     * Checks if Wi-Fi is enabled.
     *
     * @param context A context to use.
     * @return True if Wi-Fi is enabled, false otherwise.
     */
    public static boolean isWifiEnabled(Context context) {
        WifiManager wifiManager = getWifiManager(context);
        return wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED
                || wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING;
    }

    /**
     * Returns the SSID of the currently connected Wi-Fi.
     *
     * @param context A context to use.
     * @return The clean SSID of the Wi-Fi.
     */
    public static String getCurrentSSID(Context context) {
        WifiInfo currentConnection = getWifiManager(context).getConnectionInfo();
        return WifiHandler.getCleanSSID(currentConnection.getSSID());
    }

    /**
     * Cleans the passed string from quotes.
     *
     * @param rawSSID Input SSID string to process.
     * @return The cleaned string.
     */
    public static String getCleanSSID(String rawSSID) {
        String cleanSSID = rawSSID;
        if (cleanSSID.startsWith("\"") && cleanSSID.endsWith("\"")) {
            cleanSSID = cleanSSID.substring(1, cleanSSID.length() - 1);
        }
        return cleanSSID;
    }

    /**
     * Fetches Wi-Fi scan results, adds MACs to already known wifis and updates passed unknownNetworks list.
     *
     * @param context         A context to use.
     * @param unknownNetworks The object to store the unknown networks in.
     */
    public static boolean scanAndUpdateWifis(Context context, List<WifiLocationEntry> unknownNetworks) {
        List<ScanResult> scanResults = getWifiManager(context).getScanResults();
        boolean modified = false;

        for (ScanResult config : scanResults) {
            String confSSID = WifiHandler.getCleanSSID(config.SSID);

            boolean alreadyManaged = false;
            WifiListHandler handler = new WifiListHandler(context);

            List<WifiLocationEntry> knownEntries = handler.getAll();
            for (int i = 0; i < knownEntries.size(); i++) {
                WifiLocationEntry knownEntry = knownEntries.get(i);

                if (confSSID.equals(knownEntry.getSsid())) {
                    // ssid is already present, so we should add the MAC to the existing network
                    modified = knownEntry.addCellLocationCondition(new CellLocationCondition(config.BSSID)); // FIXME: Don't do this here, handle new MACs in own method!
                    alreadyManaged = true;
                    break;
                }
            }

            if (!alreadyManaged) {
                boolean isNewEntry = true;

                // check if current ssid is already present in unknown networks and add MAC accordingly
                for (WifiLocationEntry unknownEntry : unknownNetworks) {
                    if (confSSID.equals(unknownEntry.getSsid())) {
                        unknownEntry.addCellLocationCondition(new CellLocationCondition(config.BSSID));
                        isNewEntry = false;
                        break;
                    }
                }

                // current ssid has not been found -> new unknown wifi
                if (isNewEntry) {
                    unknownNetworks.add(new WifiLocationEntry(confSSID, config.BSSID));
                }
            }
        }

        return modified;
    }
}
