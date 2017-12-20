package dev.msemyak.geocam.utils

import android.util.Log

fun Logga(str: String, tag: String = "LOGGA") {
    Log.d(tag, "--($tag)------> $str")
}
