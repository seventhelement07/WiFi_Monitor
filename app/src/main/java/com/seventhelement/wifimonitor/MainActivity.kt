package com.seventhelement.wifimonitor
import android.content.res.Configuration
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.seventhelement.wifimonitor.Adapter.Adapter
import com.seventhelement.wifimonitor.databinding.ActivityMainBinding
import java.util.Timer
import java.util.TimerTask


private lateinit var handler: Handler
private lateinit var timer: Timer

private val CAMERA_PERMISSION_REQUEST_CODE = 100
private val LOCATION_PERMISSION_REQUEST_CODE = 101

class MainActivity : AppCompatActivity(), Adapter.OnItemSelectedListener {
    lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setSupportActionBar(binding.toolbar)



        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding.name.text = Settings.Global.getString(contentResolver, "device_name")
        handler = Handler(Looper.getMainLooper())
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val cm =
                    applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                // Network Capabilities of Active Network
                val nc = cm.getNetworkCapabilities(cm.activeNetwork)

                // DownSpeed in MBPS
                val downSpeed = (nc?.linkDownstreamBandwidthKbps)!! / 1000
                handler.post {
                    binding.speed.text = "Link Speed-$downSpeed mbps"
                    //Toast.makeText(this@WifiSignalLevelActivity,"Signal Strength: $rssi dBm",Toast.LENGTH_SHORT).show()
                }
            }
        }, 0, 1000)
        val fineLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationPermissionGranted || !coarseLocationPermissionGranted) {
            showLocationPermissionDialog()
        } else {
            // Location permissions already granted, proceed with location-related tasks
            // e.g., Perform location-related tasks
        }

        // Check camera permission
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showcameraPermissionDialog()
        }
        // Connectivity Manager
        binding.floatingActionButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
               showcameraPermissionDialog()
            } else {
                // Camera permission already granted, proceed with camera-related tasks
                // e.g., Perform camera-related tasks
                scanit()
            }

        }

        var list = ArrayList<data>()
        list.add(data("Wi-Fi signal level", R.drawable.signal))
        list.add(data("Wi-Fi list", R.drawable.wifi))
        list.add(data("Password generator", R.drawable.key))
        list.add(data("Devices connected", R.drawable.device))
        val adapter = Adapter(list, this)
        binding.rr.layoutManager = GridLayoutManager(this, 2);
        binding.rr.adapter = adapter
    }

    fun  showLocationPermissionDialog() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Use Your Location")
        builder.setMessage("This app collects location data for allowing you to enquire about the services based on your location only when the app is open and used.")
        builder.setPositiveButton("ACCEPT") { dialog: DialogInterface, which: Int ->
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        builder.setNegativeButton("DENY") { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        builder.setCancelable(false) // Prevent dismissal without choosing an option
        builder.create().show()
    }
    fun  showcameraPermissionDialog() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Use Your Camera")
        builder.setMessage("This app requires access to your camera  when the app is open and used.")
        builder.setPositiveButton("ACCEPT") { dialog: DialogInterface, which: Int ->
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )

        }
        builder.setNegativeButton("DENY") { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        builder.setCancelable(false) // Prevent dismissal without choosing an option
        builder.create().show()
    }



    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
    private fun scanit() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan QR code")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(false)
        integrator.setBarcodeImageEnabled(false)
        integrator.initiateScan()
    }

    private val barcodeLauncher = registerForActivityResult<ScanOptions, ScanIntentResult>(
        ScanContract()
    ) { result: ScanIntentResult ->
        Toast.makeText(this, result.contents.toString(), Toast.LENGTH_LONG).show()
        if (result.contents == null) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG)
                .show()
            val wifiInfo = result.contents
            // Split the Wi-Fi info into name, SSID, and password
            val wifiData = wifiInfo.split(";")

            val name = wifiData[0]
            val ssid = wifiData[1]
            val password = wifiData[2]
            // Pass name, SSID, and password to the next activity
            val intent = Intent(this, QRDataActivity::class.java)
            intent.putExtra("Name", name)
            intent.putExtra("SSID", ssid)
            intent.putExtra("Password", password)
            startActivity(intent)
        }
    }

    override fun onItemSelected(position: Int) {
        if (position == 1) {
            val intent = Intent(this, WiFiListActivity::class.java)
            startActivity(intent)
        } else if (position == 3) {
            val intent = Intent(this, CoonectedDevicesActivity::class.java)
            startActivity(intent)
        }
        else if(position==2)
        {
            val intent = Intent(this, PassWordGenertorActivity::class.java)
            startActivity(intent)
        }
        else {
            val intent = Intent(this, WifiSignalLevelActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Scanned" + result.contents, Toast.LENGTH_SHORT).show()
                val wifiInfo = result.contents
                // Split the Wi-Fi info into name, SSID, and password
                val wifiData = wifiInfo.split(";")

                    val name = wifiData[0]
                    val ssid = wifiData[1]
                    val password = wifiData[2]
                    // Pass name, SSID, and password to the next activity
                    val intent = Intent(this, QRDataActivity::class.java)
                    intent.putExtra("Name", name)
                    intent.putExtra("SSID", ssid)
                    intent.putExtra("Password", password)
                    startActivity(intent)

            }
        } else
            super.onActivityResult(requestCode, resultCode, data)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Location permissions granted, proceed with location-related tasks
                    // e.g., Perform location-related tasks
                } else {
                    // Location permissions denied, handle accordingly
                    Toast.makeText(
                        this,
                        "Location permissions are required for this app to function properly.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Camera permission granted, proceed with camera-related tasks
                    // e.g., Perform camera-related tasks
                } else {
                    // Camera permission denied, handle accordingly
                    Toast.makeText(
                        this,
                        "Camera permission is required for this app to access the camera.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}