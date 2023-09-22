package com.example.save_csv_test

import android.annotation.SuppressLint
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accSensor: Sensor? = null
    private var gyrSensor: Sensor? = null

    private var accSensorX: Float? = null
    private var accSensorY: Float? = null
    private var accSensorZ: Float? = null
    private var gyrSensorX: Float? = null
    private var gyrSensorY: Float? = null
    private var gyrSensorZ: Float? = null
    private var accTmp: String? = null
    private var gyrTmp: String? = null

    private lateinit var otherFileStorage: OtherFileStorage

    private var aChart: LineChart? = null
    private var gChart: LineChart? = null
    private var aNames = arrayOf("x_accel", "y_accel", "z_accel")
    private var gNames = arrayOf("x_gyro", "y_gyro", "z_gyro")
    private var aColors = intArrayOf(Color.RED, Color.GREEN, Color.BLUE)
    private var gColors = intArrayOf(Color.MAGENTA, Color.YELLOW, Color.CYAN)

    private var buttonStart: Button? = null

    private var imageView:ImageView? = null
    private var savedView:ImageView? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        gyrSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        otherFileStorage = OtherFileStorage(this)

        aChart = findViewById(R.id.accChart)
        gChart = findViewById(R.id.gyrChart)

        aChart!!.data = LineData()
        gChart!!.data = LineData()

        buttonStart = findViewById(R.id.buttonStart)

        imageView = findViewById(R.id.image_rec)
        savedView = findViewById(R.id.imageSave)

        val setDistance: EditText = findViewById(R.id.distance)
        val updateFileName: Button = findViewById(R.id.buttonName)


        updateFileName.setOnClickListener {
            val text = setDistance.text.toString()
            otherFileStorage.reName(text)
        }

    }

    //センサーに何かしらのイベントが発生したときに呼ばれる
    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    override fun onSensorChanged(event: SensorEvent) {

        // Remove the gravity contribution with the high-pass filter.
        if (event.sensor.type === Sensor.TYPE_LINEAR_ACCELERATION) {

            //センサー値を取得
            accSensorX = event.values[0]
            accSensorY = event.values[1]
            accSensorZ = event.values[2]

            accTmp = """加速度
                        X: $accSensorX
                        Y: $accSensorY
                        Z: $accSensorZ"""

            //描画
            val aData = aChart!!.lineData
            if (aData != null) {
                for (i in 0..2) {
                    var set = aData.getDataSetByIndex(i)
                    if (set == null) {
                        set = createSet(aNames[i], aColors[i])
                        aData.addDataSet(set)
                    }
                    aData.addEntry(Entry(set.entryCount.toFloat(), event.values[i]), i)
                    aData.notifyDataChanged()
                }
                aChart!!.notifyDataSetChanged()
                aChart!!.setVisibleXRangeMaximum(50f)
                aChart!!.moveViewToX(aData.entryCount.toFloat())
            }
        }
        if (event.sensor.type === Sensor.TYPE_GYROSCOPE) {

            gyrSensorX = event.values[0]
            gyrSensorY = event.values[1]
            gyrSensorZ = event.values[2]

            gyrTmp = """ジャイロ
                        X: $gyrSensorX
                        Y: $gyrSensorY
                        Z: $gyrSensorZ"""

            val gData = gChart!!.lineData
            if (gData != null) {
                for (i in 0..2) {
                    var set = gData.getDataSetByIndex(i)
                    if (set == null) {
                        set = createSet(gNames[i], gColors[i])
                        gData.addDataSet(set)
                    }
                    gData.addEntry(Entry(set.entryCount.toFloat(), event.values[i]), i)
                    gData.notifyDataChanged()
                }
                gChart!!.notifyDataSetChanged()
                gChart!!.setVisibleXRangeMaximum(50f)
                gChart!!.moveViewToX(gData.entryCount.toFloat())
            }
        }

        //表示
        val accSensorText: TextView = findViewById(R.id.accValue)
        val gyrSensorText: TextView = findViewById(R.id.gyrValue)
        accSensorText.text = accTmp
        gyrSensorText.text = gyrTmp
        //csvへ書き込み
        val log: String = accSensorX.toString().plus(",").plus(accSensorY).plus(",").plus(accSensorZ).plus(",").plus(gyrSensorX).plus(",").plus(gyrSensorY).plus(",").plus(gyrSensorZ).plus(",")
        buttonStart!!.setOnClickListener {
            otherFileStorage.setStatus(true)
            otherFileStorage.reWriteText()
        }

        if(otherFileStorage.getStatus()) {
            otherFileStorage.writeText(log)
            imageView!!.visibility = View.VISIBLE
        }else {
            imageView!!.visibility = View.INVISIBLE
        }
        if(otherFileStorage.getSaved()) {
            savedView!!.visibility = View.VISIBLE
        }else {
            savedView!!.visibility = View.INVISIBLE
        }
    }

    //センサの精度が変更されたときに呼ばれる
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onResume() {
        super.onResume()
        //リスナーとセンサーオブジェクトを渡す
        //第一引数はインターフェースを継承したクラス、今回はthis
        //第二引数は取得したセンサーオブジェクト
        //第三引数は更新頻度 UIはUI表示向き、FASTはできるだけ早く、GAMEはゲーム向き
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, gyrSensor, SensorManager.SENSOR_DELAY_UI)
    }

    //アクティビティが閉じられたときにリスナーを解除する
    override fun onPause() {
        super.onPause()
        //リスナーを解除しないとバックグラウンドにいるとき常にコールバックされ続ける
        sensorManager.unregisterListener(this)
    }

    //グラフ設定
    private fun createSet(label: String, color: Int): LineDataSet {
        val set = LineDataSet(null, label)
        set.lineWidth = 2.5f
        set.color = color
        set.setDrawCircles(false)
        set.setDrawValues(false)
        return set
    }

}