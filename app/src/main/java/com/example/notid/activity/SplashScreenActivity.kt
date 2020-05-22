package com.example.notid.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import com.example.notid.R
import com.example.notid.Util
import com.example.notid.response.InfoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val prefs = Util().getPref(applicationContext)
        val token = prefs.getString("login_token", "")
        if (token.isNullOrEmpty())
        {
            val newIntent = Intent(applicationContext, MainActivity::class.java)
            startActivity(newIntent)
            finish()
        } else {
            Util().retrofitRequest(applicationContext).getInfo().enqueue(object :
                Callback<InfoResponse> {
                override fun onResponse(
                    call: Call<InfoResponse>,
                    response: Response<InfoResponse>
                ) {
                    if (!response.isSuccessful) {
                        var message = if (response.code()==401) "Login Failed" else "Unknown error"
                        Toast.makeText(
                            applicationContext,
                            (message),
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d("NError",response.code().toString())
                        val intent = Intent(applicationContext,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        return
                    }
                    val intent = Intent(applicationContext, NotesActivity::class.java).apply {
                        putExtra(
                            "com.example.notid.FIRSTNAME",
                            response.body()!!.user!!.first_name
                        )
                        putExtra(
                            "com.example.notid.LASTNAME",
                            response.body()!!.user!!.last_name
                        )
                        putExtra("com.example.notid.EMAIL", response.body()!!.user!!.email)
                    }
                    startActivity(intent)
                    finish()
                }

                override fun onFailure(call: Call<InfoResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "Network error", Toast.LENGTH_LONG)
                        .show()
                    Log.d("NError", t.message)
                    val intent = Intent(applicationContext,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            })
        }
    }
}
