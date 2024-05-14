package com.seventhelement.wifimonitor.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NetworkScanner {

    private var BASE_IP_ADDRESS = "192.168" // Example subnet
    private val TIMEOUT_MS = 500
    private val NUM_THREADS = 20

    private lateinit var executorService: ExecutorService
    private val activeDevices = ArrayList<String>()

    fun scanNetwork(listener: OnScanCompleteListener,baseurl:String) {
        BASE_IP_ADDRESS=baseurl
        executorService = Executors.newFixedThreadPool(NUM_THREADS)
        for (j in 0..1) {
            for (i in 1..255) {
                val host = "$BASE_IP_ADDRESS.$j.$i"
                executorService.execute(PingRunnable(host, listener))
            }
        }
        Log.d("TAG", "scanNetwork: Scan Started")
        // Shutdown the executor when all tasks are complete
        executorService.shutdown()
    }

    private inner class PingRunnable(private val host: String, private val listener: OnScanCompleteListener) : Runnable {
        override fun run() {
            try {
                val inetAddress = InetAddress.getByName(host)
                if (inetAddress.isReachable(TIMEOUT_MS)) {
                    synchronized(activeDevices) {
                        activeDevices.add(host)
                    }
                    listener.onDeviceFound(host)
                }
            } catch (e: UnknownHostException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    interface OnScanCompleteListener {
        fun onDeviceFound(ipAddress: String)
    }

    fun getActiveDevices(): ArrayList<String> {
        return activeDevices
    }
}