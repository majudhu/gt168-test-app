package com.szadst.szoemhost_lib;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android_serialport_api.ComBean;
import android_serialport_api.SerialHelper;
import android_serialport_api.SerialPortFinder;

public class DevComm {

    // Packet Prefix
    static final int CMD_PREFIX_CODE = (0xAA55);
    private static final int RCM_PREFIX_CODE = (0x55AA);
    static final int CMD_DATA_PREFIX_CODE = (0xA55A);
    private static final int RCM_DATA_PREFIX_CODE = (0x5AA5);

    // Command
    static final int CMD_VERIFY_CODE = (0x0101);
    static final int CMD_IDENTIFY_CODE = (0x0102);
    static final int CMD_ENROLL_CODE = (0x0103);
    static final int CMD_ENROLL_ONETIME_CODE = (0x0104);
    static final int CMD_CLEAR_TEMPLATE_CODE = (0x0105);
    static final int CMD_CLEAR_ALLTEMPLATE_CODE = (0x0106);
    static final int CMD_GET_EMPTY_ID_CODE = (0x0107);
    static final int CMD_GET_BROKEN_TEMPLATE_CODE = (0x0109);
    static final int CMD_READ_TEMPLATE_CODE = (0x010A);
    static final int CMD_WRITE_TEMPLATE_CODE = (0x010B);
    static final int CMD_GET_FW_VERSION_CODE = (0x0112);
    static final int CMD_FINGER_DETECT_CODE = (0x0113);
    static final int CMD_FEATURE_OF_CAPTURED_FP_CODE = (0x011A);
    static final int CMD_IDENTIFY_TEMPLATE_WITH_FP_CODE = (0x011C);
    static final int CMD_IDENTIFY_FREE_CODE = (0x0125);
    static final int CMD_SET_DEVPASS_CODE = (0x0126);
    static final int CMD_VERIFY_DEVPASS_CODE = (0x0127);
    static final int CMD_GET_ENROLL_COUNT_CODE = (0x0128);
    static final int CMD_CHANGE_TEMPLATE_CODE = (0x0129);
    static final int CMD_UP_IMAGE_CODE = (0x012C);
    static final int CMD_VERIFY_WITH_DOWN_TMPL_CODE = (0x012D);
    static final int CMD_IDENTIFY_WITH_DOWN_TMPL_CODE = (0x012E);
    static final int CMD_FP_CANCEL_CODE = (0x0130);
    static final int CMD_ADJUST_SENSOR_CODE = (0x0137);
    static final int CMD_IDENTIFY_WITH_IMAGE_CODE = (0x0138);
    static final int CMD_VERIFY_WITH_IMAGE_CODE = (0x0139);
    static final int CMD_SET_PARAMETER_CODE = (0x013A);
    static final int CMD_EXIT_DEVPASS_CODE = (0x013B);
    private static final int CMD_TEST_CONNECTION_CODE = (0x0150);
    static final int CMD_ENTERSTANDBY_CODE = (0x0155);
    static final int RCM_INCORRECT_COMMAND_CODE = (0x0160);
    static final int CMD_ENTER_ISPMODE_CODE = (0x0171);

    // Error Code
    static final int ERR_SUCCESS = (0);
    private static final int ERR_FAIL = (1);
    private static final int ERR_COMM_FAIL = (3);
    static final int ERR_VERIFY = (0x11);
    static final int ERR_IDENTIFY = (0x12);
    static final int ERR_TMPL_EMPTY = (0x13);
    static final int ERR_TMPL_NOT_EMPTY = (0x14);
    static final int ERR_ALL_TMPL_EMPTY = (0x15);
    static final int ERR_EMPTY_ID_NOEXIST = (0x16);
    static final int ERR_BROKEN_ID_NOEXIST = (0x17);
    static final int ERR_INVALID_TMPL_DATA = (0x18);
    static final int ERR_DUPLICATION_ID = (0x19);
    static final int ERR_TOO_FAST = (0x20);
    static final int ERR_BAD_QUALITY = (0x21);
    static final int ERR_SMALL_LINES = (0x22);
    static final int ERR_TIME_OUT = (0x23);
    static final int ERR_NOT_AUTHORIZED = (0x24);
    static final int ERR_GENERALIZE = (0x30);
    static final int ERR_FP_CANCEL = (0x41);
    static final int ERR_INTERNAL = (0x50);
    static final int ERR_MEMORY = (0x51);
    static final int ERR_EXCEPTION = (0x52);
    static final int ERR_INVALID_TMPL_NO = (0x60);
    static final int ERR_INVALID_PARAM = (0x70);
    static final int ERR_NO_RELEASE = (0x71);
    static final int ERR_INVALID_OPERATION_MODE = (0x72);
    static final int ERR_NOT_SET_PWD = (0x74);
    static final int ERR_FP_NOT_DETECTED = (0x75);
    static final int ERR_ADJUST_SENSOR = (0x76);

    // Return Value
    static final int GD_NEED_FIRST_SWEEP = (0xFFF1);
    static final int GD_NEED_SECOND_SWEEP = (0xFFF2);
    static final int GD_NEED_THIRD_SWEEP = (0xFFF3);
    static final int GD_NEED_RELEASE_FINGER = (0xFFF4);

    static final int GD_DETECT_FINGER = (0x01);
    static final int GD_NO_DETECT_FINGER = (0x00);
    static final int GD_DOWNLOAD_SUCCESS = (0xA1);

    // Packet
    public static final int MAX_DATA_LEN = (600); /*512*/
    private static final int CMD_PACKET_LEN = (22);

    // Template
    public static final int GD_MAX_RECORD_COUNT = (5000);
    private static final int GD_TEMPLATE_SIZE = (570);
    static final int GD_RECORD_SIZE = (GD_TEMPLATE_SIZE);// + 2)	// CkeckSum len = 2
    static final int GD_MAX_RECORD_SIZE = (900);

    //--------------- For Usb Communication ------------//
    private static final int SCSI_TIMEOUT = (3000);
    private static final int GD_MAX_FP_TIME_OUT = (60);
    private static final int COMM_SLEEP_TIME = (100);
    private static final int ONCE_UP_IMAGE_UINT = (60000);
    private static final int COMM_TIMEOUT = (15000);

    private int m_nPacketSize;
    public byte m_bySrcDeviceID = 1, m_byDstDeviceID = 1;
    byte[] m_abyPacket = new byte[64 * 1024];
    private byte[] m_abyPacket2 = new byte[MAX_DATA_LEN + 10];
    private byte[] m_abyPacketTmp = new byte[64 * 1024];
    //--------------------------------------------------//

    private final Context mApplicationContext;
    private Activity m_parentAcitivity;
    private static final int VID = 0x2009;
    private static final int PID = 0x7638;

    private static boolean m_bSendPacketWork = false;

    // USB
    private UsbController m_usbBase;

    // UART ch34xuartdriver
    private static final String UART_ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";
    private byte[] m_pUARTReadBuf;
    private int m_nUARTReadLen;
    private boolean m_bBufferHandle = false;

    // Serial Port
    private SerialPortFinder mSerialPortFinder;//�����豸����
    private DispQueueThread DispQueue;//ˢ����ʾ�߳�
    private SerialControl m_SerialPort;

    // Connection
    byte m_nConnected;    // 0 : Not Connected, 1 : UART, 2 : USB, 3 : ttyUART

    public LibDebugManage m_dbgInfo;

    DevComm(Activity parentActivity, IUsbConnState usbConnState) {
        m_parentAcitivity = parentActivity;
        mApplicationContext = parentActivity.getApplicationContext();

        // USB Init
        m_usbBase = new UsbController(parentActivity, usbConnState, VID, PID);

        // Buffer Init
        m_nConnected = 0;
        m_nUARTReadLen = 0;
        byte[] m_pWriteBuffer = new byte[DevComm.MAX_DATA_LEN];
        byte[] m_pReadBuffer = new byte[DevComm.MAX_DATA_LEN];
        m_pUARTReadBuf = new byte[DevComm.MAX_DATA_LEN];

        DispQueue = new DispQueueThread();
        DispQueue.start();
    }

    void DevComm_Init(Activity parentActivity, IUsbConnState usbConnState) {
        m_nConnected = 0;
        m_nUARTReadLen = 0;
    }

    boolean IsInit() {
        if (m_nConnected == 0)
            return false;
        else if (m_nConnected == 1)
            return true;
        else if (m_nConnected == 2)
            return m_usbBase.IsInit();
        else
            return true;
    }

    boolean OpenComm(String p_szDevice, int p_nBaudrate) {
        if (m_nConnected != 0)
            return false;

        if (p_szDevice.equals("USB")) // USB
        {
            if (!m_usbBase.IsInit())
                m_usbBase.init();
            if (!m_usbBase.IsInit())
                return false;
            m_nConnected = 2;
        } else    // ttyUART
        {
            m_SerialPort.setPort(p_szDevice);
            m_SerialPort.setBaudRate(p_nBaudrate);
            try {
                m_SerialPort.open();
            } catch (SecurityException | IOException | InvalidParameterException e) {
                Toast.makeText(mApplicationContext, "Open ttyUART device failed!", Toast.LENGTH_SHORT).show();
                return false;
            }
            m_nConnected = 3;
        }

        return true;
    }

    void CloseComm() {
        if (m_nConnected == 0) {
        } else if (m_nConnected == 2)    // USB
        {
            m_nConnected = 0;
            m_usbBase.uninit();
        } else    // ttyUART
        {
            m_SerialPort.stopSend();
            m_SerialPort.close();
            m_nConnected = 0;
        }
    }

    int Run_TestConnection() {
        boolean w_bRet;

        InitPacket((short) CMD_TEST_CONNECTION_CODE, true);
        AddCheckSum(true);

        w_bRet = Send_Command((short) CMD_TEST_CONNECTION_CODE);

        if (!w_bRet) {
            return ERR_COMM_FAIL;
        }

        if (GetRetCode() != ERR_SUCCESS) {
            return ERR_FAIL;
        }

        return ERR_SUCCESS;
    }

    int Run_GetDeviceInfo() {
        return ERR_SUCCESS;
    }

    public boolean GetDeviceInformation(String[] deviceInfo) {
        int[] w_nRecvLen = new int[1];
        byte[] w_abyPCCmd = new byte[6];
        byte[] w_abyData = new byte[32];

        String w_strTmp;
        boolean w_bRet;

        Arrays.fill(w_abyPCCmd, (byte) 0);

        w_abyPCCmd[2] = 0x04;

        w_bRet = SendPackage(w_abyPCCmd, w_abyData);

        if (!w_bRet) {
            return false;
        }

        w_bRet = RecvPackage(w_abyData, w_nRecvLen);

        if (!w_bRet) {
            return false;
        }

        w_strTmp = new String(w_abyData);
        deviceInfo[0] = w_strTmp;

        return true;
    }

    private boolean SendPackage(byte[] pPCCmd, byte[] pData) {
        int nDataLen;

        pPCCmd[0] = (byte) 0xEF;
        pPCCmd[1] = 0x01;

        nDataLen = (int) ((((pPCCmd[5] & 0xFF) << 8) & 0x0000FF00) | (pPCCmd[4] & 0x000000FF));

        return m_usbBase.UsbSCSIWrite(pPCCmd, 6, pData, nDataLen, 5000);
    }

    private boolean RecvPackage(byte[] pData, int[] pLevRen) {
        int w_nLen;
        byte[] w_abyPCCmd = new byte[6];
        byte[] w_abyRespond = new byte[4];
        boolean w_bRet;

        w_abyPCCmd[0] = (byte) 0xEF;
        w_abyPCCmd[1] = 0x02;
        w_abyPCCmd[2] = 0;
        w_abyPCCmd[3] = 0;
        w_abyPCCmd[4] = 0;
        w_abyPCCmd[5] = 0;

        // receive status
        w_bRet = m_usbBase.UsbSCSIRead(w_abyPCCmd, 6, w_abyRespond, 4, 5000);

        if (!w_bRet)
            return false;

        w_nLen = (int) ((int) ((w_abyRespond[3] << 8) & 0x0000FF00) | (int) (w_abyRespond[2] & 0x000000FF));

        if (w_nLen > 0) {
            w_abyPCCmd[1] = 0x03;
            w_bRet = m_usbBase.UsbSCSIRead(w_abyPCCmd, 6, pData, w_nLen, 5000);

            if (!w_bRet)
                return false;

            pLevRen[0] = w_nLen;
        }

        return true;
    }

    private short GetRetCode() {
        return (short) ((int) ((m_abyPacket[7] << 8) & 0x0000FF00) | (int) (m_abyPacket[6] & 0x000000FF));
    }

    private short GetDataLen() {
        return (short) (((m_abyPacket[5] << 8) & 0x0000FF00) | (m_abyPacket[4] & 0x000000FF));
    }

    void SetDataLen(short p_wDataLen) {
        m_abyPacket[4] = (byte) (p_wDataLen & 0xFF);
        m_abyPacket[5] = (byte) (((p_wDataLen & 0xFF00) >> 8) & 0xFF);
    }

    void SetDataLen2(short p_wDataLen) {
        m_abyPacket2[4] = (byte) (p_wDataLen & 0xFF);
        m_abyPacket2[5] = (byte) (((p_wDataLen & 0xFF00) >> 8) & 0xFF);
    }

    void SetCmdData(short p_wData, boolean p_bFirst) {
        if (p_bFirst) {
            m_abyPacket[6] = (byte) (p_wData & 0xFF);
            m_abyPacket[7] = (byte) (((p_wData & 0xFF00) >> 8) & 0xFF);
        } else {
            m_abyPacket[8] = (byte) (p_wData & 0xFF);
            m_abyPacket[9] = (byte) (((p_wData & 0xFF00) >> 8) & 0xFF);
        }
    }

    public short GetCmdData(boolean p_bFirst) {
        if (p_bFirst) {
            return (short) (((m_abyPacket[7] << 8) & 0x0000FF00) | (m_abyPacket[6] & 0x000000FF));
        } else {
            return (short) (((m_abyPacket[9] << 8) & 0x0000FF00) | (m_abyPacket[8] & 0x000000FF));
        }
    }

    private short GetDataPacketLen() {
        return (short) (((m_abyPacket[5] << 8) & 0x0000FF00) | (m_abyPacket[4] & 0x000000FF) + 6);
    }

    void InitPacket(short p_wCmd, boolean p_bCmdData) {
        memset(m_abyPacket, (byte) 0, CMD_PACKET_LEN);

        //g_pPacketBuffer->wPrefix = p_bCmdData?CMD_PREFIX_CODE:CMD_DATA_PREFIX_CODE;
        if (p_bCmdData) {
            m_abyPacket[0] = (byte) (CMD_PREFIX_CODE & 0xFF);
            m_abyPacket[1] = (byte) (((CMD_PREFIX_CODE & 0xFF00) >> 8) & 0xFF);
        } else {
            m_abyPacket[0] = (byte) (CMD_DATA_PREFIX_CODE & 0xFF);
            m_abyPacket[1] = (byte) (((CMD_DATA_PREFIX_CODE & 0xFF00) >> 8) & 0xFF);
        }

        //g_pPacketBuffer->wCMD_RCM = p_wCMD;
        m_abyPacket[2] = (byte) (p_wCmd & 0xFF);
        m_abyPacket[3] = (byte) (((p_wCmd & 0xFF00) >> 8) & 0xFF);
    }

    void InitPacket2(short p_wCmd, boolean p_bCmdData) {
        memset(m_abyPacket2, (byte) 0, CMD_PACKET_LEN);

        //g_pPacketBuffer->wPrefix = p_bCmdData?CMD_PREFIX_CODE:CMD_DATA_PREFIX_CODE;
        if (p_bCmdData) {
            m_abyPacket2[0] = (byte) (CMD_PREFIX_CODE & 0xFF);
            m_abyPacket2[1] = (byte) (((CMD_PREFIX_CODE & 0xFF00) >> 8) & 0xFF);
        } else {
            m_abyPacket2[0] = (byte) (CMD_DATA_PREFIX_CODE & 0xFF);
            m_abyPacket2[1] = (byte) (((CMD_DATA_PREFIX_CODE & 0xFF00) >> 8) & 0xFF);
        }

        //g_pPacketBuffer->wCMD_RCM = p_wCMD;
        m_abyPacket2[2] = (byte) (p_wCmd & 0xFF);
        m_abyPacket2[3] = (byte) (((p_wCmd & 0xFF00) >> 8) & 0xFF);
    }

    private short GetCheckSum(boolean p_bCmdData) {
        short w_wRet = 0;
        short w_nI = 0;

        w_wRet = 0;
        if (p_bCmdData) {
            for (w_nI = 0; w_nI < CMD_PACKET_LEN; w_nI++)
                w_wRet += (m_abyPacket[w_nI] & 0xFF);
        } else {
            for (w_nI = 0; w_nI < GetDataPacketLen(); w_nI++)
                w_wRet += (m_abyPacket[w_nI] & 0xFF);
        }
        return w_wRet;
    }

    void AddCheckSum(boolean p_bCmdData) {
        short w_wRet = 0;
        short w_wLen = 0;
        int w_nI;

        if (p_bCmdData)
            w_wLen = CMD_PACKET_LEN;
        else
            w_wLen = GetDataPacketLen();

        w_wRet = 0;
        for (w_nI = 0; w_nI < w_wLen; w_nI++)
            w_wRet += (m_abyPacket[w_nI] & 0xFF);

        m_abyPacket[w_wLen] = (byte) (w_wRet & 0xFF);
        m_abyPacket[w_wLen + 1] = (byte) (((w_wRet & 0xFF00) >> 8) & 0xFF);

    }

    void AddCheckSum2(boolean p_bCmdData) {
        short w_wRet = 0;
        short w_wLen = 0;
        int w_nI;

        if (p_bCmdData)
            w_wLen = CMD_PACKET_LEN;
        else
            w_wLen = GetDataPacketLen();

        w_wRet = 0;
        for (w_nI = 0; w_nI < w_wLen; w_nI++)
            w_wRet += (m_abyPacket2[w_nI] & 0xFF);

        m_abyPacket2[w_wLen] = (byte) (w_wRet & 0xFF);
        m_abyPacket2[w_wLen + 1] = (byte) (((w_wRet & 0xFF00) >> 8) & 0xFF);

    }

    private boolean CheckReceive(short p_wPrefix, short p_wCmd) {
        short w_wCheckSum;
        short w_wTmpPrefix;
        short w_wTmpCmd;
        short w_wLen;

        // Check Prefix Code
        w_wTmpPrefix = (short) (((m_abyPacket[1] << 8) & 0x0000FF00) | (m_abyPacket[0] & 0x000000FF));
        w_wTmpCmd = (short) (((m_abyPacket[3] << 8) & 0x0000FF00) | (m_abyPacket[2] & 0x000000FF));

//    	if ( g_pPacketBuffer->wCMD_RCM != CMD_FP_CANCEL_CODE )
        {
            if ((p_wPrefix != w_wTmpPrefix) || (p_wCmd != w_wTmpCmd)) {
                return false;
            }
        }

        if (p_wPrefix == RCM_PREFIX_CODE)
            w_wLen = CMD_PACKET_LEN;
        else
            w_wLen = GetDataPacketLen();

        w_wCheckSum = (short) (((m_abyPacket[w_wLen + 1] << 8) & 0x0000FF00) | (m_abyPacket[w_wLen] & 0x000000FF));

        return w_wCheckSum == GetCheckSum(p_wPrefix == RCM_PREFIX_CODE);
    }

    //--------------------------- Send, Receive Communication Packet Functions ---------------------//
    private boolean Send_Command(short p_wCmd) {
        if ((m_nConnected == 1) || (m_nConnected == 3))
            return UART_SendCommand(p_wCmd);
        else if (m_nConnected == 2)
            return USB_SendPacket(p_wCmd);
        else
            return false;
    }

    public boolean Send_DataPacket(short p_wCmd) {
        if ((m_nConnected == 1) || (m_nConnected == 3))
            return UART_SendDataPacket(p_wCmd);
        else if (m_nConnected == 2)
            return USB_SendDataPacket(p_wCmd);
        else
            return false;
    }

    //------------------------------------------ USB Functions -------------------------------------//
    boolean USB_SendPacket(short wCMD) {
        byte[] btCDB = new byte[8];
        boolean w_bRet;

        Arrays.fill(btCDB, (byte) 0);

        btCDB[0] = (byte) 0xEF;
        btCDB[1] = 0x11;
        btCDB[4] = CMD_PACKET_LEN + 2;

        while (m_bSendPacketWork) {
            SystemClock.sleep(1);
        }
        m_bSendPacketWork = true;
        w_bRet = m_usbBase.UsbSCSIWrite(btCDB, 8, m_abyPacket, (int) (CMD_PACKET_LEN + 2), SCSI_TIMEOUT);
        m_bSendPacketWork = false;

        if (!w_bRet)
            return false;

        return USB_ReceiveAck(wCMD);
    }

    boolean USB_SendPacket2(short wCMD) {
        byte[] btCDB = new byte[8];
        boolean w_bRet;

        Arrays.fill(btCDB, (byte) 0);

        btCDB[0] = (byte) 0xEF;
        btCDB[1] = 0x11;
        btCDB[4] = CMD_PACKET_LEN + 2;

        while (m_bSendPacketWork) {
            SystemClock.sleep(1);
        }
        m_bSendPacketWork = true;
        w_bRet = m_usbBase.UsbSCSIWrite(btCDB, 8, m_abyPacket2, (int) (CMD_PACKET_LEN + 2), SCSI_TIMEOUT);
        m_bSendPacketWork = false;

        return w_bRet;
    }

    boolean USB_ReceiveAck(short p_wCmd) {
        int w_nLen;
        byte[] btCDB = new byte[8];
        byte[] w_abyWaitPacket = new byte[CMD_PACKET_LEN + 2];
        int w_dwTimeOut = SCSI_TIMEOUT;

        if (p_wCmd == CMD_VERIFY_CODE ||
                p_wCmd == CMD_IDENTIFY_CODE ||
                p_wCmd == CMD_IDENTIFY_FREE_CODE ||
                p_wCmd == CMD_ENROLL_CODE ||
                p_wCmd == CMD_ENROLL_ONETIME_CODE)
            w_dwTimeOut = (GD_MAX_FP_TIME_OUT + 1) * (1000);

        Arrays.fill(btCDB, (byte) 0);

        Arrays.fill(w_abyWaitPacket, (byte) 0xAF);

        do {
            Arrays.fill(m_abyPacket, (byte) 0);

            btCDB[0] = (byte) 0xEF;
            btCDB[1] = (byte) 0x12;

            w_nLen = CMD_PACKET_LEN + 2;

            if (!m_usbBase.UsbSCSIRead(btCDB, 8, m_abyPacket, w_nLen, w_dwTimeOut))
                return false;

            SystemClock.sleep(COMM_SLEEP_TIME);
        } while (memcmp(m_abyPacket, w_abyWaitPacket, CMD_PACKET_LEN + 2));

        m_nPacketSize = w_nLen;

        return CheckReceive((short) RCM_PREFIX_CODE, p_wCmd);
    }

    boolean USB_ReceiveAck2(short p_wCmd) {
        int w_nLen;
        byte[] btCDB = new byte[8];
        byte[] w_abyWaitPacket = new byte[CMD_PACKET_LEN + 2];
        int w_dwTimeOut = SCSI_TIMEOUT;

        Arrays.fill(btCDB, (byte) 0);
        Arrays.fill(w_abyWaitPacket, (byte) 0xAF);

        do {
            Arrays.fill(m_abyPacket2, (byte) 0);

            btCDB[0] = (byte) 0xEF;
            btCDB[1] = (byte) 0x12;

            w_nLen = CMD_PACKET_LEN + 2;

            if (!m_usbBase.UsbSCSIRead(btCDB, 8, m_abyPacket2, w_nLen, w_dwTimeOut))
                return false;

            SystemClock.sleep(COMM_SLEEP_TIME);
        } while (memcmp(m_abyPacket2, w_abyWaitPacket, CMD_PACKET_LEN + 2));

        m_nPacketSize = w_nLen;

        return true;
    }

    private boolean USB_ReceiveDataAck(short p_wCmd) {
        byte[] btCDB = new byte[8];
        byte[] w_WaitPacket = new byte[8];
        int w_nLen;
        int w_dwTimeOut = COMM_TIMEOUT;

        if (p_wCmd == CMD_VERIFY_CODE ||
                p_wCmd == CMD_IDENTIFY_CODE ||
                p_wCmd == CMD_IDENTIFY_FREE_CODE ||
                p_wCmd == CMD_ENROLL_CODE ||
                p_wCmd == CMD_ENROLL_ONETIME_CODE)
            w_dwTimeOut = (GD_MAX_FP_TIME_OUT + 1) * (1000);

        memset(btCDB, (byte) 0, 8);
        memset(w_WaitPacket, (byte) 0xAF, 8);
        Arrays.fill(m_abyPacketTmp, (byte) 0);

        do {
            btCDB[0] = (byte) 0xEF;
            btCDB[1] = 0x15;
            w_nLen = 6;

            if (!m_usbBase.UsbSCSIRead(btCDB, 8, m_abyPacket, w_nLen, w_dwTimeOut)) {
                return false;
            }

            SystemClock.sleep(COMM_SLEEP_TIME);
        } while (memcmp(m_abyPacket, w_WaitPacket, 6));

        do {
            w_nLen = GetDataLen() + 2;
            if (!USB_ReceiveRawData(m_abyPacketTmp, w_nLen)) {
                return false;
            }
            System.arraycopy(m_abyPacketTmp, 0, m_abyPacket, 6, w_nLen);
            SystemClock.sleep(COMM_SLEEP_TIME);
        } while (memcmp(m_abyPacket, w_WaitPacket, 4));

        return CheckReceive((short) RCM_DATA_PREFIX_CODE, p_wCmd);
    }

    boolean USB_SendDataPacket(short wCMD) {
        byte[] btCDB = new byte[8];
        short w_wLen = (short) (GetDataPacketLen() + 2);

        memset(btCDB, (byte) 0, 8);

        btCDB[0] = (byte) 0xEF;
        btCDB[1] = 0x13;

        btCDB[4] = (byte) (w_wLen & 0xFF);
        btCDB[5] = (byte) (((w_wLen & 0xFF00) >> 8) & 0xFF);

        if (!m_usbBase.UsbSCSIWrite(btCDB, 8, m_abyPacket, GetDataPacketLen() + 2, SCSI_TIMEOUT))
            return false;

        return USB_ReceiveDataAck(wCMD);
    }

    boolean USB_ReceiveDataPacket(short wCMD) {
        return USB_ReceiveDataAck(wCMD);
    }

    private boolean USB_ReceiveRawData(byte[] pBuffer, int nDataLen) {
        int w_nDataCnt = nDataLen;
        byte[] btCDB = new byte[8];

        memset(btCDB, (byte) 0, 8);
        btCDB[0] = (byte) 0xEF;
        btCDB[1] = (byte) 0x14;
        return m_usbBase.UsbSCSIRead(btCDB, 8, pBuffer, w_nDataCnt, SCSI_TIMEOUT);
    }

    public boolean USB_ReceiveImage(byte[] p_pBuffer, int p_dwDataLen) {
        byte[] btCDB = new byte[8];
        byte[] w_WaitPacket = new byte[8];
        int w_nI;
        int w_nIndex;
        int w_nRemainCount;
        byte[] w_pTmpImgBuf = new byte[ONCE_UP_IMAGE_UINT];

        memset(btCDB, (byte) 0, 8);
        memset(w_WaitPacket, (byte) 0xAF, 8);

        if (p_dwDataLen == 208 * 288 || p_dwDataLen == 242 * 266 || p_dwDataLen == 202 * 258 || p_dwDataLen == 256 * 288) {
            w_nIndex = 0;
            w_nRemainCount = p_dwDataLen;
            w_nI = 0;
            while (w_nRemainCount > ONCE_UP_IMAGE_UINT) {
                btCDB[0] = (byte) 0xEF;
                btCDB[1] = 0x16;
                btCDB[2] = (byte) (w_nI & 0xFF);
                if (!m_usbBase.UsbSCSIRead(btCDB, 8, w_pTmpImgBuf, ONCE_UP_IMAGE_UINT, SCSI_TIMEOUT))
                    return false;
                System.arraycopy(w_pTmpImgBuf, 0, p_pBuffer, w_nIndex, ONCE_UP_IMAGE_UINT);
                w_nRemainCount -= ONCE_UP_IMAGE_UINT;
                w_nIndex += ONCE_UP_IMAGE_UINT;
                w_nI++;
            }
            btCDB[0] = (byte) 0xEF;
            btCDB[1] = 0x16;
            btCDB[2] = (byte) (w_nI & 0xFF);
            if (!m_usbBase.UsbSCSIRead(btCDB, 8, w_pTmpImgBuf, w_nRemainCount, SCSI_TIMEOUT))
                return false;
            System.arraycopy(w_pTmpImgBuf, 0, p_pBuffer, w_nIndex, w_nRemainCount);
        }

        return true;
    }

    public boolean USB_DownImage(byte[] pBuf, int nBufLen) {
        byte[] w_pImgBuf = new byte[ONCE_UP_IMAGE_UINT];
        int w_nI;
        int w_nIndex = 0;
        int w_nRemainCount;
        byte[] btCDB = new byte[8];

        w_nIndex = 0;
        w_nRemainCount = nBufLen;
        w_nI = 0;
        memset(btCDB, (byte) 0, 8);

        while (w_nRemainCount > ONCE_UP_IMAGE_UINT) {
            btCDB[0] = (byte) 0xEF;
            btCDB[1] = 0x17;
            btCDB[2] = 0;
            btCDB[3] = (byte) (w_nI & 0xFF);
            btCDB[4] = LOBYTE((short) (ONCE_UP_IMAGE_UINT & 0x00FF));
            btCDB[5] = HIBYTE((short) (ONCE_UP_IMAGE_UINT & 0xFF00));

            System.arraycopy(pBuf, w_nIndex, w_pImgBuf, 0, ONCE_UP_IMAGE_UINT);
            if (!m_usbBase.UsbSCSIWrite(btCDB, 6, w_pImgBuf, ONCE_UP_IMAGE_UINT, SCSI_TIMEOUT))
                return false;

            w_nRemainCount -= ONCE_UP_IMAGE_UINT;
            w_nIndex += ONCE_UP_IMAGE_UINT;
            w_nI++;
        }

        btCDB[0] = (byte) 0xEF;
        btCDB[1] = 0x17;
        btCDB[2] = 0;
        btCDB[3] = (byte) (w_nI & 0xFF);
        btCDB[4] = LOBYTE((short) (w_nRemainCount & 0x00FF));
        btCDB[5] = HIBYTE((short) (w_nRemainCount & 0xFF00));

        System.arraycopy(pBuf, w_nIndex, w_pImgBuf, 0, w_nRemainCount);
        return m_usbBase.UsbSCSIWrite(btCDB, 6, w_pImgBuf, w_nRemainCount, SCSI_TIMEOUT);
    }

    //------------------------------------------ UART Functions -------------------------------------//
    boolean UART_SendCommand(short p_wCmd) {
        int w_nResult = 0;

        if (m_nConnected == 1) {
        } else if (m_nConnected == 3) {
            byte[] w_pData = new byte[CMD_PACKET_LEN + 2];
            System.arraycopy(m_abyPacket, 0, w_pData, 0, CMD_PACKET_LEN + 2);
            m_SerialPort.send(w_pData);
        }

        return UART_ReceiveAck(p_wCmd, true);
    }

    boolean UART_SendCommand2(short wCMD) {
        int w_nResult = 0;

        if (m_nConnected == 1) {

        } else if (m_nConnected == 3) {
            byte[] w_pData = new byte[CMD_PACKET_LEN + 2];
            System.arraycopy(m_abyPacket2, 0, w_pData, 0, CMD_PACKET_LEN + 2);
            m_SerialPort.send(w_pData);
        }

        return true;
    }
    /***************************************************************************/
    /***************************************************************************/
    public boolean UART_ReceiveAck(short p_wCmd, boolean p_bCmdData) {
//    	int	w_nResult = 0;
        int w_nReadLen = 0;
        int w_nTotalLen = CMD_PACKET_LEN + 2;
        int w_nTmpLen;
        long w_nTime;
        int i;

        w_nTime = System.currentTimeMillis();

        while (w_nReadLen < w_nTotalLen) {
//	    	w_nResult = m_uartDriver.ReadData(m_abyPacket, CMD_PACKET_LEN + 2);
            if (System.currentTimeMillis() - w_nTime > 10000) {
                m_nUARTReadLen = 0;
                return false;
            }

            i = 0;
            while (m_bBufferHandle) {
                i++;
                if (i < 10000)
                    break;
            }

            m_bBufferHandle = true;
            if (m_nUARTReadLen <= 0)
                continue;
            if (w_nTotalLen - w_nReadLen < m_nUARTReadLen) {
                w_nTmpLen = w_nTotalLen - w_nReadLen;
                System.arraycopy(m_pUARTReadBuf, 0, m_abyPacket, w_nReadLen, w_nTmpLen);
                w_nReadLen += w_nTmpLen;
                m_nUARTReadLen = m_nUARTReadLen - w_nTmpLen;
                System.arraycopy(m_pUARTReadBuf, w_nTmpLen, m_abyPacketTmp, 0, m_nUARTReadLen);
                System.arraycopy(m_abyPacketTmp, 0, m_pUARTReadBuf, 0, m_nUARTReadLen);
            } else {
                System.arraycopy(m_pUARTReadBuf, 0, m_abyPacket, w_nReadLen, m_nUARTReadLen);
                w_nReadLen += m_nUARTReadLen;
                m_nUARTReadLen = 0;
            }
            m_bBufferHandle = false;
        }

        if (p_bCmdData)
            return CheckReceive((short) RCM_PREFIX_CODE, p_wCmd);
        else
            return CheckReceive((short) RCM_DATA_PREFIX_CODE, p_wCmd);
    }

    boolean UART_ReceiveAck2(short p_wCmd) {
        int w_nReadLen = 0;
        int w_nTotalLen = CMD_PACKET_LEN + 2;
        int w_nTmpLen;
        long w_nTime;

        w_nTime = System.currentTimeMillis();

        while (w_nReadLen < w_nTotalLen) {
            if (System.currentTimeMillis() - w_nTime > 10000) {
                m_nUARTReadLen = 0;
                return false;
            }

            if (m_nUARTReadLen <= 0)
                continue;
            if (w_nTotalLen - w_nReadLen < m_nUARTReadLen) {
                w_nTmpLen = w_nTotalLen - w_nReadLen;
                System.arraycopy(m_pUARTReadBuf, 0, m_abyPacket2, w_nReadLen, w_nTmpLen);
                w_nReadLen += w_nTmpLen;
                m_nUARTReadLen = m_nUARTReadLen - w_nTmpLen;
                System.arraycopy(m_pUARTReadBuf, w_nTmpLen, m_abyPacketTmp, 0, m_nUARTReadLen);
                System.arraycopy(m_abyPacketTmp, 0, m_pUARTReadBuf, 0, m_nUARTReadLen);
            } else {
                System.arraycopy(m_pUARTReadBuf, 0, m_abyPacket2, w_nReadLen, m_nUARTReadLen);
                w_nReadLen += m_nUARTReadLen;
                m_nUARTReadLen = 0;
            }
        }

        return true;
    }

    private boolean UART_ReceiveDataAck(short p_wCmd) {
        if (!UART_ReadDataN(m_abyPacket, 0, 6))
            return false;

        if (!UART_ReadDataN(m_abyPacket, 6, GetDataLen() + 2))
            return false;

        return CheckReceive((short) RCM_DATA_PREFIX_CODE, p_wCmd);
    }

    boolean UART_SendDataPacket(short p_wCmd) {
        int w_nSendCnt = 0;

        if (m_nConnected == 1) {

        } else if (m_nConnected == 3) {
            int w_nLen = GetDataLen() + 8;
            byte[] w_pData = new byte[w_nLen];
            System.arraycopy(m_abyPacket, 0, w_pData, 0, w_nLen);
            m_SerialPort.send(w_pData);
        }

        return UART_ReceiveDataAck(p_wCmd);
    }

    boolean UART_ReceiveDataPacket(short p_wCmd) {
        return UART_ReceiveDataAck(p_wCmd);
    }

    boolean UART_ReadDataN(byte[] p_pData, int p_nStart, int p_nLen) {
//    	int		w_nAckCnt = 0;
        int w_nRecvLen, w_nTotalRecvLen;
        int w_nTmpLen;
        long w_nTime;

        w_nRecvLen = p_nLen;
        w_nTotalRecvLen = 0;
        w_nTime = System.currentTimeMillis();

        while (w_nTotalRecvLen < p_nLen) {
            if (System.currentTimeMillis() - w_nTime > 10000) {
                m_nUARTReadLen = 0;
                return false;
            }

            if (m_nUARTReadLen <= 0)
                continue;

            if (p_nLen - w_nTotalRecvLen < m_nUARTReadLen) {
                w_nTmpLen = p_nLen - w_nTotalRecvLen;
                System.arraycopy(m_pUARTReadBuf, 0, p_pData, p_nStart + w_nTotalRecvLen, w_nTmpLen);
                w_nRecvLen = w_nRecvLen - w_nTmpLen;
                w_nTotalRecvLen = w_nTotalRecvLen + w_nTmpLen;
                m_nUARTReadLen = m_nUARTReadLen - w_nTmpLen;
                System.arraycopy(m_pUARTReadBuf, w_nTmpLen, m_abyPacketTmp, 0, m_nUARTReadLen);
                System.arraycopy(m_abyPacketTmp, 0, m_pUARTReadBuf, 0, m_nUARTReadLen);
            } else {
                System.arraycopy(m_pUARTReadBuf, 0, p_pData, p_nStart + w_nTotalRecvLen, m_nUARTReadLen);
                w_nRecvLen = w_nRecvLen - m_nUARTReadLen;
                w_nTotalRecvLen = w_nTotalRecvLen + m_nUARTReadLen;
                m_nUARTReadLen = 0;
            }
        }

        return true;
    }

    private boolean memcmp(byte[] p1, byte[] p2, int nLen) {
        int i;

        for (i = 0; i < nLen; i++) {
            if (p1[i] != p2[i])
                return false;
        }

        return true;
    }

    void memset(byte[] p1, byte nValue, int nLen) {
        Arrays.fill(p1, 0, nLen, nValue);
    }

    short MAKEWORD(byte low, byte high) {
        short s;
        s = (short) ((((high & 0x00FF) << 8) & 0x0000FF00) | (low & 0x000000FF));
        return s;
    }

    byte LOBYTE(short s) {
        return (byte) (s & 0xFF);
    }

    byte HIBYTE(short s) {
        return (byte) (((s & 0xFF00) >> 8) & 0xFF);
    }

    //----------------------------------------------------���ڿ�����
    private class SerialControl extends SerialHelper {

        //		public SerialControl(String sPort, String sBaudRate){
//			super(sPort, sBaudRate);
//		}
        SerialControl() {
        }

        @Override
        protected void onDataReceived(final ComBean ComRecData) {
            //���ݽ�����������ʱ��������̣�����Ῠ��,���ܺ�6410����ʾ�����й�
            //ֱ��ˢ����ʾ��������������ʱ���������ԣ�����������ʾͬ����
            //���̶߳�ʱˢ����ʾ���Ի�ý���������ʾЧ�������ǽ��������ٶȿ�����ʾ�ٶ�ʱ����ʾ���ͺ�
            //����Ч�����-_-���̶߳�ʱˢ���Ժ�һЩ��
            DispQueue.AddQueue(ComRecData);//�̶߳�ʱˢ����ʾ(�Ƽ�)
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
    private class DispQueueThread extends Thread {
        private Queue<ComBean> QueueList = new LinkedList<ComBean>();

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int i;
                while (true) {
                    final ComBean ComData;
                    if ((ComData = QueueList.poll()) == null)
                        break;

                    i = 0;
                    while (m_bBufferHandle) {
                        i++;
                        if (i > 10000)
                            break;
                    }
                    m_bBufferHandle = true;
                    System.arraycopy(ComData.bRec, 0, m_pUARTReadBuf, m_nUARTReadLen, ComData.nSize);
                    m_nUARTReadLen = m_nUARTReadLen + ComData.nSize;
                    m_bBufferHandle = false;
//		        	break;
                }
            }
        }

        synchronized void AddQueue(ComBean ComData) {
            QueueList.add(ComData);
        }
    }
}
