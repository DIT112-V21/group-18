package com.example.smartcarapp
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.github.controlwear.virtual.joystick.android.JoystickView
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import kotlin.math.cos
import kotlin.math.sin


class ControlActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.smartcarapp.R.layout.activity_control)
        connect(this)
        val disconnect = findViewById<Button>(R.id.disconnectButton)
        val coordsText = findViewById<TextView>(R.id.CoordDisplay)
        val joystick = findViewById<JoystickView>(R.id.joyStick)
        joystick.setOnMoveListener { angle, strength ->
            //Log.d("JoyStickStrength:", strength.toString())
            val angleRadians = -angle.toFloat() * Math.PI / 180
            val length = strength.toFloat() / 100 //change from 0 to 100, to 0 to 1
            val analogX = cos(angleRadians)*length
            val analogY = -sin(angleRadians)*length
            //coordsText.text = "X: " + analogX.toString() + ", Y: " + analogY.toString() + ", Length: " + length.toString()
            publish("smartcar/analog/",  analogX.toString() + "," + analogY.toString())
        }
    }

    private lateinit var mqttClient: MqttAndroidClient
    // TAG
    companion object {
        const val TAG = "AndroidMqttClient"
    }

    fun connect(context: Context) { //Connect to domain and subscribe to topic automatically
        val domain=intent.getStringExtra("domainInput")
        val messagesBox = findViewById<TextView>(R.id.CoordDisplay)
        val serverURI = "tcp://"+domain.toString()+":1883" //Change url to tcp and use port 1883 automatically for now
        mqttClient = MqttAndroidClient(context, serverURI, "kotlin_client")
        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(ControlActivity.TAG, "Received a message from topic: $topic") //logcat logging
                messagesBox.text = "Received a message from topic $topic\n"
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(ControlActivity.TAG, "Connection lost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })
        val options = MqttConnectOptions()
        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(ControlActivity.TAG, "Connection success") //logcat logging
                    messagesBox.text = "Connected to domain: " + domain + "\n"
                    //findViewById<TextView>(R.id.publishMsgText).setVisibility(View.VISIBLE) //Set the publish mqtt message controls to visible
                    //findViewById<TextView>(R.id.publishMsgButton).setVisibility(View.VISIBLE)
                    subscribe("smartcar/#", 1)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(ControlActivity.TAG, "Connection failure") //logcat logging
                    messagesBox.text = "Failed to connect to " + domain+"\n"
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
                    Log.d(ControlActivity.TAG, "Subscribed to $topic")
                    val messagesBox = findViewById<TextView>(R.id.CoordDisplay)
                    messagesBox.text = messagesBox.text.toString() + "Subscribed to $topic\n"
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(ControlActivity.TAG, "Failed to subscribe $topic")
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
                    Log.d(ControlActivity.TAG, "$msg published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(ControlActivity.TAG, "Failed to publish $msg to $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}