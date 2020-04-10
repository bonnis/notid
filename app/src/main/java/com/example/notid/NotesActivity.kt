package com.example.notid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView

class NotesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)
        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = null
        val navController = findNavController(R.id.fragment_container)
        val title:TextView = findViewById(R.id.toolbar_title)
        val drawer : DrawerLayout = findViewById(R.id.drawer_layout)
        val appBarConfiguration = AppBarConfiguration(navController.graph, drawer)
        findViewById<NavigationView>(R.id.nav_view)
            .setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if(destination.label.toString() == "fragment_note"){
                title.text = "My Notes"
            } else if(destination.label.toString() == "fragment_history"){
                title.text = "Access History"
            }
        }

        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_open, R.string.nav_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v("ItemMenuTrigger","Triggered!"+item.itemId)
        val navController = findNavController(R.id.fragment_container)
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }


}
