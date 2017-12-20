package dev.msemyak.geocam.ui.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dev.msemyak.geocam.R
import kotlinx.android.synthetic.main.activity_map.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var myMap: GoogleMap
    private var myLatLng: LatLng = LatLng(50.433418, 30.519243)
    private lateinit var myMarker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val lon = intent.getStringExtra("lon")
        val lat = intent.getStringExtra("lat")
        if (!lon.isNullOrBlank() && !lon.equals("null", true) && !lat.isNullOrBlank() && !lat.equals("null", true)) {
            myLatLng = LatLng(lat.toDouble(), lon.toDouble())
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap

        setupMap()

        BTN_geodata_save.setOnClickListener {
            val returnIntent = Intent().apply {
                putExtra("lon", myMarker.position.longitude.toString())
                putExtra("lat", myMarker.position.latitude.toString())
            }
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

    }

    @SuppressLint("MissingPermission")
    private fun setupMap() {

        myMap.setOnCameraMoveListener {
            myMarker.position = myMap.cameraPosition.target
        }

        myMap.isMyLocationEnabled = true
        myMap.uiSettings.isZoomControlsEnabled = true
        myMap.uiSettings.isMyLocationButtonEnabled = true
        myMap.uiSettings.setAllGesturesEnabled(true)
        myMap.uiSettings.isCompassEnabled = true
        myMap.uiSettings.isMapToolbarEnabled = true

        myMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        val cameraPosition = CameraPosition.Builder().run {
            target(myLatLng)
            zoom(16f)
            bearing(0f)
            tilt(0f)
            build()
        }

        myMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        myMarker = myMap.addMarker(MarkerOptions().position(myLatLng).title(getString(R.string.photo_location)))
    }

}
