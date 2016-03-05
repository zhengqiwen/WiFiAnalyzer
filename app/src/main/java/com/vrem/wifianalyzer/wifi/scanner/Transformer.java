/*
 *    Copyright (C) 2015 - 2016 VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.vrem.wifianalyzer.wifi.scanner;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.support.annotation.NonNull;

import com.vrem.wifianalyzer.wifi.model.WiFiConnection;
import com.vrem.wifianalyzer.wifi.model.WiFiData;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import com.vrem.wifianalyzer.wifi.model.WiFiSignal;
import com.vrem.wifianalyzer.wifi.model.WiFiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Transformer {

    static final String IP_ADDRESS = "192.168.1.1";
    final static String SSID_FORMAT = "SSID-%02d";
    private static int Count = 0;
    private final Map<String, String> cache = new TreeMap<>();

    WiFiConnection transformWifiInfo(WifiInfo wifiInfo) {
        if (wifiInfo == null || wifiInfo.getNetworkId() == -1) {
            return WiFiConnection.EMPTY;
        }
        String demoSSID = getDemoSSID(WiFiUtils.convertSSID(wifiInfo.getSSID()));
        String demoBSSID = getDemoBSSID(wifiInfo.getBSSID(), demoSSID);
        return new WiFiConnection(demoSSID, demoBSSID, IP_ADDRESS);
    }

    List<String> transformWifiConfigurations(List<WifiConfiguration> configuredNetworks) {
        List<String> results = new ArrayList<>();
        if (configuredNetworks != null) {
            for (WifiConfiguration wifiConfiguration : configuredNetworks) {
                results.add(getDemoSSID(WiFiUtils.convertSSID(wifiConfiguration.SSID)));
            }
        }
        return Collections.unmodifiableList(results);
    }

    List<WiFiDetail> transformScanResults(List<ScanResult> scanResults) {
        List<WiFiDetail> results = new ArrayList<>();
        if (scanResults != null) {
            for (ScanResult scanResult : scanResults) {
                WiFiSignal wiFiSignal = new WiFiSignal(scanResult.frequency, scanResult.level);
                String demoSSID = getDemoSSID(scanResult.SSID);
                String demoBSSID = getDemoBSSID(scanResult.BSSID, demoSSID);
                WiFiDetail wiFiDetail = new WiFiDetail(demoSSID, demoBSSID, scanResult.capabilities, wiFiSignal);
                results.add(wiFiDetail);
            }
        }
        return Collections.unmodifiableList(results);
    }

    public WiFiData transformToWiFiData(List<ScanResult> scanResults, WifiInfo wifiInfo, List<WifiConfiguration> configuredNetworks) {
        List<WiFiDetail> wiFiDetails = transformScanResults(scanResults);
        WiFiConnection wiFiConnection = transformWifiInfo(wifiInfo);
        List<String> wifiConfigurations = transformWifiConfigurations(configuredNetworks);
        return new WiFiData(wiFiDetails, wiFiConnection, wifiConfigurations);
    }

    String getDemoSSID(@NonNull String SSID) {
        String demoSSID = cache.get(SSID);
        if (demoSSID == null) {
            demoSSID = String.format(SSID_FORMAT, Count++);
            cache.put(SSID, demoSSID);
        }
        return demoSSID;
    }

    String getDemoBSSID(@NonNull String BSSID, @NonNull String demoSSID) {
        String replacement = demoSSID.substring(demoSSID.length() - 2);
        return BSSID.substring(0, BSSID.length() - 8)
                + replacement + ":" + replacement + ":" + replacement;
    }
}
