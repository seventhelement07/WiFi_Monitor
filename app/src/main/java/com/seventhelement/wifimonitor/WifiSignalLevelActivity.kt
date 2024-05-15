 package com.seventhelement.wifimonitor

import android.content.Context
import android.content.Intent
import android.net.DhcpInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.seventhelement.wifimonitor.databinding.ActivityMainBinding
import com.seventhelement.wifimonitor.databinding.ActivityWifiSignalLevelBinding
import java.util.Timer
import java.util.TimerTask

 class WifiSignalLevelActivity : AppCompatActivity() {
     private var wifiSettingsDialog: AlertDialog? = null
     private var lastWifiState: Boolean = false
     lateinit var binding:ActivityWifiSignalLevelBinding
     private lateinit var handler: Handler
     private lateinit var timer: Timer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityWifiSignalLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
       fetchData()
        lastWifiState = isWifiAvailable()
    }

     override fun onResume() {
         super.onResume()
         val currentWifiState = isWifiAvailable()
         if (currentWifiState != lastWifiState) {
             // WiFi state has changed
             lastWifiState = currentWifiState
             if (currentWifiState) {
                 // WiFi is now enabled, refresh the date
                 fetchData()
             }
         }

         // Dismiss the dialog if WiFi settings is turned on
         if (currentWifiState && wifiSettingsDialog != null && wifiSettingsDialog?.isShowing == true) {
             wifiSettingsDialog?.dismiss()
         }
     }
     private fun isWifiAvailable(): Boolean {
         val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
         return wifiManager.isWifiEnabled
     }
     fun fetchData()
     {

         handler = Handler(Looper.getMainLooper())
         timer = Timer()
         val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
         val dhcpInfo: DhcpInfo? = wifiManager.dhcpInfo
         // Check if WiFi is enabled
         if (wifiManager.isWifiEnabled) {
             // Get the current WiFi connection information
             val wifiInfo: WifiInfo? = wifiManager.connectionInfo
             if (wifiInfo != null) {
                 binding.signalname.text = wifiInfo.ssid
                 binding.iptext.text= intToIp(wifiInfo.ipAddress)// Name of the connected WiFi network
                 binding.gateway.text=intToIp(dhcpInfo!!.gateway)
                 val dnsServers = wifiManager.dhcpInfo?.let { dhcpInfo ->
                     arrayOf(intToIp(dhcpInfo.dns1), intToIp(dhcpInfo.dns2))
                 }
                 binding.dns1.text= dnsServers!![0]
                 binding.dns1.text=dnsServers!![1]
                 timer.scheduleAtFixedRate(object : TimerTask() {
                     override fun run() {
                         val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                         val wifiInfo: WifiInfo? = wifiManager.connectionInfo
                         val rssi = wifiInfo!!.rssi
                         handler.post {
                             binding.speed.text = "Speed" + rssi + "db"
                             //Toast.makeText(this@WifiSignalLevelActivity,"Signal Strength: $rssi dBm",Toast.LENGTH_SHORT).show()
                         }
                     }
                 }, 0, 1000) // Update every 1 seconds

                 val bssid = wifiInfo.bssid  // MAC address of the access point
                 val ipAddress = wifiInfo.ipAddress  // IP address in integer format
                 val rssi = wifiInfo.rssi  // Received Signal Strength Indicator
                 //Toast.makeText(this,"$ssid  $bssid $ipAddress $rssi",Toast.LENGTH_SHORT).show()
                 // You may need to further process or display this information as per your requirement
                 // For instance, converting the IP address integer to a human-readable format
             }
         }
         else
         {
             showWifiSettingsDialog()
         }
     }
     private fun intToIp(ip: Int): String {
         return (ip and 0xFF).toString() + "." +
                 (ip shr 8 and 0xFF) + "." +
                 (ip shr 16 and 0xFF) + "." +
                 (ip shr 24 and 0xFF)
     }
     private fun showWifiSettingsDialog() {
         wifiSettingsDialog = AlertDialog.Builder(this)
             .setMessage("WiFi is not enabled. Do you want to go to WiFi settings?")
             .setPositiveButton("Yes") { dialog, which ->
                 // Open WiFi settings
                 val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                 startActivity(intent)
             }
             .setNegativeButton("No") { dialog, which ->
                 dialog.dismiss()
                 // Handle the case when the user chooses not to go to WiFi settings
             }
             .setCancelable(false)
             .show()
     }
     override fun onDestroy() {
         super.onDestroy()
         // Stop the timer when the activity is destroyed to avoid memory leaks
         timer.cancel()
     }
}