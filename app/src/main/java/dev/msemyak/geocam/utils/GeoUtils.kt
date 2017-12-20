package dev.msemyak.geocam.utils

import android.support.media.ExifInterface
import com.google.android.gms.maps.model.LatLng

fun latitudeRef(latitude: Double): String = if (latitude < 0.0) "S" else "N"

fun longitudeRef(longitude: Double): String = if (longitude < 0.0) "W" else "E"

fun convertGeoCoordsToDMS(geo_coordinate: Double): String {
    var geo_param = geo_coordinate
    geo_param = Math.abs(geo_param)
    val degree = geo_param.toInt()
    geo_param *= 60.0
    geo_param -= degree * 60.0
    val minute = geo_param.toInt()
    geo_param *= 60.0
    geo_param -= minute * 60.0
    val second = (geo_param * 1000.0).toInt()

    return StringBuilder(20).run {
        setLength(0)
        append(degree)
        append("/1,")
        append(minute)
        append("/1,")
        append(second)
        append("/1000,")
        toString()
    }
}

    fun saveGeoCoordinatesToFile(filename: String, latLng: LatLng) {
        Logga("Saving EXIF to $filename")
        val exif = ExifInterface(filename)
        with(exif) {
            setAttribute(ExifInterface.TAG_GPS_LATITUDE, convertGeoCoordsToDMS(latLng.latitude))
            setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitudeRef(latLng.latitude))
            setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertGeoCoordsToDMS(latLng.longitude))
            setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitudeRef(latLng.longitude))
            setAttribute(ExifInterface.TAG_SOFTWARE, "GEOCAM Android Demo")
            saveAttributes()
        }
    }
