package com.example.save_csv_test

import android.content.Context
import android.os.Environment
import android.os.Handler
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

class OtherFileStorage(private val context: Context) {


    private val fileAppend: Boolean = true //true=追記, false=上書き
    private var fileTime = System.currentTimeMillis()
    private var fileName: String = "$fileTime _ "
    private val extension: String = ".csv"
    private var filePath: String = context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString().plus("/").plus("temp.csv").plus(extension) //内部ストレージのDocumentのURL

    private var recStatus: Boolean = false

    private var oldFile = File(fileName)
    private var updated: Boolean = false


    fun writeText(text:String){
        val fil = FileWriter(filePath,fileAppend)
        val pw = PrintWriter(BufferedWriter(fil))
        val addUnixTimeCsv = System.currentTimeMillis().toString().plus(",").plus(text)
        pw.println(addUnixTimeCsv)
        pw.close()
    }

    private fun makeFile() {
        fileTime = System.currentTimeMillis()
        fileName = "$fileTime _ "
        filePath = context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString().plus("/").plus(fileName).plus(extension) //内部ストレージのDocumentのURL
        val fil = FileWriter(filePath,fileAppend)
        val pw = PrintWriter(BufferedWriter(fil))
        val text = "time,x_accel,y_accel,z_accel,x_gyro,y_gyro,z_gyro"
        pw.println(text)
        pw.close()
    }

    @Suppress("DEPRECATION")
    fun reWriteText() {
        makeFile()
        //writeText(text)
        Handler().postDelayed({
            setStatus(false)
        //    updated = false
        }, 3000)
    }

    fun setStatus(status: Boolean) {
        recStatus = status
    }

    fun getStatus(): Boolean {
        return recStatus
    }

    fun reName(text: String) {
        val temp = "$fileTime _ $text"
        val newFile = File(temp)
        if(oldFile.renameTo(newFile)) updated = true
    }

    fun getSaved(): Boolean {
        return updated
    }
}