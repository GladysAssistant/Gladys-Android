package com.gladysassistant.gladys

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.gladysassistant.gladys.database.GladysDb
import com.gladysassistant.gladys.fragments.ChatFragment
import com.gladysassistant.gladys.fragments.HomeFragment
import com.gladysassistant.gladys.fragments.TimelineFragment
import com.gladysassistant.gladys.services.MqttService
import com.gladysassistant.gladys.utils.ConnectivityAPI
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

@Suppress("DEPRECATION")
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
            //R.id.task -> {
            //    openFragment(TaskFragment.newInstance())
            //    return@OnNavigationItemSelectedListener true
            //}
        }
        false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        GladysDb.initializeDatabase(this)

        if (startChat == intent.action){
            openFragment(ChatFragment.newInstance())
            bottom_navigation.selectedItemId = R.id.chat
        } else {
            openFragment(HomeFragment.newInstance())
        }
    }

    override fun onResume() {
        super.onResume()
        connectSocket()
        //startMqttService()
    }

    override fun onPause() {
        super.onPause()
        socket.disconnect()
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
        // if it is a first run, we invite the user to go to the settings
        Handler().post {if(!ConnectivityAPI.isPreferencesSet(this)) showConfigPrompt() }
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

    private fun showConfigPrompt() {
            MaterialTapTargetPrompt.Builder(this)
                    .setTarget(R.id.settings_button)
                    .setPrimaryText(resources.getString(R.string.prompt_Title))
                    .setSecondaryText(resources.getString(R.string.prompt_message))
                    .setBackgroundColour(resources.getColor(R.color.transparentPrimaryDarkColor))
                    .setFocalColour(resources.getColor(R.color.transparentPrimaryColor))
                    .show()
    }

    private fun connectSocket(){
        try {
            socket = ConnectivityAPI.Companion.WebSocket.getInstance(this)!!
            socket.emit("post", JSONObject().put("url", "/socket/subscribe?token=${PreferenceManager.getDefaultSharedPreferences(this).getString("token", "")!!}"))
            socket.connect()

        } catch (er: Exception){ }

    }

    private fun startMqttService() {
        try {
            val intent = Intent(application, MqttService::class.java)
            startService(intent)
        } catch (er: Exception){}
    }
}
