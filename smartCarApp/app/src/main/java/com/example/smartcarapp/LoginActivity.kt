package com.example.smartcarapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

//Login Activity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val errorMsg = intent.getStringExtra("errorMsg") //Could be null, meant for returning to login screen after error.
        val connectButton = findViewById<Button>(R.id.connectButton)
        val domainInput = findViewById<EditText>(R.id.domainInput)
        connectButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("domainInput", domainInput.text.toString())
            startActivity(intent)
        }
    }

}