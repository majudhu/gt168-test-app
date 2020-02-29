package test.srtngcmpny.finger.basic

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import com.szadst.szoemhost_lib.DevComm
import com.szadst.szoemhost_lib.SZOEMHost_Lib

class MainActivity : AppCompatActivity(), View.OnClickListener {
    var m_nUserID = 0
    var m_strPost: String? = null
    var m_nBaudrate = 0
    var m_szDevice: String? = null
    // Controls
    var m_btnOpenDevice: Button? = null
    var m_btnCloseDevice: Button? = null
    var m_btnEnroll: Button? = null
    var m_btnVerify: Button? = null
    var m_btnIdentify: Button? = null
    var m_btnIdentifyFree: Button? = null
    var m_btnCaptureImage: Button? = null
    var m_btnCancel: Button? = null
    var m_btnGetUserCount: Button? = null
    var m_btnGetEmptyID: Button? = null
    var m_btnDeleteID: Button? = null
    var m_btnDeleteAll: Button? = null
    var m_btnReadTemplate: Button? = null
    var m_btnWriteTemplate: Button? = null
    var m_btnGetFWVer: Button? = null
    var m_btnSetDevPass: Button? = null
    var m_btnVerifyPass: Button? = null
    var m_btnVerifyImage: Button? = null
    var m_btnIdentifyImage: Button? = null
    var m_editUserID: EditText? = null
    var m_editDevPassword: EditText? = null
    var m_txtStatus: TextView? = null
    var m_FpImageViewer: ImageView? = null
    var m_spBaudrate: Spinner? = null
    var m_spDevice: Spinner? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Set Keep Screen On
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        InitWidget()
        SetInitialState()
        m_spBaudrate!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                m_nBaudrate = if (position == 0) 9600 else if (position == 1) 19200 else if (position == 2) 38400 else if (position == 3) 57600 else  // if (position == 4)
                    115200
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        m_spDevice!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                m_szDevice = m_spDevice!!.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onClick(view: View) {
        if (view === m_btnOpenDevice) OnOpenDeviceBtn() else if (view === m_btnCloseDevice) OnCloseDeviceBtn() else if (view === m_btnEnroll) OnEnrollBtn() else if (view === m_btnVerify) OnVerifyBtn() else if (view === m_btnIdentify) OnIdentifyBtn() else if (view === m_btnIdentifyFree) OnIdentifyFreeBtn() else if (view === m_btnCaptureImage) OnUpImage() else if (view === m_btnCancel) OnCancelBtn() else if (view === m_btnGetUserCount) OnGetUserCount() else if (view === m_btnGetEmptyID) OnGetEmptyID() else if (view === m_btnDeleteID) OnDeleteIDBtn() else if (view === m_btnDeleteAll) OnDeleteAllBtn() else if (view === m_btnReadTemplate) OnReadTemplateBtn() else if (view === m_btnWriteTemplate) OnWriteTemplateBtn() else if (view === m_btnGetFWVer) OnGetFwVersion() else if (view === m_btnSetDevPass) OnSetDevPass() else if (view === m_btnVerifyPass) OnVerifyPassBtn() else if (view === m_btnVerifyImage) OnVerifyWithImage() else if (view === m_btnIdentifyImage) OnIdentifyWithImage()
    }

    fun InitWidget() {
        m_FpImageViewer = findViewById<View>(R.id.ivImageViewer) as ImageView
        m_btnOpenDevice = findViewById<View>(R.id.btnOpenDevice) as Button
        m_btnCloseDevice = findViewById<View>(R.id.btnCloseDevice) as Button
        m_btnEnroll = findViewById<View>(R.id.btnEnroll) as Button
        m_btnVerify = findViewById<View>(R.id.btnVerify) as Button
        m_btnIdentify = findViewById<View>(R.id.btnIdentify) as Button
        m_btnIdentifyFree = findViewById<View>(R.id.btnIdentifyFree) as Button
        m_btnCaptureImage = findViewById<View>(R.id.btnCaptureImage) as Button
        m_btnCancel = findViewById<View>(R.id.btnCancel) as Button
        m_btnGetUserCount = findViewById<View>(R.id.btnGetEnrollCount) as Button
        m_btnGetEmptyID = findViewById<View>(R.id.btnGetEmptyID) as Button
        m_btnDeleteID = findViewById<View>(R.id.btnRemoveTemplate) as Button
        m_btnDeleteAll = findViewById<View>(R.id.btnRemoveAll) as Button
        m_btnReadTemplate = findViewById<View>(R.id.btnReadTemplate) as Button
        m_btnWriteTemplate = findViewById<View>(R.id.btnWriteTemplate) as Button
        m_btnGetFWVer = findViewById<View>(R.id.btnGetFWVer) as Button
        m_btnSetDevPass = findViewById<View>(R.id.btnSetDevPass) as Button
        m_btnVerifyPass = findViewById<View>(R.id.btnVerifyPass) as Button
        m_btnVerifyImage = findViewById<View>(R.id.btnVerifyImage) as Button
        m_btnIdentifyImage = findViewById<View>(R.id.btnIdentifyImage) as Button
        m_txtStatus = findViewById<View>(R.id.txtStatus) as TextView
        m_editUserID = findViewById<View>(R.id.editUserID) as EditText
        m_editDevPassword = findViewById<View>(R.id.editDevPassword) as EditText
        m_spBaudrate = findViewById<View>(R.id.spnBaudrate) as Spinner
        m_spDevice = findViewById<View>(R.id.spnDevice) as Spinner
        m_btnOpenDevice!!.setOnClickListener(this)
        m_btnCloseDevice!!.setOnClickListener(this)
        m_btnEnroll!!.setOnClickListener(this)
        m_btnVerify!!.setOnClickListener(this)
        m_btnIdentify!!.setOnClickListener(this)
        m_btnIdentifyFree!!.setOnClickListener(this)
        m_btnCaptureImage!!.setOnClickListener(this)
        m_btnCancel!!.setOnClickListener(this)
        m_btnGetUserCount!!.setOnClickListener(this)
        m_btnGetEmptyID!!.setOnClickListener(this)
        m_btnDeleteID!!.setOnClickListener(this)
        m_btnDeleteAll!!.setOnClickListener(this)
        m_btnReadTemplate!!.setOnClickListener(this)
        m_btnWriteTemplate!!.setOnClickListener(this)
        m_btnGetFWVer!!.setOnClickListener(this)
        m_btnSetDevPass!!.setOnClickListener(this)
        m_btnVerifyPass!!.setOnClickListener(this)
        m_btnVerifyImage!!.setOnClickListener(this)
        m_btnIdentifyImage!!.setOnClickListener(this)
        if (m_szHost == null) {
            m_szHost = SZOEMHost_Lib(this, m_txtStatus, m_FpImageViewer, runEnableCtrl, m_spDevice)
        } else {
            m_szHost!!.SZOEMHost_Lib_Init(this, m_txtStatus, m_FpImageViewer, runEnableCtrl, m_spDevice)
        }
    }

    fun EnableCtrl(bEnable: Boolean) {
        m_btnEnroll!!.isEnabled = bEnable
        m_btnVerify!!.isEnabled = bEnable
        m_btnIdentify!!.isEnabled = bEnable
        m_btnIdentifyFree!!.isEnabled = bEnable
        //        m_btnCancel.setEnabled(bEnable);
        m_btnGetUserCount!!.isEnabled = bEnable
        m_btnGetEmptyID!!.isEnabled = bEnable
        m_btnDeleteID!!.isEnabled = bEnable
        m_btnDeleteAll!!.isEnabled = bEnable
        m_btnReadTemplate!!.isEnabled = bEnable
        m_btnWriteTemplate!!.isEnabled = bEnable
        m_btnCaptureImage!!.isEnabled = bEnable
        m_btnGetFWVer!!.isEnabled = bEnable
        m_btnSetDevPass!!.isEnabled = bEnable
        m_btnVerifyPass!!.isEnabled = bEnable
        m_btnVerifyImage!!.isEnabled = bEnable
        m_btnIdentifyImage!!.isEnabled = bEnable
        m_editUserID!!.isEnabled = bEnable
        m_editDevPassword!!.isEnabled = bEnable
    }

    fun SetInitialState() {
        m_txtStatus!!.text = "Please open device!"
        m_btnOpenDevice!!.isEnabled = true
        m_btnCloseDevice!!.isEnabled = false
        EnableCtrl(false)
        m_btnCancel!!.isEnabled = false
    }

    fun OnOpenDeviceBtn() {
        if (m_szHost!!.OpenDevice(m_szDevice, m_nBaudrate) == 0) {
            EnableCtrl(true)
            m_btnOpenDevice!!.isEnabled = false
            m_btnCloseDevice!!.isEnabled = true
        }
    }

    fun OnCloseDeviceBtn() {
        m_szHost!!.CloseDevice()
        SetInitialState()
    }

    fun OnEnrollBtn() {
        val w_nTemplateNo: Int
        w_nTemplateNo = GetInputTemplateNo()
        if (w_nTemplateNo < 0) return
        EnableCtrl(false)
        m_btnCloseDevice!!.isEnabled = false
        m_btnCancel!!.isEnabled = true
        m_szHost!!.Run_CmdEnroll(w_nTemplateNo)
    }

    fun OnIdentifyBtn() {
        EnableCtrl(false)
        m_btnCloseDevice!!.isEnabled = false
        m_btnCancel!!.isEnabled = true
        m_szHost!!.Run_CmdIdentify()
    }

    fun OnIdentifyFreeBtn() {
        EnableCtrl(false)
        m_btnCloseDevice!!.isEnabled = false
        m_btnCancel!!.isEnabled = true
        m_szHost!!.Run_CmdIdentifyFree()
    }

    fun OnVerifyBtn() {
        val w_nTemplateNo: Int
        w_nTemplateNo = GetInputTemplateNo()
        if (w_nTemplateNo < 0) return
        EnableCtrl(false)
        m_btnCloseDevice!!.isEnabled = false
        m_btnCancel!!.isEnabled = true
        m_szHost!!.Run_CmdVerify(w_nTemplateNo)
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
    fun OnDeleteIDBtn() {
        val w_nTemplateNo: Int
        w_nTemplateNo = GetInputTemplateNo()
        if (w_nTemplateNo < 0) return
        m_szHost!!.Run_CmdDeleteID(w_nTemplateNo)
    }

    fun OnDeleteAllBtn() {
        m_szHost!!.Run_CmdDeleteAll()
    }

    fun OnReadTemplateBtn() {
        val w_nTemplateNo: Int
        w_nTemplateNo = GetInputTemplateNo()
        if (w_nTemplateNo < 0) return
        if (m_szHost!!.Run_CmdReadTemplate(w_nTemplateNo) == 2) {
            OnCloseDeviceBtn()
        }
    }

    fun OnWriteTemplateBtn() {
        val w_nTemplateNo: Int
        w_nTemplateNo = GetInputTemplateNo()
        if (w_nTemplateNo < 0) return
        if (m_szHost!!.Run_CmdWriteTemplate(w_nTemplateNo) == 2) {
            OnCloseDeviceBtn()
        }
    }

    fun OnGetEmptyID() {
        m_szHost!!.Run_CmdGetEmptyID()
    }

    fun OnGetUserCount() {
        m_szHost!!.Run_CmdGetUserCount()
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
    fun OnGetFwVersion() {
        m_szHost!!.Run_CmdGetFwVersion()
    }

    //    public void OnDetectFingerBtn(){
//        EnableCtrl(false);
//        m_btnCloseDevice.setEnabled(false);
//        m_btnCancel.setEnabled(true);
//
//        m_szHost.Run_CmdDetectFinger();
//    }
    fun OnSetDevPass() {
        if (m_editDevPassword!!.length() != 0 && m_editDevPassword!!.length() != 14) {
            m_strPost = "Invalid Device Password. \nPlease input valid device password(length=14)!"
            m_txtStatus!!.text = m_strPost
            EnableCtrl(true)
            return
        }
        m_szHost!!.Run_CmdSetDevPass(m_editDevPassword!!.text.toString())
    }

    fun OnVerifyPassBtn() {
        if (m_editDevPassword!!.length() != 14) {
            m_strPost = "Invalid Device Password. \nPlease input valid device password(length=14)!"
            m_txtStatus!!.text = m_strPost
            EnableCtrl(true)
            return
        }
        m_szHost!!.Run_CmdVerifyPass(m_editDevPassword!!.text.toString())
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
    fun OnCancelBtn() {
        m_btnCloseDevice!!.isEnabled = false
        m_szHost!!.Run_CmdCancel()
    }

    //    public void OnGetFeatureOfCapturedFP()
//    {
//        m_szHost.Run_CmdGetFeatureOfCapturedFP();
//    }
//    public void OnIdentifyWithTemplate2()
//    {
//        m_szHost.Run_CmdIdentifyWithTemplate2();
//    }
    fun OnUpImage() {
        EnableCtrl(false)
        m_btnCloseDevice!!.isEnabled = false
        m_btnCancel!!.isEnabled = true
        m_szHost!!.Run_CmdUpImage()
    }

    fun OnIdentifyWithImage() {
        m_szHost!!.Run_CmdIdentifyWithImage()
    }

    fun OnVerifyWithImage() {
        val w_nTemplateNo: Int
        w_nTemplateNo = GetInputTemplateNo()
        if (w_nTemplateNo < 0) return
        m_szHost!!.Run_CmdVerifyWithImage(w_nTemplateNo)
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
    fun GetInputTemplateNo(): Int {
        val str: String
        str = m_editUserID!!.text.toString()
        if (str.isEmpty()) {
            m_txtStatus!!.text = "Please input user id"
            return -1
        }
        try {
            m_nUserID = str.toInt()
        } catch (e: NumberFormatException) {
            m_txtStatus!!.text = String.format("Please input correct user id(1~%d)", DevComm.GD_MAX_RECORD_COUNT.toShort())
            return -1
        }
        return m_nUserID
    }

    var runEnableCtrl = Runnable {
        EnableCtrl(true)
        m_btnOpenDevice!!.isEnabled = false
        m_btnCloseDevice!!.isEnabled = true
        m_btnCancel!!.isEnabled = false
    }

    override fun onKeyDown(KeyCode: Int, event: KeyEvent): Boolean {
        when (KeyCode) {
            KeyEvent.KEYCODE_BACK -> if (event.repeatCount == 0) {
                if (m_btnCancel!!.isEnabled) {
                    val toast = Toast.makeText(applicationContext, "Please cancel your command first", Toast.LENGTH_SHORT)
                    toast.show()
                    return true
                }
                if (m_btnCloseDevice!!.isEnabled) OnCloseDeviceBtn()
            }
        }
        return super.onKeyDown(KeyCode, event)
    }

    companion object {
        private var m_szHost: SZOEMHost_Lib? = null
    }
}