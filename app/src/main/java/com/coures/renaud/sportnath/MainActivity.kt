package com.coures.renaud.sportnath

//import org.junit.experimental.theories.DataPoint
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Chronometer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.DataPointInterface
import com.jjoe64.graphview.series.LineGraphSeries
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.timerTask


class MainActivity : AppCompatActivity() {

    // Chrono
     var chronometer: Chronometer? = null
     var pauseOffset: Long = 0
     var running = false
     var btnStartStop: FloatingActionButton? = null
     var imageResource_play = 0
     var imageResource_pause = 0

    // Compteur
    var tourMinute = 0
    var bicycleWheelCircumference = (2.1206 * 2.44).toFloat()
    var distance = 0

    // FAKE
    var nbtour = 0
    var timer:Timer? = null

    // Interface
    var textview_distance : TextView? = null
    var textview_vitesse : TextView? = null

    // Graphique
    var mSeries2 : LineGraphSeries<DataPointInterface> = LineGraphSeries<DataPointInterface>()
    var graph2LastXValue = 5.0

   // Bluetooth
    val MESSAGE_READ = 9999
    private var recDataString = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Recup des URI pour changement de boutton
        GetUriForButtons()

        // Refresh horloge
        Horloge()

        chronometer = findViewById<Chronometer>(R.id.textView_chrono)
        chronometer!!.base = SystemClock.elapsedRealtime()

        btnStartStop = findViewById<FloatingActionButton>(R.id.fab_start_pause)
        btnStartStop!!.setImageResource(imageResource_play)

        textview_distance = findViewById<TextView>(R.id.textView_distance)
        textview_vitesse = findViewById<TextView>(R.id.textView_vitesse)


        // TEST TEST TEST //
        Timer().schedule(timerTask {
            Log.e("CLODO", "Dans le timer")
            var tm = (10..60).random()
            nbtour++

            // https://stackoverflow.com/questions/34490859/textview-values-does-not-update-when-data-received-from-arduino
            var msg = mHandler!!.obtainMessage()
            var bundle: Bundle? = Bundle()
            bundle!!.putString("TM", tm.toString());
            bundle!!.putString("T", nbtour.toString());

            msg.data = bundle
            //mHandler!!.handleMessage(msg)
            mHandler!!.sendMessage(msg);

        }, 2000, 2000)


        // TEST GRAPHIQUE
        val graph: GraphView = findViewById(R.id.graph)
        //mSeries2 = LineGraphSeries<DataPointInterface>()
        graph.addSeries(mSeries2)
        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX(40.0)

    }



    private fun GetUriForButtons() {
        val uri_play = "@android:drawable/ic_media_play" // where myresource (without the extension) is the file
        val uri_pause = "@android:drawable/ic_media_pause" // where myresource (without the extension) is the file
        imageResource_play = resources.getIdentifier(uri_play, null, packageName)
        imageResource_pause = resources.getIdentifier(uri_pause, null, packageName)
    }

    fun StartAndPauseChronometer(v: View?) {
        if (!running)
        {
            chronometer!!.base = SystemClock.elapsedRealtime() - pauseOffset
            chronometer!!.start()
            running = true
            btnStartStop!!.setImageResource(imageResource_pause)
        }
        else
        {
            chronometer!!.stop()
            pauseOffset = SystemClock.elapsedRealtime() - chronometer!!.base
            running = false
            btnStartStop!!.setImageResource(imageResource_play)
        }

    }

    fun ResetChronometer(v: View?) {

        chronometer!!.base = SystemClock.elapsedRealtime()
        pauseOffset = 0
        running = true
        StartAndPauseChronometer(v)
    }

    fun Horloge() {
        val thread: Thread = object : Thread() {
            override fun run() {
                try {
                    while (!this.isInterrupted) {
                        sleep(1000)
                        runOnUiThread {
                            var currentDateTime = LocalDateTime.now()
                            var time = currentDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                            var displayTextView = findViewById<TextView>(R.id.textview_date_heure)
                            displayTextView.setText(time)
                        }
                    }
                } catch (e: InterruptedException) {
                }
            }
        }

        thread.start()
    }


    // Bluetooth ////////////////////////////////////////////////

    private var outputStream: OutputStream? = null
    private var inStream: InputStream? = null

    private fun init() {
        val blueAdapter = BluetoothAdapter.getDefaultAdapter()
        if (blueAdapter != null) {
            if (blueAdapter.isEnabled) {
                val bondedDevices = blueAdapter.bondedDevices
                if (bondedDevices.size > 0) {
                    val devices = bondedDevices.toTypedArray() as Array<Any>
                    val device = devices[1] as BluetoothDevice
                    val uuids = device.uuids
                    val socket = device.createRfcommSocketToServiceRecord(uuids[0].uuid)
                    socket.connect()
                    outputStream = socket.outputStream
                    inStream = socket.inputStream
                }
                Log.e("error", "No appropriate paired devices.")
            } else {
                Log.e("error", "Bluetooth is disabled.")
            }
        }
    }

    fun write(s: String) {
        outputStream!!.write(s.toByteArray())
    }

    fun run() {
        val BUFFER_SIZE = 1024
        val buffer = ByteArray(BUFFER_SIZE)
        var bytes = 0
        val b = BUFFER_SIZE
        while (true) {
            try {
                bytes = inStream!!.read(buffer, bytes, BUFFER_SIZE - bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Bluetooth ////////////////////////////////////////////////

    // Calcul //////////////////////
    fun kmParHeure(tourParMinute: Int): Float {
        return (tourParMinute * bicycleWheelCircumference / 1000) * 60;
    }

    // gestionnaire événement arduino pour communication thread UI
    val mHandler = object : Handler() {
        @SuppressLint("HandlerLeak")
        override fun handleMessage(msg: Message) {

            if (running) {
                Log.e("CLODO", "Dans le handler")

                val bundle = msg.data

                var TM = bundle.getString("TM")!!.toInt()
                var T = bundle.getString("T")!!.toInt()


                textview_vitesse!!.setText("$TM  Km/h")
                textview_distance!!.setText("$T  Km")

                // Ajout graphique
                graph2LastXValue += 1.0
                mSeries2.appendData(DataPoint(graph2LastXValue, TM.toDouble()), true, 40)

                /*  when (msg.what) {
                MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val readMessage = String(readBuf, 0, msg.arg1)
                    recDataString = readMessage;
                    Log.d("read", readMessage)
                    if (recDataString.startsWith('TM')) {
                        val BPM: String = recDataString.substring(1, 3)
                        val SPO2: String = recDataString.substring(3, 5)
                        val Temp: String = recDataString.substring(5, 9)
                        Log.d("BPM", BPM)
                        Log.d("SPO2", SPO2)
                        Log.d("Temp", Temp)
                        textview_vitesse.setText("BPM  $BPM")
                        textview_distance.setText("SPO2  $SPO2")
                    }
                }
            }*/
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}