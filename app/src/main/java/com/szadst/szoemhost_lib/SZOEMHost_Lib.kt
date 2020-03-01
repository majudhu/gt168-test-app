package com.szadst.szoemhost_lib

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.SystemClock
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class SZOEMHost_Lib(parentActivity: Activity, p_pStatusView: TextView, p_FpImageViewer: ImageView, p_runEnableCtrl: Runnable, p_spDevice: Spinner) {
    var m_bParamGet = false
    var m_TemplateData = ByteArray(GD_MAX_RECORD_SIZE)
    var m_TemplateData2 = ByteArray(GD_MAX_RECORD_SIZE)
    var m_nTemplateSize = 0
    //    var m_nTemplateSize2 = 0
//    var m_nParam = 0
    var m_nImgWidth = 0
    var m_nImgHeight = 0
    //    var m_nPassedTime: Long = 0
    var m_binImage: ByteArray?
    var m_bmpImage: ByteArray?
    var m_nImageBufOffset = 0
    var m_strPost: String? = null
    //    var m_bCancel = false
//    var m_bConCapture = false
    var m_txtStatus: TextView
    var m_FpImageViewer: ImageView
    var m_runEnableCtrl: Runnable

    private val m_IConnectionHandler: IUsbConnState = object : IUsbConnState {
        override fun onUsbConnected() {
            if (m_devComm!!.runTestConnection() == ERR_SUCCESS) {
                if (m_devComm!!.runGetDeviceInfo() == ERR_SUCCESS) {
//                    runEnableCtrl(true);
//                    m_btnOpenDevice.setEnabled(false);
//                    m_btnCloseDevice.setEnabled(true);
                    m_txtStatus.text = "Open Device Success!";
                }
            } else {
                m_txtStatus.text = "Can not connect to device!";
            }
        }

        init {
            m_binImage = ByteArray(1024 * 100)
            m_bmpImage = ByteArray(1024 * 100)
            m_txtStatus = p_pStatusView
            m_FpImageViewer = p_FpImageViewer
            m_runEnableCtrl = p_runEnableCtrl
        }

        override fun onUsbPermissionDenied() {
            m_txtStatus.text = "Permission denied!"
        }

        override fun onDeviceNotFound() {
            m_txtStatus.text = "Can not find usb device!"
        }
    }


    init {
        if (m_devComm == null) {
            m_devComm = DevComm(parentActivity, m_IConnectionHandler, p_spDevice)
        }
    }

    fun SZOEMHost_Lib_Init(parentActivity: Activity, p_pStatusView: TextView, p_FpImageViewer: ImageView, p_runEnableCtrl: Runnable, p_spDevice: Spinner): Int {
        m_bThreadWork = false
        if (m_devComm == null) {
            m_devComm = DevComm(parentActivity, m_IConnectionHandler, p_spDevice)
        } else {
            m_devComm!!.devCommInit(p_spDevice)
        }
        if (m_binImage == null) m_binImage = ByteArray(1024 * 100)
        if (m_bmpImage == null) m_bmpImage = ByteArray(1024 * 100)
        m_txtStatus = p_pStatusView
        m_FpImageViewer = p_FpImageViewer
        m_runEnableCtrl = p_runEnableCtrl
        return 0
    }

    /*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	*/
    fun OpenDevice(p_szDevice: String, p_nBaudrate: Int): Int {
        if (m_devComm != null) {
            if (!m_devComm!!.isInit()) {
                if (!m_devComm!!.openComm(p_szDevice, p_nBaudrate)) {
                    m_txtStatus.text = "Failed init device!"
                    return 1
                }
            }
            if (m_devComm!!.runTestConnection() == ERR_SUCCESS) {
                if (m_devComm!!.runGetDeviceInfo() == ERR_SUCCESS) {
                    m_txtStatus.text = "Open Device Success"
                    return 0
                } else {
                    m_txtStatus.text = "Can not connect to device!"
                    return 1
                }
            } else {
                m_txtStatus.text = "Can not connect to device!"
                m_devComm!!.closeComm()
                return 1
            }
        }
        return 1
    }

    fun CloseDevice(): Int {
        m_devComm!!.closeComm()
        return 0
    }

    fun StartSendThread() {
        m_bCmdDone = false
        while (m_bThreadWork) {
            SystemClock.sleep(1)
        }
        Thread(Runnable {
            var w_blRet = false
            var w_wPrefix: Short = 0
            m_bThreadWork = true
            w_wPrefix = (m_devComm!!.mAbyPacket[1].toInt() shl 8 and 0x0000FF00 or (m_devComm!!.mAbyPacket[0].toInt() and 0x000000FF)).toShort()
            if (w_wPrefix == CMD_PREFIX_CODE.toShort()) {
                if (m_dwCode != CMD_FP_CANCEL_CODE) {
//                    if (m_devComm!!.m_nConnected.toInt() == 1 || m_devComm!!.m_nConnected.toInt() == 3) w_blRet = m_devComm!!.UART_SendCommand(m_dwCode.toShort()) else
                    if (m_devComm!!.mNconnected.toInt() == 2) w_blRet = m_devComm!!.usbSendPacket(m_dwCode.toShort())
                } else {
//                    if (m_devComm!!.m_nConnected.toInt() == 1 || m_devComm!!.m_nConnected.toInt() == 3) w_blRet = m_devComm!!.UART_ReceiveAck(m_dwCode.toShort(), true) else
                    if (m_devComm!!.mNconnected.toInt() == 2) w_blRet = m_devComm!!.usbReceiveAck(m_dwCode.toShort())
                }
            } else if (w_wPrefix == CMD_DATA_PREFIX_CODE.toShort()) {
//                if (m_devComm!!.m_nConnected.toInt() == 1 || m_devComm!!.m_nConnected.toInt() == 3) w_blRet = m_devComm!!.UART_SendDataPacket(m_dwCode.toShort()) else
                if (m_devComm!!.mNconnected.toInt() == 2) w_blRet = m_devComm!!.usbSendDataPacket(m_dwCode.toShort())
            } else {
                if (m_dwCode != CMD_FEATURE_OF_CAPTURED_FP_CODE) {
//                    if (m_devComm!!.m_nConnected.toInt() == 1 || m_devComm!!.m_nConnected.toInt() == 3) w_blRet = m_devComm!!.UART_ReceiveAck(m_dwCode.toShort(), true) else
                    if (m_devComm!!.mNconnected.toInt() == 2) w_blRet = m_devComm!!.usbReceiveAck(m_dwCode.toShort())
                } else {
//                    if (m_devComm!!.m_nConnected.toInt() == 1 || m_devComm!!.m_nConnected.toInt() == 3) w_blRet = m_devComm!!.UART_ReceiveDataPacket(CMD_FEATURE_OF_CAPTURED_FP_CODE.toShort()) else
                    if (m_devComm!!.mNconnected.toInt() == 2) w_blRet = m_devComm!!.usbReceiveDataPacket(CMD_FEATURE_OF_CAPTURED_FP_CODE.toShort())
                }
            }
            m_bSendResult = w_blRet
            m_txtStatus.post(procRspPacket)
            m_bThreadWork = false
        }).start()
    }

    private fun Run_Command_NP(p_wCmd: Short) { //. Assemble command packet
        m_devComm!!.initPacket(p_wCmd, true)
        m_devComm!!.addCheckSum(true)
        m_dwCode = p_wCmd.toInt()
        StartSendThread()
    }

    private fun Run_Command_1P(p_wCmd: Short, p_wData: Short) { //. Assemble command packet
        m_devComm!!.initPacket(p_wCmd, true)
        m_devComm!!.setDataLen(0x0002.toShort())
        m_devComm!!.setCmdData(p_wData, true)
        m_devComm!!.addCheckSum(true)
        m_dwCode = p_wCmd.toInt()
        StartSendThread()
    }

    fun Run_CmdEnroll(p_nTmpNo: Int): Int {
        var w_nTemplateNo = 0
        //. Check inputed template no
        if (CheckInputTemplateNo(p_nTmpNo) == false) {
            return 1
        }
        w_nTemplateNo = p_nTmpNo
        Run_Command_1P(CMD_ENROLL_CODE.toShort(), w_nTemplateNo.toShort())
        return 0
    }

    fun Run_CmdIdentify(): Int {
        m_strPost = "Input your finger"
        m_txtStatus.text = m_strPost
        Run_Command_NP(CMD_IDENTIFY_CODE.toShort())
        return 0
    }

    fun Run_CmdIdentifyFree(): Int {
        m_strPost = "Input your finger"
        m_txtStatus.text = m_strPost
        Run_Command_NP(CMD_IDENTIFY_FREE_CODE.toShort())
        return 0
    }

    fun Run_CmdVerify(p_nTmpNo: Int): Int {
        var w_nTemplateNo = 0
        //. Check inputed template no
        if (CheckInputTemplateNo(p_nTmpNo) == false) return 1
        w_nTemplateNo = p_nTmpNo
        m_strPost = "Input your finger"
        m_txtStatus.text = m_strPost
        Run_Command_1P(CMD_VERIFY_CODE.toShort(), w_nTemplateNo.toShort())
        return 0
    }

    fun Run_CmdEnrollOneTime(p_nTmpNo: Int): Int {
        var w_nTemplateNo = 0
        //. Check inputed template no
        if (CheckInputTemplateNo(p_nTmpNo) == false) return 1
        w_nTemplateNo = p_nTmpNo
        m_strPost = "Input your finger"
        m_txtStatus.text = m_strPost
        Run_Command_1P(CMD_ENROLL_ONETIME_CODE.toShort(), w_nTemplateNo.toShort())
        return 0
    }

    fun Run_CmdChangeTemplate(p_nTmpNo: Int): Int {
        var w_nTemplateNo = 0
        //. Check inputed template no
        if (CheckInputTemplateNo(p_nTmpNo) == false) return 1
        w_nTemplateNo = p_nTmpNo
        Run_Command_1P(CMD_CHANGE_TEMPLATE_CODE.toShort(), w_nTemplateNo.toShort())
        return 0
    }

    fun Run_CmdDeleteID(p_nTmpNo: Int): Int {
        var w_nTemplateNo = 0
        //. Check inputed template no
        if (CheckInputTemplateNo(p_nTmpNo) == false) return 1
        w_nTemplateNo = p_nTmpNo
        Run_Command_1P(CMD_CLEAR_TEMPLATE_CODE.toShort(), w_nTemplateNo.toShort())
        return 0
    }

    fun Run_CmdDeleteAll(): Int {
        Run_Command_NP(CMD_CLEAR_ALLTEMPLATE_CODE.toShort())
        return 0
    }

    fun Run_CmdGetEmptyID(): Int {
        Run_Command_NP(CMD_GET_EMPTY_ID_CODE.toShort())
        return 0
    }

    fun Run_CmdGetUserCount(): Int {
        Run_Command_NP(CMD_GET_ENROLL_COUNT_CODE.toShort())
        return 0
    }

    fun Run_CmdGetBrokenTemplate(): Int {
        Run_Command_NP(CMD_GET_BROKEN_TEMPLATE_CODE.toShort())
        return 0
    }

    fun Run_CmdReadTemplate(p_nTmpNo: Int): Int {
        var w_blRet = false
        var w_nTemplateNo = 0
        var w_nLen = 0
        var w_nBufOffset = 0
        //. Check inputed template no
        if (CheckInputTemplateNo(p_nTmpNo) == false) return 1
        w_nTemplateNo = p_nTmpNo
        DevComm.memSet(m_TemplateData, 0.toByte(), GD_MAX_RECORD_SIZE)
        //. Assemble command packet
        m_devComm!!.initPacket(CMD_READ_TEMPLATE_CODE.toShort(), true)
        m_devComm!!.setDataLen(0x0002.toShort())
        m_devComm!!.setCmdData(w_nTemplateNo.toShort(), true)
        m_devComm!!.addCheckSum(true)
        m_dwCode = CMD_READ_TEMPLATE_CODE
        w_blRet = m_devComm!!.sendCommand(CMD_READ_TEMPLATE_CODE.toShort())
        if (w_blRet == false) {
            CloseDevice()
            return 1
        }
        if (m_devComm!!.getRetCode() != ERR_SUCCESS.toShort()) {
            DisplayResponsePacket(CMD_READ_TEMPLATE_CODE.toShort())
            return 1
        }
        if (m_devComm!!.getCmdData(false).toInt() == GD_TEMPLATE_SIZE) {
            w_blRet = m_devComm!!.receiveDataPacket(CMD_READ_TEMPLATE_CODE.toShort())
            w_nLen = GD_TEMPLATE_SIZE
            System.arraycopy(m_devComm!!.mAbyPacket, 10, m_TemplateData, 0, GD_TEMPLATE_SIZE)
        } else {
            w_nLen = m_devComm!!.getCmdData(false).toInt()
            w_nBufOffset = 0
            while (true) {
                w_blRet = m_devComm!!.receiveDataPacket(CMD_READ_TEMPLATE_CODE.toShort())
                if (w_blRet == false) {
                    break
                } else {
                    if (m_devComm!!.getRetCode().toInt() == ERR_SUCCESS) {
                        if (m_devComm!!.getDataLen() > DATA_SPLIT_UNIT + 4) {
                            m_devComm!!.setCmdData(ERR_FAIL.toShort(), true)
                            m_devComm!!.setCmdData(ERR_INVALID_PARAM.toShort(), false)
                            w_blRet = false
                            break
                        } else {
                            System.arraycopy(m_devComm!!.mAbyPacket, 10, m_TemplateData, w_nBufOffset, m_devComm!!.getDataLen() - 4)
                            w_nBufOffset = w_nBufOffset + (m_devComm!!.getDataLen() - 4)
                            if (w_nBufOffset == w_nLen) {
                                break
                            }
                        }
                    } else {
                        w_blRet = false
                        break
                    }
                }
            }
        }
        if (w_blRet == false) {
            return 2
        } else {
            m_nTemplateSize = w_nLen
            DisplayResponsePacket(CMD_READ_TEMPLATE_CODE.toShort())
        }
        return 0
    }

    fun Run_CmdWriteTemplate(p_nTmpNo: Int): Int {
        var w_blRet = false
        var w_nTemplateNo = 0
        var i: Int
        val n: Int
        val r: Int
        //. Check inputed template no and Read template file
        if (CheckInputTemplateNo(p_nTmpNo) == false || ReadTemplateFile(p_nTmpNo) == false) {
            return 1
        }
        w_nTemplateNo = p_nTmpNo
        //. Assemble command packet
        m_devComm!!.initPacket(CMD_WRITE_TEMPLATE_CODE.toShort(), true)
        m_devComm!!.setDataLen(0x0002.toShort())
        m_devComm!!.setCmdData(m_nTemplateSize.toShort(), true)
        m_devComm!!.addCheckSum(true)
        //. Send command packet to target
        w_blRet = m_devComm!!.sendCommand(CMD_WRITE_TEMPLATE_CODE.toShort())
        if (w_blRet == false) {
            CloseDevice()
            return 1
        }
        if (m_devComm!!.getRetCode() != ERR_SUCCESS.toShort()) {
            DisplayResponsePacket(CMD_WRITE_TEMPLATE_CODE.toShort())
            return 1
        }
        if (m_nTemplateSize == GD_RECORD_SIZE || m_nTemplateSize == ID_USER_TEMPLATE_SIZE) { //. Assemble data packet
            m_devComm!!.initPacket(CMD_WRITE_TEMPLATE_CODE.toShort(), false)
            m_devComm!!.setDataLen((m_nTemplateSize + 2).toShort())
            m_devComm!!.setCmdData(w_nTemplateNo.toShort(), true)
            System.arraycopy(m_TemplateData, 0, m_devComm!!.mAbyPacket, 8, m_nTemplateSize)
            m_devComm!!.addCheckSum(false)
            //. Send data packet to target
            w_blRet = m_devComm!!.sendDataPacket(CMD_WRITE_TEMPLATE_CODE.toShort())
            if (w_blRet == false) {
                return 2
            }
        } else {
            n = m_nTemplateSize / DATA_SPLIT_UNIT
            r = m_nTemplateSize % DATA_SPLIT_UNIT
            i = 0
            while (i < n) {
                //. Assemble data packet
                m_devComm!!.initPacket(CMD_WRITE_TEMPLATE_CODE.toShort(), false)
                m_devComm!!.setDataLen((DATA_SPLIT_UNIT + 4).toShort())
                m_devComm!!.setCmdData(w_nTemplateNo.toShort(), true)
                m_devComm!!.setCmdData(DATA_SPLIT_UNIT.toShort(), false)
                System.arraycopy(m_TemplateData, i * DATA_SPLIT_UNIT, m_devComm!!.mAbyPacket, 10, DATA_SPLIT_UNIT)
                m_devComm!!.addCheckSum(false)
                //. Send data packet to target
                w_blRet = m_devComm!!.sendDataPacket(CMD_WRITE_TEMPLATE_CODE.toShort())
                if (w_blRet == false) {
                    return 2
                }
                i++
            }
            if (r > 0) {
                m_devComm!!.initPacket(CMD_WRITE_TEMPLATE_CODE.toShort(), false)
                m_devComm!!.setDataLen((r + 4).toShort())
                m_devComm!!.setCmdData(w_nTemplateNo.toShort(), true)
                m_devComm!!.setCmdData((r and 0xFFFF).toShort(), false)
                System.arraycopy(m_TemplateData, i * DATA_SPLIT_UNIT, m_devComm!!.mAbyPacket, 10, r)
                m_devComm!!.addCheckSum(false)
                //. Send data packet to target
                w_blRet = m_devComm!!.sendDataPacket(CMD_WRITE_TEMPLATE_CODE.toShort())
                if (w_blRet == false) {
                    return 2
                }
            }
        }
        //. Display response packet
        DisplayResponsePacket(CMD_WRITE_TEMPLATE_CODE.toShort())
        return 0
    }

    fun Run_CmdSetParameter(): Int {
        val w_nMode = 0
        val w_nIndex = 0
        val w_nValue = 0
        m_bParamGet = w_nMode == 0
        m_devComm!!.initPacket(CMD_SET_PARAMETER_CODE.toShort(), true)
        m_devComm!!.setDataLen(0x0006.toShort())
        m_devComm!!.mAbyPacket[6] = (w_nMode and 0xFF).toByte()
        m_devComm!!.mAbyPacket[7] = (w_nIndex and 0xFF).toByte()
        m_devComm!!.mAbyPacket[8] = (w_nValue and 0xFF).toByte()
        m_devComm!!.mAbyPacket[9] = (w_nValue and 0x0000FF00 shr 8 and 0xFF).toByte()
        m_devComm!!.mAbyPacket[10] = (w_nValue and 0x00FF0000 shr 16 and 0xFF).toByte()
        m_devComm!!.mAbyPacket[11] = (w_nValue and -0x1000000 shr 24 and 0xFF).toByte()
        m_devComm!!.addCheckSum(true)
        m_dwCode = CMD_SET_PARAMETER_CODE
        StartSendThread()
        return 0
    }

    fun Run_CmdGetFwVersion(): Int {
        Run_Command_NP(CMD_GET_FW_VERSION_CODE.toShort())
        return 0
    }

    fun Run_CmdDetectFinger(): Int {
        m_strPost = "Input your finger"
        m_txtStatus.text = m_strPost
        Run_Command_NP(CMD_FINGER_DETECT_CODE.toShort())
        return 0
    }

    fun Run_CmdSetDevPass(p_szPassword: String): Int {
        var w_nI: Int
        if (p_szPassword.length != 0 && p_szPassword.length != 14) {
            m_strPost = "Invalid Device Password. \nPlease input valid device password(length=14)!"
            m_txtStatus.text = m_strPost
            return 1
        }
        m_devComm!!.initPacket(CMD_SET_DEVPASS_CODE.toShort(), true)
        m_devComm!!.setDataLen(0x000E.toShort()) // 14
        if (p_szPassword.length == 0) {
            w_nI = 0
            while (w_nI < 14) {
                m_devComm!!.mAbyPacket[6 + w_nI] = 0x00
                w_nI++
            }
        } else {
            w_nI = 0
            while (w_nI < 14) {
                m_devComm!!.mAbyPacket[6 + w_nI] = (p_szPassword[w_nI].toInt() and 0xFF).toByte()
                w_nI++
            }
        }
        m_devComm!!.addCheckSum(true)
        m_dwCode = CMD_SET_DEVPASS_CODE
        StartSendThread()
        return 0
    }

    fun Run_CmdVerifyPass(p_szPassword: String): Int {
        var w_nI: Int
        if (p_szPassword.length != 14) {
            m_strPost = "Invalid Device Password. \nPlease input valid device password(length=14)!"
            m_txtStatus.text = m_strPost
            return 1
        }
        m_devComm!!.initPacket(CMD_VERIFY_DEVPASS_CODE.toShort(), true)
        m_devComm!!.setDataLen(0x000E.toShort()) // 14
        w_nI = 0
        while (w_nI < 14) {
            m_devComm!!.mAbyPacket[6 + w_nI] = (p_szPassword[w_nI].toInt() and 0xFF).toByte()
            w_nI++
        }
        //    	System.arraycopy(m_editDevPassword.toString().toCharArray(), 0, m_devComm.m_abyPacket, 6, 14);
        m_devComm!!.addCheckSum(true)
        m_dwCode = CMD_VERIFY_DEVPASS_CODE
        StartSendThread()
        return 0
    }

    fun Run_CmdExitDevPass(): Int {
        Run_Command_NP(CMD_EXIT_DEVPASS_CODE.toShort())
        return 0
    }

    fun Run_CmdAdjustSensor(): Int {
        m_strPost = "Adjusting sensor..."
        m_txtStatus.text = m_strPost
        Run_Command_NP(CMD_ADJUST_SENSOR_CODE.toShort())
        return 0
    }

    fun Run_CmdEnterStandByMode(): Int {
        m_strPost = "Enter Standby Mode..."
        m_txtStatus.text = m_strPost
        Run_Command_NP(CMD_ENTERSTANDBY_CODE.toShort())
        return 0
    }

    fun Run_CmdCancel(): Int {
        Thread(Runnable //    		@Override
        {
            var w_bRet: Boolean
            //. Init Packet
            m_devComm!!.initPacket2(CMD_FP_CANCEL_CODE.toShort(), true)
            m_devComm!!.setDataLen2(0x00.toShort())
            m_devComm!!.addCheckSum2(true)
            //. Send Packet
            w_bRet = false
//            if (m_devComm!!.m_nConnected.toInt() == 1 || m_devComm!!.m_nConnected.toInt() == 3) {
//                w_bRet = m_devComm!!.UART_SendCommand2(CMD_FP_CANCEL_CODE.toShort())
//            } else
            if (m_devComm!!.mNconnected.toInt() == 2) {
                w_bRet = m_devComm!!.usbSendPacket2()
            }
            if (w_bRet != true) {
                m_strPost = "Result : Cancel Send Failed\r\n"
                m_txtStatus.post(runShowStatus)
                m_txtStatus.post(m_runEnableCtrl)
                return@Runnable
            }
            //. Wait while processing cmd exit
            while (m_bCmdDone == false) {
                SystemClock.sleep(1)
            }
//            if (m_devComm!!.m_nConnected.toInt() == 1 || m_devComm!!.m_nConnected.toInt() == 3) {
//                w_bRet = m_devComm!!.UART_ReceiveAck2(CMD_FP_CANCEL_CODE.toShort())
//            } else
            if (m_devComm!!.mNconnected.toInt() == 2) {
                w_bRet = m_devComm!!.usbReceiveAck2()
            }
            m_strPost = if (w_bRet == true) {
                "Result : FP Cancel Success."
            } else {
                "Result : Cancel Failed\r\n"
            }
            m_txtStatus.post(runShowStatus)
            m_txtStatus.post(m_runEnableCtrl)
        }).start()
        return 0
    }

    fun Run_CmdGetFeatureOfCapturedFP(): Int {
        var w_blRet = false
        DevComm.memSet(m_TemplateData, 0.toByte(), GD_RECORD_SIZE)
        //. Assemble command packet
        m_devComm!!.initPacket(CMD_FEATURE_OF_CAPTURED_FP_CODE.toShort(), true)
        m_devComm!!.addCheckSum(true)
        m_strPost = "Input your finger"
        m_txtStatus.text = m_strPost
        m_dwCode = CMD_FEATURE_OF_CAPTURED_FP_CODE
        w_blRet = m_devComm!!.sendCommand(CMD_FEATURE_OF_CAPTURED_FP_CODE.toShort())
        if (w_blRet == false) {
            CloseDevice()
            return 1
        }
        if (m_devComm!!.getRetCode() != ERR_SUCCESS.toShort()) {
            DisplayResponsePacket(CMD_FEATURE_OF_CAPTURED_FP_CODE.toShort())
            return 1
        }
        StartSendThread()
        return 0
    }

    fun Run_CmdIdentifyWithTemplate2(): Int {
        var w_blRet = false
        //. Read template file
        if (ReadTemplateFile(0) == false || ReadTemplateFile2() == false) {
            return 1
        }
        //. Assemble command packet
        m_devComm!!.initPacket(CMD_IDENTIFY_TEMPLATE_WITH_FP_CODE.toShort(), true)
        m_devComm!!.setDataLen(0x0002.toShort())
        m_devComm!!.setCmdData(GD_RECORD_SIZE.toShort(), true)
        m_devComm!!.addCheckSum(true)
        m_dwCode = CMD_IDENTIFY_TEMPLATE_WITH_FP_CODE
        w_blRet = m_devComm!!.sendCommand(CMD_IDENTIFY_TEMPLATE_WITH_FP_CODE.toShort())
        if (w_blRet == false) {
            CloseDevice()
            return 1
        }
        if (m_devComm!!.getRetCode() != ERR_SUCCESS.toShort()) {
            DisplayResponsePacket(CMD_IDENTIFY_TEMPLATE_WITH_FP_CODE.toShort())
            return 1
        }
        m_strPost = "Input your finger"
        m_txtStatus.text = m_strPost
        //. Assemble data packet
        m_devComm!!.initPacket(CMD_IDENTIFY_TEMPLATE_WITH_FP_CODE.toShort(), false)
        m_devComm!!.setDataLen((GD_RECORD_SIZE + 2).toShort())
        m_devComm!!.mAbyPacket[6] = 0 // Template Index
        m_devComm!!.mAbyPacket[7] = 0 // Mode (0 : Set Buffer, 1 : Identify)
        System.arraycopy(m_TemplateData, 0, m_devComm!!.mAbyPacket, 8, GD_RECORD_SIZE)
        m_devComm!!.addCheckSum(false)
        //. Send data packet to target
        w_blRet = m_devComm!!.sendDataPacket(CMD_IDENTIFY_TEMPLATE_WITH_FP_CODE.toShort())
        if (w_blRet == false) {
            CloseDevice()
            return 1
        }
        if (m_devComm!!.getRetCode().toInt() != ERR_SUCCESS) {
            DisplayResponsePacket(CMD_IDENTIFY_TEMPLATE_WITH_FP_CODE.toShort())
            return 1
        }
        //. Assemble data packet
        m_devComm!!.initPacket(CMD_IDENTIFY_TEMPLATE_WITH_FP_CODE.toShort(), false)
        m_devComm!!.setDataLen((GD_RECORD_SIZE + 2).toShort())
        m_devComm!!.mAbyPacket[6] = 1 // Template Index
        m_devComm!!.mAbyPacket[7] = 1 // Mode (0 : Set Buffer, 1 : Set Buffer and Identify)
        System.arraycopy(m_TemplateData2, 0, m_devComm!!.mAbyPacket, 8, GD_RECORD_SIZE)
        m_devComm!!.addCheckSum(false)
        //. Send data packet to target
        StartSendThread()
        SystemClock.sleep(500)
        return 0
    }

    fun Run_CmdUpImage(): Int {
        m_bCmdDone = false
        m_nImageBufOffset = 0
        while (m_bThreadWork) {
            SystemClock.sleep(1)
        }
        Thread(Runnable //    		@Override
        {
            var w_blRet = false
            m_bThreadWork = true
            m_strPost = "Input your finger"
            m_txtStatus.post(runShowStatus)
            //. Assemble command packet
            m_devComm!!.initPacket(CMD_UP_IMAGE_CODE.toShort(), true)
            m_devComm!!.setDataLen(0x00.toShort())
            m_devComm!!.addCheckSum(true)
            m_dwCode = CMD_UP_IMAGE_CODE
            w_blRet = m_devComm!!.sendCommand(CMD_UP_IMAGE_CODE.toShort())
            if (w_blRet == false) {
                m_bSendResult = w_blRet
                m_txtStatus.post(procRspPacket)
                m_bThreadWork = false
                return@Runnable  // goto
            }
            if (m_devComm!!.getRetCode().toInt() != ERR_SUCCESS) {
                m_bSendResult = w_blRet
                m_txtStatus.post(procRspPacket)
                m_bThreadWork = false
                return@Runnable  // goto
            }
            m_nImgWidth = (m_devComm!!.mAbyPacket[9].toInt() shl 8 and 0x0000FF00 or (m_devComm!!.mAbyPacket[8].toInt() and 0x000000FF))
            m_nImgHeight = (m_devComm!!.mAbyPacket[11].toInt() shl 8 and 0x0000FF00 or (m_devComm!!.mAbyPacket[10].toInt() and 0x000000FF))
//            if (m_devComm!!.m_nConnected.toInt() == 1 || m_devComm!!.m_nConnected.toInt() == 3) {
//                while (true) {
//                    w_blRet = m_devComm!!.UART_ReceiveDataPacket(CMD_UP_IMAGE_CODE.toShort())
//                    if (w_blRet == false) {
//                        m_bSendResult = w_blRet
//                        m_txtStatus.post(procRspPacket)
//                        m_bThreadWork = false
//                        return@Runnable  // goto
//                    } else {
//                        if (m_devComm!!.GetRetCode().toInt() == ERR_SUCCESS) {
//                            if (m_devComm!!.GetDataLen() > IMAGE_RECEIVE_UINT + 2) {
//                                m_bSendResult = w_blRet
//                                m_txtStatus.post(procRspPacket)
//                                m_bThreadWork = false
//                                return@Runnable  // goto
//                            } else {
//                                if (m_nImageBufOffset == 0) {
//                                    m_strPost = "Uploading image..."
//                                    m_txtStatus.post(runShowStatus)
//                                }
//                                System.arraycopy(m_devComm!!.m_abyPacket, 8, m_binImage, m_nImageBufOffset, m_devComm!!.GetDataLen() - 2)
//                                m_nImageBufOffset = m_nImageBufOffset + (m_devComm!!.GetDataLen() - 2)
//                                if (m_nImageBufOffset == m_nImgWidth * m_nImgHeight) {
//                                    m_bSendResult = w_blRet
//                                    m_txtStatus.post(procRspPacket)
//                                    m_bThreadWork = false
//                                    return@Runnable  // goto
//                                }
//                            }
//                        } else {
//                            m_bSendResult = w_blRet
//                            m_txtStatus.post(procRspPacket)
//                            m_bThreadWork = false
//                            return@Runnable  // goto
//                        }
//                    }
//                }
//            } else {
            if (m_binImage != null) w_blRet = m_devComm!!.usbReceiveImage(m_binImage!!, m_nImgWidth * m_nImgHeight)
//            }
            m_bSendResult = w_blRet
            m_txtStatus.post(procRspPacket)
            m_bThreadWork = false
        }).start()
        return 0
    }

    fun Run_CmdIdentifyWithImage(): Int {
        var i: Int
        val r: Int
        val n: Int
        val w_nImgSize: Int
        var w_blRet = false
        //. Read image file
        if (!ReadImage(m_binImage)) {
            return 1
        }
        //. Assemble command packet
        m_devComm!!.initPacket(CMD_IDENTIFY_WITH_IMAGE_CODE.toShort(), true)
        m_devComm!!.setDataLen(0x0004.toShort())
        m_devComm!!.setCmdData(m_nImgWidth.toShort(), true)
        m_devComm!!.setCmdData(m_nImgHeight.toShort(), false)
        m_devComm!!.addCheckSum(true)
        m_dwCode = CMD_IDENTIFY_WITH_IMAGE_CODE
        w_blRet = m_devComm!!.sendCommand(CMD_IDENTIFY_WITH_IMAGE_CODE.toShort())
        if (w_blRet == false) {
            CloseDevice()
            return 1
        }
        if (m_devComm!!.getRetCode() != ERR_SUCCESS.toShort()) {
            DisplayResponsePacket(CMD_IDENTIFY_WITH_IMAGE_CODE.toShort())
            return 1
        }
//        if (m_devComm!!.m_nConnected.toInt() == 1 || m_devComm!!.m_nConnected.toInt() == 3) {
//            m_strPost = "Downloading image..."
//            m_txtStatus.post(runShowStatus)
//            w_nImgSize = m_nImgWidth * m_nImgHeight
//            n = w_nImgSize / IMAGE_RECEIVE_UINT
//            r = w_nImgSize % IMAGE_RECEIVE_UINT
//            i = 0
//            while (i < n) {
//                //. Assemble data packet
//                m_devComm!!.InitPacket(CMD_IDENTIFY_WITH_IMAGE_CODE.toShort(), false)
//                m_devComm!!.SetDataLen((0x0004 + GD_RECORD_SIZE).toShort())
//                m_devComm!!.m_abyPacket[6] = m_devComm!!.LOBYTE(i.toShort())
//                m_devComm!!.m_abyPacket[7] = m_devComm!!.HIBYTE(i.toShort())
//                m_devComm!!.m_abyPacket[8] = m_devComm!!.LOBYTE(IMAGE_RECEIVE_UINT.toShort())
//                m_devComm!!.m_abyPacket[9] = m_devComm!!.HIBYTE(IMAGE_RECEIVE_UINT.toShort())
//                System.arraycopy(m_binImage!!, i * IMAGE_RECEIVE_UINT, m_devComm!!.m_abyPacket, 10, IMAGE_RECEIVE_UINT)
//                m_devComm!!.AddCheckSum(false)
//                w_blRet = m_devComm!!.UART_SendDataPacket(CMD_IDENTIFY_WITH_IMAGE_CODE.toShort())
//                if (!w_blRet) {
//                    CloseDevice()
//                    return 1
//                }
//                m_strPost = String.format("%d%%...", (i + 1) * IMAGE_RECEIVE_UINT * 100 / w_nImgSize)
//                m_txtStatus.post(runShowStatus)
//                i++
//            }
//            if (r > 0) {
//                m_devComm!!.InitPacket(CMD_IDENTIFY_WITH_IMAGE_CODE.toShort(), false)
//                m_devComm!!.SetDataLen((0x0004 + GD_RECORD_SIZE).toShort())
//                m_devComm!!.m_abyPacket[6] = m_devComm!!.LOBYTE(i.toShort())
//                m_devComm!!.m_abyPacket[7] = m_devComm!!.HIBYTE(i.toShort())
//                m_devComm!!.m_abyPacket[8] = m_devComm!!.LOBYTE(r.toShort())
//                m_devComm!!.m_abyPacket[9] = m_devComm!!.HIBYTE(r.toShort())
//                System.arraycopy(m_binImage!!, i * IMAGE_RECEIVE_UINT, m_devComm!!.m_abyPacket, 10, r)
//                m_devComm!!.AddCheckSum(false)
//                w_blRet = m_devComm!!.UART_SendDataPacket(CMD_IDENTIFY_WITH_IMAGE_CODE.toShort())
//                if (!w_blRet) {
//                    CloseDevice()
//                    return 1
//                }
//            }
//            m_strPost = "100%..."
//            m_txtStatus.post(runShowStatus)
//        } else {
        w_blRet = m_devComm!!.usbDownImage(m_binImage, m_nImgWidth * m_nImgHeight)
        if (!w_blRet) {
            CloseDevice()
            return 1
        }
//        }
        // Identify
        m_devComm!!.initPacket(CMD_IDENTIFY_WITH_IMAGE_CODE.toShort(), false)
        m_devComm!!.setDataLen(0x0004.toShort())
        m_devComm!!.mAbyPacket[6] = 0
        m_devComm!!.mAbyPacket[7] = 0
        m_devComm!!.mAbyPacket[8] = 0
        m_devComm!!.mAbyPacket[9] = 0
        m_devComm!!.addCheckSum(false)
        StartSendThread()
        SystemClock.sleep(200)
        return 0
    }

    fun Run_CmdVerifyWithImage(p_nTmpNo: Int): Int {
        var i: Int
        val r: Int
        val n: Int
        val w_nImgSize: Int
        val w_nTemplateNo: Int
        var w_blRet = false
        //. Check inputed template no
        if (CheckInputTemplateNo(p_nTmpNo) == false) {
            return 1
        }
        //. Read image file
        if (!ReadImage(m_binImage)) {
            return 1
        }
        w_nTemplateNo = p_nTmpNo
        //. Assemble command packet
        m_devComm!!.initPacket(CMD_VERIFY_WITH_IMAGE_CODE.toShort(), true)
        m_devComm!!.setDataLen(0x0006.toShort())
        m_devComm!!.mAbyPacket[6] = DevComm.loByte(w_nTemplateNo.toShort())
        m_devComm!!.mAbyPacket[7] = DevComm.hiByte(w_nTemplateNo.toShort())
        m_devComm!!.mAbyPacket[8] = DevComm.loByte(m_nImgWidth.toShort())
        m_devComm!!.mAbyPacket[9] = DevComm.hiByte(m_nImgWidth.toShort())
        m_devComm!!.mAbyPacket[10] = DevComm.loByte(m_nImgHeight.toShort())
        m_devComm!!.mAbyPacket[11] = DevComm.hiByte(m_nImgHeight.toShort())
        m_devComm!!.addCheckSum(true)
        m_dwCode = CMD_VERIFY_WITH_IMAGE_CODE
        w_blRet = m_devComm!!.sendCommand(CMD_VERIFY_WITH_IMAGE_CODE.toShort())
        if (w_blRet == false) {
            CloseDevice()
            return 1
        }
        if (m_devComm!!.getRetCode() != ERR_SUCCESS.toShort()) {
            DisplayResponsePacket(CMD_VERIFY_WITH_IMAGE_CODE.toShort())
            return 1
        }
//        if (m_devComm!!.m_nConnected.toInt() == 1 || m_devComm!!.m_nConnected.toInt() == 3) {
//            m_strPost = "Downloading image..."
//            m_txtStatus.post(runShowStatus)
//            w_nImgSize = m_nImgWidth * m_nImgHeight
//            n = w_nImgSize / IMAGE_RECEIVE_UINT
//            r = w_nImgSize % IMAGE_RECEIVE_UINT
//            i = 0
//            while (i < n) {
//                //. Assemble data packet
//                m_devComm!!.InitPacket(CMD_VERIFY_WITH_IMAGE_CODE.toShort(), false)
//                m_devComm!!.SetDataLen((0x0004 + GD_RECORD_SIZE).toShort())
//                m_devComm!!.m_abyPacket[6] = m_devComm!!.LOBYTE(i.toShort())
//                m_devComm!!.m_abyPacket[7] = m_devComm!!.HIBYTE(i.toShort())
//                m_devComm!!.m_abyPacket[8] = m_devComm!!.LOBYTE(IMAGE_RECEIVE_UINT.toShort())
//                m_devComm!!.m_abyPacket[9] = m_devComm!!.HIBYTE(IMAGE_RECEIVE_UINT.toShort())
//                System.arraycopy(m_binImage!!, i * IMAGE_RECEIVE_UINT, m_devComm!!.m_abyPacket, 10, IMAGE_RECEIVE_UINT)
//                m_devComm!!.AddCheckSum(false)
//                w_blRet = m_devComm!!.UART_SendDataPacket(CMD_VERIFY_WITH_IMAGE_CODE.toShort())
//                if (!w_blRet) {
//                    CloseDevice()
//                }
//                m_strPost = String.format("%d%%...", (i + 1) * IMAGE_RECEIVE_UINT * 100 / w_nImgSize)
//                runShowStatus.run()
//                m_txtStatus.post(runShowStatus)
//                i++
//            }
//            if (r > 0) {
//                m_devComm!!.InitPacket(CMD_VERIFY_WITH_IMAGE_CODE.toShort(), false)
//                m_devComm!!.SetDataLen((0x0004 + GD_RECORD_SIZE).toShort())
//                m_devComm!!.m_abyPacket[6] = m_devComm!!.LOBYTE(i.toShort())
//                m_devComm!!.m_abyPacket[7] = m_devComm!!.HIBYTE(i.toShort())
//                m_devComm!!.m_abyPacket[8] = m_devComm!!.LOBYTE(r.toShort())
//                m_devComm!!.m_abyPacket[9] = m_devComm!!.HIBYTE(r.toShort())
//                System.arraycopy(m_binImage!!, i * IMAGE_RECEIVE_UINT, m_devComm!!.m_abyPacket, 10, r)
//                m_devComm!!.AddCheckSum(false)
//                w_blRet = m_devComm!!.UART_SendDataPacket(CMD_VERIFY_WITH_IMAGE_CODE.toShort())
//                if (!w_blRet) {
//                    CloseDevice()
//                }
//            }
//            m_strPost = "100%..."
//            m_txtStatus.post(runShowStatus)
//        } else {
        w_blRet = m_devComm!!.usbDownImage(m_binImage, m_nImgWidth * m_nImgHeight)
        if (!w_blRet) {
            CloseDevice()
            return 1
        }
//        }
        // Identify
        m_devComm!!.initPacket(CMD_VERIFY_WITH_IMAGE_CODE.toShort(), false)
        m_devComm!!.setDataLen(0x0004.toShort())
        m_devComm!!.mAbyPacket[6] = 0
        m_devComm!!.mAbyPacket[7] = 0
        m_devComm!!.mAbyPacket[8] = 0
        m_devComm!!.mAbyPacket[9] = 0
        m_devComm!!.addCheckSum(false)
        StartSendThread()
        SystemClock.sleep(200)
        return 0
    }

    fun Run_CmdVerifyWithDownTmpl(p_nTmpNo: Int): Int {
        val w_nTemplateNo: Int
        var w_blRet = false
        //. Check inputed template no
        if (CheckInputTemplateNo(p_nTmpNo) == false) {
            return 1
        }
        w_nTemplateNo = p_nTmpNo
        //. Read template file
        w_blRet = ReadTemplateFile(0)
        if (w_blRet == false) {
            return 1
        }
        //. Assemble command packet
        m_devComm!!.initPacket(CMD_VERIFY_WITH_DOWN_TMPL_CODE.toShort(), true)
        m_devComm!!.setDataLen(0x0004.toShort())
        m_devComm!!.mAbyPacket[6] = DevComm.loByte(w_nTemplateNo.toShort())
        m_devComm!!.mAbyPacket[7] = DevComm.hiByte(w_nTemplateNo.toShort())
        m_devComm!!.mAbyPacket[8] = DevComm.loByte(GD_RECORD_SIZE.toShort())
        m_devComm!!.mAbyPacket[9] = DevComm.hiByte(GD_RECORD_SIZE.toShort())
        m_devComm!!.addCheckSum(true)
        m_dwCode = CMD_VERIFY_WITH_DOWN_TMPL_CODE
        w_blRet = m_devComm!!.sendCommand(CMD_VERIFY_WITH_DOWN_TMPL_CODE.toShort())
        if (w_blRet == false) {
            CloseDevice()
            return 1
        }
        if (m_devComm!!.getRetCode().toInt() != ERR_SUCCESS) {
            DisplayResponsePacket(CMD_VERIFY_WITH_DOWN_TMPL_CODE.toShort())
            return 1
        }
        //. Assemble data packet
        m_devComm!!.initPacket(CMD_VERIFY_WITH_DOWN_TMPL_CODE.toShort(), false)
        m_devComm!!.setDataLen(GD_RECORD_SIZE.toShort())
        System.arraycopy(m_TemplateData, 0, m_devComm!!.mAbyPacket, 6, GD_RECORD_SIZE)
        m_devComm!!.addCheckSum(false)
        StartSendThread()
        SystemClock.sleep(200)
        return 0
    }

    fun Run_CmdIdentifyWithDownTmpl(): Int {
        var w_blRet = false
        //. Read template file
        w_blRet = ReadTemplateFile(0)
        if (w_blRet == false) {
            return 1
        }
        //. Assemble command packet
        m_devComm!!.initPacket(CMD_IDENTIFY_WITH_DOWN_TMPL_CODE.toShort(), true)
        m_devComm!!.setDataLen(0x0002.toShort())
        m_devComm!!.setCmdData(GD_RECORD_SIZE.toShort(), true)
        m_devComm!!.addCheckSum(true)
        m_dwCode = CMD_IDENTIFY_WITH_DOWN_TMPL_CODE
        w_blRet = m_devComm!!.sendCommand(CMD_IDENTIFY_WITH_DOWN_TMPL_CODE.toShort())
        if (w_blRet == false) {
            CloseDevice()
            return 1
        }
        if (m_devComm!!.getRetCode() != ERR_SUCCESS.toShort()) {
            DisplayResponsePacket(CMD_IDENTIFY_WITH_DOWN_TMPL_CODE.toShort())
            return 1
        }
        //. Assemble data packet
        m_devComm!!.initPacket(CMD_IDENTIFY_WITH_DOWN_TMPL_CODE.toShort(), false)
        m_devComm!!.setDataLen(GD_RECORD_SIZE.toShort())
        System.arraycopy(m_TemplateData, 0, m_devComm!!.mAbyPacket, 6, GD_RECORD_SIZE)
        m_devComm!!.addCheckSum(false)
        StartSendThread()
        SystemClock.sleep(200)
        return 0
    }
    /** */
    /** */
    fun Run_CmdEnterISPMode() {
        Run_Command_NP(CMD_ENTER_ISPMODE_CODE.toShort())
    }

    fun CheckInputTemplateNo(p_nTmpNo: Int): Boolean {
        if (p_nTmpNo > GD_MAX_RECORD_COUNT || p_nTmpNo < 1) {
            m_txtStatus.text = "Please input correct user id(1~" + GD_MAX_RECORD_COUNT.toShort() + ")"
            return false
        }
        return true
    }

    //    private void StopOperation(){
//        m_strPost = "Canceled";
//        m_FpImageViewer.post(runShowStatus);
//        m_FpImageViewer.post(runEnableCtrl);
//    }
    private fun DisplayResponsePacket(p_nCode: Short) {
        val w_nRet: Short
        val w_nData: Short
        val w_nData2: Short
        val w_nSize /*, w_wPrefix*/: Short
        m_strPost = ""
        m_txtStatus.text = m_strPost
        //    	w_wPrefix = m_devComm.MAKEWORD(m_devComm.m_abyPacket[0], m_devComm.m_abyPacket[1]);
        w_nRet = DevComm.makeWord(m_devComm!!.mAbyPacket[6], m_devComm!!.mAbyPacket[7])
        w_nData = DevComm.makeWord(m_devComm!!.mAbyPacket[8], m_devComm!!.mAbyPacket[9])
        w_nData2 = DevComm.makeWord(m_devComm!!.mAbyPacket[10], m_devComm!!.mAbyPacket[11])
        w_nSize = DevComm.makeWord(m_devComm!!.mAbyPacket[4], m_devComm!!.mAbyPacket[5])
        when (p_nCode.toInt()) {
            CMD_CLEAR_TEMPLATE_CODE -> if (w_nRet.toInt() == ERR_SUCCESS) {
                m_strPost = String.format("Result : Success\r\nTemplate No : %d", w_nData)
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
            }
            CMD_UP_IMAGE_CODE -> if (w_nRet.toInt() == ERR_SUCCESS) {
                m_strPost = String.format("Result : Receive Image Success")
                m_txtStatus.post(runDrawImage)
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
            }
            CMD_READ_TEMPLATE_CODE -> if (w_nRet == ERR_SUCCESS.toShort()) {
                m_strPost = String.format("Result : Success\r\nTemplate No : %d", w_nData)
                WriteTemplateFile(w_nData.toInt(), m_TemplateData)
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
            }
            CMD_WRITE_TEMPLATE_CODE -> if (w_nRet == ERR_SUCCESS.toShort()) {
                m_strPost = String.format("Result : Success\r\nTemplate No : %d", w_nData)
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
                if (w_nData.toInt() == ERR_DUPLICATION_ID) {
                    m_strPost += String.format(" %d.", w_nData2)
                }
            }
            CMD_GET_EMPTY_ID_CODE -> if (w_nRet == ERR_SUCCESS.toShort()) {
                m_strPost = String.format("Result : Success\r\nEmpty ID : %d", w_nData)
                //    				m_editUserID.setText(String.format("%d", w_nData));
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
            }
            CMD_GET_ENROLL_COUNT_CODE -> if (w_nRet == ERR_SUCCESS.toShort()) {
                m_strPost = String.format("Result : Success\r\nEnroll Count : %d", w_nData)
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
            }
            CMD_VERIFY_WITH_DOWN_TMPL_CODE, CMD_IDENTIFY_WITH_DOWN_TMPL_CODE, CMD_VERIFY_CODE, CMD_IDENTIFY_CODE, CMD_IDENTIFY_FREE_CODE, CMD_ENROLL_CODE, CMD_ENROLL_ONETIME_CODE, CMD_CHANGE_TEMPLATE_CODE, CMD_IDENTIFY_WITH_IMAGE_CODE, CMD_VERIFY_WITH_IMAGE_CODE -> if (w_nRet == ERR_SUCCESS.toShort()) {
                m_strPost = when (w_nData.toInt()) {
                    GD_NEED_RELEASE_FINGER -> "Release your finger"
                    GD_NEED_FIRST_SWEEP -> "Input your finger"
                    GD_NEED_SECOND_SWEEP -> "Two More"
                    GD_NEED_THIRD_SWEEP -> "One More"
                    else ->  //    					if( p_nCode != (short)DevComm.CMD_IDENTIFY_FREE_CODE || m_devComm.LOBYTE(w_nData) == DevComm.ERR_FP_CANCEL )
//    						m_btnCloseDevice.setEnabled(true);
                        String.format("Result : Success\r\nTemplate No : %d", w_nData)
                }
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
                if (DevComm.loByte(w_nData).toInt() == ERR_BAD_QUALITY) {
                    m_strPost += "\r\nAgain... !"
                } else {
                    if (w_nData.toInt() == ERR_DUPLICATION_ID) {
                        m_strPost += String.format(" %d.", w_nData2)
                    }
                }
                //    				if( p_nCode != (short)DevComm.CMD_IDENTIFY_FREE_CODE || m_devComm.LOBYTE(w_nData) == DevComm.ERR_FP_CANCEL ||
//						m_devComm.LOBYTE(w_nData) == DevComm.ERR_ALL_TMPL_EMPTY || m_devComm.LOBYTE(w_nData) == DevComm.ERR_INVALID_OPERATION_MODE ||
//    					m_devComm.LOBYTE(w_nData) == DevComm.ERR_NOT_AUTHORIZED)
//    					m_btnCloseDevice.setEnabled(true);
            }
            CMD_CLEAR_ALLTEMPLATE_CODE -> if (w_nRet == ERR_SUCCESS.toShort()) {
                m_strPost = String.format("Result : Success\r\nCleared Template Count : %d", w_nData)
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
            }
            CMD_GET_BROKEN_TEMPLATE_CODE -> if (w_nRet == ERR_SUCCESS.toShort()) {
                m_strPost = String.format("Result : Success\r\nBroken Template Count : %d\r\nFirst Broken Template ID : %d", w_nData, w_nData2)
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
            }
            CMD_VERIFY_DEVPASS_CODE, CMD_SET_DEVPASS_CODE, CMD_EXIT_DEVPASS_CODE ->  //    		case (short)DevComm.CMD_SET_COMMNAD_VALID_FLAG_CODE:
                if (w_nRet == ERR_SUCCESS.toShort()) {
                    m_strPost = String.format("Result : Success.")
                } else {
                    m_strPost = String.format("Result : Fail\r\n")
                    m_strPost += GetErrorMsg(w_nData)
                }
            CMD_SET_PARAMETER_CODE -> if (w_nRet == ERR_SUCCESS.toShort()) {
                m_strPost = if (m_bParamGet) String.format("Result : Success\r\nParameter Value = %d",
                        (m_devComm!!.mAbyPacket[8].toInt() and 0x000000FF) + (m_devComm!!.mAbyPacket[9].toInt() shl 8 and 0x0000FF00) + (m_devComm!!.mAbyPacket[10].toInt() shl 16 and 0x00FF0000) + (m_devComm!!.mAbyPacket[24].toInt() shl 8 and -0x1000000)) else String.format("Result : Success\r\n")
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
            }
            CMD_ADJUST_SENSOR_CODE -> if (w_nRet == ERR_SUCCESS.toShort()) {
                m_strPost = String.format("Result : Adjust Success")
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
            }
            CMD_ENTERSTANDBY_CODE -> if (w_nRet == ERR_SUCCESS.toShort()) {
                m_strPost = String.format("Result : Enter Standby Mode Success")
            } else {
                m_strPost = String.format("Result : Enter Standby Mode Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
            }
            CMD_GET_FW_VERSION_CODE -> if (w_nRet == ERR_SUCCESS.toShort()) {
                m_strPost = String.format("Result : Success\r\nFirmware Version: %d.%d", DevComm.loByte(w_nData), DevComm.hiByte(w_nData))
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
            }
            CMD_FINGER_DETECT_CODE -> if (w_nRet == ERR_SUCCESS.toShort()) {
                if (w_nData == GD_DETECT_FINGER.toShort()) {
                    m_strPost = String.format("Finger Detected.")
                } else if (w_nData == GD_NO_DETECT_FINGER.toShort()) {
                    m_strPost = String.format("Finger not Detected.")
                }
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
            }
            CMD_FP_CANCEL_CODE -> if (w_nRet == ERR_SUCCESS.toShort()) {
                m_strPost = String.format("Result : FP Cancel Success.")
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
            }
            CMD_FEATURE_OF_CAPTURED_FP_CODE -> if (w_nRet == ERR_SUCCESS.toShort()) {
                m_strPost = if (w_nSize.toInt() != GD_RECORD_SIZE.toShort() + 2) {
                    String.format("Result : Fail\r\nCommunication Error")
                } else {
                    System.arraycopy(m_devComm!!.mAbyPacket, 8, m_TemplateData, 0, GD_RECORD_SIZE)
                    String.format("Result : Success")
                }
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
            }
            CMD_IDENTIFY_TEMPLATE_WITH_FP_CODE -> if (w_nRet == ERR_SUCCESS.toShort()) {
                if (DevComm.loByte(w_nData).toShort() == GD_DOWNLOAD_SUCCESS.toShort()) {
                    m_strPost = String.format("Result : Download Success\r\nInput your finger")
                    m_txtStatus.text = m_strPost
                    return
                } else {
                    m_strPost = String.format("Result : Identify OK.")
                    m_txtStatus.text = m_strPost
                }
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
            }
            RCM_INCORRECT_COMMAND_CODE -> m_strPost = String.format("Received incorrect command !")
            CMD_ENTER_ISPMODE_CODE -> if (w_nRet == ERR_SUCCESS.toShort()) {
                m_strPost = String.format("Result : Success\r\nRunning ISP. Can you programming.")
            } else {
                m_strPost = String.format("Result : Fail\r\n")
                m_strPost += GetErrorMsg(w_nData)
            }
            else -> {
            }
        }
        if (p_nCode == CMD_IDENTIFY_FREE_CODE.toShort()) {
            if (w_nRet == ERR_SUCCESS.toShort() ||
                    DevComm.loByte(w_nData).toInt() != ERR_NOT_AUTHORIZED && DevComm.loByte(w_nData).toInt() != ERR_FP_CANCEL && DevComm.loByte(w_nData).toInt() != ERR_INVALID_OPERATION_MODE && DevComm.loByte(w_nData).toInt() != ERR_ALL_TMPL_EMPTY) {
                m_txtStatus.text = m_strPost
                DevComm.memSet(m_devComm!!.mAbyPacket, 0.toByte(), 64 * 1024)
                StartSendThread()
                return
            }
        }
        if (p_nCode == CMD_ENROLL_CODE.toShort() ||
                p_nCode == CMD_CHANGE_TEMPLATE_CODE.toShort()) {
            when (w_nData.toInt()) {
                GD_NEED_RELEASE_FINGER, GD_NEED_FIRST_SWEEP, GD_NEED_SECOND_SWEEP, GD_NEED_THIRD_SWEEP, ERR_BAD_QUALITY -> {
                    m_txtStatus.text = m_strPost
                    DevComm.memSet(m_devComm!!.mAbyPacket, 0.toByte(), 64 * 1024)
                    StartSendThread()
                    return
                }
                else -> {
                }
            }
        }
        if (p_nCode == CMD_ENROLL_ONETIME_CODE.toShort() || p_nCode == CMD_VERIFY_CODE.toShort() ||
                p_nCode == CMD_IDENTIFY_CODE.toShort() || p_nCode == CMD_IDENTIFY_FREE_CODE.toShort()) {
            when (w_nData.toInt()) {
                GD_NEED_RELEASE_FINGER -> {
                    m_txtStatus.text = m_strPost
                    DevComm.memSet(m_devComm!!.mAbyPacket, 0.toByte(), 64 * 1024)
                    StartSendThread()
                    return
                }
                else -> {
                }
            }
        }
        m_txtStatus.post(m_runEnableCtrl)
        m_txtStatus.text = m_strPost
        DevComm.memSet(m_devComm!!.mAbyPacket, 0.toByte(), 64 * 1024)
        m_bCmdDone = true
    }

    private fun GetErrorMsg(p_wErrorCode: Short): String {
        val w_ErrMsg: String
        w_ErrMsg = when (p_wErrorCode.toInt() and 0xFF) {
            ERR_VERIFY -> "Verify NG"
            ERR_IDENTIFY -> "Identify NG"
            ERR_EMPTY_ID_NOEXIST -> "Empty Template no Exist"
            ERR_BROKEN_ID_NOEXIST -> "Broken Template no Exist"
            ERR_TMPL_NOT_EMPTY -> "Template of this ID Already Exist"
            ERR_TMPL_EMPTY -> "This Template is Already Empty"
            ERR_INVALID_TMPL_NO -> "Invalid Template No"
            ERR_ALL_TMPL_EMPTY -> "All Templates are Empty"
            ERR_INVALID_TMPL_DATA -> "Invalid Template Data"
            ERR_DUPLICATION_ID ->  //    		w_ErrMsg.Format("Duplicated ID : %d.", HIBYTE(p_wErrorCode));
                "Duplicated ID : "
            ERR_BAD_QUALITY -> "Bad Quality Image"
            ERR_SMALL_LINES -> "Small line Image"
            ERR_TOO_FAST -> "Too fast swiping"
            ERR_TIME_OUT -> "Time Out"
            ERR_GENERALIZE -> "Fail to Generalize"
            ERR_NOT_AUTHORIZED -> "Device not authorized."
            ERR_EXCEPTION -> "Exception Error "
            ERR_MEMORY -> "Memory Error "
            ERR_INVALID_PARAM -> "Invalid Parameter"
            ERR_NO_RELEASE -> "No Release Finger Fail"
            ERR_INTERNAL -> "Internal Error."
            ERR_FP_CANCEL -> "Canceled."
            ERR_INVALID_OPERATION_MODE -> "Invalid Operation Mode"
            ERR_NOT_SET_PWD -> "Password was not set."
            ERR_FP_NOT_DETECTED -> "Finger is not detected."
            ERR_ADJUST_SENSOR -> "Failed to adjust sensor."
            else -> "Fail"
        }
        return w_ErrMsg
    }

    fun ReadTemplateFile(p_nUserID: Int): Boolean { // Load Template from (mnt/sdcard/sz_template)
        val w_nLen: Int
        var i: Int
        var w_nChkSum = 0
        var w_nCalcChkSum: Short = 0
        // Open Template File
        val w_szSaveDirPath = Environment.getExternalStorageDirectory().absolutePath + "/sz_template"
        val w_fpTemplate = File("$w_szSaveDirPath/$p_nUserID.fpt")
        if (!w_fpTemplate.exists()) { // Show Save Path
            m_strPost = "Can't load $w_szSaveDirPath/$p_nUserID.fpt"
            return false
        }
        // Get File Length
        w_nLen = w_fpTemplate.length().toInt()
        if (w_nLen > GD_MAX_RECORD_SIZE) {
            m_strPost = "Invalid template file."
            return false
        }
        // Load Template Data
        var w_fiTemplate: FileInputStream? = null
        try {
            w_fiTemplate = FileInputStream(w_fpTemplate)
            w_fiTemplate.read(m_TemplateData, 0, w_nLen)
            w_fiTemplate.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        // Set Template Length
        if (w_nLen == GD_RECORD_SIZE) {
            i = 0
            while (i < w_nLen - 2) {
                w_nChkSum += (m_TemplateData[i].toInt() and 0xFF).toShort()
                i++
            }
            w_nCalcChkSum = DevComm.makeWord(m_TemplateData[w_nLen - 2], m_TemplateData[w_nLen - 1])
            if (w_nChkSum != w_nCalcChkSum.toInt()) {
                m_strPost = "Invalid template data."
                return false
            }
        }
        m_nTemplateSize = w_nLen
        return true
    }

    fun ReadTemplateFile2(): Boolean { //    	int				i = 0;
//    	WORD			w_nChkSum = 0, w_nCaclChkSum = 0;
//    	CFile			w_clsFile;
//    	CFileDialog		w_dlgOpen(TRUE , _T("First Template") , NULL, OFN_HIDEREADONLY, "Template File(*.fpt)|*.fpt|");
//
//    	if (w_dlgOpen.DoModal() == IDOK)
//    	{
//    		if (!w_dlgOpen.GetPathName().IsEmpty())
//    		{
//    			if (!w_clsFile.Open(w_dlgOpen.GetPathName(), CFile::modeRead))
//    			{
//    				AfxMessageBox(_T("Failed to read template!"));
//    				return FALSE;
//    			}
//
//    			if (w_clsFile.GetLength() != GD_RECORD_SIZE)
//    			{
//    				AfxMessageBox(_T("Invalid template data !"));
//    				return FALSE;
//    			}
//
//    			w_clsFile.Read(m_TemplateData2, GD_RECORD_SIZE);
//    			w_clsFile.Close();
//
//    			for (i = 0; i < GD_TEMPLATE_SIZE - 2 ; i++){
//    				w_nChkSum += m_TemplateData2[i];
//    			}
//    			w_nCaclChkSum = MAKEWORD(m_TemplateData2[GD_TEMPLATE_SIZE - 2], m_TemplateData2[GD_TEMPLATE_SIZE - 1]);
//
//    			if (w_nChkSum != w_nCaclChkSum)
//    			{
//    				AfxMessageBox(_T("Invalid template data !"));
//    				return FALSE;
//    			}
//
//    			return TRUE;
//    		}
//    	}
        return false
    }
    /** */
    /** */
    fun WriteTemplateFile(p_nUserID: Int, pTemplate: ByteArray?): Boolean { // Save Template to (mnt/sdcard/sz_template)
// Create Directory
        val w_szSaveDirPath = Environment.getExternalStorageDirectory().absolutePath + "/sz_template"
        val w_fpDir = File(w_szSaveDirPath)
        if (!w_fpDir.exists()) w_fpDir.mkdirs()
        // Create Template File
        val w_fpTemplate = File("$w_szSaveDirPath/$p_nUserID.fpt")
        if (!w_fpTemplate.exists()) {
            try {
                w_fpTemplate.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
        }
        // Save Template Data
        var w_foTemplate: FileOutputStream? = null
        try {
            w_foTemplate = FileOutputStream(w_fpTemplate)
            w_foTemplate.write(pTemplate, 0, m_nTemplateSize)
            w_foTemplate.close()
            // Show Save Path
            m_strPost += "\nSaved file path = $w_szSaveDirPath/$p_nUserID.fpt"
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun ReadImage(pImage: ByteArray?): Boolean { //    	int				i = 0, w_nWidth, w_nHeight;
//    	BYTE*			w_pBuf;
//    	WORD			w_nChkSum = 0, w_nCaclChkSum = 0;
//    	CString			w_strPath;
//    	CFile			w_clsFile;
//    	CFileDialog		w_dlgOpen(TRUE , _T("FingerPrint Image") , NULL, OFN_HIDEREADONLY, "Image File(*.bmp)|*.bmp|");
//
//    	if (w_dlgOpen.DoModal() == IDOK)
//    	{
//    		if (!w_dlgOpen.GetPathName().IsEmpty())
//    		{
//    			w_strPath = w_dlgOpen.GetPathName();
//
//    			if (FCLoadImage(w_strPath.GetBuffer(0), &w_pBuf, &w_nWidth, &w_nHeight, 0) != 0)
//    			{
//    				AfxMessageBox(_T("Load Fail!!!"));
//    				return FALSE;
//    			}
//
//    			if ( !((w_nWidth == 242 && w_nHeight == 266) ||
//    				   (w_nWidth == 202 && w_nHeight == 258) ||
//    				   (w_nWidth == 256 && w_nHeight == 288)))
//    			{
//    				AfxMessageBox(_T("Image size is not correct!"));
//    				goto l_exit;
//    			}
//
//    			g_nImageWidth = w_nWidth;
//    			g_nImageHeight = w_nHeight;
//
//    			memcpy(pImage, w_pBuf, w_nWidth*w_nHeight);
//
//    			delete[] w_pBuf;
//
//    			return TRUE;
//    		}
//    	}
//
//    	return FALSE;
//
//    l_exit:
//
//    	delete[] w_pBuf;
        return m_nImgWidth > 0 && m_nImgHeight > 0
    }

    fun WriteImage(pImage: ByteArray?): Boolean {
        return true
    }

    var procRspPacket = Runnable {
        val w_wCmd: Short
        if (m_bSendResult == false) {
            m_strPost = "Fail to receive response! \n Please check the connection to target."
            m_txtStatus.text = m_strPost
            m_txtStatus.post(m_runEnableCtrl)
            m_bCmdDone = true
            return@Runnable
        }
        //. Display response packet
        w_wCmd = (m_devComm!!.mAbyPacket[3].toInt() shl 8 and 0x0000FF00 or (m_devComm!!.mAbyPacket[2].toInt() and 0x000000FF)).toShort()
        DisplayResponsePacket(w_wCmd)
    }
    var runShowStatus = Runnable { m_txtStatus.text = m_strPost }
    var runDrawImage = Runnable {
        var nSize: Int
        MakeBMPBuf(m_binImage, m_bmpImage, m_nImgWidth, m_nImgHeight)
        nSize = if (m_nImgWidth % 4 != 0) m_nImgWidth + (4 - m_nImgWidth % 4) else m_nImgWidth
        nSize = 1078 + nSize * m_nImgHeight
        //            DebugManage.WriteBmp(m_bmpImage, nSize);
        val image = BitmapFactory.decodeByteArray(m_bmpImage, 0, nSize)
        m_FpImageViewer.setImageBitmap(image)
    }

    private fun MakeBMPBuf(Input: ByteArray?, Output: ByteArray?, iImageX: Int, iImageY: Int) {
        val w_bTemp = ByteArray(4)
        val head = ByteArray(1078)
        val head2 = byteArrayOf(/** */ //file header
                0x42, 0x4d,  //file type
//0x36,0x6c,0x01,0x00, //file size***
                0x0, 0x0, 0x0, 0x00,  //file size***
                0x00, 0x00,  //reserved
                0x00, 0x00,  //reserved
                0x36, 0x4, 0x00, 0x00,  //head byte***
                /** */ //infoheader
                0x28, 0x00, 0x00, 0x00,  //struct size
//0x00,0x01,0x00,0x00,//map width***
                0x00, 0x00, 0x0, 0x00,  //map width***
//0x68,0x01,0x00,0x00,//map height***
                0x00, 0x00, 0x00, 0x00,  //map height***
                0x01, 0x00,  //must be 1
                0x08, 0x00,  //color count***
                0x00, 0x00, 0x00, 0x00,  //compression
//0x00,0x68,0x01,0x00,//data size***
                0x00, 0x00, 0x00, 0x00,  //data size***
                0x00, 0x00, 0x00, 0x00,  //dpix
                0x00, 0x00, 0x00, 0x00,  //dpiy
                0x00, 0x00, 0x00, 0x00,  //color used
                0x00, 0x00, 0x00, 0x00)
        var i: Int
        var j: Int
        var num: Int
        var iImageStep: Int
        Arrays.fill(w_bTemp, 0.toByte())
        System.arraycopy(head2, 0, head, 0, head2.size)
        iImageStep = if (iImageX % 4 != 0) iImageX + (4 - iImageX % 4) else iImageX
        num = iImageX
        head[18] = (num and 0xFF).toByte()
        num = num shr 8
        head[19] = (num and 0xFF).toByte()
        num = num shr 8
        head[20] = (num and 0xFF).toByte()
        num = num shr 8
        head[21] = (num and 0xFF).toByte()
        num = iImageY
        head[22] = (num and 0xFF).toByte()
        num = num shr 8
        head[23] = (num and 0xFF).toByte()
        num = num shr 8
        head[24] = (num and 0xFF).toByte()
        num = num shr 8
        head[25] = (num and 0xFF).toByte()
        j = 0
        i = 54
        while (i < 1078) {
            head[i + 2] = j.toByte()
            head[i + 1] = head[i + 2]
            head[i] = head[i + 1]
            head[i + 3] = 0
            j++
            i = i + 4
        }
        System.arraycopy(head, 0, Output, 0, 1078)
        if (iImageStep == iImageX) {
            i = 0
            while (i < iImageY) {
                System.arraycopy(Input!!, i * iImageX, Output, 1078 + i * iImageX, iImageX)
                i++
            }
        } else {
            iImageStep = iImageStep - iImageX
            i = 0
            while (i < iImageY) {
                System.arraycopy(Input!!, i * iImageX, Output, 1078 + i * (iImageX + iImageStep), iImageX)
                System.arraycopy(w_bTemp, 0, Output, 1078 + i * (iImageX + iImageStep) + iImageX, iImageStep)
                i++
            }
        }
    }

    companion object {
        /** Called when the activity is first created.  */
        var m_devComm: DevComm? = null
        var m_dwCode: Int = 0
        var m_bThreadWork = false
        var m_bCmdDone = false
        var m_bSendResult = false
    }

}