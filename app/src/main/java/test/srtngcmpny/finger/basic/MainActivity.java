package test.srtngcmpny.finger.basic;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.szadst.szoemhost_lib.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static SZOEMHost_Lib    m_szHost;
    int        m_nUserID;
    String      m_strPost;
    int			m_nBaudrate;
    String		m_szDevice;

    // Controls
    Button      m_btnOpenDevice;
    Button      m_btnCloseDevice;
    Button      m_btnEnroll;
    Button      m_btnVerify;
    Button      m_btnIdentify;
    Button      m_btnIdentifyFree;
    Button      m_btnCaptureImage;
    Button      m_btnCancel;
    Button      m_btnGetUserCount;
    Button      m_btnGetEmptyID;
    Button      m_btnDeleteID;
    Button      m_btnDeleteAll;
    Button      m_btnReadTemplate;
    Button      m_btnWriteTemplate;
    Button		m_btnGetFWVer;
    Button		m_btnSetDevPass;
    Button		m_btnVerifyPass;
    Button      m_btnVerifyImage;
    Button      m_btnIdentifyImage;
    EditText    m_editUserID;
    EditText	m_editDevPassword;
    TextView    m_txtStatus;
    ImageView   m_FpImageViewer;
    Spinner     m_spBaudrate;
    Spinner		m_spDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set Keep Screen On
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        InitWidget();

        SetInitialState();

        m_spBaudrate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?>  parent, View view, int position, long id) {
                if (position == 0)
                    m_nBaudrate = 9600;
                else if (position == 1)
                    m_nBaudrate = 19200;
                else if (position == 2)
                    m_nBaudrate = 38400;
                else if (position == 3)
                    m_nBaudrate = 57600;
                else// if (position == 4)
                    m_nBaudrate = 115200;
            }
            public void onNothingSelected(AdapterView<?>  parent) {
            }
        });

        m_spDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?>  parent, View view, int position, long id) {
                m_szDevice = m_spDevice.getItemAtPosition(position).toString();
            }
            public void onNothingSelected(AdapterView<?>  parent) {
            }
        });
    }

    public void onClick(View view)
    {
        if(view == m_btnOpenDevice)
            OnOpenDeviceBtn();
        else if(view == m_btnCloseDevice)
            OnCloseDeviceBtn();
        else if(view == m_btnEnroll)
            OnEnrollBtn();
        else if(view == m_btnVerify)
            OnVerifyBtn();
        else if(view == m_btnIdentify)
            OnIdentifyBtn();
        else if(view == m_btnIdentifyFree)
            OnIdentifyFreeBtn();
        else if(view == m_btnCaptureImage)
            OnUpImage();
        else if(view == m_btnCancel)
            OnCancelBtn();
        else if(view == m_btnGetUserCount)
            OnGetUserCount();
        else if(view == m_btnGetEmptyID)
            OnGetEmptyID();
        else if(view == m_btnDeleteID)
            OnDeleteIDBtn();
        else if(view == m_btnDeleteAll)
            OnDeleteAllBtn();
        else if(view == m_btnReadTemplate)
            OnReadTemplateBtn();
        else if(view == m_btnWriteTemplate)
            OnWriteTemplateBtn();
        else if(view == m_btnGetFWVer)
            OnGetFwVersion();
        else if(view == m_btnSetDevPass)
            OnSetDevPass();
        else if(view == m_btnVerifyPass)
            OnVerifyPassBtn();
        else if(view == m_btnVerifyImage)
            OnVerifyWithImage();
        else if(view == m_btnIdentifyImage)
            OnIdentifyWithImage();
    }

    public void InitWidget()
    {
        m_FpImageViewer = (ImageView)findViewById(R.id.ivImageViewer);
        m_btnOpenDevice = (Button)findViewById(R.id.btnOpenDevice);
        m_btnCloseDevice = (Button)findViewById(R.id.btnCloseDevice);
        m_btnEnroll = (Button)findViewById(R.id.btnEnroll);
        m_btnVerify = (Button)findViewById(R.id.btnVerify);
        m_btnIdentify = (Button)findViewById(R.id.btnIdentify);
        m_btnIdentifyFree = (Button)findViewById(R.id.btnIdentifyFree);
        m_btnCaptureImage = (Button)findViewById(R.id.btnCaptureImage);
        m_btnCancel = (Button)findViewById(R.id.btnCancel);
        m_btnGetUserCount = (Button)findViewById(R.id.btnGetEnrollCount);
        m_btnGetEmptyID = (Button)findViewById(R.id.btnGetEmptyID);
        m_btnDeleteID = (Button)findViewById(R.id.btnRemoveTemplate);
        m_btnDeleteAll = (Button)findViewById(R.id.btnRemoveAll);
        m_btnReadTemplate = (Button)findViewById(R.id.btnReadTemplate);
        m_btnWriteTemplate = (Button)findViewById(R.id.btnWriteTemplate);
        m_btnGetFWVer = (Button)findViewById(R.id.btnGetFWVer);
        m_btnSetDevPass = (Button)findViewById(R.id.btnSetDevPass);
        m_btnVerifyPass = (Button)findViewById(R.id.btnVerifyPass);
        m_btnVerifyImage = (Button)findViewById(R.id.btnVerifyImage);
        m_btnIdentifyImage = (Button)findViewById(R.id.btnIdentifyImage);
        m_txtStatus = (TextView)findViewById(R.id.txtStatus);
        m_editUserID = (EditText)findViewById(R.id.editUserID);
        m_editDevPassword = (EditText)findViewById(R.id.editDevPassword);
        m_spBaudrate = (Spinner)findViewById(R.id.spnBaudrate);
        m_spDevice = (Spinner)findViewById(R.id.spnDevice);

        m_btnOpenDevice.setOnClickListener(this);
        m_btnCloseDevice.setOnClickListener(this);
        m_btnEnroll.setOnClickListener(this);
        m_btnVerify.setOnClickListener(this);
        m_btnIdentify.setOnClickListener(this);
        m_btnIdentifyFree.setOnClickListener(this);
        m_btnCaptureImage.setOnClickListener(this);
        m_btnCancel.setOnClickListener(this);
        m_btnGetUserCount.setOnClickListener(this);
        m_btnGetEmptyID.setOnClickListener(this);
        m_btnDeleteID.setOnClickListener(this);
        m_btnDeleteAll.setOnClickListener(this);
        m_btnReadTemplate.setOnClickListener(this);
        m_btnWriteTemplate.setOnClickListener(this);
        m_btnGetFWVer.setOnClickListener(this);
        m_btnSetDevPass.setOnClickListener(this);
        m_btnVerifyPass.setOnClickListener(this);
        m_btnVerifyImage.setOnClickListener(this);
        m_btnIdentifyImage.setOnClickListener(this);

        if(m_szHost == null){
            m_szHost = new SZOEMHost_Lib(this, m_txtStatus, m_FpImageViewer, runEnableCtrl, m_spDevice);
        }
        else
        {
            m_szHost.SZOEMHost_Lib_Init(this, m_txtStatus, m_FpImageViewer, runEnableCtrl, m_spDevice);
        }
    }

    public void EnableCtrl(boolean bEnable)
    {
        m_btnEnroll.setEnabled(bEnable);
        m_btnVerify.setEnabled(bEnable);
        m_btnIdentify.setEnabled(bEnable);
        m_btnIdentifyFree.setEnabled(bEnable);
//        m_btnCancel.setEnabled(bEnable);
        m_btnGetUserCount.setEnabled(bEnable);
        m_btnGetEmptyID.setEnabled(bEnable);
        m_btnDeleteID.setEnabled(bEnable);
        m_btnDeleteAll.setEnabled(bEnable);
        m_btnReadTemplate.setEnabled(bEnable);
        m_btnWriteTemplate.setEnabled(bEnable);
        m_btnCaptureImage.setEnabled(bEnable);
        m_btnGetFWVer.setEnabled(bEnable);
        m_btnSetDevPass.setEnabled(bEnable);
        m_btnVerifyPass.setEnabled(bEnable);
        m_btnVerifyImage.setEnabled(bEnable);
        m_btnIdentifyImage.setEnabled(bEnable);

        m_editUserID.setEnabled(bEnable);
        m_editDevPassword.setEnabled(bEnable);
    }

    public void SetInitialState()
    {
        m_txtStatus.setText("Please open device!");
        m_btnOpenDevice.setEnabled(true);
        m_btnCloseDevice.setEnabled(false);
        EnableCtrl(false);
        m_btnCancel.setEnabled(false);
    }

    public void OnOpenDeviceBtn()
    {
        if (m_szHost.OpenDevice(m_szDevice, m_nBaudrate) == 0)
        {
            EnableCtrl(true);
            m_btnOpenDevice.setEnabled(false);
            m_btnCloseDevice.setEnabled(true);
        }
    }

    public void OnCloseDeviceBtn()
    {
        m_szHost.CloseDevice();

        SetInitialState();
    }

    public void OnEnrollBtn()
    {
        int w_nTemplateNo;

        w_nTemplateNo = GetInputTemplateNo();
        if (w_nTemplateNo < 0)
            return;

        EnableCtrl(false);
        m_btnCloseDevice.setEnabled(false);
        m_btnCancel.setEnabled(true);

        m_szHost.Run_CmdEnroll(w_nTemplateNo);
    }

    public void OnIdentifyBtn(){
        EnableCtrl(false);
        m_btnCloseDevice.setEnabled(false);
        m_btnCancel.setEnabled(true);

        m_szHost.Run_CmdIdentify();
    }

    public void OnIdentifyFreeBtn(){
        EnableCtrl(false);
        m_btnCloseDevice.setEnabled(false);
        m_btnCancel.setEnabled(true);

        m_szHost.Run_CmdIdentifyFree();
    }

    public void OnVerifyBtn(){
        int w_nTemplateNo;

        w_nTemplateNo = GetInputTemplateNo();
        if (w_nTemplateNo < 0)
            return;

        EnableCtrl(false);
        m_btnCloseDevice.setEnabled(false);
        m_btnCancel.setEnabled(true);

        m_szHost.Run_CmdVerify(w_nTemplateNo);
    }

//    public void OnEnrollOneTime(){
//        int w_nTemplateNo;
//
//        w_nTemplateNo = GetInputTemplateNo();
//        if (w_nTemplateNo < 0)
//            return;
//
//        EnableCtrl(false);
//        m_btnCloseDevice.setEnabled(false);
//        m_btnCancel.setEnabled(true);
//
//        m_szHost.Run_CmdEnrollOneTime(w_nTemplateNo);
//    }

//    public void OnChangeTemplate(){
//        int w_nTemplateNo;
//
//        w_nTemplateNo = GetInputTemplateNo();
//        if (w_nTemplateNo < 0)
//            return;
//
//        EnableCtrl(false);
//        m_btnCloseDevice.setEnabled(false);
//        m_btnCancel.setEnabled(true);
//
//        m_szHost.Run_CmdChangeTemplate(w_nTemplateNo);
//    }

    public void OnDeleteIDBtn(){
        int w_nTemplateNo;

        w_nTemplateNo = GetInputTemplateNo();
        if (w_nTemplateNo < 0)
            return;

        m_szHost.Run_CmdDeleteID(w_nTemplateNo);
    }

    public void OnDeleteAllBtn(){
        m_szHost.Run_CmdDeleteAll();
    }

    public void OnReadTemplateBtn()
    {
        int w_nTemplateNo;

        w_nTemplateNo = GetInputTemplateNo();
        if (w_nTemplateNo < 0)
            return;

        if (m_szHost.Run_CmdReadTemplate(w_nTemplateNo) == 2)
        {
            OnCloseDeviceBtn();
        }
    }

    public void OnWriteTemplateBtn()
    {
        int w_nTemplateNo;

        w_nTemplateNo = GetInputTemplateNo();
        if (w_nTemplateNo < 0)
            return;

        if (m_szHost.Run_CmdWriteTemplate(w_nTemplateNo) == 2)
        {
            OnCloseDeviceBtn();
        }
    }

    public void OnGetEmptyID(){
        m_szHost.Run_CmdGetEmptyID();
    }

    public void OnGetUserCount(){
        m_szHost.Run_CmdGetUserCount();
    }

//    public void OnGetBrokenTemplate(){
//        m_szHost.Run_CmdGetBrokenTemplate();
//    }

//    public void OnReadTemplate()
//    {
//        int w_nTemplateNo;
//
//        w_nTemplateNo = GetInputTemplateNo();
//        if (w_nTemplateNo < 0)
//            return;
//
//        m_szHost.Run_CmdReadTemplate(w_nTemplateNo);
//    }

//    public void OnWriteTemplate()
//    {
//        int w_nTemplateNo;
//
//        w_nTemplateNo = GetInputTemplateNo();
//        if (w_nTemplateNo < 0)
//            return;
//
//        m_szHost.Run_CmdWriteTemplate(w_nTemplateNo);
//    }

//    public void OnSetParameter()
//    {
//        m_szHost.Run_CmdSetParameter();
//    }

    public void OnGetFwVersion()
    {
        m_szHost.Run_CmdGetFwVersion();
    }

//    public void OnDetectFingerBtn(){
//        EnableCtrl(false);
//        m_btnCloseDevice.setEnabled(false);
//        m_btnCancel.setEnabled(true);
//
//        m_szHost.Run_CmdDetectFinger();
//    }

    public void OnSetDevPass()
    {
        if (m_editDevPassword.length() != 0 && m_editDevPassword.length() != 14)
        {
            m_strPost = "Invalid Device Password. \nPlease input valid device password(length=14)!";
            m_txtStatus.setText(m_strPost);
            EnableCtrl(true);
            return;
        }

        m_szHost.Run_CmdSetDevPass(m_editDevPassword.getText().toString());
    }

    public void OnVerifyPassBtn()
    {
        if (m_editDevPassword.length() != 14)
        {
            m_strPost = "Invalid Device Password. \nPlease input valid device password(length=14)!";
            m_txtStatus.setText(m_strPost);
            EnableCtrl(true);
            return;
        }

        m_szHost.Run_CmdVerifyPass(m_editDevPassword.getText().toString());
    }

//    public void OnExitDevPass()
//    {
//        m_szHost.Run_CmdExitDevPass();
//    }

//    public void OnAdjustSensor()
//    {
//        m_szHost.Run_CmdAdjustSensor();
//    }

//    public void OnEnterStandByMode()
//    {
//        m_szHost.Run_CmdEnterStandByMode();
//    }

    public void OnCancelBtn(){
        m_btnCloseDevice.setEnabled(false);

        m_szHost.Run_CmdCancel();
    }

//    public void OnGetFeatureOfCapturedFP()
//    {
//        m_szHost.Run_CmdGetFeatureOfCapturedFP();
//    }

//    public void OnIdentifyWithTemplate2()
//    {
//        m_szHost.Run_CmdIdentifyWithTemplate2();
//    }

    public void OnUpImage()
    {
        EnableCtrl(false);
        m_btnCloseDevice.setEnabled(false);
        m_btnCancel.setEnabled(true);

        m_szHost.Run_CmdUpImage();
    }

    public void OnIdentifyWithImage()
    {
        m_szHost.Run_CmdIdentifyWithImage();
    }

    public void OnVerifyWithImage()
    {
        int w_nTemplateNo;

        w_nTemplateNo = GetInputTemplateNo();
        if (w_nTemplateNo < 0)
            return;

        m_szHost.Run_CmdVerifyWithImage(w_nTemplateNo);
    }

//    public void OnVerifyWithDownTmpl()
//    {
//        int w_nTemplateNo;
//
//        w_nTemplateNo = GetInputTemplateNo();
//        if (w_nTemplateNo < 0)
//            return;
//
//        m_szHost.Run_CmdVerifyWithDownTmpl(w_nTemplateNo);
//    }

//    public void OnIdentifyWithDownTmpl()
//    {
//        m_szHost.Run_CmdIdentifyWithDownTmpl();
//    }

//    public void OnEnterISPMode()
//    {
//        m_szHost.Run_CmdEnterISPMode();
//    }

    public int GetInputTemplateNo(){
        String str;

        str = m_editUserID.getText().toString();

        if (str.isEmpty())
        {
            m_txtStatus.setText("Please input user id");
            return -1;
        }

        try {
            m_nUserID = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            m_txtStatus.setText(String.format("Please input correct user id(1~%d)", (short)DevComm.GD_MAX_RECORD_COUNT));
            return -1;
        }

        return m_nUserID;
    }

    Runnable runEnableCtrl = new Runnable() {
        public void run()
        {
            EnableCtrl(true);
            m_btnOpenDevice.setEnabled(false);
            m_btnCloseDevice.setEnabled(true);
            m_btnCancel.setEnabled(false);
        }
    };

    @Override
    public boolean onKeyDown(int KeyCode, KeyEvent event) {
        switch (KeyCode){
        case KeyEvent.KEYCODE_BACK:
            if (event.getRepeatCount() == 0) {
                if (m_btnCancel.isEnabled()) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please cancel your command first", Toast.LENGTH_SHORT);
                    toast.show();
                    return true;
                }
                if (m_btnCloseDevice.isEnabled())
                    OnCloseDeviceBtn();
            }
            break;
        }

        return super.onKeyDown(KeyCode, event);
    }
}
