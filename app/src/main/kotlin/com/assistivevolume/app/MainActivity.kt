package com.assistivevolume.app

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 1001
        private const val REQUEST_DND_PERMISSION = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnToggle = findViewById<Button>(R.id.btnToggleService)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        // Update status text based on whether service is running
        updateStatus(tvStatus)

        btnToggle.setOnClickListener {
            if (FloatingService.isRunning) {
                stopService(Intent(this, FloatingService::class.java))
                updateStatus(tvStatus)
                Toast.makeText(this, "Tombol mengambang dimatikan", Toast.LENGTH_SHORT).show()
            } else {
                checkPermissionsAndStart()
            }
        }
    }

    private fun checkPermissionsAndStart() {
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Check Overlay Permission
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
            return
        }

        // 2. Check Do Not Disturb (DND) Access (required to toggle ringer mode)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
            Toast.makeText(this, "Mohon izinkan 'Akses Jangan Ganggu' agar fitur Profil Suara berfungsi", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivityForResult(intent, REQUEST_DND_PERMISSION)
            return
        }

        // If both permissions are granted, start service
        startFloatingService()
        updateStatus(tvStatus)
    }

    private fun startFloatingService() {
        val intent = Intent(this, FloatingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun updateStatus(tvStatus: TextView) {
        if (FloatingService.isRunning) {
            tvStatus.text = "✅ Tombol Aktif"
        } else {
            tvStatus.text = "⭕ Tombol Tidak Aktif"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION || requestCode == REQUEST_DND_PERMISSION) {
            // Re-evaluate permissions after returning from settings
            if (Settings.canDrawOverlays(this)) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notificationManager.isNotificationPolicyAccessGranted) {
                    startFloatingService()
                    updateStatus(findViewById(R.id.tvStatus))
                }
            } else {
                Toast.makeText(this, "Izin diperlukan agar aplikasi berfungsi penuh", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus(findViewById(R.id.tvStatus))
    }
}
