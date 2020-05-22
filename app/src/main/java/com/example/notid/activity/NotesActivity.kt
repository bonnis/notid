package com.example.notid.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.notid.MyFirebaseMessagingService
import com.example.notid.R
import com.example.notid.Util
import com.example.notid.fragment.CreationDialogFragment
import com.example.notid.fragment.FindNoteDialogFragment
import com.example.notid.request.FCMTokenRequest
import com.example.notid.response.InfoResponse
import com.example.notid.response.MessageResponse
import com.google.android.material.navigation.NavigationView
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import kotlinx.android.synthetic.main.activity_notes.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NotesActivity : AppCompatActivity() {
    private lateinit var sharedPrefs :SharedPreferences
    var token:String?=null
    var fullname : TextView? = null
    var email : TextView? = null
    var navView : NavigationView? = null
    var fcm_token : String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)
        sharedPrefs = Util().getPref(applicationContext)
        token = sharedPrefs?.getString("login_token",null)
        val drawer : DrawerLayout = findViewById(R.id.drawer_layout)

        navView = drawer.findViewById(R.id.nav_view)!!
        val header : View = navView!!.getHeaderView(0)
        fullname = header.findViewById(R.id.full_name)
        email = header.findViewById(R.id.user_email)

        val toolbar : Toolbar = findViewById(R.id.toolbar)
        var thisIntent:Intent= intent
        val placeholder =thisIntent.getStringExtra("com.example.notid.FIRSTNAME")+" "+thisIntent.getStringExtra("com.example.notid.LASTNAME")
        fullname!!.text= placeholder
        email!!.text=thisIntent.getStringExtra("com.example.notid.EMAIL")

        val submenu = navView!!.menu.get(3).subMenu
        val logout = submenu!!.get(0)
        val openNote = submenu!!.get(1)
        logout.setOnMenuItemClickListener { logout() }
        openNote.setOnMenuItemClickListener { openNote() }
        new_note_btn.setOnClickListener{ createNote() }

        setSupportActionBar(toolbar)

        toolbar.title = null
        val navController = findNavController(R.id.fragment_container)
        val title:TextView = findViewById(R.id.toolbar_title)
        val appBarConfiguration = AppBarConfiguration(navController.graph, drawer)
//        toolbar.setupWithNavController(navController)
        findViewById<NavigationView>(R.id.nav_view).setupWithNavController(navController)


        navController.addOnDestinationChangedListener { _, destination, _ ->
            when {
                destination.label.toString() == "fragment_note" -> {
                    title.text = "My Notes"
                    new_note_btn.visibility=View.VISIBLE
                }
                destination.label.toString() == "fragment_history" -> {
                    title.text = "Access History"
                    new_note_btn.visibility=View.INVISIBLE
                }
                destination.label.toString() == "fragment_profile" -> {
                    title.text = "Your Profile"
                    new_note_btn.visibility=View.INVISIBLE
                }
            }
        }

        val toggle = ActionBarDrawerToggle(this, drawer, toolbar,
            R.string.nav_open,
            R.string.nav_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        fcm_token = MyFirebaseMessagingService.getToken(applicationContext)
    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        Log.d("Item_MENU","Triggered! "+item.itemId)
//        val navController = findNavController(R.id.fragment_container)
//        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
//    }

    fun logout():Boolean{
        findViewById<FrameLayout>(R.id.progress_overlay).visibility=View.VISIBLE
        Util().retrofitRequest(applicationContext).logout().enqueue(object:Callback<MessageResponse>{
            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                Toast.makeText(applicationContext,"Network error",Toast.LENGTH_LONG).show()
                findViewById<FrameLayout>(R.id.progress_overlay).visibility=View.INVISIBLE

            }

            override fun onResponse(
                call: Call<MessageResponse>,
                response: Response<MessageResponse>
            ) {
                if (response.isSuccessful)
                {
                    Toast.makeText(applicationContext,"Logged out", Toast.LENGTH_LONG).show()
                    findViewById<FrameLayout>(R.id.progress_overlay).visibility=View.INVISIBLE
                    val newIntent = Intent(applicationContext, MainActivity::class.java)
                    sharedPrefs.edit().apply {
                        remove("login_token")
                        remove("user_id")
                        commit()
                    }
                    startActivity(newIntent)
                    finish()
                }
                else
                {
                    Toast.makeText(applicationContext, response.errorBody().toString(), Toast.LENGTH_LONG).show()
                    findViewById<FrameLayout>(R.id.progress_overlay).visibility=View.INVISIBLE
                }
            }
        })


        return true
    }

    override fun onStart() {
        super.onStart()
        Util().retrofitRequest(applicationContext).getInfo().enqueue( object : Callback<InfoResponse>{
            override fun onResponse(call: Call<InfoResponse>, response: Response<InfoResponse>) {
                if (response.isSuccessful){
                    val user = response.body()!!.user!!
                    email!!.text = user.email.toString()
                    fullname!!.text = user.first_name.toString() + ' ' + user.last_name.toString()
                    sharedPrefs!!.edit().apply{
                        putInt("user_id",response.body()?.user?.id?.toInt()?:0)
                        commit()
                    }
                    Util().retrofitRequest(applicationContext).register_fcm(FCMTokenRequest(fcm_token!!)).enqueue(object : Callback<MessageResponse>{
                        override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                            Util().networkError(applicationContext)
                        }

                        override fun onResponse(
                            call: Call<MessageResponse>,
                            response: Response<MessageResponse>
                        ) {
                            Log.d("FCM", "OK")
                        }
                    })
                }
                else
                {
                    if (response.code()==401||response.code()==405)
                    {
                        val newIntent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(newIntent)
                        finish()
                    } else{
                        Toast.makeText(applicationContext,"Unknown error", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<InfoResponse>, t: Throwable) {
                Toast.makeText(applicationContext,"Network Error", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun openNote() : Boolean
    {
        val dialog = FindNoteDialogFragment()
        dialog.show(supportFragmentManager,"open note")
        return true
    }

    private fun createNote(): Boolean{
        val dialog = CreationDialogFragment()
        dialog.show(supportFragmentManager,"create note")
        return true
    }


}
