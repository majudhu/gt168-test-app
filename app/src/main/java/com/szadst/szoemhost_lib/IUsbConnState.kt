package com.szadst.szoemhost_lib

interface IUsbConnState {
    fun onUsbConnected()
    fun onUsbPermissionDenied()
    fun onDeviceNotFound()
}