/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package android_serialport_api

import android.util.Log
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.LineNumberReader
import java.util.*

class SerialPortFinder {
    inner class Driver(val name: String, private val mDeviceRoot: String) {
        private val mDevices = Vector<File>()
        val devices: Vector<File>
            get() {
                val dev = File("/dev")
                val files = dev.listFiles()
                if (files != null) for (file in files)
                    if (file.absolutePath.startsWith(mDeviceRoot)) {
                        Log.d(TAG, "Found new device: $file")
                        mDevices.add(file)
                    }

                return mDevices
            }

    }

    private var mDrivers: Vector<Driver>? = null
    // Issue 3:
// Since driver name may contain spaces, we do not extract driver name with split()
    @get:Throws(IOException::class)
    val drivers: Vector<Driver>
        get() {
            if (mDrivers == null) {
                mDrivers = Vector()
                val r = LineNumberReader(FileReader("/proc/tty/drivers"))
                var l: String
                while (r.readLine().also { l = it } != null) { // Issue 3:
// Since driver name may contain spaces, we do not extract driver name with split()
                    val drivername = l.substring(0, 0x15).trim { it <= ' ' }
                    val w = l.split(" +").toTypedArray()
                    if (w.size >= 5 && w[w.size - 1] == "serial") {
                        Log.d(TAG, "Found new driver " + drivername + " on " + w[w.size - 4])
                        mDrivers!!.add(Driver(drivername, w[w.size - 4]))
                    }
                }
                r.close()
            }
            return mDrivers!!
        }

    // Parse each driver
//    val allDevices: Array<String>
//        get() {
//            val devices = Vector<String>()
//            // Parse each driver
//            val itdriv: Iterator<Driver>
//            try {
//                itdriv = drivers.iterator()
//                while (itdriv.hasNext()) {
//                    val driver = itdriv.next()
//                    val itdev: Iterator<File> = driver.devices.iterator()
//                    while (itdev.hasNext()) {
//                        val device = itdev.next().name
//                        val value = String.format("%s (%s)", device, driver.name)
//                        devices.add(value)
//                    }
//                }
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//            return devices.toTypedArray()
//        }

    // Parse each driver
    val allDevicesPath: Array<String>
        get() {
            val devices = Vector<String>()
            // Parse each driver
            val itdriv: Iterator<Driver>
            try {
                itdriv = drivers.iterator()
                while (itdriv.hasNext()) {
                    val driver = itdriv.next()
                    val itdev: Iterator<File> = driver.devices.iterator()
                    while (itdev.hasNext()) {
                        val device = itdev.next().absolutePath
                        devices.add(device)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return devices.toTypedArray()
        }

    companion object {
        private const val TAG = "SerialPort"
    }
}