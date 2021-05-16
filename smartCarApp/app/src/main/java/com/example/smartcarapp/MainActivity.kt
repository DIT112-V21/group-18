package com.example.smartcarapp
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.activity_control.*
import kotlinx.android.synthetic.main.content_main.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import kotlin.math.cos
import kotlin.math.sin


class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.smartcarapp.R.layout.activity_control)
        connect(this)
        setSupportActionBar(main_toolbar)
        val toggle = ActionBarDrawerToggle(this, drawerLayout,main_toolbar,R.string.open, R.string.close)
        toggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        nav_menu.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.disconnect_item -> disconnect()
                //Todo: create "layouts" for different items: Manual control, camera, mapview
                R.id.manual_control_item -> {
                    manual_control_layout.visibility = View.VISIBLE
                    closeDrawer()
                }
            }
            true
        }
        joyStick.setOnMoveListener { angle, strength ->
            if(!manualSwitch.isChecked) return@setOnMoveListener //if not manual control, skip this function
            publish("smartcar/analog/",  strength.toString() + "," + angle.toString())
        }
        manualSwitch.setOnClickListener {
            if(manualSwitch.isChecked) {
                joyStick.alpha = 1.0F
                publish("smartcar/cleansurfaces/",  "manual")
            }
            else {
                joyStick.alpha = 0.1F
                publish("smartcar/cleansurfaces/",  "auto")
            }
        }
    }

    private lateinit var mqttClient: MqttAndroidClient
    // TAG
    companion object {
        const val TAG = "AndroidMqttClient"
    }

    fun connect(context: Context) { //Connect to domain and subscribe to topic automatically
        val domain=intent.getStringExtra("domainInput")
        val serverURI = "tcp://"+domain.toString()+":1883" //Change url to tcp and use port 1883 automatically for now
        mqttClient = MqttAndroidClient(context, serverURI, "kotlin_client")
        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(MainActivity.TAG, "Received a message from topic: $topic") //logcat logging
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(MainActivity.TAG, "Connection lost ${cause.toString()}")
                finish() //Return to login activity
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })
        val options = MqttConnectOptions()
        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(MainActivity.TAG, "Connection success") //logcat logging
                    subscribe("smartcar/#", 1)
                    if(!manualSwitch.isChecked) publish("smartcar/cleansurfaces/",  "auto")
                    publish("smartcar/",  "App has been connected.")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(MainActivity.TAG, "Connection failure") //logcat logging
                    finish() //return to login activity
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }

    fun disconnect() {
        try {
            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Disconnected")
                    finish()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to disconnect")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribe(topic: String, qos: Int = 1) {
        try {
            mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(MainActivity.TAG, "Subscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(MainActivity.TAG, "Failed to subscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(MainActivity.TAG, "$msg published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(MainActivity.TAG, "Failed to publish $msg to $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun closeDrawer() { //Close main navigation drawer (hamburger button)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }
}