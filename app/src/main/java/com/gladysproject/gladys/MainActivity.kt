package com.gladysproject.gladys

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.gladysproject.gladys.fragments.ChatFragment
import com.gladysproject.gladys.fragments.HomeFragment
import com.gladysproject.gladys.fragments.TaskFragment
import com.gladysproject.gladys.fragments.TimelineFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

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
            R.id.message -> {
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
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        openFragment(HomeFragment.newInstance())
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
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
}
