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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.seventhelement.wifimonitor.Adapter.Adapte3
import com.seventhelement.wifimonitor.databinding.ActivityCoonectedDevicesBinding
import com.seventhelement.wifimonitor.network.NetworkScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask

class CoonectedDevicesActivity : AppCompatActivity(),NetworkScanner.OnScanCompleteListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: Adapte3
    private val itemList = mutableListOf<String>()
    private var wifiSettingsDialog: AlertDialog? = null
    private var lastWifiState: Boolean = false
    lateinit var binding: ActivityCoonectedDevicesBinding
    private var scanningComplete = false
    var count=0;
var baseurl="";
    private var devicesFound = 0
    val list= mutableListOf<String>()
    private var deviceCount = 254
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoonectedDevicesBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbar)
        setContentView(binding.root)
        scaning()
    }
   fun scaning()
   {
       list.clear()
       count=0;
       recyclerView = findViewById(R.id.rr)
       fetchData()
       lastWifiState = isWifiAvailable()
       adapter = Adapte3(list)

       // Set RecyclerView's layout manager and adapter
       binding.rr.layoutManager = LinearLayoutManager(this)
       binding.rr.adapter = adapter

       // Initialize ProgressBar
       val progressBar = findViewById<ProgressBar>(R.id.progressBar2)

       // Show ProgressBar
       progressBar.visibility = View.VISIBLE

       // Start network scan
       val networkScanner = NetworkScanner()
       networkScanner.scanNetwork(this,extractNetworkPrefix(baseurl))
       Handler(Looper.getMainLooper()).postDelayed({
           if (!scanningComplete) {
               scanningComplete = true
               progressBar.visibility = View.GONE // Hide ProgressBar
           }
       }, SCAN_TIMEOUT_MS)
   }
    override fun onDeviceFound(ipAddress: String) {
        // Add the discovered device to the list and update the adapter
      count++;
        Handler(Looper.getMainLooper()).post {
            adapter.addDevice(ipAddress)
            binding.toolbar.title="$count  Devices Connected"
        }


        // Hide ProgressBar if all data is loaded

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                // Refresh action
                scaning()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val SCAN_TIMEOUT_MS = 10000L // 1 minute timeout
    }
    fun extractNetworkPrefix(ipAddress: String): String {
        val parts = ipAddress.split(".")
        if (parts.size >= 2) {
            return "${parts[0]}.${parts[1]}"
        }
        return ""
    }
    private fun isWifiAvailable(): Boolean {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }
    fun fetchData()
    {

        val handler = Handler(Looper.getMainLooper())
        val timer = Timer()
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcpInfo: DhcpInfo? = wifiManager.dhcpInfo
        // Check if WiFi is enabled
        if (wifiManager.isWifiEnabled) {
            // Get the current WiFi connection information
            val wifiInfo: WifiInfo? = wifiManager.connectionInfo
            if (wifiInfo != null) {

                baseurl= intToIp(wifiInfo.ipAddress)// Name of the connected WiFi network

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


}