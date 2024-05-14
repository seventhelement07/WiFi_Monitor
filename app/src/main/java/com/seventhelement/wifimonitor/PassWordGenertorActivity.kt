package com.seventhelement.wifimonitor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.DhcpInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.seventhelement.wifimonitor.databinding.ActivityPassWordGenertorBinding

import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

class PassWordGenertorActivity : AppCompatActivity() {
    var name: String = ""
    private lateinit var handler: Handler
    private lateinit var timer: Timer
    private var wifiSettingsDialog: AlertDialog? = null
    private var lastWifiState: Boolean = false
    lateinit var binding: ActivityPassWordGenertorBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityPassWordGenertorBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fetchData()
        lastWifiState = isWifiAvailable()
        val editableName = Editable.Factory.getInstance().newEditable(name)
        binding.nonEditableTextInputEditText.text = editableName
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_copy)
        drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val editText: TextInputEditText = findViewById(R.id.nonEditableTextInputEditText)
        binding.nonEditableTextInputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null,
            null,
            drawable,
            null
        )
        editText.setCompoundDrawableClickListener(
            drawablePosition = 2, // 0 for left, 1 for top, 2 for right, 3 for bottom
            onClickListener = {
                // Perform copy operation here
                val textToCopy = editText.text.toString()
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Copied Text", textToCopy)
                clipboard.setPrimaryClip(clip)
               
                // Implement your copy logic here
            }
        )
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

    fun fetchData() {

        handler = Handler(Looper.getMainLooper())
        timer = Timer()
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcpInfo: DhcpInfo? = wifiManager.dhcpInfo
        // Check if WiFi is enabled
        if (wifiManager.isWifiEnabled) {
            // Get the current WiFi connection information
            val wifiInfo: WifiInfo? = wifiManager.connectionInfo
            if (wifiInfo != null) {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val yearString = currentYear.toString()
                name = wifiInfo.ssid + currentYear

                timer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        val wifiManager =
                            applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                        val wifiInfo: WifiInfo? = wifiManager.connectionInfo
                        val rssi = wifiInfo!!.ssid
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                        val yearString = currentYear.toString()
                        handler.post {

                            name = rssi + yearString.toString()
                            //Toast.makeText(this@WifiSignalLevelActivity,"Signal Strength: $rssi dBm",Toast.LENGTH_SHORT).show()
                        }
                    }
                }, 0, 1000) // Update every 1 seconds

            }
        } else {
            showWifiSettingsDialog()
        }
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

    fun TextInputEditText.setCompoundDrawableClickListener(
        drawablePosition: Int,
        onClickListener: () -> Unit
    ) {
        val drawable = compoundDrawablesRelative[drawablePosition]
        val extraPadding = 20 // Adjust this value as needed
        val delegateArea = Rect()

        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP && drawable != null) {
                delegateArea.set(
                    right - drawable.bounds.width() - extraPadding,
                    top,
                    right,
                    bottom
                )
                if (delegateArea.contains(event.x.toInt(), event.y.toInt())) {
                    onClickListener.invoke()
                    return@setOnTouchListener true
                }
            }
            false
        }

        val parent = parent as? TextView
        parent?.post {
            val touchDelegate = TouchDelegate(delegateArea, this)
            parent.touchDelegate = touchDelegate
        }
    }
}