package com.szadst.szoemhost_lib

import android.R
import android.app.Activity
import android.content.Context
//import android.hardware.usb.UsbManager
import android.os.SystemClock
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import android_serialport_api.ComBean
import android_serialport_api.SerialHelper
import android_serialport_api.SerialPortFinder
//import cn.wch.ch34xuartdriver.CH34xUARTDriver
//import com.szadst.szoemhost_lib.DevComm.DispQueueThread
//import com.szadst.szoemhost_lib.DevComm.SerialControl
import java.io.IOException
import java.security.InvalidParameterException
import java.util.*

// Packet Prefix
const val CMD_PREFIX_CODE = 0xAA55
const val RCM_PREFIX_CODE = 0x55AA
const val CMD_DATA_PREFIX_CODE = 0xA55A
const val RCM_DATA_PREFIX_CODE = 0x5AA5
// Command
const val CMD_VERIFY_CODE = 0x0101
const val CMD_IDENTIFY_CODE = 0x0102
const val CMD_ENROLL_CODE = 0x0103
const val CMD_ENROLL_ONETIME_CODE = 0x0104
const val CMD_CLEAR_TEMPLATE_CODE = 0x0105
const val CMD_CLEAR_ALLTEMPLATE_CODE = 0x0106
const val CMD_GET_EMPTY_ID_CODE = 0x0107
const val CMD_GET_BROKEN_TEMPLATE_CODE = 0x0109
const val CMD_READ_TEMPLATE_CODE = 0x010A
const val CMD_WRITE_TEMPLATE_CODE = 0x010B
const val CMD_GET_FW_VERSION_CODE = 0x0112
const val CMD_FINGER_DETECT_CODE = 0x0113
const val CMD_FEATURE_OF_CAPTURED_FP_CODE = 0x011A
const val CMD_IDENTIFY_TEMPLATE_WITH_FP_CODE = 0x011C
//const val CMD_SLED_CTRL_CODE = 0x0124
const val CMD_IDENTIFY_FREE_CODE = 0x0125
const val CMD_SET_DEVPASS_CODE = 0x0126
const val CMD_VERIFY_DEVPASS_CODE = 0x0127
const val CMD_GET_ENROLL_COUNT_CODE = 0x0128
const val CMD_CHANGE_TEMPLATE_CODE = 0x0129
const val CMD_UP_IMAGE_CODE = 0x012C
const val CMD_VERIFY_WITH_DOWN_TMPL_CODE = 0x012D
const val CMD_IDENTIFY_WITH_DOWN_TMPL_CODE = 0x012E
const val CMD_FP_CANCEL_CODE = 0x0130
const val CMD_ADJUST_SENSOR_CODE = 0x0137
const val CMD_IDENTIFY_WITH_IMAGE_CODE = 0x0138
const val CMD_VERIFY_WITH_IMAGE_CODE = 0x0139
const val CMD_SET_PARAMETER_CODE = 0x013A
const val CMD_EXIT_DEVPASS_CODE = 0x013B
// public static final int     CMD_SET_COMMNAD_VALID_FLAG_CODE			     = (0x013C);
// public static final int     CMD_GET_COMMNAD_VALID_FLAG_CODE			     = (0x013D);
const val CMD_TEST_CONNECTION_CODE = 0x0150
const val CMD_ENTERSTANDBY_CODE = 0x0155
const val RCM_INCORRECT_COMMAND_CODE = 0x0160
const val CMD_ENTER_ISPMODE_CODE = 0x0171
// Error Code
const val ERR_SUCCESS = 0
const val ERR_FAIL = 1
//const val ERR_CONTINUE = 2
const val ERR_COMM_FAIL = 3
const val ERR_VERIFY = 0x11
const val ERR_IDENTIFY = 0x12
const val ERR_TMPL_EMPTY = 0x13
const val ERR_TMPL_NOT_EMPTY = 0x14
const val ERR_ALL_TMPL_EMPTY = 0x15
const val ERR_EMPTY_ID_NOEXIST = 0x16
const val ERR_BROKEN_ID_NOEXIST = 0x17
const val ERR_INVALID_TMPL_DATA = 0x18
const val ERR_DUPLICATION_ID = 0x19
const val ERR_TOO_FAST = 0x20
const val ERR_BAD_QUALITY = 0x21
const val ERR_SMALL_LINES = 0x22
const val ERR_TIME_OUT = 0x23
const val ERR_NOT_AUTHORIZED = 0x24
const val ERR_GENERALIZE = 0x30
//const val ERR_COM_TIMEOUT = 0x40
const val ERR_FP_CANCEL = 0x41
const val ERR_INTERNAL = 0x50
const val ERR_MEMORY = 0x51
const val ERR_EXCEPTION = 0x52
const val ERR_INVALID_TMPL_NO = 0x60
const val ERR_INVALID_PARAM = 0x70
const val ERR_NO_RELEASE = 0x71
const val ERR_INVALID_OPERATION_MODE = 0x72
const val ERR_NOT_SET_PWD = 0x74
const val ERR_FP_NOT_DETECTED = 0x75
const val ERR_ADJUST_SENSOR = 0x76
// Return Value
const val GD_NEED_FIRST_SWEEP = 0xFFF1
const val GD_NEED_SECOND_SWEEP = 0xFFF2
const val GD_NEED_THIRD_SWEEP = 0xFFF3
const val GD_NEED_RELEASE_FINGER = 0xFFF4
//const val GD_TEMPLATE_NOT_EMPTY = 0x01
//const val GD_TEMPLATE_EMPTY = 0x00
const val GD_DETECT_FINGER = 0x01
const val GD_NO_DETECT_FINGER = 0x00
const val GD_DOWNLOAD_SUCCESS = 0xA1
// Packet
const val MAX_DATA_LEN = 600 /*512*/
const val CMD_PACKET_LEN = 22
//const val ST_COMMAND_LEN = 66
//const val IMAGE_RECEIVE_UINT = 498
const val DATA_SPLIT_UNIT = 498
const val ID_USER_TEMPLATE_SIZE = 498
// Template
const val GD_MAX_RECORD_COUNT = 5000
const val GD_TEMPLATE_SIZE = 570
const val GD_RECORD_SIZE = GD_TEMPLATE_SIZE // + 2)	// CkeckSum len = 2
const val GD_MAX_RECORD_SIZE = 900
//--------------- For Usb Communication ------------//
const val SCSI_TIMEOUT = 3000
const val GD_MAX_FP_TIME_OUT = 60
const val COMM_SLEEP_TIME = 100
const val ONCE_UP_IMAGE_UINT = 60000
const val COMM_TIMEOUT = 15000
const val VID = 0x2009
const val PID = 0x7638
// UART ch34xuartdriver
//private const val UART_ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION"

//import com.szadst.szoemhost_lib.LibDebugManage;
class DevComm(private val m_parentAcitivity: Activity, usbConnState: IUsbConnState, p_spDevice: Spinner) {

    //    var m_uartDriver: CH34xUARTDriver
    private var mbSendPacketWork = false
    private var mnPacketSize = 0
    //    var m_bySrcDeviceID: Byte = 1
//    var m_byDstDeviceID: Byte = 1
    var mAbyPacket = ByteArray(64 * 1024)
    private var mAbyPacket2 = ByteArray(MAX_DATA_LEN + 10)
    private var mAbypackettmp = ByteArray(64 * 1024)
    //--------------------------------------------------//
    private val mApplicationContext: Context = m_parentAcitivity.applicationContext
    // USB
    private val mUsbbase: UsbController = UsbController(m_parentAcitivity, usbConnState, VID, PID)
    private var mPwritebuffer: ByteArray
    private var mPreadbuffer: ByteArray
    var mPuartreadbuf: ByteArray
    var mNuartreadlen: Int
    var mBbufferhandle = false
    // Serial Port
    private var mSerialPortFinder //�����豸����
            : SerialPortFinder
    var dispQueue //ˢ����ʾ�߳�
            : DispQueueThread
    private var mSerialport: SerialControl
    // Connection
    var mNconnected // 0 : Not Connected, 1 : UART, 2 : USB, 3 : ttyUART
            : Byte
//    var m_dbgInfo: LibDebugManage? = null

    init { //    	LibDebugManage.DeleteLog();
        // USB Init
        // UART Driver Init
//        m_uartDriver = CH34xUARTDriver(
//                mApplicationContext.getSystemService(Context.USB_SERVICE) as UsbManager, m_parentAcitivity,
//                UART_ACTION_USB_PERMISSION)
        // USB Support Check
// 		if (!m_uartDriver.UsbFeatureSupported())
// 		{
// 			Dialog dialog = new AlertDialog.Builder(m_parentAcitivity)
// 					.setTitle("Alert")
// 					.setMessage("Your device doesn't support USB HOST, Please run on the other device.")
// 					.setPositiveButton("OK",
// 							new DialogInterface.OnClickListener() {
// 								@Override
// 								public void onClick(DialogInterface arg0,
// 										int arg1) {
// 									System.exit(0);
// 								}
// 							}).create();
// 			dialog.setCanceledOnTouchOutside(false);
// 			dialog.show();
// 		}
// Buffer Init
        mNconnected = 0
        mNuartreadlen = 0
        mPwritebuffer = ByteArray(MAX_DATA_LEN)
        mPreadbuffer = ByteArray(MAX_DATA_LEN)
        mPuartreadbuf = ByteArray(MAX_DATA_LEN)
        dispQueue = DispQueueThread()
        dispQueue.start()
        mSerialport = SerialControl()
        mSerialPortFinder = SerialPortFinder()
        val entryValues = mSerialPortFinder.allDevicesPath
        val allDevices: MutableList<String?> = ArrayList()
        allDevices.add("USB")
//        allDevices.add("CH34xUART")
        for (i in entryValues.indices) {
            allDevices.add(entryValues[i])
            //			LibDebugManage.WriteLog2(entryValues[i]);
        }
        val aspnDevices = ArrayAdapter(m_parentAcitivity, R.layout.simple_spinner_item, allDevices)
        p_spDevice.adapter = aspnDevices
    }


    //    fun devcommInit(parentActivity: Activity?, usbConnState: IUsbConnState?, p_spDevice: Spinner): Int {
    fun devCommInit(p_spDevice: Spinner): Int {
        mNconnected = 0
        mNuartreadlen = 0
        val entryValues = mSerialPortFinder.allDevicesPath
        val allDevices: MutableList<String?> = ArrayList()
        allDevices.add("USB")
//        allDevices.add("CH34xUART")
        for (i in entryValues.indices) {
            allDevices.add(entryValues[i])
        }
        val aspnDevices = ArrayAdapter(m_parentAcitivity, R.layout.simple_spinner_item, allDevices)
        p_spDevice.adapter = aspnDevices
        return 0
    }

    fun isInit(): Boolean {
        return if (mNconnected.toInt() == 0) false else if (mNconnected.toInt() == 1) true else if (mNconnected.toInt() == 2) mUsbbase.IsInit() else true
    }

    fun openComm(p_szDevice: String, p_nBaudrate: Int): Boolean {
        if (mNconnected.toInt() != 0) return false
        if (p_szDevice === "USB") // USB
        {
            if (!mUsbbase.IsInit()) mUsbbase.init()
            if (!mUsbbase.IsInit()) return false
            mNconnected = 2
        }
//        else if (p_szDevice === "CH34xUART") // UART
//        {
//            if (!m_uartDriver.ResumeUsbList()) // ResumeUsbList��������ö��CH34X�豸�Լ�������豸
//            {
//                Toast.makeText(mApplicationContext, "Open UART device failed!", Toast.LENGTH_SHORT).show()
//                m_uartDriver.CloseDevice()
//                return false
//            } else {
//                if (!m_uartDriver.UartInit()) {
//                    Toast.makeText(mApplicationContext, "Initialize UART device failed!", Toast.LENGTH_SHORT).show()
//                    Toast.makeText(mApplicationContext, "Open UART device failed!", Toast.LENGTH_SHORT).show()
//                    return false
//                }
//                if (!m_uartDriver.SetConfig(p_nBaudrate, 8.toByte(), 1.toByte(), 0.toByte(), 0.toByte())) {
//                    Toast.makeText(mApplicationContext, "Configuration UART device failed!", Toast.LENGTH_SHORT).show()
//                    Toast.makeText(mApplicationContext, "Open UART device failed!", Toast.LENGTH_SHORT).show()
//                    return false
//                }
//                Toast.makeText(mApplicationContext, "Open UART device success!", Toast.LENGTH_SHORT).show()
//                m_nConnected = 1
//                m_nUARTReadLen = 0
//                UART_ReadThread().start()
//            }
//        }
        else  // ttyUART
        {
            mSerialport.port = p_szDevice
            mSerialport.baudRate = p_nBaudrate
            try {
                mSerialport.open()
            } catch (e: SecurityException) {
                Toast.makeText(mApplicationContext, "Open ttyUART device failed!", Toast.LENGTH_SHORT).show()
                return false
            } catch (e: IOException) {
                Toast.makeText(mApplicationContext, "Open ttyUART device failed!", Toast.LENGTH_SHORT).show()
                return false
            } catch (e: InvalidParameterException) {
                Toast.makeText(mApplicationContext, "Open ttyUART device failed!", Toast.LENGTH_SHORT).show()
                return false
            }
            mNconnected = 3
        }
        return true
    }

    fun closeComm(): Boolean {
        when {
            mNconnected.toInt() == 0 -> {
                return false
            }
            mNconnected.toInt() == 1 // UART
            -> {
//            m_uartDriver.CloseDevice()
                mNconnected = 0
            }
            mNconnected.toInt() == 2 // USB
            -> {
                mNconnected = 0
                mUsbbase.uninit()
            }
            else  // ttyUART
            -> {
                mSerialport.stopSend()
                mSerialport.close()
                mNconnected = 0
            }
        }
        return true
    }
    /** */
    /** */
    fun runTestConnection(): Int {
        initPacket(CMD_TEST_CONNECTION_CODE.toShort(), true)
        addCheckSum(true)
        val wBRet: Boolean = sendCommand(CMD_TEST_CONNECTION_CODE.toShort())
        if (!wBRet) {
            return ERR_COMM_FAIL
        }
        return if (getRetCode().toInt() != ERR_SUCCESS) {
            ERR_FAIL
        } else ERR_SUCCESS
    }
    /** */
    /** */
    fun runGetDeviceInfo(): Int {
        return ERR_SUCCESS
    }

//    fun GetDeviceInformation(deviceInfo: Array<String?>): Boolean {
//        val w_nRecvLen = IntArray(1)
//        val w_abyPCCmd = ByteArray(6)
//        val w_abyData = ByteArray(32)
//        val w_strTmp: String
//        var w_bRet: Boolean
//        Arrays.fill(w_abyPCCmd, 0.toByte())
//        w_abyPCCmd[2] = 0x04
//        w_bRet = SendPackage(w_abyPCCmd, w_abyData)
//        //Toast.makeText(mApplicationContext, "GetDeviceInformation, SendPackage ret = " + w_bRet, Toast.LENGTH_SHORT).show();
//        if (!w_bRet) {
//            return false
//        }
//        w_bRet = RecvPackage(w_abyData, w_nRecvLen)
//        //Toast.makeText(mApplicationContext, "GetDeviceInformation, RecvPackage : " + w_bRet, Toast.LENGTH_SHORT).show();
//        if (!w_bRet) {
//            return false
//        }
//        w_strTmp = String(w_abyData)
//        deviceInfo[0] = w_strTmp
//        //Toast.makeText(mApplicationContext, "GetDeviceInformation, Recv Data : " + w_strTmp, Toast.LENGTH_SHORT).show();
//        return true
//    }

//    private fun SendPackage(pPCCmd: ByteArray, pData: ByteArray): Boolean {
//        val nDataLen: Int
//        pPCCmd[0] = 0xEF.toByte()
//        pPCCmd[1] = 0x01
//        nDataLen = (pPCCmd[5].toInt() and 0xFF shl 8 and 0x0000FF00 or (pPCCmd[4].toInt() and 0x000000FF))
//        return mUsbbase.UsbSCSIWrite(pPCCmd, 6, pData, nDataLen, 5000)
//    }

//    private fun RecvPackage(pData: ByteArray, pLevRen: IntArray): Boolean {
//        val w_nLen: Int
//        val w_abyPCCmd = ByteArray(6)
//        val w_abyRespond = ByteArray(4)
//        var w_bRet: Boolean
//        w_abyPCCmd[0] = 0xEF.toByte()
//        w_abyPCCmd[1] = 0x02
//        w_abyPCCmd[2] = 0
//        w_abyPCCmd[3] = 0
//        w_abyPCCmd[4] = 0
//        w_abyPCCmd[5] = 0
//        // receive status
//        w_bRet = mUsbbase.UsbSCSIRead(w_abyPCCmd, 6, w_abyRespond, 4, 5000)
//        if (!w_bRet) return false
//        // receive data
////w_nLen = (int)((w_abyRespond[3] << 8) | w_abyRespond[2]);
//        w_nLen = ((w_abyRespond[3].toInt() shl 8 and 0x0000FF00) as Int or (w_abyRespond[2].toInt() and 0x000000FF) as Int)
//        if (w_nLen > 0) { //w_nTime = SystemClock.elapsedRealtime();
//            w_abyPCCmd[1] = 0x03
//            w_bRet = mUsbbase.UsbSCSIRead(w_abyPCCmd, 6, pData, w_nLen, 5000)
//            //w_nTime = SystemClock.elapsedRealtime() - w_nTime;
//            if (!w_bRet) return false
//            pLevRen[0] = w_nLen
//        }
//        return true
//    }

    /***************************************************************************
     * Get Return Code
     */
    fun getRetCode(): Short {
        return ((mAbyPacket[7].toInt() shl 8 and 0x0000FF00) or (mAbyPacket[6].toInt() and 0x000000FF)).toShort()
    }

    /***************************************************************************
     * Get Data Length
     */
    fun getDataLen(): Short {
        return (mAbyPacket[5].toInt() shl 8 and 0x0000FF00 or (mAbyPacket[4].toInt() and 0x000000FF)).toShort()
    }

    /***************************************************************************
     * Set Data Length
     */
    fun setDataLen(p_wDataLen: Short) {
        mAbyPacket[4] = (p_wDataLen.toInt() and 0xFF).toByte()
        mAbyPacket[5] = (p_wDataLen.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
    }

    fun setDataLen2(p_wDataLen: Short) {
        mAbyPacket2[4] = (p_wDataLen.toInt() and 0xFF).toByte()
        mAbyPacket2[5] = (p_wDataLen.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
    }

    /***************************************************************************
     * Set Command Data
     */
    fun setCmdData(p_wData: Short, p_bFirst: Boolean) {
        if (p_bFirst) {
            mAbyPacket[6] = (p_wData.toInt() and 0xFF).toByte()
            mAbyPacket[7] = (p_wData.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
        } else {
            mAbyPacket[8] = (p_wData.toInt() and 0xFF).toByte()
            mAbyPacket[9] = (p_wData.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
        }
    }

    /***************************************************************************
     * Get Command Data
     */
    fun getCmdData(p_bFirst: Boolean): Short {
        return if (p_bFirst) {
            (mAbyPacket[7].toInt() shl 8 and 0x0000FF00 or (mAbyPacket[6].toInt() and 0x000000FF)).toShort()
        } else {
            (mAbyPacket[9].toInt() shl 8 and 0x0000FF00 or (mAbyPacket[8].toInt() and 0x000000FF)).toShort()
        }
    }

    /***************************************************************************
     * Get Data Packet Length
     */
    private fun getDataPacketLen(): Short {
        return (mAbyPacket[5].toInt() shl 8 and 0x0000FF00 or (mAbyPacket[4].toInt() and 0x000000FF) + 6).toShort()
    }

    /***************************************************************************
     * Make Packet
     */
    fun initPacket(p_wCmd: Short, p_bCmdData: Boolean) {
        memSet(mAbyPacket, 0.toByte(), CMD_PACKET_LEN)
        //g_pPacketBuffer->wPrefix = p_bCmdData?CMD_PREFIX_CODE:CMD_DATA_PREFIX_CODE;
        if (p_bCmdData) {
            mAbyPacket[0] = (CMD_PREFIX_CODE and 0xFF).toByte()
            mAbyPacket[1] = (CMD_PREFIX_CODE and 0xFF00 shr 8 and 0xFF).toByte()
        } else {
            mAbyPacket[0] = (CMD_DATA_PREFIX_CODE and 0xFF).toByte()
            mAbyPacket[1] = (CMD_DATA_PREFIX_CODE and 0xFF00 shr 8 and 0xFF).toByte()
        }
        //g_pPacketBuffer->wCMD_RCM = p_wCMD;
        mAbyPacket[2] = (p_wCmd.toInt() and 0xFF).toByte()
        mAbyPacket[3] = (p_wCmd.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
    }

    fun initPacket2(p_wCmd: Short, p_bCmdData: Boolean) {
        memSet(mAbyPacket2, 0.toByte(), CMD_PACKET_LEN)
        //g_pPacketBuffer->wPrefix = p_bCmdData?CMD_PREFIX_CODE:CMD_DATA_PREFIX_CODE;
        if (p_bCmdData) {
            mAbyPacket2[0] = (CMD_PREFIX_CODE and 0xFF).toByte()
            mAbyPacket2[1] = (CMD_PREFIX_CODE and 0xFF00 shr 8 and 0xFF).toByte()
        } else {
            mAbyPacket2[0] = (CMD_DATA_PREFIX_CODE and 0xFF).toByte()
            mAbyPacket2[1] = (CMD_DATA_PREFIX_CODE and 0xFF00 shr 8 and 0xFF).toByte()
        }
        //g_pPacketBuffer->wCMD_RCM = p_wCMD;
        mAbyPacket2[2] = (p_wCmd.toInt() and 0xFF).toByte()
        mAbyPacket2[3] = (p_wCmd.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
    }

    /***************************************************************************
     * Set CheckSum
     */
    fun addCheckSum(p_bCmdData: Boolean): Short {
        val wWlen: Short = if (p_bCmdData) CMD_PACKET_LEN.toShort() else getDataPacketLen()
        var wWret = 0
        var wNi = 0
        while (wNi < wWlen) {
            wWret += (mAbyPacket[wNi].toInt() and 0xFF).toShort()
            wNi++
        }
        mAbyPacket[wWlen.toInt()] = (wWret and 0xFF).toByte()
        mAbyPacket[wWlen + 1] = (wWret and 0xFF00 shr 8 and 0xFF).toByte()
        return wWret.toShort()
    }

    fun addCheckSum2(p_bCmdData: Boolean): Short {
        var wRet = 0
        val wLen: Short = if (p_bCmdData) CMD_PACKET_LEN.toShort() else getDataPacketLen()
        var nI = 0
        while (nI < wLen) {
            wRet += mAbyPacket2[nI].toInt() and 0xFF
            nI++
        }
        mAbyPacket2[wLen.toInt()] = (wRet and 0xFF).toByte()
        mAbyPacket2[wLen + 1] = (wRet and 0xFF00 shr 8 and 0xFF).toByte()
        return wRet.toShort()
    }

    //--------------------------- Send, Receive Communication Packet Functions ---------------------//
    fun sendCommand(p_wCmd: Short): Boolean {
        return if (mNconnected.toInt() == 2) usbSendPacket(p_wCmd) else false
//        if (m_nConnected.toInt() == 1 || m_nConnected.toInt() == 3) UART_SendCommand(p_wCmd) else

    }
    /** */
    /** */
    fun sendDataPacket(p_wCmd: Short): Boolean {
        return if (mNconnected.toInt() == 2) usbSendDataPacket(p_wCmd) else false
//        if (m_nConnected.toInt() == 1 || m_nConnected.toInt() == 3) UART_SendDataPacket(p_wCmd) else
    }
    /** */
    /** */
    fun receiveDataPacket(p_wCmd: Short): Boolean {
        return if (mNconnected.toInt() == 2) usbReceiveDataPacket(p_wCmd) else false
//        if (m_nConnected.toInt() == 1 || m_nConnected.toInt() == 3) UART_ReceiveDataPacket(p_wCmd) else
    }

    //------------------------------------------ USB Functions -------------------------------------//
    fun usbSendPacket(wCMD: Short): Boolean {
        val btCDB = ByteArray(8)
        val bRet: Boolean
        Arrays.fill(btCDB, 0.toByte())
        btCDB[0] = 0xEF.toByte()
        btCDB[1] = 0x11
        btCDB[4] = (CMD_PACKET_LEN + 2).toByte()
        while (mbSendPacketWork) {
            SystemClock.sleep(1)
        }
        mbSendPacketWork = true
        bRet = mUsbbase.UsbSCSIWrite(btCDB, 8, mAbyPacket, (CMD_PACKET_LEN + 2), SCSI_TIMEOUT)
        mbSendPacketWork = false
        return if (!bRet) false else usbReceiveAck(wCMD)
    }
    /** */
    /** */
    fun usbSendPacket2(): Boolean {
        val btCDB = ByteArray(8)
        val wbRet: Boolean
        Arrays.fill(btCDB, 0.toByte())
        btCDB[0] = 0xEF.toByte()
        btCDB[1] = 0x11
        btCDB[4] = (CMD_PACKET_LEN + 2).toByte()
        while (mbSendPacketWork) {
            SystemClock.sleep(1)
        }
        mbSendPacketWork = true
        wbRet = mUsbbase.UsbSCSIWrite(btCDB, 8, mAbyPacket2, (CMD_PACKET_LEN + 2), SCSI_TIMEOUT)
        mbSendPacketWork = false
        return wbRet
    }
    /** */
    /** */
    fun usbReceiveAck(p_wCmd: Short): Boolean {
        var wnLen: Int
        val btCDB = ByteArray(8)
        val wAbyWaitPacket = ByteArray(CMD_PACKET_LEN + 2)
        var wDwtimeout = SCSI_TIMEOUT
        if (p_wCmd.toInt() == CMD_VERIFY_CODE || p_wCmd.toInt() == CMD_IDENTIFY_CODE || p_wCmd.toInt() == CMD_IDENTIFY_FREE_CODE || p_wCmd.toInt() == CMD_ENROLL_CODE || p_wCmd.toInt() == CMD_ENROLL_ONETIME_CODE) wDwtimeout = (GD_MAX_FP_TIME_OUT + 1) * 1000
        Arrays.fill(btCDB, 0.toByte())
        //w_nReadCount = GetReadWaitTime(p_byCMD);
        Arrays.fill(wAbyWaitPacket, 0xAF.toByte())
        do {
            Arrays.fill(mAbyPacket, 0.toByte())
            btCDB[0] = 0xEF.toByte()
            btCDB[1] = 0x12.toByte()
            wnLen = CMD_PACKET_LEN + 2
            if (!mUsbbase.UsbSCSIRead(btCDB, 8, mAbyPacket, wnLen, wDwtimeout)) return false
            SystemClock.sleep(COMM_SLEEP_TIME.toLong())
        } while (memcmp(mAbyPacket, wAbyWaitPacket, CMD_PACKET_LEN + 2))
        mnPacketSize = wnLen
        return checkReceive(this, RCM_PREFIX_CODE.toShort(), p_wCmd)
    }
    /** */
    /** */
    fun usbReceiveAck2(): Boolean {
        var wNlen: Int
        val btCDB = ByteArray(8)
        val wAbywaitpacket = ByteArray(CMD_PACKET_LEN + 2)
        val wDwtimeout = SCSI_TIMEOUT
        Arrays.fill(btCDB, 0.toByte())
        Arrays.fill(wAbywaitpacket, 0xAF.toByte())
        do {
            Arrays.fill(mAbyPacket2, 0.toByte())
            btCDB[0] = 0xEF.toByte()
            btCDB[1] = 0x12.toByte()
            wNlen = CMD_PACKET_LEN + 2
            if (!mUsbbase.UsbSCSIRead(btCDB, 8, mAbyPacket2, wNlen, wDwtimeout)) return false
            SystemClock.sleep(COMM_SLEEP_TIME.toLong())
        } while (memcmp(mAbyPacket2, wAbywaitpacket, CMD_PACKET_LEN + 2))
        mnPacketSize = wNlen
        //    	if (!CheckReceive((short)RCM_PREFIX_CODE, p_wCmd))
//    		return false;
        return true
    }
    /** */
    /** */
    private fun usbReceiveDataAck(p_wCmd: Short): Boolean {
        val btCDB = ByteArray(8)
        val wWaitpacket = ByteArray(8)
        var wNlen: Int
        var wDwtimeout = COMM_TIMEOUT
        if (p_wCmd.toInt() == CMD_VERIFY_CODE || p_wCmd.toInt() == CMD_IDENTIFY_CODE || p_wCmd.toInt() == CMD_IDENTIFY_FREE_CODE || p_wCmd.toInt() == CMD_ENROLL_CODE || p_wCmd.toInt() == CMD_ENROLL_ONETIME_CODE) wDwtimeout = (GD_MAX_FP_TIME_OUT + 1) * 1000
        memSet(btCDB, 0.toByte(), 8)
        memSet(wWaitpacket, 0xAF.toByte(), 8)
        Arrays.fill(mAbypackettmp, 0.toByte())
        do {
            btCDB[0] = 0xEF.toByte()
            btCDB[1] = 0x15
            wNlen = 6
            if (!mUsbbase.UsbSCSIRead(btCDB, 8, mAbyPacket, wNlen, wDwtimeout)) {
                return false
            }
            SystemClock.sleep(COMM_SLEEP_TIME.toLong())
        } while (memcmp(mAbyPacket, wWaitpacket, 6))
        do {
            wNlen = getDataLen() + 2
            if (!usbReceiveRawData(mAbypackettmp, wNlen)) {
                return false
            }
            System.arraycopy(mAbypackettmp, 0, mAbyPacket, 6, wNlen)
            SystemClock.sleep(COMM_SLEEP_TIME.toLong())
        } while (memcmp(mAbyPacket, wWaitpacket, 4))
        return checkReceive(this, RCM_DATA_PREFIX_CODE.toShort(), p_wCmd)
    }
    /** */
    /** */
    fun usbSendDataPacket(wCMD: Short): Boolean {
        val btCDB = ByteArray(8)
        val wWlen = (getDataPacketLen() + 2).toShort()
        memSet(btCDB, 0.toByte(), 8)
        btCDB[0] = 0xEF.toByte()
        btCDB[1] = 0x13
        btCDB[4] = (wWlen.toInt() and 0xFF).toByte()
        btCDB[5] = (wWlen.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
        return if (!mUsbbase.UsbSCSIWrite(btCDB, 8, mAbyPacket, getDataPacketLen() + 2, SCSI_TIMEOUT)) false else usbReceiveDataAck(wCMD)
    }
    /** */
    /** */
    fun usbReceiveDataPacket(wCMD: Short): Boolean {
        return usbReceiveDataAck(wCMD)
    }
    /** */
    /** */
    private fun usbReceiveRawData(pBuffer: ByteArray?, nDataLen: Int): Boolean {
        val btCDB = ByteArray(8)
        memSet(btCDB, 0.toByte(), 8)
        btCDB[0] = 0xEF.toByte()
        btCDB[1] = 0x14.toByte()
        return mUsbbase.UsbSCSIRead(btCDB, 8, pBuffer, nDataLen, SCSI_TIMEOUT)
    }
    /** */
    /** */
    fun usbReceiveImage(p_pBuffer: ByteArray, p_dwDataLen: Int): Boolean {
        val btCDB = ByteArray(8)
        val wWaitpacket = ByteArray(8)
        var wNi: Int
        var wNindex: Int
        var wNremaincount: Int
        val wPtmpimgbuf = ByteArray(ONCE_UP_IMAGE_UINT)
        memSet(btCDB, 0.toByte(), 8)
        memSet(wWaitpacket, 0xAF.toByte(), 8)
        if (p_dwDataLen == 208 * 288 || p_dwDataLen == 242 * 266 || p_dwDataLen == 202 * 258 || p_dwDataLen == 256 * 288) {
            wNindex = 0
            wNremaincount = p_dwDataLen
            wNi = 0
            while (wNremaincount > ONCE_UP_IMAGE_UINT) {
                btCDB[0] = 0xEF.toByte()
                btCDB[1] = 0x16
                btCDB[2] = (wNi and 0xFF).toByte()
                if (!mUsbbase.UsbSCSIRead(btCDB, 8, wPtmpimgbuf, ONCE_UP_IMAGE_UINT, SCSI_TIMEOUT)) return false
                System.arraycopy(wPtmpimgbuf, 0, p_pBuffer, wNindex, ONCE_UP_IMAGE_UINT)
                wNremaincount -= ONCE_UP_IMAGE_UINT
                wNindex += ONCE_UP_IMAGE_UINT
                wNi++
            }
            btCDB[0] = 0xEF.toByte()
            btCDB[1] = 0x16
            btCDB[2] = (wNi and 0xFF).toByte()
            if (!mUsbbase.UsbSCSIRead(btCDB, 8, wPtmpimgbuf, wNremaincount, SCSI_TIMEOUT)) return false
            System.arraycopy(wPtmpimgbuf, 0, p_pBuffer, wNindex, wNremaincount)
        }
        return true
    }
    /** */
    /** */
    fun usbDownImage(pBuf: ByteArray?, nBufLen: Int): Boolean {
        val wpImgBuf = ByteArray(ONCE_UP_IMAGE_UINT)
        val btCDB = ByteArray(8)
        var wnIndex = 0
        var wnRemainCount: Int = nBufLen
        var wnI = 0
        memSet(btCDB, 0.toByte(), 8)
        while (wnRemainCount > ONCE_UP_IMAGE_UINT) {
            btCDB[0] = 0xEF.toByte()
            btCDB[1] = 0x17
            btCDB[2] = 0
            btCDB[3] = (wnI and 0xFF).toByte()
            btCDB[4] = loByte((ONCE_UP_IMAGE_UINT and 0x00FF).toShort())
            btCDB[5] = hiByte((ONCE_UP_IMAGE_UINT and 0xFF00).toShort())
            System.arraycopy(pBuf!!, wnIndex, wpImgBuf, 0, ONCE_UP_IMAGE_UINT)
            if (!mUsbbase.UsbSCSIWrite(btCDB, 6, wpImgBuf, ONCE_UP_IMAGE_UINT, SCSI_TIMEOUT)) return false
            wnRemainCount -= ONCE_UP_IMAGE_UINT
            wnIndex += ONCE_UP_IMAGE_UINT
            wnI++
        }
        btCDB[0] = 0xEF.toByte()
        btCDB[1] = 0x17
        btCDB[2] = 0
        btCDB[3] = (wnI and 0xFF).toByte()
        btCDB[4] = loByte((wnRemainCount and 0x00FF).toShort())
        btCDB[5] = hiByte((wnRemainCount and 0xFF00).toShort())
        System.arraycopy(pBuf!!, wnIndex, wpImgBuf, 0, wnRemainCount)
        return mUsbbase.UsbSCSIWrite(btCDB, 6, wpImgBuf, wnRemainCount, SCSI_TIMEOUT)
    }

//    //------------------------------------------ UART Functions -------------------------------------//
//    fun UART_SendCommand(p_wCmd: Short): Boolean {
//        var w_nResult = 0
//        if (m_nConnected.toInt() == 1) {
//            w_nResult = m_uartDriver.WriteData(m_abyPacket, CMD_PACKET_LEN + 2)
//            if (w_nResult < 0) {
//                return false
//            }
//        } else if (m_nConnected.toInt() == 3) {
//            val w_pData = ByteArray(CMD_PACKET_LEN + 2)
//            System.arraycopy(m_abyPacket, 0, w_pData, 0, CMD_PACKET_LEN + 2)
//            m_SerialPort.send(w_pData)
//        }
//        return UART_ReceiveAck(p_wCmd, true)
//    }
//    /** */
//    /** */
//    fun UART_SendCommand2(wCMD: Short): Boolean {
//        var w_nResult = 0
//        if (m_nConnected.toInt() == 1) {
//            w_nResult = m_uartDriver.WriteData(m_abyPacket2, CMD_PACKET_LEN + 2)
//            if (w_nResult < 0) {
//                return false
//            }
//        } else if (m_nConnected.toInt() == 3) {
//            val w_pData = ByteArray(CMD_PACKET_LEN + 2)
//            System.arraycopy(m_abyPacket2, 0, w_pData, 0, CMD_PACKET_LEN + 2)
//            m_SerialPort.send(w_pData)
//        }
//        return true
//    }
//    /** */
//    /** */
//    fun UART_ReceiveAck(p_wCmd: Short, p_bCmdData: Boolean): Boolean { //    	int	w_nResult = 0;
//        var w_nReadLen = 0
//        val w_nTotalLen = CMD_PACKET_LEN + 2
//        var w_nTmpLen: Int
//        val w_nTime: Long
//        var i: Int
//        w_nTime = System.currentTimeMillis()
//        while (w_nReadLen < w_nTotalLen) { //	    	w_nResult = m_uartDriver.ReadData(m_abyPacket, CMD_PACKET_LEN + 2);
//            if (System.currentTimeMillis() - w_nTime > 10000) {
//                m_nUARTReadLen = 0
//                return false
//            }
//            i = 0
//            while (m_bBufferHandle) {
//                i++
//                if (i < 10000) break
//            }
//            m_bBufferHandle = true
//            if (m_nUARTReadLen <= 0) continue
//            if (w_nTotalLen - w_nReadLen < m_nUARTReadLen) {
//                w_nTmpLen = w_nTotalLen - w_nReadLen
//                System.arraycopy(m_pUARTReadBuf, 0, m_abyPacket, w_nReadLen, w_nTmpLen)
//                w_nReadLen += w_nTmpLen
//                m_nUARTReadLen = m_nUARTReadLen - w_nTmpLen
//                System.arraycopy(m_pUARTReadBuf, w_nTmpLen, m_abyPacketTmp, 0, m_nUARTReadLen)
//                System.arraycopy(m_abyPacketTmp, 0, m_pUARTReadBuf, 0, m_nUARTReadLen)
//            } else {
//                System.arraycopy(m_pUARTReadBuf, 0, m_abyPacket, w_nReadLen, m_nUARTReadLen)
//                w_nReadLen += m_nUARTReadLen
//                m_nUARTReadLen = 0
//            }
//            m_bBufferHandle = false
//        }
//        return if (p_bCmdData) CheckReceive(RCM_PREFIX_CODE.toShort(), p_wCmd) else CheckReceive(RCM_DATA_PREFIX_CODE.toShort(), p_wCmd)
//    }
//    /** */
//    /** */
//    fun UART_ReceiveAck2(p_wCmd: Short): Boolean { //    	int	w_nResult = 0;
//        var w_nReadLen = 0
//        val w_nTotalLen = CMD_PACKET_LEN + 2
//        var w_nTmpLen: Int
//        val w_nTime: Long
//        w_nTime = System.currentTimeMillis()
//        while (w_nReadLen < w_nTotalLen) { //	    	w_nResult = m_uartDriver.ReadData(m_abyPacket2, CMD_PACKET_LEN + 2);
//            if (System.currentTimeMillis() - w_nTime > 10000) {
//                m_nUARTReadLen = 0
//                return false
//            }
//            if (m_nUARTReadLen <= 0) continue
//            if (w_nTotalLen - w_nReadLen < m_nUARTReadLen) {
//                w_nTmpLen = w_nTotalLen - w_nReadLen
//                System.arraycopy(m_pUARTReadBuf, 0, m_abyPacket2, w_nReadLen, w_nTmpLen)
//                w_nReadLen += w_nTmpLen
//                m_nUARTReadLen = m_nUARTReadLen - w_nTmpLen
//                System.arraycopy(m_pUARTReadBuf, w_nTmpLen, m_abyPacketTmp, 0, m_nUARTReadLen)
//                System.arraycopy(m_abyPacketTmp, 0, m_pUARTReadBuf, 0, m_nUARTReadLen)
//            } else {
//                System.arraycopy(m_pUARTReadBuf, 0, m_abyPacket2, w_nReadLen, m_nUARTReadLen)
//                w_nReadLen += m_nUARTReadLen
//                m_nUARTReadLen = 0
//            }
//        }
//        return true
//    }
//    /** */
//    /** */
//    fun UART_ReceiveDataAck(p_wCmd: Short): Boolean {
//        if (!UART_ReadDataN(m_abyPacket, 0, 6)) return false
//        return if (!UART_ReadDataN(m_abyPacket, 6, GetDataLen() + 2)) false else CheckReceive(RCM_DATA_PREFIX_CODE.toShort(), p_wCmd)
//    }
//    /** */
//    /** */
//    fun UART_SendDataPacket(p_wCmd: Short): Boolean {
//        var w_nSendCnt = 0
//        if (m_nConnected.toInt() == 1) {
//            w_nSendCnt = m_uartDriver.WriteData(m_abyPacket, GetDataLen() + 8)
//            if (w_nSendCnt < 0) return false
//        } else if (m_nConnected.toInt() == 3) {
//            val w_nLen = GetDataLen() + 8
//            val w_pData = ByteArray(w_nLen)
//            System.arraycopy(m_abyPacket, 0, w_pData, 0, w_nLen)
//            m_SerialPort.send(w_pData)
//        }
//        return UART_ReceiveDataAck(p_wCmd)
//    }
//    /** */
//    /** */
//    fun UART_ReceiveDataPacket(p_wCmd: Short): Boolean {
//        return UART_ReceiveDataAck(p_wCmd)
//    }
//    /** */
//    /** */
//    fun UART_ReceiveData(p_wCmd: Short, p_nDataLen: Int, p_pBuffer: ByteArray?): Boolean {
//        var w_nReceivedCnt: Int
//        var w_wPacketDataLen = 0
//        w_nReceivedCnt = 0
//        while (w_nReceivedCnt < p_nDataLen) {
//            w_wPacketDataLen = p_nDataLen - w_nReceivedCnt
//            if (w_wPacketDataLen > MAX_DATA_LEN) w_wPacketDataLen = MAX_DATA_LEN
//            if (UART_ReceiveDataPacket(p_wCmd) == false) return false
//            System.arraycopy(m_abyPacket, 8, p_pBuffer, w_nReceivedCnt, GetDataLen() + 4)
//            w_nReceivedCnt += w_wPacketDataLen
//        }
//        return true
//    }
//    /** */
//    /** */
//    fun UART_ReadDataN(p_pData: ByteArray?, p_nStart: Int, p_nLen: Int): Boolean { //    	int		w_nAckCnt = 0;
//        var w_nRecvLen: Int
//        var w_nTotalRecvLen: Int
//        var w_nTmpLen: Int
//        val w_nTime: Long
//        w_nRecvLen = p_nLen
//        w_nTotalRecvLen = 0
//        w_nTime = System.currentTimeMillis()
//        while (w_nTotalRecvLen < p_nLen) { //    		w_nAckCnt = m_uartDriver.ReadData(m_abyPacketTmp, w_nRecvLen);
////    		if (w_nAckCnt < 0)
////    			return false;
//            if (System.currentTimeMillis() - w_nTime > 10000) {
//                m_nUARTReadLen = 0
//                return false
//            }
//            if (m_nUARTReadLen <= 0) continue
//            if (p_nLen - w_nTotalRecvLen < m_nUARTReadLen) {
//                w_nTmpLen = p_nLen - w_nTotalRecvLen
//                System.arraycopy(m_pUARTReadBuf, 0, p_pData, p_nStart + w_nTotalRecvLen, w_nTmpLen)
//                w_nRecvLen = w_nRecvLen - w_nTmpLen
//                w_nTotalRecvLen = w_nTotalRecvLen + w_nTmpLen
//                m_nUARTReadLen = m_nUARTReadLen - w_nTmpLen
//                System.arraycopy(m_pUARTReadBuf, w_nTmpLen, m_abyPacketTmp, 0, m_nUARTReadLen)
//                System.arraycopy(m_abyPacketTmp, 0, m_pUARTReadBuf, 0, m_nUARTReadLen)
//            } else {
//                System.arraycopy(m_pUARTReadBuf, 0, p_pData, p_nStart + w_nTotalRecvLen, m_nUARTReadLen)
//                w_nRecvLen = w_nRecvLen - m_nUARTReadLen
//                w_nTotalRecvLen = w_nTotalRecvLen + m_nUARTReadLen
//                m_nUARTReadLen = 0
//            }
//        }
//        return true
//    }
//    /** */
//    /** */
//    inner class UART_ReadThread : Thread() {
//        override fun run() {
//            while (true) {
//                if (m_nConnected.toInt() != 1) break
//                if (m_nUARTReadLen > 0) continue
//                m_nUARTReadLen = m_uartDriver.ReadData(m_pUARTReadBuf, MAX_DATA_LEN)
//            }
//        }
//    }

    //----------------------------------------------------���ڿ�����
    inner class SerialControl : SerialHelper() {
        //		public SerialControl(String sPort, String sBaudRate){

        //			super(sPort, sBaudRate);
//		} : SerialHelper() {
        override fun onDataReceived(ComRecData: ComBean) { //���ݽ�����������ʱ��������̣�����Ῠ��,���ܺ�6410����ʾ�����й�
//ֱ��ˢ����ʾ��������������ʱ���������ԣ�����������ʾͬ����
//���̶߳�ʱˢ����ʾ���Ի�ý���������ʾЧ�������ǽ��������ٶȿ�����ʾ�ٶ�ʱ����ʾ���ͺ�
//����Ч�����-_-���̶߳�ʱˢ���Ժ�һЩ��
            dispQueue.addQueue(ComRecData) //�̶߳�ʱˢ����ʾ(�Ƽ�)
            /*
                runOnUiThread(new Runnable()//ֱ��ˢ����ʾ
                {
                    public void run()
                    {
                        DispRecData(ComRecData);
                    }
                });*/
        }
    }

    //----------------------------------------------------ˢ����ʾ�߳�
    inner class DispQueueThread : Thread() {
        private val queueList: Queue<ComBean> = LinkedList()
        override fun run() {
            super.run()
            while (!isInterrupted) {
                var i: Int
                while (true) {
                    var comData: ComBean?
                    if (queueList.poll().also { comData = it } == null) break
                    i = 0
                    while (mBbufferhandle) {
                        i++
                        if (i > 10000) break
                    }
                    mBbufferhandle = true
                    System.arraycopy(comData!!.bRec!!, 0, mPuartreadbuf, mNuartreadlen, comData!!.nSize)
                    mNuartreadlen += comData!!.nSize
                    mBbufferhandle = false
                    //		        	break;
                }
            }
        }

        @Synchronized
        fun addQueue(ComData: ComBean) {
            queueList.add(ComData)
        }

    }

    companion object {
        /***************************************************************************
         * Get CheckSum
         */
        private fun getCheckSum(devComm: DevComm, p_bCmdData: Boolean): Short {
            var wni: Int
            var wWRet = 0
            if (p_bCmdData) {
                wni = 0
                while (wni < CMD_PACKET_LEN) {
                    wWRet += (devComm.mAbyPacket[wni].toInt() and 0xFF).toShort()
                    wni++
                }
            } else {
                wni = 0
                while (wni < devComm.getDataPacketLen()) {
                    wWRet += (devComm.mAbyPacket[wni].toInt() and 0xFF).toShort()
                    wni++
                }
            }
            return wWRet.toShort()
        }

        /***************************************************************************
         * Check Packet
         */
        fun checkReceive(devComm: DevComm, p_wPrefix: Short, p_wCmd: Short): Boolean {
            val wCheckSum: Short
            // Check Prefix Code
            val wTmpPrefix: Short = (devComm.mAbyPacket[1].toInt() shl 8 and 0x0000FF00 or (devComm.mAbyPacket[0].toInt() and 0x000000FF)).toShort()
            val wTmpCmd: Short = (devComm.mAbyPacket[3].toInt() shl 8 and 0x0000FF00 or (devComm.mAbyPacket[2].toInt() and 0x000000FF)).toShort()
            //    	if ( g_pPacketBuffer->wCMD_RCM != CMD_FP_CANCEL_CODE )
            if (p_wPrefix != wTmpPrefix || p_wCmd != wTmpCmd) {
                return false
            }

            val wLen: Short = if (p_wPrefix.toInt() == RCM_PREFIX_CODE) CMD_PACKET_LEN.toShort() else devComm.getDataPacketLen()
            wCheckSum = (devComm.mAbyPacket[wLen + 1].toInt() shl 8 and 0x0000FF00 or (devComm.mAbyPacket[wLen.toInt()].toInt() and 0x000000FF)).toShort()
            return wCheckSum == getCheckSum(devComm, p_wPrefix.toInt() == RCM_PREFIX_CODE)
        }

        /** */
        fun memcmp(p1: ByteArray, p2: ByteArray, nLen: Int): Boolean {
            var i = 0
            while (i < nLen) {
                if (p1[i] != p2[i]) return false
                i++
            }
            return true
        }

        /** */

        fun memSet(p1: ByteArray?, nValue: Byte, nLen: Int) {
            Arrays.fill(p1!!, 0, nLen, nValue)
        }

//        fun memCpy(p1: ByteArray?, nValue: Byte, nLen: Int) {
//            Arrays.fill(p1, 0, nLen, nValue)
//        }

        fun makeWord(low: Byte, high: Byte): Short {
            return (high.toInt() and 0x00FF shl 8 and 0x0000FF00 or (low.toInt() and 0x000000FF)).toShort()
        }

        fun loByte(s: Short): Byte {
            return (s.toInt() and 0xFF).toByte()
        }

        fun hiByte(s: Short): Byte {
            return (s.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
        }
    }
}