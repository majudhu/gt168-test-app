package android_serialport_api

import java.text.SimpleDateFormat
import java.util.*

/**
 * @author benjaminwan
 */
class ComBean(sPort: String, buffer: ByteArray, size: Int) {
    var bRec: ByteArray? = null
    var sRecTime = ""
    var sComPort = ""
    var nSize = 0

    init {
        sComPort = sPort
        bRec = ByteArray(size)
        for (i in 0 until size) {
            bRec!![i] = buffer[i]
        }
        nSize = size
        val sDateFormat = SimpleDateFormat("hh:mm:ss")
        sRecTime = sDateFormat.format(Date())
    }
}