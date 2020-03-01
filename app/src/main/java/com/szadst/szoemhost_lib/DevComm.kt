package com.szadst.szoemhost_lib

import android.R
import android.app.Activity
import android.content.Context
import android.hardware.usb.UsbManager
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
const val CMD_SLED_CTRL_CODE = 0x0124
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
const val ERR_CONTINUE = 2
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
const val ERR_COM_TIMEOUT = 0x40
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
const val GD_TEMPLATE_NOT_EMPTY = 0x01
const val GD_TEMPLATE_EMPTY = 0x00
const val GD_DETECT_FINGER = 0x01
const val GD_NO_DETECT_FINGER = 0x00
const val GD_DOWNLOAD_SUCCESS = 0xA1
// Packet
const val MAX_DATA_LEN = 600 /*512*/
const val CMD_PACKET_LEN = 22
const val ST_COMMAND_LEN = 66
const val IMAGE_RECEIVE_UINT = 498
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
private const val VID = 0x2009
private const val PID = 0x7638
// UART ch34xuartdriver
private const val UART_ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION"

//import com.szadst.szoemhost_lib.LibDebugManage;
class DevComm(private val m_parentAcitivity: Activity, usbConnState: IUsbConnState, p_spDevice: Spinner) {

    //    var m_uartDriver: CH34xUARTDriver
    var m_bSendPacketWork = false
    var m_nPacketSize = 0
    var m_bySrcDeviceID: Byte = 1
    var m_byDstDeviceID: Byte = 1
    var m_abyPacket = ByteArray(64 * 1024)
    var m_abyPacket2 = ByteArray(MAX_DATA_LEN + 10)
    var m_abyPacketTmp = ByteArray(64 * 1024)
    //--------------------------------------------------//
    private val mApplicationContext: Context
    // USB
    private val m_usbBase: UsbController
    var m_pWriteBuffer: ByteArray
    var m_pReadBuffer: ByteArray
    var m_pUARTReadBuf: ByteArray
    var m_nUARTReadLen: Int
    var m_bBufferHandle = false
    // Serial Port
    var mSerialPortFinder //�����豸����
            : SerialPortFinder
    var DispQueue //ˢ����ʾ�߳�
            : DispQueueThread
    var m_SerialPort: SerialControl
    // Connection
    var m_nConnected // 0 : Not Connected, 1 : UART, 2 : USB, 3 : ttyUART
            : Byte
    var m_dbgInfo: LibDebugManage? = null

    init { //    	LibDebugManage.DeleteLog();
        mApplicationContext = m_parentAcitivity.applicationContext
        // USB Init
        m_usbBase = UsbController(m_parentAcitivity, usbConnState, VID, PID)
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
        m_nConnected = 0
        m_nUARTReadLen = 0
        m_pWriteBuffer = ByteArray(MAX_DATA_LEN)
        m_pReadBuffer = ByteArray(MAX_DATA_LEN)
        m_pUARTReadBuf = ByteArray(MAX_DATA_LEN)
        DispQueue = DispQueueThread()
        DispQueue.start()
        m_SerialPort = SerialControl()
        mSerialPortFinder = SerialPortFinder()
        val entryValues = mSerialPortFinder.allDevicesPath
        val allDevices: MutableList<String?> = ArrayList()
        allDevices.add("USB")
//        allDevices.add("CH34xUART")
        for (i in entryValues!!.indices) {
            allDevices.add(entryValues!![i])
            //			LibDebugManage.WriteLog2(entryValues[i]);
        }
        val aspnDevices = ArrayAdapter(m_parentAcitivity, R.layout.simple_spinner_item, allDevices)
        p_spDevice.adapter = aspnDevices
    }


    fun DevComm_Init(parentActivity: Activity?, usbConnState: IUsbConnState?, p_spDevice: Spinner): Int {
        m_nConnected = 0
        m_nUARTReadLen = 0
        val entryValues = mSerialPortFinder.allDevicesPath
        val allDevices: MutableList<String?> = ArrayList()
        allDevices.add("USB")
//        allDevices.add("CH34xUART")
        for (i in entryValues!!.indices) {
            allDevices.add(entryValues[i])
        }
        val aspnDevices = ArrayAdapter(m_parentAcitivity, R.layout.simple_spinner_item, allDevices)
        p_spDevice.adapter = aspnDevices
        return 0
    }

    fun IsInit(): Boolean {
        return if (m_nConnected.toInt() == 0) false else if (m_nConnected.toInt() == 1) true else if (m_nConnected.toInt() == 2) m_usbBase.IsInit() else true
    }

    fun OpenComm(p_szDevice: String, p_nBaudrate: Int): Boolean {
        if (m_nConnected.toInt() != 0) return false
        if (p_szDevice === "USB") // USB
        {
            if (!m_usbBase.IsInit()) m_usbBase.init()
            if (!m_usbBase.IsInit()) return false
            m_nConnected = 2
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
            m_SerialPort.port = p_szDevice
            m_SerialPort.baudRate = p_nBaudrate
            try {
                m_SerialPort.open()
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
            m_nConnected = 3
        }
        return true
    }

    fun CloseComm(): Boolean {
        if (m_nConnected.toInt() == 0) {
            return false
        } else if (m_nConnected.toInt() == 1) // UART
        {
//            m_uartDriver.CloseDevice()
            m_nConnected = 0
        } else if (m_nConnected.toInt() == 2) // USB
        {
            m_nConnected = 0
            m_usbBase.uninit()
        } else  // ttyUART
        {
            m_SerialPort.stopSend()
            m_SerialPort.close()
            m_nConnected = 0
        }
        return true
    }
    /** */
    /** */
    fun Run_TestConnection(): Int {
        val w_bRet: Boolean
        InitPacket(CMD_TEST_CONNECTION_CODE.toShort(), true)
        AddCheckSum(true)
        w_bRet = Send_Command(CMD_TEST_CONNECTION_CODE.toShort())
        if (!w_bRet) {
            return ERR_COMM_FAIL
        }
        return if (GetRetCode().toInt() != ERR_SUCCESS) {
            ERR_FAIL
        } else ERR_SUCCESS
    }
    /** */
    /** */
    fun Run_GetDeviceInfo(): Int {
        return ERR_SUCCESS
    }

    fun GetDeviceInformation(deviceInfo: Array<String?>): Boolean {
        val w_nRecvLen = IntArray(1)
        val w_abyPCCmd = ByteArray(6)
        val w_abyData = ByteArray(32)
        val w_strTmp: String
        var w_bRet: Boolean
        Arrays.fill(w_abyPCCmd, 0.toByte())
        w_abyPCCmd[2] = 0x04
        w_bRet = SendPackage(w_abyPCCmd, w_abyData)
        //Toast.makeText(mApplicationContext, "GetDeviceInformation, SendPackage ret = " + w_bRet, Toast.LENGTH_SHORT).show();
        if (!w_bRet) {
            return false
        }
        w_bRet = RecvPackage(w_abyData, w_nRecvLen)
        //Toast.makeText(mApplicationContext, "GetDeviceInformation, RecvPackage : " + w_bRet, Toast.LENGTH_SHORT).show();
        if (!w_bRet) {
            return false
        }
        w_strTmp = String(w_abyData)
        deviceInfo[0] = w_strTmp
        //Toast.makeText(mApplicationContext, "GetDeviceInformation, Recv Data : " + w_strTmp, Toast.LENGTH_SHORT).show();
        return true
    }

    private fun SendPackage(pPCCmd: ByteArray, pData: ByteArray): Boolean {
        val nDataLen: Int
        pPCCmd[0] = 0xEF.toByte()
        pPCCmd[1] = 0x01
        nDataLen = (pPCCmd[5].toInt() and 0xFF shl 8 and 0x0000FF00 or (pPCCmd[4].toInt() and 0x000000FF))
        return m_usbBase.UsbSCSIWrite(pPCCmd, 6, pData, nDataLen, 5000)
    }

    private fun RecvPackage(pData: ByteArray, pLevRen: IntArray): Boolean {
        val w_nLen: Int
        val w_abyPCCmd = ByteArray(6)
        val w_abyRespond = ByteArray(4)
        var w_bRet: Boolean
        w_abyPCCmd[0] = 0xEF.toByte()
        w_abyPCCmd[1] = 0x02
        w_abyPCCmd[2] = 0
        w_abyPCCmd[3] = 0
        w_abyPCCmd[4] = 0
        w_abyPCCmd[5] = 0
        // receive status
        w_bRet = m_usbBase.UsbSCSIRead(w_abyPCCmd, 6, w_abyRespond, 4, 5000)
        if (!w_bRet) return false
        // receive data
//w_nLen = (int)((w_abyRespond[3] << 8) | w_abyRespond[2]);
        w_nLen = ((w_abyRespond[3].toInt() shl 8 and 0x0000FF00) as Int or (w_abyRespond[2].toInt() and 0x000000FF) as Int)
        if (w_nLen > 0) { //w_nTime = SystemClock.elapsedRealtime();
            w_abyPCCmd[1] = 0x03
            w_bRet = m_usbBase.UsbSCSIRead(w_abyPCCmd, 6, pData, w_nLen, 5000)
            //w_nTime = SystemClock.elapsedRealtime() - w_nTime;
            if (!w_bRet) return false
            pLevRen[0] = w_nLen
        }
        return true
    }

    /***************************************************************************
     * Get Return Code
     */
    fun GetRetCode(): Short {
        return ((m_abyPacket[7].toInt() shl 8 and 0x0000FF00) as Int or (m_abyPacket[6].toInt() and 0x000000FF) as Int).toShort()
    }

    /***************************************************************************
     * Get Data Length
     */
    fun GetDataLen(): Short {
        return (m_abyPacket[5].toInt() shl 8 and 0x0000FF00 or (m_abyPacket[4].toInt() and 0x000000FF)).toShort()
    }

    /***************************************************************************
     * Set Data Length
     */
    fun SetDataLen(p_wDataLen: Short) {
        m_abyPacket[4] = (p_wDataLen.toInt() and 0xFF).toByte()
        m_abyPacket[5] = (p_wDataLen.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
    }

    fun SetDataLen2(p_wDataLen: Short) {
        m_abyPacket2[4] = (p_wDataLen.toInt() and 0xFF).toByte()
        m_abyPacket2[5] = (p_wDataLen.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
    }

    /***************************************************************************
     * Set Command Data
     */
    fun SetCmdData(p_wData: Short, p_bFirst: Boolean) {
        if (p_bFirst) {
            m_abyPacket[6] = (p_wData.toInt() and 0xFF).toByte()
            m_abyPacket[7] = (p_wData.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
        } else {
            m_abyPacket[8] = (p_wData.toInt() and 0xFF).toByte()
            m_abyPacket[9] = (p_wData.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
        }
    }

    /***************************************************************************
     * Get Command Data
     */
    fun GetCmdData(p_bFirst: Boolean): Short {
        return if (p_bFirst) {
            (m_abyPacket[7].toInt() shl 8 and 0x0000FF00 or (m_abyPacket[6].toInt() and 0x000000FF)).toShort()
        } else {
            (m_abyPacket[9].toInt() shl 8 and 0x0000FF00 or (m_abyPacket[8].toInt() and 0x000000FF)).toShort()
        }
    }

    /***************************************************************************
     * Get Data Packet Length
     */
    private fun GetDataPacketLen(): Short {
        return (m_abyPacket[5].toInt() shl 8 and 0x0000FF00 or (m_abyPacket[4].toInt() and 0x000000FF) + 6).toShort()
    }

    /***************************************************************************
     * Make Packet
     */
    fun InitPacket(p_wCmd: Short, p_bCmdData: Boolean) {
        memset(m_abyPacket, 0.toByte(), CMD_PACKET_LEN)
        //g_pPacketBuffer->wPrefix = p_bCmdData?CMD_PREFIX_CODE:CMD_DATA_PREFIX_CODE;
        if (p_bCmdData) {
            m_abyPacket[0] = (CMD_PREFIX_CODE and 0xFF).toByte()
            m_abyPacket[1] = (CMD_PREFIX_CODE and 0xFF00 shr 8 and 0xFF).toByte()
        } else {
            m_abyPacket[0] = (CMD_DATA_PREFIX_CODE and 0xFF).toByte()
            m_abyPacket[1] = (CMD_DATA_PREFIX_CODE and 0xFF00 shr 8 and 0xFF).toByte()
        }
        //g_pPacketBuffer->wCMD_RCM = p_wCMD;
        m_abyPacket[2] = (p_wCmd.toInt() and 0xFF).toByte()
        m_abyPacket[3] = (p_wCmd.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
    }

    fun InitPacket2(p_wCmd: Short, p_bCmdData: Boolean) {
        memset(m_abyPacket2, 0.toByte(), CMD_PACKET_LEN)
        //g_pPacketBuffer->wPrefix = p_bCmdData?CMD_PREFIX_CODE:CMD_DATA_PREFIX_CODE;
        if (p_bCmdData) {
            m_abyPacket2[0] = (CMD_PREFIX_CODE and 0xFF).toByte()
            m_abyPacket2[1] = (CMD_PREFIX_CODE and 0xFF00 shr 8 and 0xFF).toByte()
        } else {
            m_abyPacket2[0] = (CMD_DATA_PREFIX_CODE and 0xFF).toByte()
            m_abyPacket2[1] = (CMD_DATA_PREFIX_CODE and 0xFF00 shr 8 and 0xFF).toByte()
        }
        //g_pPacketBuffer->wCMD_RCM = p_wCMD;
        m_abyPacket2[2] = (p_wCmd.toInt() and 0xFF).toByte()
        m_abyPacket2[3] = (p_wCmd.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
    }

    /***************************************************************************
     * Get CheckSum
     */
    fun GetCheckSum(p_bCmdData: Boolean): Short {
        var w_wRet: Int = 0
        var w_nI: Short = 0
        w_wRet = 0
        if (p_bCmdData) {
            w_nI = 0
            while (w_nI < CMD_PACKET_LEN) {
                w_wRet += (m_abyPacket[w_nI.toInt()].toInt() and 0xFF).toShort()
                w_nI++
            }
        } else {
            w_nI = 0
            while (w_nI < GetDataPacketLen()) {
                w_wRet += (m_abyPacket[w_nI.toInt()].toInt() and 0xFF).toShort()
                w_nI++
            }
        }
        return w_wRet.toShort()
    }

    /***************************************************************************
     * Set CheckSum
     */
    fun AddCheckSum(p_bCmdData: Boolean): Short {
        var w_wRet: Int = 0
        var w_wLen: Short = 0
        var w_nI: Int
        w_wLen = if (p_bCmdData) CMD_PACKET_LEN.toShort() else GetDataPacketLen()
        w_wRet = 0
        w_nI = 0
        while (w_nI < w_wLen) {
            w_wRet += (m_abyPacket[w_nI].toInt() and 0xFF).toShort()
            w_nI++
        }
        m_abyPacket[w_wLen.toInt()] = (w_wRet.toInt() and 0xFF).toByte()
        m_abyPacket[w_wLen + 1] = (w_wRet.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
        return w_wRet.toShort()
    }

    fun AddCheckSum2(p_bCmdData: Boolean): Short {
        var w_wRet: Int = 0
        var w_wLen: Short = 0
        var w_nI: Int
        w_wLen = if (p_bCmdData) CMD_PACKET_LEN.toShort() else GetDataPacketLen()
        w_wRet = 0
        w_nI = 0
        while (w_nI < w_wLen) {
            w_wRet += m_abyPacket2[w_nI].toInt() and 0xFF
            w_nI++
        }
        m_abyPacket2[w_wLen.toInt()] = (w_wRet.toInt() and 0xFF).toByte()
        m_abyPacket2[w_wLen + 1] = (w_wRet.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
        return w_wRet.toShort()
    }

    /***************************************************************************
     * Check Packet
     */
    fun CheckReceive(p_wPrefix: Short, p_wCmd: Short): Boolean {
        val w_wCheckSum: Short
        val w_wTmpPrefix: Short
        val w_wTmpCmd: Short
        val w_wLen: Short
        // Check Prefix Code
        w_wTmpPrefix = (m_abyPacket[1].toInt() shl 8 and 0x0000FF00 or (m_abyPacket[0].toInt() and 0x000000FF)).toShort()
        w_wTmpCmd = (m_abyPacket[3].toInt() shl 8 and 0x0000FF00 or (m_abyPacket[2].toInt() and 0x000000FF)).toShort()
        //    	if ( g_pPacketBuffer->wCMD_RCM != CMD_FP_CANCEL_CODE )
        run {
            if (p_wPrefix != w_wTmpPrefix || p_wCmd != w_wTmpCmd) {
                return false
            }
        }
        w_wLen = if (p_wPrefix.toInt() == RCM_PREFIX_CODE) CMD_PACKET_LEN.toShort() else GetDataPacketLen()
        w_wCheckSum = (m_abyPacket[w_wLen + 1].toInt() shl 8 and 0x0000FF00 or (m_abyPacket[w_wLen.toInt()].toInt() and 0x000000FF)).toShort()
        return if (w_wCheckSum != GetCheckSum(p_wPrefix.toInt() == RCM_PREFIX_CODE)) {
            false
        } else true
    }

    //--------------------------- Send, Receive Communication Packet Functions ---------------------//
    fun Send_Command(p_wCmd: Short): Boolean {
        return if (m_nConnected.toInt() == 2) USB_SendPacket(p_wCmd) else false
//        if (m_nConnected.toInt() == 1 || m_nConnected.toInt() == 3) UART_SendCommand(p_wCmd) else

    }
    /** */
    /** */
    fun Send_DataPacket(p_wCmd: Short): Boolean {
        return if (m_nConnected.toInt() == 2) USB_SendDataPacket(p_wCmd) else false
//        if (m_nConnected.toInt() == 1 || m_nConnected.toInt() == 3) UART_SendDataPacket(p_wCmd) else
    }
    /** */
    /** */
    fun Receive_DataPacket(p_wCmd: Short): Boolean {
        return if (m_nConnected.toInt() == 2) USB_ReceiveDataPacket(p_wCmd) else false
//        if (m_nConnected.toInt() == 1 || m_nConnected.toInt() == 3) UART_ReceiveDataPacket(p_wCmd) else
    }

    //------------------------------------------ USB Functions -------------------------------------//
    fun USB_SendPacket(wCMD: Short): Boolean {
        val btCDB = ByteArray(8)
        val w_bRet: Boolean
        Arrays.fill(btCDB, 0.toByte())
        btCDB[0] = 0xEF.toByte()
        btCDB[1] = 0x11
        btCDB[4] = (CMD_PACKET_LEN + 2).toByte()
        while (m_bSendPacketWork) {
            SystemClock.sleep(1)
        }
        m_bSendPacketWork = true
        w_bRet = m_usbBase.UsbSCSIWrite(btCDB, 8, m_abyPacket, (CMD_PACKET_LEN + 2) as Int, SCSI_TIMEOUT)
        m_bSendPacketWork = false
        return if (!w_bRet) false else USB_ReceiveAck(wCMD)
    }
    /** */
    /** */
    fun USB_SendPacket2(wCMD: Short): Boolean {
        val btCDB = ByteArray(8)
        val w_bRet: Boolean
        Arrays.fill(btCDB, 0.toByte())
        btCDB[0] = 0xEF.toByte()
        btCDB[1] = 0x11
        btCDB[4] = (CMD_PACKET_LEN + 2).toByte()
        while (m_bSendPacketWork) {
            SystemClock.sleep(1)
        }
        m_bSendPacketWork = true
        w_bRet = m_usbBase.UsbSCSIWrite(btCDB, 8, m_abyPacket2, (CMD_PACKET_LEN + 2) as Int, SCSI_TIMEOUT)
        m_bSendPacketWork = false
        return if (!w_bRet) false else true
    }
    /** */
    /** */
    fun USB_ReceiveAck(p_wCmd: Short): Boolean {
        var w_nLen: Int
        val btCDB = ByteArray(8)
        val w_abyWaitPacket = ByteArray(CMD_PACKET_LEN + 2)
        var w_dwTimeOut = SCSI_TIMEOUT
        if (p_wCmd.toInt() == CMD_VERIFY_CODE || p_wCmd.toInt() == CMD_IDENTIFY_CODE || p_wCmd.toInt() == CMD_IDENTIFY_FREE_CODE || p_wCmd.toInt() == CMD_ENROLL_CODE || p_wCmd.toInt() == CMD_ENROLL_ONETIME_CODE) w_dwTimeOut = (GD_MAX_FP_TIME_OUT + 1) * 1000
        Arrays.fill(btCDB, 0.toByte())
        //w_nReadCount = GetReadWaitTime(p_byCMD);
        Arrays.fill(w_abyWaitPacket, 0xAF.toByte())
        do {
            Arrays.fill(m_abyPacket, 0.toByte())
            btCDB[0] = 0xEF.toByte()
            btCDB[1] = 0x12.toByte()
            w_nLen = CMD_PACKET_LEN + 2
            if (!m_usbBase.UsbSCSIRead(btCDB, 8, m_abyPacket, w_nLen, w_dwTimeOut)) return false
            SystemClock.sleep(COMM_SLEEP_TIME.toLong())
        } while (memcmp(m_abyPacket, w_abyWaitPacket, CMD_PACKET_LEN + 2) == true)
        m_nPacketSize = w_nLen
        return if (!CheckReceive(RCM_PREFIX_CODE.toShort(), p_wCmd)) false else true
    }
    /** */
    /** */
    fun USB_ReceiveAck2(p_wCmd: Short): Boolean {
        var w_nLen: Int
        val btCDB = ByteArray(8)
        val w_abyWaitPacket = ByteArray(CMD_PACKET_LEN + 2)
        val w_dwTimeOut = SCSI_TIMEOUT
        Arrays.fill(btCDB, 0.toByte())
        Arrays.fill(w_abyWaitPacket, 0xAF.toByte())
        do {
            Arrays.fill(m_abyPacket2, 0.toByte())
            btCDB[0] = 0xEF.toByte()
            btCDB[1] = 0x12.toByte()
            w_nLen = CMD_PACKET_LEN + 2
            if (!m_usbBase.UsbSCSIRead(btCDB, 8, m_abyPacket2, w_nLen, w_dwTimeOut)) return false
            SystemClock.sleep(COMM_SLEEP_TIME.toLong())
        } while (memcmp(m_abyPacket2, w_abyWaitPacket, CMD_PACKET_LEN + 2) == true)
        m_nPacketSize = w_nLen
        //    	if (!CheckReceive((short)RCM_PREFIX_CODE, p_wCmd))
//    		return false;
        return true
    }
    /** */
    /** */
    fun USB_ReceiveDataAck(p_wCmd: Short): Boolean {
        val btCDB = ByteArray(8)
        val w_WaitPacket = ByteArray(8)
        var w_nLen: Int
        var w_dwTimeOut = COMM_TIMEOUT
        if (p_wCmd.toInt() == CMD_VERIFY_CODE || p_wCmd.toInt() == CMD_IDENTIFY_CODE || p_wCmd.toInt() == CMD_IDENTIFY_FREE_CODE || p_wCmd.toInt() == CMD_ENROLL_CODE || p_wCmd.toInt() == CMD_ENROLL_ONETIME_CODE) w_dwTimeOut = (GD_MAX_FP_TIME_OUT + 1) * 1000
        memset(btCDB, 0.toByte(), 8)
        memset(w_WaitPacket, 0xAF.toByte(), 8)
        Arrays.fill(m_abyPacketTmp, 0.toByte())
        do {
            btCDB[0] = 0xEF.toByte()
            btCDB[1] = 0x15
            w_nLen = 6
            if (!m_usbBase.UsbSCSIRead(btCDB, 8, m_abyPacket, w_nLen, w_dwTimeOut)) {
                return false
            }
            SystemClock.sleep(COMM_SLEEP_TIME.toLong())
        } while (memcmp(m_abyPacket, w_WaitPacket, 6) == true)
        do {
            w_nLen = GetDataLen() + 2
            if (USB_ReceiveRawData(m_abyPacketTmp, w_nLen) == false) {
                return false
            }
            System.arraycopy(m_abyPacketTmp, 0, m_abyPacket, 6, w_nLen)
            SystemClock.sleep(COMM_SLEEP_TIME.toLong())
        } while (memcmp(m_abyPacket, w_WaitPacket, 4) == true)
        return if (!CheckReceive(RCM_DATA_PREFIX_CODE.toShort(), p_wCmd)) {
            false
        } else true
    }
    /** */
    /** */
    fun USB_SendDataPacket(wCMD: Short): Boolean {
        val btCDB = ByteArray(8)
        val w_wLen = (GetDataPacketLen() + 2).toShort()
        memset(btCDB, 0.toByte(), 8)
        btCDB[0] = 0xEF.toByte()
        btCDB[1] = 0x13
        btCDB[4] = (w_wLen.toInt() and 0xFF).toByte()
        btCDB[5] = (w_wLen.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
        return if (!m_usbBase.UsbSCSIWrite(btCDB, 8, m_abyPacket, GetDataPacketLen() + 2, SCSI_TIMEOUT)) false else USB_ReceiveDataAck(wCMD)
    }
    /** */
    /** */
    fun USB_ReceiveDataPacket(wCMD: Short): Boolean {
        return USB_ReceiveDataAck(wCMD)
    }
    /** */
    /** */
    fun USB_ReceiveRawData(pBuffer: ByteArray?, nDataLen: Int): Boolean {
        val btCDB = ByteArray(8)
        memset(btCDB, 0.toByte(), 8)
        btCDB[0] = 0xEF.toByte()
        btCDB[1] = 0x14.toByte()
        return if (!m_usbBase.UsbSCSIRead(btCDB, 8, pBuffer, nDataLen, SCSI_TIMEOUT)) {
            false
        } else true
    }
    /** */
    /** */
    fun USB_ReceiveImage(p_pBuffer: ByteArray?, p_dwDataLen: Int): Boolean {
        val btCDB = ByteArray(8)
        val w_WaitPacket = ByteArray(8)
        var w_nI: Int
        var w_nIndex: Int
        var w_nRemainCount: Int
        val w_pTmpImgBuf = ByteArray(ONCE_UP_IMAGE_UINT)
        memset(btCDB, 0.toByte(), 8)
        memset(w_WaitPacket, 0xAF.toByte(), 8)
        if (p_dwDataLen == 208 * 288 || p_dwDataLen == 242 * 266 || p_dwDataLen == 202 * 258 || p_dwDataLen == 256 * 288) {
            w_nIndex = 0
            w_nRemainCount = p_dwDataLen
            w_nI = 0
            while (w_nRemainCount > ONCE_UP_IMAGE_UINT) {
                btCDB[0] = 0xEF.toByte()
                btCDB[1] = 0x16
                btCDB[2] = (w_nI and 0xFF).toByte()
                if (!m_usbBase.UsbSCSIRead(btCDB, 8, w_pTmpImgBuf, ONCE_UP_IMAGE_UINT, SCSI_TIMEOUT)) return false
                System.arraycopy(w_pTmpImgBuf, 0, p_pBuffer, w_nIndex, ONCE_UP_IMAGE_UINT)
                w_nRemainCount -= ONCE_UP_IMAGE_UINT
                w_nIndex += ONCE_UP_IMAGE_UINT
                w_nI++
            }
            btCDB[0] = 0xEF.toByte()
            btCDB[1] = 0x16
            btCDB[2] = (w_nI and 0xFF).toByte()
            if (!m_usbBase.UsbSCSIRead(btCDB, 8, w_pTmpImgBuf, w_nRemainCount, SCSI_TIMEOUT)) return false
            System.arraycopy(w_pTmpImgBuf, 0, p_pBuffer, w_nIndex, w_nRemainCount)
        }
        return true
    }
    /** */
    /** */
    fun USB_DownImage(pBuf: ByteArray?, nBufLen: Int): Boolean {
        val w_pImgBuf = ByteArray(ONCE_UP_IMAGE_UINT)
        var w_nI: Int
        var w_nIndex = 0
        var w_nRemainCount: Int
        val btCDB = ByteArray(8)
        w_nIndex = 0
        w_nRemainCount = nBufLen
        w_nI = 0
        memset(btCDB, 0.toByte(), 8)
        while (w_nRemainCount > ONCE_UP_IMAGE_UINT) {
            btCDB[0] = 0xEF.toByte()
            btCDB[1] = 0x17
            btCDB[2] = 0
            btCDB[3] = (w_nI and 0xFF).toByte()
            btCDB[4] = LOBYTE((ONCE_UP_IMAGE_UINT and 0x00FF).toShort())
            btCDB[5] = HIBYTE((ONCE_UP_IMAGE_UINT and 0xFF00).toShort())
            System.arraycopy(pBuf!!, w_nIndex, w_pImgBuf, 0, ONCE_UP_IMAGE_UINT)
            if (!m_usbBase.UsbSCSIWrite(btCDB, 6, w_pImgBuf, ONCE_UP_IMAGE_UINT, SCSI_TIMEOUT)) return false
            w_nRemainCount -= ONCE_UP_IMAGE_UINT
            w_nIndex += ONCE_UP_IMAGE_UINT
            w_nI++
        }
        btCDB[0] = 0xEF.toByte()
        btCDB[1] = 0x17
        btCDB[2] = 0
        btCDB[3] = (w_nI and 0xFF).toByte()
        btCDB[4] = LOBYTE((w_nRemainCount and 0x00FF).toShort())
        btCDB[5] = HIBYTE((w_nRemainCount and 0xFF00).toShort())
        System.arraycopy(pBuf!!, w_nIndex, w_pImgBuf, 0, w_nRemainCount)
        return if (!m_usbBase.UsbSCSIWrite(btCDB, 6, w_pImgBuf, w_nRemainCount, SCSI_TIMEOUT)) false else true
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
    /** */
    /** */
    fun memcmp(p1: ByteArray, p2: ByteArray, nLen: Int): Boolean {
        var i: Int
        i = 0
        while (i < nLen) {
            if (p1[i] != p2[i]) return false
            i++
        }
        return true
    }

    fun memset(p1: ByteArray?, nValue: Byte, nLen: Int) {
        Arrays.fill(p1, 0, nLen, nValue)
    }

    fun memcpy(p1: ByteArray?, nValue: Byte, nLen: Int) {
        Arrays.fill(p1, 0, nLen, nValue)
    }

    fun MAKEWORD(low: Byte, high: Byte): Short {
        val s: Short
        s = (high.toInt() and 0x00FF shl 8 and 0x0000FF00 or (low.toInt() and 0x000000FF)).toShort()
        return s
    }

    fun LOBYTE(s: Short): Byte {
        return (s.toInt() and 0xFF).toByte()
    }

    fun HIBYTE(s: Short): Byte {
        return (s.toInt() and 0xFF00 shr 8 and 0xFF).toByte()
    }

    //----------------------------------------------------���ڿ�����
    inner class SerialControl() : SerialHelper() {
        //		public SerialControl(String sPort, String sBaudRate){

        //			super(sPort, sBaudRate);
//		} : SerialHelper() {
        override fun onDataReceived(ComRecData: ComBean) { //���ݽ�����������ʱ��������̣�����Ῠ��,���ܺ�6410����ʾ�����й�
//ֱ��ˢ����ʾ��������������ʱ���������ԣ�����������ʾͬ����
//���̶߳�ʱˢ����ʾ���Ի�ý���������ʾЧ�������ǽ��������ٶȿ�����ʾ�ٶ�ʱ����ʾ���ͺ�
//����Ч�����-_-���̶߳�ʱˢ���Ժ�һЩ��
            DispQueue.AddQueue(ComRecData) //�̶߳�ʱˢ����ʾ(�Ƽ�)
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
        private val QueueList: Queue<ComBean> = LinkedList()
        override fun run() {
            super.run()
            while (!isInterrupted) {
                var i: Int
                while (true) {
                    var ComData: ComBean?
                    if (QueueList.poll().also { ComData = it } == null) break
                    i = 0
                    while (m_bBufferHandle) {
                        i++
                        if (i > 10000) break
                    }
                    m_bBufferHandle = true
                    System.arraycopy(ComData!!.bRec!!, 0, m_pUARTReadBuf, m_nUARTReadLen, ComData!!.nSize)
                    m_nUARTReadLen = m_nUARTReadLen + ComData!!.nSize
                    m_bBufferHandle = false
                    //		        	break;
                }
            }
        }

        @Synchronized
        fun AddQueue(ComData: ComBean) {
            QueueList.add(ComData)
        }

    }
}