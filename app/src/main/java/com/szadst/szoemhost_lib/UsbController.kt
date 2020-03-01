/*
 * UsbController.java
 * This file is part of UsbController
 *
 * Copyright (C) 2012 - Manuel Di Cerbo
 *
 * UsbController is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * UsbController is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UsbController. If not, see <http://www.gnu.org/licenses/>.
 */
package com.szadst.szoemhost_lib

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*
import android.os.Parcelable
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import java.util.*

//import android.os.
/**
 * (c) Neuxs-Computing GmbH Switzerland
 * @author Manuel Di Cerbo, 02.02.2012
 */
class UsbController(parentActivity: Activity, private val mConnectionHandler: IUsbConnState, vid: Int, pid: Int) {
    private val mApplicationContext: Context
    private val mUsbManager: UsbManager
    private val VID: Int
    private val PID: Int
    private var m_nEPInSize = 0
    private var m_nEPOutSize = 0
    private val m_abyTransferBuf: ByteArray
    private var m_bInit = false
    //    private UsbDevice   m_usbDevice;
    private var m_usbConn: UsbDeviceConnection? = null
    private var m_usbIf: UsbInterface? = null
    private var m_epIN: UsbEndpoint? = null
    private var m_epOUT: UsbEndpoint? = null
    fun init() {
        enumerate(object : IPermissionListener {
            override fun onPermissionDenied(d: UsbDevice) {
                val usbman = mApplicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
                val pi = PendingIntent.getBroadcast(mApplicationContext, 0, Intent(ACTION_USB_PERMISSION), 0)
                mApplicationContext.registerReceiver(mPermissionReceiver, IntentFilter(ACTION_USB_PERMISSION))
                usbman.requestPermission(d, pi)
            }
        })
    }

    fun uninit() {
        if (m_usbConn != null) {
            m_usbConn!!.releaseInterface(m_usbIf)
            m_usbConn!!.close()
            m_usbConn = null
            m_bInit = false
        }
        //stop();
    }

    fun stop() {
        try {
            mApplicationContext.unregisterReceiver(mPermissionReceiver)
        } catch (e: IllegalArgumentException) {
        }
        //bravo
    }

    fun IsInit(): Boolean {
        return m_bInit
    }

    private fun enumerate(listener: IPermissionListener) {
        var bFound = false
        l("enumerating")
        val devlist = mUsbManager.deviceList
        val deviter: Iterator<UsbDevice> = devlist.values.iterator()
        while (deviter.hasNext()) {
            val d = deviter.next()
            l("Found device: " + String.format("%04X:%04X", d.vendorId, d.productId))
            Toast.makeText(mApplicationContext, "Found device: " + String.format("%04X:%04X", d.vendorId, d.productId), Toast.LENGTH_SHORT).show()
            //			DebugManage.WriteLog2("Found device: " + String.format("%04X:%04X", d.getVendorId(), d.getProductId()));
            if (d.vendorId == VID && d.productId == PID) {
                bFound = true
                l("Device under: " + d.deviceName)
                if (!mUsbManager.hasPermission(d)) {
                    Toast.makeText(mApplicationContext, "enumerate, hasPermission return false", Toast.LENGTH_SHORT).show()
                    listener.onPermissionDenied(d)
                } else {
                    Toast.makeText(mApplicationContext, "enumerate, GetConnInerface start", Toast.LENGTH_SHORT).show()
                    //startHandler(d);
                    GetConnInerface(d)
                    //TestComm(d);
                    return
                }
                break
            }
        }
        if (bFound == false) {
            Toast.makeText(mApplicationContext, "no more devices found", Toast.LENGTH_SHORT).show()
            //			DebugManage.WriteLog2("no more devices found");
            mConnectionHandler.onDeviceNotFound()
        }
    }

    private inner class PermissionReceiver(private val mPermissionListener: IPermissionListener) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mApplicationContext.unregisterReceiver(this)
            if (intent.action == ACTION_USB_PERMISSION) {
                if (!intent.getBooleanExtra(
                                UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    mPermissionListener.onPermissionDenied(intent
                            .getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice)
                    mConnectionHandler.onUsbPermissionDenied()
                } else {
                    l("Permission granted")
                    val dev = intent
                            .getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice
                    if (dev != null) {
                        if (dev.vendorId == VID
                                && dev.productId == PID) { //startHandler(dev);// has new thread
                            GetConnInerface(dev)
                            //TestComm(dev);
                        }
                    } else { //						DebugManage.WriteLog2("device not present!");
                        mConnectionHandler.onDeviceNotFound()
                    }
                }
            }
        }

    }

    private fun GetConnInerface(dev: UsbDevice) {
        var n: Int
        //Toast.makeText(mApplicationContext, "GetConnInerface start", Toast.LENGTH_SHORT).show();
//        m_usbDevice = dev;
        m_usbConn = mUsbManager.openDevice(dev)
        n = dev.interfaceCount
        if (n <= 0) return
        if (!m_usbConn!!.claimInterface(dev.getInterface(0), true)) {
            return
        }
        m_usbIf = dev.getInterface(0)
        n = m_usbIf!!.endpointCount
        if (n < 2) return
        for (i in 0 until n) {
            if (m_usbIf!!.getEndpoint(i).type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (m_usbIf!!.getEndpoint(i).direction == UsbConstants.USB_DIR_IN) m_epIN = m_usbIf!!.getEndpoint(i) else m_epOUT = m_usbIf!!.getEndpoint(i)
            }
        }
        m_nEPInSize = m_epIN!!.maxPacketSize
        m_nEPOutSize = m_epOUT!!.maxPacketSize
        m_bInit = true
        //m_epOUT.getMaxPacketSize();
//Toast.makeText(mApplicationContext, "GetConnInerface OK, Out Max Size="+m_nEPOutSize+" In Max Size=" + m_nEPInSize, Toast.LENGTH_SHORT).show();
//        DebugManage.WriteLog2("device connected");
        mConnectionHandler.onUsbConnected()
    }

    fun OperationInternal(pData: ByteArray, nDataLen: Int, nTimeOut: Int, bRead: Boolean): Boolean {
        val w_abyTmp = ByteArray(31)
        val w_abyCSW = ByteArray(13)
        var w_bRet: Boolean
        Arrays.fill(w_abyTmp, 0.toByte())
        w_abyTmp[0] = 0x55
        w_abyTmp[1] = 0x53
        w_abyTmp[2] = 0x42
        w_abyTmp[3] = 0x43
        w_abyTmp[4] = 0x28
        w_abyTmp[5] = 0x2b
        w_abyTmp[6] = 0x18
        w_abyTmp[7] = 0x89.toByte()
        w_abyTmp[8] = (nDataLen and 0xFF).toByte()
        w_abyTmp[9] = (nDataLen shr 8 and 0xFF).toByte()
        w_abyTmp[10] = (nDataLen shr 16 and 0xFF).toByte()
        w_abyTmp[11] = (nDataLen shr 24 and 0xFF).toByte()
        if (bRead) w_abyTmp[12] = 0x80.toByte() else w_abyTmp[12] = 0x00 //cCBWFlags
        w_abyTmp[13] = 0x00 //cCBWlun
        w_abyTmp[14] = 0x0a //cCBWCBLength
        w_abyTmp[15] = 0xef.toByte()
        if (bRead) w_abyTmp[16] = 0xff.toByte() else w_abyTmp[16] = 0xfe.toByte()
        // send 31bytes
        w_bRet = UsbBulkSend(w_abyTmp, 31, nTimeOut)
        if (!w_bRet) return false
        // read or write real data
        w_bRet = if (bRead) UsbBulkReceive(pData, nDataLen, nTimeOut) else UsbBulkSend(pData, nDataLen, nTimeOut)
        if (!w_bRet) return false
        // receive csw
        w_bRet = UsbBulkReceive(w_abyCSW, 13, nTimeOut)
        return w_bRet
    }

    fun UsbSCSIWrite(pCDB: ByteArray?, nCDBLen: Int, pData: ByteArray, nDataLen: Int, nTimeOut: Int): Boolean {
        val w_abyTmp = ByteArray(31)
        val w_abyCSW = ByteArray(13)
        var w_bRet: Boolean
        //Arrays.fill(w_abyTmp, (byte)0);
        w_abyTmp[0] = 0x55
        w_abyTmp[1] = 0x53
        w_abyTmp[2] = 0x42
        w_abyTmp[3] = 0x43
        w_abyTmp[4] = 0x28
        w_abyTmp[5] = 0x2b
        w_abyTmp[6] = 0x18
        w_abyTmp[7] = 0x89.toByte()
        w_abyTmp[8] = 0x00
        w_abyTmp[9] = 0x00
        w_abyTmp[10] = 0x00
        w_abyTmp[11] = 0x00
        w_abyTmp[12] = 0x00 //cCBWFlags
        w_abyTmp[13] = 0x00 //cCBWlun
        w_abyTmp[14] = 0x0a //cCBWCBLength
        System.arraycopy(pCDB!!, 0, w_abyTmp, 15, nCDBLen)
        //System.arraycopy(pData, 0, w_abyTmp, 31, nDataLen);
        w_bRet = UsbBulkSend(w_abyTmp, 31, nTimeOut)
        if (!w_bRet) return false
        w_bRet = UsbBulkSend(pData, nDataLen, nTimeOut)
        if (!w_bRet) return false
        // receive csw
        w_bRet = UsbBulkReceive(w_abyCSW, 13, nTimeOut)
        return w_bRet
    }

    fun UsbSCSIRead(pCDB: ByteArray?, nCDBLen: Int, pData: ByteArray?, nDataLen: Int, nTimeOut: Int): Boolean {
        var w_nTime: Long
        val w_abyTmp = ByteArray(31)
        val w_abyCSW = ByteArray(13)
        var w_bRet: Boolean
        //Arrays.fill(w_abyTmp, (byte)0);
        w_abyTmp[0] = 0x55
        w_abyTmp[1] = 0x53
        w_abyTmp[2] = 0x42
        w_abyTmp[3] = 0x43
        w_abyTmp[4] = 0x28
        w_abyTmp[5] = 0x2b
        w_abyTmp[6] = 0x18
        w_abyTmp[7] = 0x89.toByte()
        w_abyTmp[8] = 0x00
        w_abyTmp[9] = 0x00
        w_abyTmp[10] = 0x00
        w_abyTmp[11] = 0x00
        w_abyTmp[12] = 0x80.toByte() //cCBWFlags
        w_abyTmp[13] = 0x00 //cCBWlun
        w_abyTmp[14] = 0x0a //cCBWCBLength
        System.arraycopy(pCDB!!, 0, w_abyTmp, 15, nCDBLen)
        w_bRet = UsbBulkSend(w_abyTmp, 31, nTimeOut)
        if (!w_bRet) {
            return false
        }
        w_nTime = SystemClock.elapsedRealtime()
        w_bRet = UsbBulkReceive(pData, nDataLen, nTimeOut)
        w_nTime = SystemClock.elapsedRealtime() - w_nTime
        //Toast.makeText(mApplicationContext, "UsbSCSIRead, UsbBulkReceive Time : " + w_nTime , Toast.LENGTH_SHORT).show();
        if (!w_bRet) {
            return false
        }
        // receive csw
        w_bRet = UsbBulkReceive(w_abyCSW, 13, nTimeOut)
        return w_bRet
    }

    private fun UsbBulkSend(pBuf: ByteArray, nLen: Int, nTimeOut: Int): Boolean {
        var i: Int
        val n: Int
        val r: Int
        var w_nRet: Int
        //byte[] w_abyTmp = new byte[m_nEPOutSize];
        n = nLen / m_nEPOutSize
        r = nLen % m_nEPOutSize
        i = 0
        while (i < n) {
            System.arraycopy(pBuf, i * m_nEPOutSize, m_abyTransferBuf, 0, m_nEPOutSize)
            w_nRet = m_usbConn!!.bulkTransfer(m_epOUT, m_abyTransferBuf, m_nEPOutSize, nTimeOut)
            if (w_nRet != m_nEPOutSize) return false
            i++
        }
        if (r > 0) {
            System.arraycopy(pBuf, i * m_nEPOutSize, m_abyTransferBuf, 0, r)
            w_nRet = m_usbConn!!.bulkTransfer(m_epOUT, m_abyTransferBuf, r, nTimeOut)
            if (w_nRet != r) return false
        }
        return true
    }

    private fun UsbBulkReceive(pBuf: ByteArray?, nLen: Int, nTimeOut: Int): Boolean {
        var i: Int
        val n: Int
        val r: Int
        var w_nRet: Int
        //byte[] w_abyTmp = new byte[m_nEPInSize];
//w_nRet = m_usbConn.bulkTransfer(m_epIN, pBuf, nLen, nTimeOut);
//if (w_nRet != nLen)
//    return false;
        n = nLen / m_nEPInSize
        r = nLen % m_nEPInSize
        //Toast.makeText(mApplicationContext, "UsbBulkReceive, Buf Len = " + pBuf.length, Toast.LENGTH_SHORT).show();
        i = 0
        while (i < n) {
            w_nRet = m_usbConn!!.bulkTransfer(m_epIN, m_abyTransferBuf, m_nEPInSize, nTimeOut)
            if (w_nRet != m_nEPInSize) {
                return false
            }
            System.arraycopy(m_abyTransferBuf, 0, pBuf, i * m_nEPInSize, m_nEPInSize)
            i++
        }
        if (r > 0) {
            w_nRet = m_usbConn!!.bulkTransfer(m_epIN, m_abyTransferBuf, r, nTimeOut)
            if (w_nRet != r) {
                return false
            }
            System.arraycopy(m_abyTransferBuf, 0, pBuf, i * m_nEPInSize, r)
        }
        return true
    }

    // END MAIN LOOP
    private val mPermissionReceiver: BroadcastReceiver = PermissionReceiver(
            object : IPermissionListener {
                override fun onPermissionDenied(d: UsbDevice) {
                    l("Permission denied on " + d.deviceId)
                }
            })

    private interface IPermissionListener {
        fun onPermissionDenied(d: UsbDevice)
    }

    private fun l(msg: Any) {
        Log.d(TAG, ">==< $msg >==<")
    } //	private void e(Object msg) {

    //		Log.e(TAG, ">==< " + msg.toString() + " >==<");
//	}
    companion object {
        const val ACTION_USB_PERMISSION = "ch.serverbox.android.USB"
        const val TAG = "USBController"
    }

    /**
     * Activity is needed for onResult
     *
     * @param parentActivity
     */
    init {
        mApplicationContext = parentActivity.applicationContext
        mUsbManager = mApplicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
        VID = vid
        PID = pid
        m_abyTransferBuf = ByteArray(512)
        //		init();
    }
}