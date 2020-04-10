package com.example.notid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import com.facebook.appevents.codeless.internal.ViewHierarchy.setOnClickListener

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        //this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        val loginBtn: Button = findViewById(R.id.login_btn)
        loginBtn.setOnClickListener { v -> login(v) }
        val regBtn: Button = findViewById(R.id.regBtn)
        regBtn.setOnClickListener { v ->
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
         }
    }

    private fun login(view: View){
        val intent = Intent(this, NotesActivity::class.java).apply {
            putExtra("com.example.main.Sample", "Wacemeh")
        }
        startActivity(intent)
    }


}
