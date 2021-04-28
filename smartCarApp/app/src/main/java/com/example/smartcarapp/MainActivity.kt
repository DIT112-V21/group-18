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
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputLayout
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

//Main Activity
//Potential todo: create class for MQTT client to function between multiple activities
//Temporary solution: MQTT client initialised on each activity (for now only active on ControlActivity)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val connectButton = findViewById<Button>(R.id.testButton)
        val publishMsgButton = findViewById<Button>(R.id.publishMsgButton)
        val publishMsgText = findViewById<EditText>(R.id.publishMsgText)

        val domainInput = findViewById<EditText>(R.id.domainInput)
        connectButton.setOnClickListener {
            val intent = Intent(this, ControlActivity::class.java)
            intent.putExtra("domainInput", domainInput.text.toString())
            startActivity(intent)
        }
        publishMsgButton.setOnClickListener {
            //val newMsg = publishMsgText.text.toString()
            //publish("smartcar/", newMsg)
            //publishMsgText.setText("")
        }
        //Old debug screen with manual MQTT message sending:
        /*connectButton.setOnClickListener {
            connect(this);
        }
        publishMsgButton.setOnClickListener {
            val newMsg = publishMsgText.text.toString()
            publish("smartcar/", newMsg)
            publishMsgText.setText("")
        }*/
    }
    private lateinit var mqttClient: MqttAndroidClient
    // TAG
    companion object {
        const val TAG = "AndroidMqttClient"
    }


    fun connect(context: Context) { //Connect to domain and subscribe to topic automatically
        val domainInput = findViewById<EditText>(R.id.domainInput)
        val messagesBox = findViewById<TextView>(R.id.messagesBox)
        val serverURI = "tcp://"+domainInput.text.toString()+":1883" //Change url to tcp and use port 1883 automatically for now
        mqttClient = MqttAndroidClient(context, serverURI, "kotlin_client")
        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(TAG, "Receive message: ${message.toString()} from topic: $topic") //logcat logging
                messagesBox.text = messagesBox.text.toString() + "Receive message: ${message.toString()} from topic: $topic\n"
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })
        val options = MqttConnectOptions()
        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success") //logcat logging
                    messagesBox.text = "Connected to domain: " + domainInput.text + "\n"
                    findViewById<TextView>(R.id.publishMsgText).setVisibility(View.VISIBLE) //Set the publish mqtt message controls to visible
                    findViewById<TextView>(R.id.publishMsgButton).setVisibility(View.VISIBLE)
                    subscribe("smartcar/#", 1)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Connection failure") //logcat logging
                    messagesBox.text = "Failed to connect to " + domainInput.text+"\n"
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
                    Log.d(TAG, "Subscribed to $topic")
                    val messagesBox = findViewById<TextView>(R.id.messagesBox)
                    messagesBox.text = messagesBox.text.toString() + "Subscribed to $topic\n"
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to subscribe $topic")
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
                    Log.d(TAG, "$msg published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to publish $msg to $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}