package com.szadst.szoemhost_lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ImageView;

import com.szadst.szoemhost_lib.DevComm;
import com.szadst.szoemhost_lib.IUsbConnState;

public class SZOEMHost_Lib {
    /**
     * Called when the activity is first created.
     */
    private static DevComm m_devComm;
    private static short m_dwCode;
    private static boolean m_bThreadWork;
    private static boolean m_bCmdDone;
    private static boolean m_bSendResult;

    private boolean m_bParamGet;

    byte[] m_TemplateData = new byte[DevComm.GD_MAX_RECORD_SIZE];
    byte[] m_TemplateData2 = new byte[DevComm.GD_MAX_RECORD_SIZE];

    int m_nTemplateSize = 0;
    int m_nTemplateSize2 = 0;

    int m_nParam, m_nImgWidth, m_nImgHeight;
    long m_nPassedTime;
    byte[] m_binImage, m_bmpImage;
    int m_nImageBufOffset = 0;
    String m_strPost;
    boolean m_bCancel, m_bConCapture;

    TextView m_txtStatus;
    Runnable m_runEnableCtrl;

    public SZOEMHost_Lib(Activity parentActivity, TextView p_pStatusView, Runnable p_runEnableCtrl) {
        m_bThreadWork = false;
        if (m_devComm == null) {
            m_devComm = new DevComm(parentActivity, m_IConnectionHandler);
        }

        m_binImage = new byte[1024 * 100];
        m_bmpImage = new byte[1024 * 100];

        m_txtStatus = p_pStatusView;
        m_runEnableCtrl = p_runEnableCtrl;
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
                short w_wPrefix = 0;

                m_bThreadWork = true;

                w_wPrefix = (short) (((m_devComm.m_abyPacket[1] << 8) & 0x0000FF00) | (m_devComm.m_abyPacket[0] & 0x000000FF));
                if (w_wPrefix == (short) (DevComm.CMD_PREFIX_CODE)) {
                    if (m_dwCode != (short) (DevComm.CMD_FP_CANCEL_CODE)) {
                        if ((m_devComm.m_nConnected == 1) || (m_devComm.m_nConnected == 3))
                            w_blRet = m_devComm.UART_SendCommand(m_dwCode);
                        else if (m_devComm.m_nConnected == 2)
                            w_blRet = m_devComm.USB_SendPacket(m_dwCode);
                    } else {
                        if ((m_devComm.m_nConnected == 1) || (m_devComm.m_nConnected == 3))
                            w_blRet = m_devComm.UART_ReceiveAck(m_dwCode, true);
                        else if (m_devComm.m_nConnected == 2)
                            w_blRet = m_devComm.USB_ReceiveAck(m_dwCode);
                    }
                } else if (w_wPrefix == (short) (DevComm.CMD_DATA_PREFIX_CODE)) {
                    if ((m_devComm.m_nConnected == 1) || (m_devComm.m_nConnected == 3))
                        w_blRet = m_devComm.UART_SendDataPacket(m_dwCode);
                    else if (m_devComm.m_nConnected == 2)
                        w_blRet = m_devComm.USB_SendDataPacket(m_dwCode);
                } else {
                    if (m_dwCode != (short) (DevComm.CMD_FEATURE_OF_CAPTURED_FP_CODE)) {
                        if ((m_devComm.m_nConnected == 1) || (m_devComm.m_nConnected == 3)) {
                            w_blRet = m_devComm.UART_ReceiveAck(m_dwCode, true);
                        } else if (m_devComm.m_nConnected == 2) {
                            w_blRet = m_devComm.USB_ReceiveAck(m_dwCode);
                        }
                    } else {
                        if ((m_devComm.m_nConnected == 1) || (m_devComm.m_nConnected == 3)) {
                            w_blRet = m_devComm.UART_ReceiveDataPacket((short) DevComm.CMD_FEATURE_OF_CAPTURED_FP_CODE);
                        } else if (m_devComm.m_nConnected == 2) {
                            w_blRet = m_devComm.USB_ReceiveDataPacket((short) DevComm.CMD_FEATURE_OF_CAPTURED_FP_CODE);
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
        int w_nTemplateNo = 0;

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

    public int Run_CmdVerify(int p_nTmpNo) {
        int w_nTemplateNo = 0;

        //. Check inputed template no
        if (!CheckInputTemplateNo(p_nTmpNo))
            return 1;

        w_nTemplateNo = p_nTmpNo;
        m_strPost = "Input your finger";
        m_txtStatus.setText(m_strPost);

        Run_Command_1P((short) DevComm.CMD_VERIFY_CODE, (short) w_nTemplateNo);

        return 0;
    }

    public int Run_CmdDeleteID(int p_nTmpNo) {
        int w_nTemplateNo = 0;

        //. Check inputed template no
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
                if (w_bRet == true) {
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

    public boolean CheckInputTemplateNo(int p_nTmpNo) {
        if (p_nTmpNo > (DevComm.GD_MAX_RECORD_COUNT) || p_nTmpNo < 1) {
            m_txtStatus.setText("Please input correct user id(1~" + (short) DevComm.GD_MAX_RECORD_COUNT + ")");
            return false;
        }

        return true;
    }

    private void DisplayResponsePacket(short p_nCode) {
        short w_nRet;
        short w_nData, w_nData2, w_nSize/*, w_wPrefix*/;

        m_strPost = "";
        m_txtStatus.setText(m_strPost);

        w_nRet = m_devComm.MAKEWORD(m_devComm.m_abyPacket[6], m_devComm.m_abyPacket[7]);
        w_nData = m_devComm.MAKEWORD(m_devComm.m_abyPacket[8], m_devComm.m_abyPacket[9]);



        w_nData2 = m_devComm.MAKEWORD(m_devComm.m_abyPacket[10], m_devComm.m_abyPacket[11]);
        w_nSize = m_devComm.MAKEWORD(m_devComm.m_abyPacket[4], m_devComm.m_abyPacket[5]);

        switch (p_nCode) {
            case (short) DevComm.CMD_CLEAR_TEMPLATE_CODE:
				Log.e("p_nCode", "CMD_CLEAR_TEMPLATE_CODE");

				if (w_nRet == (short) DevComm.ERR_SUCCESS) {
					Log.e("userId", String.valueOf(w_nData));
				} else {
					Log.e("userId", "-1");
				}

                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Success\r\nTemplate No : %d", w_nData);
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;
            case (short) DevComm.CMD_UP_IMAGE_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Receive Image Success");
                    m_txtStatus.post(runDrawImage);
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;
            case (short) DevComm.CMD_READ_TEMPLATE_CODE:
				Log.e("p_nCode", "CMD_READ_TEMPLATE_CODE");

                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Success\r\nTemplate No : %d", w_nData);
                    WriteTemplateFile(w_nData, m_TemplateData);
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;
            case (short) DevComm.CMD_WRITE_TEMPLATE_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Success\r\nTemplate No : %d", w_nData);
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);

                    if (w_nData == DevComm.ERR_DUPLICATION_ID) {
                        m_strPost += String.format(" %d.", w_nData2);
                    }
                }
                break;

            case (short) DevComm.CMD_GET_EMPTY_ID_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Success\r\nEmpty ID : %d", w_nData);
//    				m_editUserID.setText(String.format("%d", w_nData));
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;
            case (short) DevComm.CMD_GET_ENROLL_COUNT_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Success\r\nEnroll Count : %d", w_nData);
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;

            case (short) DevComm.CMD_VERIFY_WITH_DOWN_TMPL_CODE:
            case (short) DevComm.CMD_IDENTIFY_WITH_DOWN_TMPL_CODE:
            case (short) DevComm.CMD_VERIFY_CODE:
            case (short) DevComm.CMD_IDENTIFY_CODE:
            case (short) DevComm.CMD_IDENTIFY_FREE_CODE:
            case (short) DevComm.CMD_ENROLL_CODE:
            case (short) DevComm.CMD_ENROLL_ONETIME_CODE:
            case (short) DevComm.CMD_CHANGE_TEMPLATE_CODE:
            case (short) DevComm.CMD_IDENTIFY_WITH_IMAGE_CODE:
            case (short) DevComm.CMD_VERIFY_WITH_IMAGE_CODE:
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
//    					if( p_nCode != (short)DevComm.CMD_IDENTIFY_FREE_CODE || m_devComm.LOBYTE(w_nData) == DevComm.ERR_FP_CANCEL )
//    						m_btnCloseDevice.setEnabled(true);
                            m_strPost = String.format("Result : Success\r\nTemplate No : %d", w_nData);
                            break;
                    }
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                    if (m_devComm.LOBYTE(w_nData) == DevComm.ERR_BAD_QUALITY) {
                        m_strPost += "\r\nAgain... !";
                    } else {
                        if (w_nData == DevComm.ERR_DUPLICATION_ID) {
                            m_strPost += String.format(" %d.", w_nData2);
                        }
                    }
//    				if( p_nCode != (short)DevComm.CMD_IDENTIFY_FREE_CODE || m_devComm.LOBYTE(w_nData) == DevComm.ERR_FP_CANCEL || 
//						m_devComm.LOBYTE(w_nData) == DevComm.ERR_ALL_TMPL_EMPTY || m_devComm.LOBYTE(w_nData) == DevComm.ERR_INVALID_OPERATION_MODE || 
//    					m_devComm.LOBYTE(w_nData) == DevComm.ERR_NOT_AUTHORIZED)
//    					m_btnCloseDevice.setEnabled(true);
                }
                break;

            case (short) DevComm.CMD_CLEAR_ALLTEMPLATE_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Success\r\nCleared Template Count : %d", w_nData);
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;

            case (short) DevComm.CMD_GET_BROKEN_TEMPLATE_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Success\r\nBroken Template Count : %d\r\nFirst Broken Template ID : %d", w_nData, w_nData2);
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;

            case (short) DevComm.CMD_VERIFY_DEVPASS_CODE:
            case (short) DevComm.CMD_SET_DEVPASS_CODE:
            case (short) DevComm.CMD_EXIT_DEVPASS_CODE:
//    		case (short)DevComm.CMD_SET_COMMNAD_VALID_FLAG_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Success.");
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;

            case (short) DevComm.CMD_SET_PARAMETER_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    if (m_bParamGet)
                        m_strPost = String.format("Result : Success\r\nParameter Value = %d",
                                (m_devComm.m_abyPacket[8] & 0x000000FF) + ((m_devComm.m_abyPacket[9] << 8) & 0x0000FF00) + ((m_devComm.m_abyPacket[10] << 16) & 0x00FF0000) + ((m_devComm.m_abyPacket[24] << 8) & 0xFF000000));
                    else
                        m_strPost = String.format("Result : Success\r\n");
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;

            case (short) DevComm.CMD_ADJUST_SENSOR_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Adjust Success");
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;

            case (short) DevComm.CMD_ENTERSTANDBY_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Enter Standby Mode Success");
                } else {
                    m_strPost = String.format("Result : Enter Standby Mode Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;

            case (short) DevComm.CMD_GET_FW_VERSION_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Success\r\nFirmware Version: %d.%d", m_devComm.LOBYTE(w_nData), m_devComm.HIBYTE(w_nData));
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;

            case (short) DevComm.CMD_FINGER_DETECT_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    if (w_nData == (short) DevComm.GD_DETECT_FINGER) {
                        m_strPost = String.format("Finger Detected.");
                    } else if (w_nData == (short) DevComm.GD_NO_DETECT_FINGER) {
                        m_strPost = String.format("Finger not Detected.");
                    }
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;

            case (short) DevComm.CMD_FP_CANCEL_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : FP Cancel Success.");
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;

            case (short) DevComm.CMD_FEATURE_OF_CAPTURED_FP_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    if (w_nSize != (short) DevComm.GD_RECORD_SIZE + 2) {
                        m_strPost = String.format("Result : Fail\r\nCommunication Error");
                    } else {
                        System.arraycopy(m_devComm.m_abyPacket, 8, m_TemplateData, 0, (short) DevComm.GD_RECORD_SIZE);
                        m_strPost = String.format("Result : Success");
                    }
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;

            case (short) DevComm.CMD_IDENTIFY_TEMPLATE_WITH_FP_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    if (m_devComm.LOBYTE(w_nData) == (short) DevComm.GD_DOWNLOAD_SUCCESS) {
                        m_strPost = String.format("Result : Download Success\r\nInput your finger");
                        m_txtStatus.setText(m_strPost);
                        return;
                    } else {
                        m_strPost = String.format("Result : Identify OK.");
                        m_txtStatus.setText(m_strPost);
                    }
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
                    m_strPost += GetErrorMsg(w_nData);
                }
                break;

            case (short) DevComm.RCM_INCORRECT_COMMAND_CODE:
                m_strPost = String.format("Received incorrect command !");
                break;

            case (short) DevComm.CMD_ENTER_ISPMODE_CODE:
                if (w_nRet == (short) DevComm.ERR_SUCCESS) {
                    m_strPost = String.format("Result : Success\r\nRunning ISP. Can you programming.");
                } else {
                    m_strPost = String.format("Result : Fail\r\n");
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
            switch (w_nData) {
                case (short) DevComm.GD_NEED_RELEASE_FINGER:
                    m_txtStatus.setText(m_strPost);
                    m_devComm.memset(m_devComm.m_abyPacket, (byte) 0, 64 * 1024);
                    StartSendThread();
                    return;
                default:
                    break;
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

    public boolean WriteTemplateFile(int p_nUserID, byte[] pTemplate) {
        // Save Template to (mnt/sdcard/sz_template)
        // Create Directory
        String w_szSaveDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sz_template";
        File w_fpDir = new File(w_szSaveDirPath);
        if (!w_fpDir.exists())
            w_fpDir.mkdirs();

        // Create Template File
        File w_fpTemplate = new File(w_szSaveDirPath + "/" + String.valueOf(p_nUserID) + ".fpt");
        if (!w_fpTemplate.exists()) {
            try {
                w_fpTemplate.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        // Save Template Data
        FileOutputStream w_foTemplate = null;
        try {
            w_foTemplate = new FileOutputStream(w_fpTemplate);
            w_foTemplate.write(pTemplate, 0, m_nTemplateSize);
            w_foTemplate.close();

            // Show Save Path
            m_strPost += "\nSaved file path = " + w_szSaveDirPath + "/" + String.valueOf(p_nUserID) + ".fpt";
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    Runnable procRspPacket = new Runnable() {
        public void run() {
            short w_wCmd;

            if (m_bSendResult == false) {
                m_strPost = "Fail to receive response! \n Please check the connection to target.";
                m_txtStatus.setText(m_strPost);

                m_txtStatus.post(m_runEnableCtrl);

                m_bCmdDone = true;

                return;
            }
            //. Display response packet
            w_wCmd = (short) (((m_devComm.m_abyPacket[3] << 8) & 0x0000FF00) | (m_devComm.m_abyPacket[2] & 0x000000FF));
            DisplayResponsePacket(w_wCmd);
        }
    };

    Runnable runShowStatus = new Runnable() {
        public void run() {
            m_txtStatus.setText(m_strPost);
        }
    };

    Runnable runDrawImage = new Runnable() {
        public void run() {
            int nSize;

            MakeBMPBuf(m_binImage, m_bmpImage, m_nImgWidth, m_nImgHeight);

            if ((m_nImgWidth % 4) != 0)
                nSize = m_nImgWidth + (4 - (m_nImgWidth % 4));
            else
                nSize = m_nImgWidth;

            nSize = 1078 + nSize * m_nImgHeight;

//            DebugManage.WriteBmp(m_bmpImage, nSize);

            Bitmap image = BitmapFactory.decodeByteArray(m_bmpImage, 0, nSize);
        }
    };

    private void MakeBMPBuf(byte[] Input, byte[] Output, int iImageX, int iImageY) {

        byte[] w_bTemp = new byte[4];
        byte[] head = new byte[1078];
        byte[] head2 = {
                /***************************/
                //file header
                0x42, 0x4d,//file type
                //0x36,0x6c,0x01,0x00, //file size***
                0x0, 0x0, 0x0, 0x00, //file size***
                0x00, 0x00, //reserved
                0x00, 0x00,//reserved
                0x36, 0x4, 0x00, 0x00,//head byte***
                /***************************/
                //infoheader
                0x28, 0x00, 0x00, 0x00,//struct size

                //0x00,0x01,0x00,0x00,//map width***
                0x00, 0x00, 0x0, 0x00,//map width***
                //0x68,0x01,0x00,0x00,//map height***
                0x00, 0x00, 0x00, 0x00,//map height***

                0x01, 0x00,//must be 1
                0x08, 0x00,//color count***
                0x00, 0x00, 0x00, 0x00, //compression
                //0x00,0x68,0x01,0x00,//data size***
                0x00, 0x00, 0x00, 0x00,//data size***
                0x00, 0x00, 0x00, 0x00, //dpix
                0x00, 0x00, 0x00, 0x00, //dpiy
                0x00, 0x00, 0x00, 0x00,//color used
                0x00, 0x00, 0x00, 0x00,//color important
        };

        int i, j, num, iImageStep;

        Arrays.fill(w_bTemp, (byte) 0);

        System.arraycopy(head2, 0, head, 0, head2.length);

        if ((iImageX % 4) != 0)
            iImageStep = iImageX + (4 - (iImageX % 4));
        else
            iImageStep = iImageX;

        num = iImageX;
        head[18] = (byte) (num & (byte) 0xFF);
        num = num >> 8;
        head[19] = (byte) (num & (byte) 0xFF);
        num = num >> 8;
        head[20] = (byte) (num & (byte) 0xFF);
        num = num >> 8;
        head[21] = (byte) (num & (byte) 0xFF);

        num = iImageY;
        head[22] = (byte) (num & (byte) 0xFF);
        num = num >> 8;
        head[23] = (byte) (num & (byte) 0xFF);
        num = num >> 8;
        head[24] = (byte) (num & (byte) 0xFF);
        num = num >> 8;
        head[25] = (byte) (num & (byte) 0xFF);

        j = 0;
        for (i = 54; i < 1078; i = i + 4) {
            head[i] = head[i + 1] = head[i + 2] = (byte) j;
            head[i + 3] = 0;
            j++;
        }

        System.arraycopy(head, 0, Output, 0, 1078);

        if (iImageStep == iImageX) {
            for (i = 0; i < iImageY; i++) {
                System.arraycopy(Input, i * iImageX, Output, 1078 + i * iImageX, iImageX);
            }
        } else {
            iImageStep = iImageStep - iImageX;

            for (i = 0; i < iImageY; i++) {
                System.arraycopy(Input, i * iImageX, Output, 1078 + i * (iImageX + iImageStep), iImageX);
                System.arraycopy(w_bTemp, 0, Output, 1078 + i * (iImageX + iImageStep) + iImageX, iImageStep);
            }
        }
    }

    private final IUsbConnState m_IConnectionHandler = new IUsbConnState() {
        @Override
        public void onUsbConnected() {
            if (m_devComm.Run_TestConnection() == (short) DevComm.ERR_SUCCESS) {
                if (m_devComm.Run_GetDeviceInfo() == (short) DevComm.ERR_SUCCESS) {
//        			EnableCtrl(true);
//		            m_btnOpenDevice.setEnabled(false);
//		            m_btnCloseDevice.setEnabled(true);
//		            m_txtStatus.setText("Open Device Success!");
                }
            } else {
//        		m_txtStatus.setText("Can not connect to device!");
            }
        }

        @Override
        public void onUsbPermissionDenied() {
            m_txtStatus.setText("Permission denied!");
        }

        @Override
        public void onDeviceNotFound() {
            m_txtStatus.setText("Can not find usb device!");
        }
    };

}
