package dev.msemyak.geocam.ui.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import dev.msemyak.geocam.R
import dev.msemyak.geocam.ui.camera.CameraActivity
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity() {

    private val RC_CAMERA_AND_STORAGE = 22
    private val permissionsNeeded = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        BTN_start_camera.setOnClickListener {
            if (EasyPermissions.hasPermissions(this, *permissionsNeeded)) {
                slideOutActivity()
            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.permission_camera), RC_CAMERA_AND_STORAGE, *permissionsNeeded)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

        if (EasyPermissions.hasPermissions(this, *permissionsNeeded)) slideOutActivity()
        else Toast.makeText(this, getString(R.string.permissions_cant_start_camera), Toast.LENGTH_LONG).show()
    }

    private fun slideOutActivity() {
        startActivity(Intent(this, CameraActivity::class.java))
    }

}
