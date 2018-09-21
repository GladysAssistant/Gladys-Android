package com.gladysproject.gladys

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.Menu
import android.view.MenuItem
import com.gladysproject.gladys.database.GladysDb
import com.gladysproject.gladys.fragments.ChatFragment
import com.gladysproject.gladys.fragments.HomeFragment
import com.gladysproject.gladys.fragments.TaskFragment
import com.gladysproject.gladys.fragments.TimelineFragment
import com.gladysproject.gladys.services.MqttService
import com.gladysproject.gladys.utils.ConnectivityAPI
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var socket: Socket
    private val startChat = "com.gladysproject.gladys.startChat"

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.home -> {
                openFragment(HomeFragment.newInstance())
                return@OnNavigationItemSelectedListener true
            }
            R.id.timeline -> {
                openFragment(TimelineFragment.newInstance())
                return@OnNavigationItemSelectedListener true
            }
            R.id.chat -> {
                openFragment(ChatFragment.newInstance())
                return@OnNavigationItemSelectedListener true
            }
            R.id.task -> {
                openFragment(TaskFragment.newInstance())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        GladysDb.initializeDatabase(this)
        connectSocket()
        startMqttService()

        if (startChat == intent.action){
            openFragment(ChatFragment.newInstance())
            bottom_navigation.selectedItemId = R.id.chat
        } else {
            openFragment(HomeFragment.newInstance())
        }

    }

    override fun onBackPressed() {
        val fragments = supportFragmentManager.backStackEntryCount
        if (fragments == 1) {
            finish()
        } else {
            if (fragmentManager.backStackEntryCount > 1) {
                fragmentManager.popBackStack()
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack("$fragment")
        transaction.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.settings_button -> {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun connectSocket(){
        try {
            socket = ConnectivityAPI.Companion.WebSocket.getInstance(this)!!
            socket.emit("post", JSONObject().put("url", "/socket/subscribe?token=${PreferenceManager.getDefaultSharedPreferences(this).getString("token", "")!!}"))
            socket.connect()
        } catch (er: Exception){}
    }

    private fun startMqttService() {
        try {
            val intent = Intent(application, MqttService::class.java)
            startService(intent)
        } catch (er: Exception){}
    }
}
