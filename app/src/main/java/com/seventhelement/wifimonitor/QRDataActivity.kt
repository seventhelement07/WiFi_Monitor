package com.seventhelement.wifimonitor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.seventhelement.wifimonitor.databinding.ActivityQrdataBinding

class QRDataActivity : AppCompatActivity() {
    lateinit var binding: ActivityQrdataBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityQrdataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.nametext.text= intent.getStringExtra("Name")
        binding.passwordtext.text = intent.getStringExtra("Password")
    }
}