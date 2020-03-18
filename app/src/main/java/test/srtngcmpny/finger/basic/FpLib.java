package test.srtngcmpny.finger.basic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

public class FpLib {
    private static DevComm m_devComm;
    private static short m_dwCode;
    private static boolean m_bThreadWork;
    private static boolean m_bCmdDone;
    private static boolean m_bSendResult;

    private byte[] m_binImage, m_bmpImage;
    private String m_strPost;

    private TextView m_txtStatus;
    private Runnable m_runEnableCtrl;

    private FpDataReceived listener = null;

    private void setListener(FpDataReceived listener) {
        this.listener = listener;
    }

    public FpLib(Activity parentActivity, TextView p_pStatusView, Runnable p_runEnableCtrl) {
        m_bThreadWork = false;
        if (m_devComm == null) {
            m_devComm = new DevComm(parentActivity, m_IConnectionHandler);
        }

        m_binImage = new byte[1024 * 100];
        m_bmpImage = new byte[1024 * 100];

        m_txtStatus = p_pStatusView;
        m_runEnableCtrl = p_runEnableCtrl;

        setListener((FpDataReceived) parentActivity);
    }

    public int SZOEMHost_Lib_Init(Activity parentActivity, TextView p_pStatusView, Runnable p_runEnableCtrl) {
        m_bThreadWork = false;

        if (m_devComm == null) {
            m_devComm = new DevComm(parentActivity, m_IConnectionHandler);
        } else {
            m_devComm.DevComm_Init(parentActivity, m_IConnectionHandler);
        }

        if (m_binImage == null)
            m_binImage = new byte[1024 * 100];
        if (m_bmpImage == null)
            m_bmpImage = new byte[1024 * 100];

        m_txtStatus = p_pStatusView;
        m_runEnableCtrl = p_runEnableCtrl;

        return 0;
    }

    @SuppressLint("SetTextI18n")
    public int OpenDevice(String p_szDevice, int p_nBaudrate) {
        if (m_devComm != null) {
            if (!m_devComm.IsInit()) {
                if (!m_devComm.OpenComm(p_szDevice, p_nBaudrate)) {
                    m_txtStatus.setText("Failed init device!");
                    return 1;
                }
            }
            if (m_devComm.Run_TestConnection() == (short) DevComm.ERR_SUCCESS) {
                if (m_devComm.Run_GetDeviceInfo() == (short) DevComm.ERR_SUCCESS) {
                    m_txtStatus.setText("Open Device Success");
                    return 0;
                } else {
                    m_txtStatus.setText("Can not connect to device!");
                    return 1;
                }
            } else {
                m_txtStatus.setText("Can not connect to device!");
                m_devComm.CloseComm();
                return 1;
            }
        }

        return 1;
    }

    public int CloseDevice() {
        m_devComm.CloseComm();
        return 0;
    }

    private void StartSendThread() {
        m_bCmdDone = false;

        while (m_bThreadWork) {
            SystemClock.sleep(1);
        }

        new Thread(new Runnable() {
            public void run() {
                boolean w_blRet = false;
                short w_wPrefix;

                m_bThreadWork = true;

                w_wPrefix = (short) (((m_devComm.m_abyPacket[1] << 8) & 0x0000FF00) | (m_devComm.m_abyPacket[0] & 0x000000FF));
                if (w_wPrefix == (short) (DevComm.CMD_PREFIX_CODE)) {
                    if (m_dwCode != (short) (DevComm.CMD_FP_CANCEL_CODE)) {
                        if (m_devComm.m_nConnected == 2) {
                            w_blRet = m_devComm.USB_SendPacket(m_dwCode);
                        }
                    }
                } else if (w_wPrefix == (short) (DevComm.CMD_DATA_PREFIX_CODE)) {
                    if (m_devComm.m_nConnected == 2) {
                        w_blRet = m_devComm.USB_SendDataPacket(m_dwCode);
                    }
                } else {
                    if (m_dwCode != (short) (DevComm.CMD_FEATURE_OF_CAPTURED_FP_CODE)) {
                        if (m_devComm.m_nConnected == 2) {
                            w_blRet = m_devComm.USB_ReceiveAck(m_dwCode);
                        }
                    }
                }
                m_bSendResult = w_blRet;
                m_txtStatus.post(procRspPacket);

                m_bThreadWork = false;
            }
        }).start();
    }

    private void Run_Command_NP(short p_wCmd) {
        //. Assemble command packet
        m_devComm.InitPacket(p_wCmd, true);
        m_devComm.AddCheckSum(true);

        m_dwCode = p_wCmd;
        StartSendThread();
    }

    private void Run_Command_1P(short p_wCmd, short p_wData) {
        //. Assemble command packet
        m_devComm.InitPacket(p_wCmd, true);
        m_devComm.SetDataLen((short) 0x0002);
        m_devComm.SetCmdData(p_wData, true);
        m_devComm.AddCheckSum(true);

        m_dwCode = p_wCmd;
        StartSendThread();
    }

    public int Run_CmdEnroll(int p_nTmpNo) {
        int w_nTemplateNo;

        //. Check inputed template no
        if (!CheckInputTemplateNo(p_nTmpNo)) {
            return 1;
        }
        w_nTemplateNo = p_nTmpNo;

        Run_Command_1P((short) DevComm.CMD_ENROLL_CODE, (short) w_nTemplateNo);

        return 0;
    }

    public int Run_CmdIdentify() {
        m_strPost = "Input your finger";
        m_txtStatus.setText(m_strPost);

        Run_Command_NP((short) DevComm.CMD_IDENTIFY_CODE);

        return 0;
    }

    public int Run_CmdIdentifyFree() {
        m_strPost = "Input your finger";
        m_txtStatus.setText(m_strPost);

        Run_Command_NP((short) DevComm.CMD_IDENTIFY_FREE_CODE);

        return 0;
    }

    public int Run_CmdDeleteID(int p_nTmpNo) {
        int w_nTemplateNo;

        if (!CheckInputTemplateNo(p_nTmpNo))
            return 1;

        w_nTemplateNo = p_nTmpNo;
        Run_Command_1P((short) DevComm.CMD_CLEAR_TEMPLATE_CODE, (short) w_nTemplateNo);

        return 0;
    }

    public int Run_CmdDeleteAll() {
        Run_Command_NP((short) DevComm.CMD_CLEAR_ALLTEMPLATE_CODE);

        return 0;
    }

    public int Run_CmdGetEmptyID() {
        Run_Command_NP((short) DevComm.CMD_GET_EMPTY_ID_CODE);

        return 0;
    }

    public int Run_CmdGetUserCount() {
        Run_Command_NP((short) DevComm.CMD_GET_ENROLL_COUNT_CODE);

        return 0;
    }

    public int Run_CmdCancel() {

        new Thread(new Runnable() {
            //    		@Override
            public void run() {
                boolean w_bRet;

                //. Init Packet
                m_devComm.InitPacket2((short) DevComm.CMD_FP_CANCEL_CODE, true);
                m_devComm.SetDataLen2((short) 0x00);
                m_devComm.AddCheckSum2(true);

                //. Send Packet
                w_bRet = false;
                if ((m_devComm.m_nConnected == 1) || (m_devComm.m_nConnected == 3)) {
                    w_bRet = m_devComm.UART_SendCommand2((short) DevComm.CMD_FP_CANCEL_CODE);
                } else if (m_devComm.m_nConnected == 2) {
                    w_bRet = m_devComm.USB_SendPacket2((short) DevComm.CMD_FP_CANCEL_CODE);
                }
                if (!w_bRet) {
                    m_strPost = "Result : Cancel Send Failed\r\n";
                    m_txtStatus.post(runShowStatus);
                    m_txtStatus.post(m_runEnableCtrl);
                    return;
                }

                //. Wait while processing cmd exit
                while (!m_bCmdDone) {
                    SystemClock.sleep(1);
                }

                if ((m_devComm.m_nConnected == 1) || (m_devComm.m_nConnected == 3)) {
                    w_bRet = m_devComm.UART_ReceiveAck2((short) DevComm.CMD_FP_CANCEL_CODE);
                } else if (m_devComm.m_nConnected == 2) {
                    w_bRet = m_devComm.USB_ReceiveAck2((short) DevComm.CMD_FP_CANCEL_CODE);
                }
                if (w_bRet) {
                    m_strPost = "Result : FP Cancel Success.";
                } else {
                    m_strPost = "Result : Cancel Failed\r\n";
                }

                m_txtStatus.post(runShowStatus);
                m_txtStatus.post(m_runEnableCtrl);
            }
        }).start();

        return 0;
    }

    @SuppressLint("SetTextI18n")
    private boolean CheckInputTemplateNo(int p_nTmpNo) {
        if (p_nTmpNo > (DevComm.GD_MAX_RECORD_COUNT) || p_nTmpNo < 1) {
            m_txtStatus.setText("Please input correct user id(1~" + (short) DevComm.GD_MAX_RECORD_COUNT + ")");
            return false;
        }

        return true;
    }

    @SuppressLint("DefaultLocale")
    private void DisplayResponsePacket(short p_nCode) {
        short w_nRet;
        short w_nData, w_nData2, w_nSize/*, w_wPrefix*/;

        m_strPost = "";
        m_txtStatus.setText(m_strPost);

        w_nRet = m_devComm.MAKEWORD(m_devComm.m_abyPacket[6], m_devComm.m_abyPacket[7]);
        w_nData = m_devComm.MAKEWORD(m_devComm.m_abyPacket[8], m_devComm.m_abyPacket[9]);

        w_nData2 = m_devComm.MAKEWORD(m_devComm.m_abyPacket[10], m_devComm.m_abyPacket[11]);

        switch (p_nCode) {
            case (short) DevComm.CMD_CLEAR_TEMPLATE_CODE:

				if (w_nRet == (short) DevComm.ERR_SUCCESS) {
					Log.e("userId", String.valueOf(w_nData));
				} else {
					Log.e("userId", "-1");
				}

                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Success\r\nTemplate No : %d", w_nData);
                } else {
                    m_strPost = "Result : Fail\r\n";
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;
            case (short) DevComm.CMD_GET_EMPTY_ID_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Success\r\nEmpty ID : %d", w_nData);
                } else {
                    m_strPost = "Result : Fail\r\n";
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;
            case (short) DevComm.CMD_GET_ENROLL_COUNT_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Success\r\nEnroll Count : %d", w_nData);
                } else {
                    m_strPost = "Result : Fail\r\n";
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;
            case (short) DevComm.CMD_IDENTIFY_CODE:
            case (short) DevComm.CMD_ENROLL_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    switch (w_nData) {
                        case (short) DevComm.GD_NEED_RELEASE_FINGER:
                            m_strPost = "Release your finger";
                            break;
                        case (short) DevComm.GD_NEED_FIRST_SWEEP:
                            m_strPost = "Input your finger";
                            break;
                        case (short) DevComm.GD_NEED_SECOND_SWEEP:
                            m_strPost = "Two More";
                            break;
                        case (short) DevComm.GD_NEED_THIRD_SWEEP:
                            m_strPost = "One More";
                            break;
                        default:
                            m_strPost = String.format("Result : Success\r\nTemplate No : %d", w_nData);
                            break;
                    }
                } else {
                    m_strPost = "Result : Fail\r\n";
                    m_strPost += GetErrorMsg(w_nData);
                    if (m_devComm.LOBYTE(w_nData) == DevComm.ERR_BAD_QUALITY) {
                        m_strPost += "\r\nAgain... !";
                    } else {
                        if (w_nData == DevComm.ERR_DUPLICATION_ID) {
                            m_strPost += String.format(" %d.", w_nData2);
                        }
                    }
                }
                break;

            case (short) DevComm.CMD_CLEAR_ALLTEMPLATE_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Success\r\nCleared Template Count : %d", w_nData);
                } else {
                    m_strPost = "Result : Fail\r\n";
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;
            case (short) DevComm.CMD_FP_CANCEL_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = "Result : FP Cancel Success.";
                } else {
                    m_strPost = "Result : Fail\r\n";
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;
            default:
                break;
        }

        if ((p_nCode == (short) DevComm.CMD_IDENTIFY_FREE_CODE)) {
            if (w_nRet == (short) DevComm.ERR_SUCCESS ||
                    m_devComm.LOBYTE(w_nData) != DevComm.ERR_NOT_AUTHORIZED &&
                            m_devComm.LOBYTE(w_nData) != DevComm.ERR_FP_CANCEL &&
                            m_devComm.LOBYTE(w_nData) != DevComm.ERR_INVALID_OPERATION_MODE &&
                            m_devComm.LOBYTE(w_nData) != DevComm.ERR_ALL_TMPL_EMPTY) {
                m_txtStatus.setText(m_strPost);
                m_devComm.memset(m_devComm.m_abyPacket, (byte) 0, 64 * 1024);
                StartSendThread();
                return;
            }
        }
        if ((p_nCode == (short) DevComm.CMD_ENROLL_CODE) ||
                (p_nCode == (short) DevComm.CMD_CHANGE_TEMPLATE_CODE)) {
            switch (w_nData) {
                case (short) DevComm.GD_NEED_RELEASE_FINGER:
                case (short) DevComm.GD_NEED_FIRST_SWEEP:
                case (short) DevComm.GD_NEED_SECOND_SWEEP:
                case (short) DevComm.GD_NEED_THIRD_SWEEP:
                case (short) DevComm.ERR_BAD_QUALITY:
                    m_txtStatus.setText(m_strPost);
                    m_devComm.memset(m_devComm.m_abyPacket, (byte) 0, 64 * 1024);
                    StartSendThread();
                    return;
                default:
                    break;
            }
        }
        if ((p_nCode == (short) DevComm.CMD_ENROLL_ONETIME_CODE) || (p_nCode == (short) DevComm.CMD_VERIFY_CODE) ||
                (p_nCode == (short) DevComm.CMD_IDENTIFY_CODE) || (p_nCode == (short) DevComm.CMD_IDENTIFY_FREE_CODE)) {
            if (w_nData == (short) DevComm.GD_NEED_RELEASE_FINGER) {
                m_txtStatus.setText(m_strPost);
                m_devComm.memset(m_devComm.m_abyPacket, (byte) 0, 64 * 1024);
                StartSendThread();
                return;
            }
        }

        m_txtStatus.post(m_runEnableCtrl);

        m_txtStatus.setText(m_strPost);

        m_devComm.memset(m_devComm.m_abyPacket, (byte) 0, 64 * 1024);
        m_bCmdDone = true;
    }

    private String GetErrorMsg(short p_wErrorCode) {
        String w_ErrMsg;
        switch (p_wErrorCode & 0xFF) {
            case DevComm.ERR_VERIFY:
                w_ErrMsg = "Verify NG";
                break;
            case DevComm.ERR_IDENTIFY:
                w_ErrMsg = "Identify NG";
                break;
            case DevComm.ERR_EMPTY_ID_NOEXIST:
                w_ErrMsg = "Empty Template no Exist";
                break;
            case DevComm.ERR_BROKEN_ID_NOEXIST:
                w_ErrMsg = "Broken Template no Exist";
                break;
            case DevComm.ERR_TMPL_NOT_EMPTY:
                w_ErrMsg = "Template of this ID Already Exist";
                break;
            case DevComm.ERR_TMPL_EMPTY:
                w_ErrMsg = "This Template is Already Empty";
                break;
            case DevComm.ERR_INVALID_TMPL_NO:
                w_ErrMsg = "Invalid Template No";
                break;
            case DevComm.ERR_ALL_TMPL_EMPTY:
                w_ErrMsg = "All Templates are Empty";
                break;
            case DevComm.ERR_INVALID_TMPL_DATA:
                w_ErrMsg = "Invalid Template Data";
                break;
            case DevComm.ERR_DUPLICATION_ID:
//    		w_ErrMsg.Format("Duplicated ID : %d.", HIBYTE(p_wErrorCode));
                w_ErrMsg = "Duplicated ID : ";
                break;
            case DevComm.ERR_BAD_QUALITY:
                w_ErrMsg = "Bad Quality Image";
                break;
            case DevComm.ERR_SMALL_LINES:
                w_ErrMsg = "Small line Image";
                break;
            case DevComm.ERR_TOO_FAST:
                w_ErrMsg = "Too fast swiping";
                break;
            case DevComm.ERR_TIME_OUT:
                w_ErrMsg = "Time Out";
                break;
            case DevComm.ERR_GENERALIZE:
                w_ErrMsg = "Fail to Generalize";
                break;
            case DevComm.ERR_NOT_AUTHORIZED:
                w_ErrMsg = "Device not authorized.";
                break;
            case DevComm.ERR_EXCEPTION:
                w_ErrMsg = "Exception Error ";
                break;
            case DevComm.ERR_MEMORY:
                w_ErrMsg = "Memory Error ";
                break;
            case DevComm.ERR_INVALID_PARAM:
                w_ErrMsg = "Invalid Parameter";
                break;
            case DevComm.ERR_NO_RELEASE:
                w_ErrMsg = "No Release Finger Fail";
                break;
            case DevComm.ERR_INTERNAL:
                w_ErrMsg = "Internal Error.";
                break;
            case DevComm.ERR_FP_CANCEL:
                w_ErrMsg = "Canceled.";
                break;
            case DevComm.ERR_INVALID_OPERATION_MODE:
                w_ErrMsg = "Invalid Operation Mode";
                break;
            case DevComm.ERR_NOT_SET_PWD:
                w_ErrMsg = "Password was not set.";
                break;
            case DevComm.ERR_FP_NOT_DETECTED:
                w_ErrMsg = "Finger is not detected.";
                break;
            case DevComm.ERR_ADJUST_SENSOR:
                w_ErrMsg = "Failed to adjust sensor.";
                break;
            default:
                w_ErrMsg = "Fail";
                break;

        }
        return w_ErrMsg;
    }

    private Runnable procRspPacket = new Runnable() {
        public void run() {
            short w_wCmd;
            if (!m_bSendResult) {
                m_strPost = "Fail to receive response! \n Please check the connection to target.";
                m_txtStatus.setText(m_strPost);

                m_txtStatus.post(m_runEnableCtrl);

                m_bCmdDone = true;

                return;
            }
            //. Display response packet
            w_wCmd = (short) (((m_devComm.m_abyPacket[3] << 8) & 0x0000FF00) | (m_devComm.m_abyPacket[2] & 0x000000FF));

            if (w_wCmd == 258) {
                short w_nRet = m_devComm.MAKEWORD(m_devComm.m_abyPacket[6], m_devComm.m_abyPacket[7]);
                short w_nData = m_devComm.MAKEWORD(m_devComm.m_abyPacket[8], m_devComm.m_abyPacket[9]);

                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    if (listener != null)
                        listener.onReceived(String.valueOf(w_nData));
                } else {
                    Log.e("userId", "-1");
                }
            }

            DisplayResponsePacket(w_wCmd);
        }
    };

    private Runnable runShowStatus = new Runnable() {
        public void run() {
            m_txtStatus.setText(m_strPost);
        }
    };

    private final IUsbConnState m_IConnectionHandler = new IUsbConnState() {
        @Override
        public void onUsbConnected() {
//            if (m_devComm.Run_TestConnection() == (short) DevComm.ERR_SUCCESS) {
//                if (m_devComm.Run_GetDeviceInfo() == (short) DevComm.ERR_SUCCESS) {
//		            m_txtStatus.setText("Open Device Success!");
//                }
//            } else {
//        		m_txtStatus.setText("Can not connect to device!");
//            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onUsbPermissionDenied() {
            m_txtStatus.setText("Permission denied!");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onDeviceNotFound() {
            m_txtStatus.setText("Can not find usb device!");
        }
    };

}
