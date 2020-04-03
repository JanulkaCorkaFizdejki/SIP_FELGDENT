package model

import datamodel.INTERNET_ACCESS
import java.io.IOException
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException

object CheckNetworkAccess {
    fun check () : INTERNET_ACCESS {
        val eni = NetworkInterface.getNetworkInterfaces()
        while (eni.hasMoreElements()) {
            val eia = eni.nextElement().inetAddresses
            while (eia.hasMoreElements()) {
                val ia = eia.nextElement()
                if (!ia.isAnyLocalAddress && !ia.isLoopbackAddress && !ia.isSiteLocalAddress) {
                    if (ia.hostName != ia.hostAddress) {
                        try {
                            val inetAddress: InetAddress = InetAddress.getByName("google.com")
                            if  (inetAddress.isReachable(200)) {
                                return INTERNET_ACCESS.CONNECTION
                            } else { return INTERNET_ACCESS.NO_CONNECTION }
                        } catch (ue: UnknownHostException) {
                            println(ue)
                            return INTERNET_ACCESS.NO_CONNECTION
                        }
                        catch (ioe: IOException) {
                            println(ioe)
                            return INTERNET_ACCESS.NO_CONNECTION
                        }
                    }
                }
            }
        }
        return INTERNET_ACCESS.NO_CONNECTION
    }
}