package model

import datamodel.INTERNET_ACCESS

interface NetworkObservable {
    fun attach(networkObserver: NetworkObserver)
    fun detach(networkObserver: NetworkObserver)
    fun notifyObservers(connectStatus: INTERNET_ACCESS)
    fun clear()
}