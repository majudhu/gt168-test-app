package android_serialport_api

import com.szadst.szoemhost_lib.DevComm
import com.szadst.szoemhost_lib.MAX_DATA_LEN
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.InvalidParameterException
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

abstract class SerialHelper @JvmOverloads constructor(sPort: String = "/dev/s3c2410_serial0", iBaudRate: Int = 9600) {
    private var mSerialPort: SerialPort? = null
    private var mOutputStream: OutputStream? = null
    private var mInputStream: InputStream? = null
    private var mReadThread: ReadThread? = null
    private var mSendThread: SendThread? = null
    //----------------------------------------------------
    var port = "/dev/s3c2410_serial0"
        set
    //----------------------------------------------------
    var baudRate = 9600
        set
    //----------------------------------------------------
    var isOpen = false
        private set
    private var _bLoopData = byteArrayOf(0x30)
    private var iDelay = 500

    constructor(sPort: String, sBaudRate: String) : this(sPort, sBaudRate.toInt()) {}

    //----------------------------------------------------
    @Throws(SecurityException::class, IOException::class, InvalidParameterException::class)
    fun open() {
        mSerialPort = SerialPort(File(port), baudRate, 0)
        mOutputStream = mSerialPort!!.outputStream
        mInputStream = mSerialPort!!.inputStream
        mReadThread = ReadThread()
        mReadThread!!.start()
        mSendThread = SendThread()
        mSendThread!!.setSuspendFlag()
        mSendThread!!.start()
        isOpen = true
    }

    //----------------------------------------------------
    fun close() {
        if (mReadThread != null) mReadThread!!.interrupt()
        if (mSerialPort != null) {
            mSerialPort!!.close()
            mSerialPort = null
        }
        isOpen = false
    }

    //----------------------------------------------------
    fun send(bOutArray: ByteArray?) {
        try {
            mOutputStream!!.write(bOutArray)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //----------------------------------------------------
    fun sendTxt(sTxt: String) {
        val bOutArray = sTxt.toByteArray()
        send(bOutArray)
    }

    //----------------------------------------------------
    private inner class ReadThread : Thread() {
        override fun run() {
            super.run()
            while (!isInterrupted) {
                try {
                    if (mInputStream == null) return
                    val buffer = ByteArray(MAX_DATA_LEN)
                    val size = mInputStream!!.read(buffer)
                    if (size > 0) {
                        val ComRecData = ComBean(port, buffer, size)
                        onDataReceived(ComRecData)
                    }
                    try {
                        sleep(50) //��ʱ50ms
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    return
                }
            }
        }
    }

    //----------------------------------------------------
    private inner class SendThread : Thread() {
        private val lock = ReentrantLock()
        private val condition = lock.newCondition()
        var suspendFlag = true // �����̵߳�ִ��
        override fun run() {
            super.run()
            while (!isInterrupted) {
                lock.withLock {
                    while (suspendFlag) {
                        try {
                            condition.await()
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }
                }
                send(getbLoopData())
                try {
                    sleep(iDelay.toLong())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }

        //�߳���ͣ
        fun setSuspendFlag() {
            suspendFlag = true
        }

        //�����߳�
        @Synchronized
        fun setResume() {
            suspendFlag = false
            condition.signal()
        }
    }

    fun setBaudRate(iBaud: Int): Boolean {
        return if (isOpen) {
            false
        } else {
            baudRate = iBaud
            true
        }
    }

    fun setBaudRate(sBaud: String): Boolean {
        val iBaud = sBaud.toInt()
        return setBaudRate(iBaud)
    }

    fun setPort(sPort: String): Boolean {
        return if (isOpen) {
            false
        } else {
            port = sPort
            true
        }
    }

    //----------------------------------------------------
    fun getbLoopData(): ByteArray {
        return _bLoopData
    }

    //----------------------------------------------------
    fun setbLoopData(bLoopData: ByteArray) {
        _bLoopData = bLoopData
    }

    //----------------------------------------------------
    fun setTxtLoopData(sTxt: String) {
        _bLoopData = sTxt.toByteArray()
    }

    //----------------------------------------------------
    fun getiDelay(): Int {
        return iDelay
    }

    //----------------------------------------------------
    fun setiDelay(iDelay: Int) {
        this.iDelay = iDelay
    }

    //----------------------------------------------------
    fun startSend() {
        if (mSendThread != null) {
            mSendThread!!.setResume()
        }
    }

    //----------------------------------------------------
    fun stopSend() {
        if (mSendThread != null) {
            mSendThread!!.setSuspendFlag()
        }
    }

    //----------------------------------------------------
    protected abstract fun onDataReceived(ComRecData: ComBean)

    //----------------------------------------------------
    init {
        port = sPort
        baudRate = iBaudRate
    }
}