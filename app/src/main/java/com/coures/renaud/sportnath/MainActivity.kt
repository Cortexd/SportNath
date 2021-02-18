package com.coures.renaud.sportnath

import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Chronometer
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {

     var chronometer: Chronometer? = null
     var pauseOffset: Long = 0
     var running = false


    internal var isWorking = false
    internal var imageResource_play = 0
    internal var imageResource_pause = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab_reset).setOnClickListener { view ->
            Snackbar.make(view, "reset", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val uri_play = "@android:drawable/ic_media_play" // where myresource (without the extension) is the file
        val uri_pause = "@android:drawable/ic_media_pause" // where myresource (without the extension) is the file
        imageResource_play = resources.getIdentifier(uri_play, null, packageName)
        imageResource_pause = resources.getIdentifier(uri_pause, null, packageName)

        // Refresh horloge
        horloge()


        val chr = findViewById<Chronometer>(R.id.textView_chrono)
        val btnStartStop = findViewById<FloatingActionButton>(R.id.fab_start_pause)

        // depart a zero

        if (chr != null) {
            chronometer = chr
            chronometer!!.stop()
            chronometer!!.base = SystemClock.elapsedRealtime()
        }

        isWorking = false
        btnStartStop.setImageResource(imageResource_play)

        chronometer!!.onChronometerTickListener = OnChronometerTickListener { chronometer ->
            if (SystemClock.elapsedRealtime() - chronometer.base >= 10000) {
                chronometer.base = SystemClock.elapsedRealtime()
                Toast.makeText(this@MainActivity, "Bing!", Toast.LENGTH_SHORT).show()
            }
        }




        // gestion bouton
        btnStartStop.setOnClickListener(object : View.OnClickListener {

            //internal var isWorking = false
            override fun onClick(v: View) {
                if (!isWorking) {
                    meter.start()
                    isWorking = true
                    btnStartStop.setImageResource(imageResource_pause)
                } else {
                    meter.stop()
                    isWorking = false
                    btnStartStop.setImageResource(imageResource_play)

                }


            }
        })

        val btnReset = findViewById<FloatingActionButton>(R.id.fab_reset)
        btnReset.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                meter.base = SystemClock.elapsedRealtime()
                meter.stop()
                isWorking = false
                btnStartStop.setImageResource(imageResource_play)

            }
        })


    }

    private fun horloge() {
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