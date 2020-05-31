package com.adman.shadman.naqashishoo.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.adman.shadman.naqashishoo.Applications.Companion.context
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

class NetworkConnectionChecker(val context: Context){
    fun isConnected(): Boolean { // چک کردن اتصال دستگاه ب اینترنت
        var result = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            cm?.run {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                        result = when {
                            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                            else -> false
                        }
                    }
                } else {
                    cm?.run {
                        cm.activeNetworkInfo?.run {
                            if (type == ConnectivityManager.TYPE_WIFI) {
                                result = true
                            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                                result = true
                            }
                        }
                    }
                }
            }
        if (vpn())
            result=true

        return result
    }

    fun vpn(): Boolean { // چک کردن اتصال دستگاه به vpn
        var iface = ""
        try {
            for (networkInterface in Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isUp()) iface = networkInterface.getName()
                Log.d("DEBUG", "IFACE NAME: $iface")
                if (iface.contains("tun") || iface.contains("ppp") || iface.contains("pptp")) {
                    return true
                }
            }
        } catch (e1: SocketException) {
            e1.printStackTrace()
        }
        return false
    }
}