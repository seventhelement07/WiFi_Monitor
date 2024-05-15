package com.seventhelement.wifimonitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.seventhelement.wifimonitor.Adapter.Adapter2
import com.seventhelement.wifimonitor.databinding.ActivityWiFiListBinding

class WiFiListActivity : AppCompatActivity(), Adapter2.OnItemSelectedListener {
    lateinit var binding: ActivityWiFiListBinding
    private lateinit var wifiManager: WifiManager



    companion object {
        const val PERMISSIONS_REQUEST_CODE = 1001
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityWiFiListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager


        // Initialize adapter

        checkAndRequestPermissions()

        // Register WiFi scan results receiver
        registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
    }
    override fun onDestroy() {
        super.onDestroy()
        // Unregister WiFi scan results receiver
        unregisterReceiver(wifiScanReceiver)
    }
    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                val results = wifiManager.scanResults
                updateList(results)
            }
        }
    }
    private fun updateList(scanResults: List  <ScanResult>) {
        val list=ArrayList<String>()
       binding.progressBar.visibility= View.INVISIBLE
        for (result in scanResults) {
            val ssid = result.SSID
            val bssid = result.BSSID

            val level = result.level
           list.add(ssid)
        }
        val adapter= Adapter2(list,this)
        binding.rr.layoutManager= LinearLayoutManager(this)
        binding.rr.adapter=adapter
        adapter.notifyDataSetChanged()
    }

private fun checkAndRequestPermissions() {
    val permissionToAccessCoarseLocation = ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (permissionToAccessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
            PERMISSIONS_REQUEST_CODE
        )
    } else {
        // Permission already granted, start scanning
        startWifiScan()
    }
}
    private fun startWifiScan() {
        wifiManager.startScan()
    }

    override fun onItemSelected(position: Int) {
        TODO("Not yet implemented")
    }
}