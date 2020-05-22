package com.example.notid.activity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.notid.R
import com.example.notid.Util
import com.example.notid.request.Credentials
import com.example.notid.request.Register
import com.example.notid.response.InfoResponse
import com.example.notid.response.LoginResponse
import com.example.notid.response.MessageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    lateinit var submitButton:Button
    lateinit var emailText: EditText
    lateinit var firstNameText: EditText
    lateinit var lastNameText: EditText
    lateinit var passwordText: EditText
    lateinit var confirmPasswordText: EditText
    private var sharedPrefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        sharedPrefs = Util().getPref(applicationContext)
        submitButton = findViewById(R.id.register_submitBtn)
        emailText = findViewById(R.id.emailTxt)
        firstNameText = findViewById(R.id.firstNameText)
        lastNameText = findViewById(R.id.lastNameText)
        passwordText = findViewById(R.id.passwordTxt)
        confirmPasswordText = findViewById(R.id.confPasswordTxt)
        submitButton.setOnClickListener { v -> registerSubmit(v) }
    }

    private fun registerSubmit(v:View){
        if (validate()){
            val regdata = Register(password=passwordText.text.toString(), first_name = firstNameText.text.toString(), last_name = lastNameText.text.toString(), email = emailText.text.toString())
            Util().retrofitRequest(applicationContext).register(regdata).enqueue( object : Callback<MessageResponse>{
                override fun onResponse(
                    call: Call<MessageResponse>,
                    response: Response<MessageResponse>
                ) {
                    if(response.isSuccessful){
                        Toast.makeText(applicationContext,response.body()!!.message.toString(),Toast.LENGTH_LONG)
                        login()
                        finish()
                    }
                    else
                    {
                        Toast.makeText(applicationContext,response.errorBody().toString(),Toast.LENGTH_LONG)
                    }
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    Toast.makeText(applicationContext,"Network error", Toast.LENGTH_LONG)
                }
            } )
        }
    }

    private fun validate(): Boolean {
        if(emailText.text.isNullOrEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailText.text).matches()){
            Toast.makeText(applicationContext, "Email is not valid", Toast.LENGTH_LONG)
            return false
        }

        if(firstNameText.text.isNullOrEmpty()){
            Toast.makeText(applicationContext, "First name must be filled", Toast.LENGTH_LONG)
            return false
        }

        if(passwordText.text.isNullOrEmpty()){
            Toast.makeText(applicationContext,"Password is required", Toast.LENGTH_LONG)
            return false
        }

        if(confirmPasswordText.text.isNullOrEmpty()){
            Toast.makeText(applicationContext,"Repeat your password one more time", Toast.LENGTH_LONG)
            return false
        }

        if(confirmPasswordText!=passwordText){
            Toast.makeText(applicationContext, "The passwords don't match", Toast.LENGTH_LONG)
            return false
        }

        return true
    }

    private fun login() {
        val credentials = Credentials(
            emailText!!.text.toString(),
            passwordText!!.text.toString()
        )
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
                        return
                    }
                    sharedPrefs!!.edit().apply {
                        putString("login_token", response.body()!!.token)
                        commit()
                    }
                    Toast.makeText(
                        applicationContext,
                        "Response : " + response.code().toString() + " " + (response.body()!!.token),
                        Toast.LENGTH_LONG
                    ).show()
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
                        }
                    })
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "Network error", Toast.LENGTH_LONG).show()
                    Log.d("NError", t.message)

                }

            })
    }

}
