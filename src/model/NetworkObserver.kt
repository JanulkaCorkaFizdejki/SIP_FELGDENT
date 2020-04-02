package model

import datamodel.INTERNET_ACCESS

interface NetworkObserver {
    fun update(internetAccess: INTERNET_ACCESS)
}