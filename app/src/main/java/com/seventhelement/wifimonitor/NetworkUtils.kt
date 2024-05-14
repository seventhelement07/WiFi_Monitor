package com.seventhelement.wifimonitor

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.util.concurrent.Executors
import java.util.regex.Pattern

class NetworkUtils {
    fun getConnectedDevices(ipRange: String): List<String> {

            val devices = mutableListOf<String>()
            val process = Runtime.getRuntime().exec("/system/bin/ping -c 1 $ipRange")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?=""
            while (reader.readLine().also { line = it } != null) {
                if (line!!.contains("bytes")) {
                    val ip =
                        line!!.substring(line!!.indexOf("from") + 5, line!!.indexOf("icmp_seq") - 1)
                    devices.add(ip)
                }
            }
            reader.close()
            return devices
        }
    }