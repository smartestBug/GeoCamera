package dev.msemyak.geocam.ui.preview

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.media.ExifInterface
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import dev.msemyak.geocam.AppBoss
import dev.msemyak.geocam.R
import dev.msemyak.geocam.ui.map.MapActivity
import dev.msemyak.geocam.utils.saveGeoCoordinatesToFile
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_preview.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.File


class PreviewActivity : AppCompatActivity() {

    private lateinit var filename: String
    private var imageLatLng: LatLng? = null

    private val MAP_ACTIVITY_RESULT_CODE = 77
    private val RC_ACCESS_LOCATION = 33
    private val permissionsNeeded = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

    private var locationSubscription: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        filename = if (savedInstanceState == null) intent.getStringExtra("filename") else savedInstanceState.getString("filename")

        if (EasyPermissions.hasPermissions(this, *permissionsNeeded)) {
            setupPreview()
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.permission_location), RC_ACCESS_LOCATION, *permissionsNeeded)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupPreview() {

        updateImageLatLngLabel()

        val orientation = ExifInterface(filename).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

        val imageBitmap = BitmapFactory.decodeFile(filename)

        val previewImageHeight = 2000
        val previewImageWidth = previewImageHeight * imageBitmap.width / imageBitmap.height

        val matrix = Matrix()
        matrix.postScale(previewImageWidth / imageBitmap.width.toFloat(), previewImageHeight / imageBitmap.height.toFloat())

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        IV_preview_image.setImageBitmap(Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.width, imageBitmap.height, matrix, true))

        if (!(getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            MaterialDialog.Builder(this)
                    .title(getString(R.string.gps_not_available))
                    .content(getString(R.string.gps_not_available_message))
                    .positiveText(getString(R.string.turn_gps_on))
                    .negativeText(getString(R.string.manually))
                    .onPositive { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                    .onNegative { _, _ -> startMapActivity() }
                    .cancelable(false)
                    .show()
        }

        if (imageLatLng == null) {
            val locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(5000)

            locationSubscription = AppBoss.rxLocation.location().updates(locationRequest)
                    .subscribe {
                        imageLatLng = LatLng(it.latitude, it.longitude)
                        updateImageLatLngLabel()
                    }
        }


        BTN_preview_geodata.setOnClickListener {
            startMapActivity()
        }

        BTN_preview_save.setOnClickListener {
            if (imageLatLng != null) {
                saveGeoCoordinatesToFile(filename, imageLatLng!!)
                promoteImageToGallery(filename)
                Toast.makeText(this, getString(R.string.image_saved), Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.set_location_coordinates), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startMapActivity() {
        startActivityForResult(Intent(this, MapActivity::class.java).apply {
            putExtra("lat", imageLatLng?.latitude.toString())
            putExtra("lon", imageLatLng?.longitude.toString())
        }, MAP_ACTIVITY_RESULT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MAP_ACTIVITY_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            val resultExtras = data?.extras

            locationSubscription?.dispose()

            imageLatLng = LatLng(resultExtras?.getString("lat")?.toDouble()!!, resultExtras.getString("lon").toDouble())

            updateImageLatLngLabel()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

        if (EasyPermissions.hasPermissions(this, *permissionsNeeded)) setupPreview()
        else {
            Toast.makeText(this, getString(R.string.permissions_cant_start_preview), Toast.LENGTH_LONG).show()
            onBackPressed()
        }
    }

    private fun updateImageLatLngLabel() {
        if (imageLatLng != null) {

            val lol = Location("")
            lol.latitude = imageLatLng?.latitude!!
            lol.longitude = imageLatLng?.longitude!!

            AppBoss.rxLocation.geocoding().fromLocation(lol).toObservable()
                    .subscribe(
                            /* onNext */
                            { address ->
                                TV_geodata.text = address.getAddressLine(0)
                            },
                            /* onError */
                            { _ ->
                                TV_geodata.text = String.format("%.4f, %.4f", imageLatLng?.latitude, imageLatLng?.longitude)
                            })
        } else {
            TV_geodata.text = getString(R.string.no_geodata)
        }

    }

    override fun onSaveInstanceState(outState: Bundle?) {

        outState?.putString("filename", filename)

        super.onSaveInstanceState(outState)
    }

    private fun promoteImageToGallery(path: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(path)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        sendBroadcast(mediaScanIntent)
    }

    override fun onBackPressed() {
        File(filename).let { if (it.exists()) it.delete() }
        super.onBackPressed()
    }

}
