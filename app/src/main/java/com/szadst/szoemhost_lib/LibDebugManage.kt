package com.szadst.szoemhost_lib

//import java.io.*

/**
 * Created by Administrator on 8/2/13.
 */
//object LibDebugManage {
//    fun DeleteLog() {
//        val file = File("mnt/sdcard/log/liblog.txt")
//        file.delete()
//    }
//
//    fun WriteLog(strLog: String?) {
//        val logPath = "log\\liblog.txt"
//        val templog = File(logPath)
//        try {
//            if (!templog.exists()) templog.createNewFile()
//            val raf = RandomAccessFile(logPath, "rw")
//            raf.seek(raf.length())
//            raf.writeBytes(strLog)
//            raf.close()
//        } catch (e: Exception) { // TODO Auto-generated catch block
//            e.printStackTrace()
//        }
//    }
//
//    fun WriteLog2(str: String?) {
//        val str_Path_Full = "mnt/sdcard/log/liblog.txt"
//        val file = File(str_Path_Full)
//        if (file.exists() == false) {
//            try {
//                file.createNewFile()
//            } catch (e: IOException) {
//            }
//        } else {
//            try {
//                val bfw = BufferedWriter(FileWriter(str_Path_Full, true))
//                bfw.write(str)
//                bfw.write("\n")
//                bfw.flush()
//                bfw.close()
//            } catch (e: FileNotFoundException) {
//            } catch (e: IOException) {
//            }
//        }
//    }
//
//    fun WriteBuffer(p_pBuf: ByteArray, p_nLen: Int) {
//        val str_Path_Full = "mnt/sdcard/log/liblog.txt"
//        val file = File(str_Path_Full)
//        var i: Int
//        if (file.exists() == false) {
//            try {
//                file.createNewFile()
//            } catch (e: IOException) {
//            }
//        } else {
//            try {
//                val bfw = BufferedWriter(FileWriter(str_Path_Full, true))
//                i = 0
//                while (i < p_nLen) {
//                    bfw.write(String.format("%02X ", p_pBuf[i]))
//                    i++
//                }
//                bfw.write("\n")
//                bfw.flush()
//                bfw.close()
//            } catch (e: FileNotFoundException) {
//            } catch (e: IOException) {
//            }
//        }
//    }
//
//    fun WriteBmp(image: ByteArray?, nlen: Int) {
//        val logPath = "mnt/sdcard/log/libfp.bmp"
//        val templog = File(logPath)
//        try {
//            if (!templog.exists()) templog.createNewFile()
//            val raf = RandomAccessFile(logPath, "rw")
//            raf.seek(raf.length())
//            raf.write(image, 0, nlen)
//            raf.close()
//        } catch (e: Exception) { // TODO Auto-generated catch block
//            e.printStackTrace()
//        }
//    }
//}