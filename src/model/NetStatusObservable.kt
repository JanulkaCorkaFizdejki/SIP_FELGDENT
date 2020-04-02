package model

import datamodel.INTERNET_ACCESS
import java.io.IOException
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException
import java.util.*

class NetStatusObservable : NetworkObservable {

    private val timer = Timer()
    private var observers = hashSetOf<NetworkObserver>()
    private var internetAccess: INTERNET_ACCESS = INTERNET_ACCESS.INIT

    init {
        checkConnect()
    }


    override fun attach(networkObserver: NetworkObserver) {
        observers.add(networkObserver)
    }

    override fun detach(networkObserver: NetworkObserver) {
        observers.remove(networkObserver)
    }

    override fun notifyObservers(connectStatus: INTERNET_ACCESS) {
        observers.forEach{it
            it.update(connectStatus)
        }
    }

    override fun clear() {
        timer.cancel()
    }


    private fun checkConnect() {
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                var currentConnection = getConnection()
                if (currentConnection != internetAccess) {
                    notifyObservers(currentConnection)
                    internetAccess = currentConnection
                }


            }
        }
        timer.schedule(task, 500, 500)
    }

    private fun getConnection () : INTERNET_ACCESS {
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