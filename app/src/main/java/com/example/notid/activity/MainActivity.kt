package com.example.notid.activity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import com.example.notid.R
import com.example.notid.Util
import com.example.notid.request.Credentials
import com.example.notid.response.InfoResponse
import com.example.notid.response.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private var email: EditText? = null
    private var password: EditText? = null
    private var sharedPrefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefs = Util().getPref(applicationContext)
        if(sharedPrefs!!.getString("login_token","0")!="0"){

        }
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        //this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        var loginBtn: Button = findViewById(R.id.login_btn)
        email = findViewById(R.id.email)
        password = findViewById(R.id.pass)
        loginBtn.setOnClickListener { v -> login(v) }
        var regBtn: Button = findViewById(R.id.regBtn)
        regBtn.setOnClickListener { v ->
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(view: View) {
        val credentials = Credentials(
            email!!.text.toString(),
            password!!.text.toString()
        )
        findViewById<FrameLayout>(R.id.progress_overlay).visibility=View.VISIBLE
        Util().retrofitRequest(applicationContext)
            .login(credentials).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (!response.isSuccessful) {
                    var message = if (response.code()==400) "Login Failed" else "Unknown error"
                    Toast.makeText(
                        applicationContext,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                    findViewById<FrameLayout>(R.id.progress_overlay).visibility=View.INVISIBLE
                    return
                }
                sharedPrefs!!.edit().apply {
                    putString("login_token", response.body()!!.token)
                    commit()
                }
//                Toast.makeText(
//                    applicationContext,
//                    "Response : " + response.code().toString() + " " + (response.body()!!.token),
//                    Toast.LENGTH_LONG
//                ).show()
                Util().retrofitRequest(applicationContext).getInfo().enqueue(object : Callback<InfoResponse> {
                    override fun onResponse(
                        call: Call<InfoResponse>,
                        response: Response<InfoResponse>
                    ) {
                        if (!response.isSuccessful) {
                            var message = if (response.code()==400) "Login Failed" else "Unknown error"
                            Toast.makeText(
                                applicationContext,
                                (message),
                                Toast.LENGTH_LONG
                            ).show()
                            findViewById<FrameLayout>(R.id.progress_overlay).visibility=View.INVISIBLE
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
                        sharedPrefs!!.edit().apply{
                            putInt("user_id",response.body()?.user?.id?.toInt()?:0)
                            commit()
                        }
                        startActivity(intent)
                        finish()
                    }

                    override fun onFailure(call: Call<InfoResponse>, t: Throwable) {
                        Toast.makeText(applicationContext, "Network error", Toast.LENGTH_LONG)
                            .show()
                        Log.d("NError", t.message)
                        findViewById<FrameLayout>(R.id.progress_overlay).visibility=View.INVISIBLE

                    }
                })
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "Network error", Toast.LENGTH_LONG).show()
                Log.d("NError", t.message)
                findViewById<FrameLayout>(R.id.progress_overlay).visibility=View.INVISIBLE

            }

        })
    }


}
