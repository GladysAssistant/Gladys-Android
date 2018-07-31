package com.gladysproject.gladys

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
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
}
