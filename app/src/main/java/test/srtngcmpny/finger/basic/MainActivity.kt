package test.srtngcmpny.finger.basic

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), View.OnClickListener, FpDataReceived {
    var m_nUserID = 0
    var m_nBaudrate = 9600
    var m_szDevice: String = "USB"
    // Controls
    var m_btnOpenDevice: Button? = null
    var m_btnCloseDevice: Button? = null
    var m_btnEnroll: Button? = null
    var m_btnIdentify: Button? = null
    var m_btnIdentifyFree: Button? = null
    var m_btnCancel: Button? = null
    var m_btnGetUserCount: Button? = null
    var m_btnGetEmptyID: Button? = null
    var m_btnDeleteID: Button? = null
    var m_btnDeleteAll: Button? = null
    var m_editUserID: EditText? = null
    var m_txtStatus: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Set Keep Screen On
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initWidget()
        setInitialState()
        onOpenDeviceBtn()
    }

    override fun onClick(view: View) {
        when {
            view === m_btnOpenDevice -> onOpenDeviceBtn()
            view === m_btnCloseDevice -> onCloseDeviceBtn()
            view === m_btnEnroll -> onEnrollBtn()
            view === m_btnIdentify -> onIdentifyBtn()
            view === m_btnIdentifyFree -> onIdentifyFreeBtn()
            view === m_btnCancel -> onCancelBtn()
            view === m_btnGetUserCount -> onGetUserCount()
            view === m_btnGetEmptyID -> onGetEmptyID()
            view === m_btnDeleteID -> onDeleteIDBtn()
            view === m_btnDeleteAll -> onDeleteAllBtn()
        }
    }

    private fun initWidget() {
        m_btnOpenDevice = findViewById<View>(R.id.btnOpenDevice) as Button
        m_btnCloseDevice = findViewById<View>(R.id.btnCloseDevice) as Button
        m_btnEnroll = findViewById<View>(R.id.btnEnroll) as Button
        m_btnIdentify = findViewById<View>(R.id.btnIdentify) as Button
        m_btnCancel = findViewById<View>(R.id.btnCancel) as Button
        m_btnGetUserCount = findViewById<View>(R.id.btnGetEnrollCount) as Button
        m_btnGetEmptyID = findViewById<View>(R.id.btnGetEmptyID) as Button
        m_btnDeleteID = findViewById<View>(R.id.btnRemoveTemplate) as Button
        m_btnDeleteAll = findViewById<View>(R.id.btnRemoveAll) as Button
        m_txtStatus = findViewById<View>(R.id.txtStatus) as TextView
        m_editUserID = findViewById<View>(R.id.editUserID) as EditText
        m_btnOpenDevice!!.setOnClickListener(this)
        m_btnCloseDevice!!.setOnClickListener(this)
        m_btnEnroll!!.setOnClickListener(this)
        m_btnIdentify!!.setOnClickListener(this)
        m_btnCancel!!.setOnClickListener(this)
        m_btnGetUserCount!!.setOnClickListener(this)
        m_btnGetEmptyID!!.setOnClickListener(this)
        m_btnDeleteID!!.setOnClickListener(this)
        m_btnDeleteAll!!.setOnClickListener(this)
        if (m_szHost == null) {
            m_szHost = FpLib(this, m_txtStatus, runEnableCtrl)
        }
//        else {
//            m_szHost!!.SZOEMHost_Lib_Init(this, m_txtStatus, runEnableCtrl)
//        }
    }

    private fun enableCtrl(bEnable: Boolean) {
        m_btnEnroll!!.isEnabled = bEnable
        m_btnIdentify!!.isEnabled = bEnable
        m_btnGetUserCount!!.isEnabled = bEnable
        m_btnGetEmptyID!!.isEnabled = bEnable
        m_btnDeleteID!!.isEnabled = bEnable
        m_btnDeleteAll!!.isEnabled = bEnable
        m_editUserID!!.isEnabled = bEnable
    }

    private fun setInitialState() {
        m_txtStatus!!.text = "Please open device!"
        m_btnOpenDevice!!.isEnabled = true
        m_btnCloseDevice!!.isEnabled = false
        enableCtrl(false)
        m_btnCancel!!.isEnabled = false
    }

    private fun onOpenDeviceBtn() {
        if (m_szHost!!.OpenDevice(m_szDevice, m_nBaudrate) == 0) {
            enableCtrl(true)
            m_btnOpenDevice!!.isEnabled = false
            m_btnCloseDevice!!.isEnabled = true
            onIdentifyBtn()
        }
    }

    private fun onCloseDeviceBtn() {
        m_szHost!!.CloseDevice()
        setInitialState()
    }

    private fun onEnrollBtn() {
        val wnTemplateNo: Int = getInputTemplateNo()
        if (wnTemplateNo < 0) return
        enableCtrl(false)
        m_btnCloseDevice!!.isEnabled = false
        m_btnCancel!!.isEnabled = true
        m_szHost!!.Run_CmdEnroll(wnTemplateNo)
    }

    private fun onIdentifyBtn() {
        enableCtrl(false)
        m_btnCloseDevice!!.isEnabled = false
        m_btnCancel!!.isEnabled = true
        m_szHost!!.Run_CmdIdentify()
    }

    private fun onIdentifyFreeBtn() {
        enableCtrl(false)
        m_btnCloseDevice!!.isEnabled = false
        m_btnCancel!!.isEnabled = true
        m_szHost!!.Run_CmdIdentifyFree()
    }

    private fun onDeleteIDBtn() {
        val wnTemplateNo: Int = getInputTemplateNo()
        if (wnTemplateNo < 0) return
        m_szHost!!.Run_CmdDeleteID(wnTemplateNo)
    }

    private fun onDeleteAllBtn() {
        m_szHost!!.Run_CmdDeleteAll()
    }

    private fun onGetEmptyID() {
        m_szHost!!.Run_CmdGetEmptyID()
    }

    private fun onGetUserCount() {
        m_szHost!!.Run_CmdGetUserCount()
    }

    private fun onCancelBtn() {
        m_btnCloseDevice!!.isEnabled = false
        m_szHost!!.Run_CmdCancel()
    }

    private fun getInputTemplateNo(): Int {
        val str: String = m_editUserID!!.text.toString()
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

    private var runEnableCtrl = Runnable {
        enableCtrl(true)
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
                if (m_btnCloseDevice!!.isEnabled) onCloseDeviceBtn()
            }
        }
        return super.onKeyDown(KeyCode, event)
    }

    companion object {
        private var m_szHost: FpLib? = null
    }

    override fun onReceived(data: String?) {
        // Do anything you want here \(^_^)/
        Log.e("data", data!!)
    }
}