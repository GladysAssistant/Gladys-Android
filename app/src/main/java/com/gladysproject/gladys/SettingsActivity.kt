@file:Suppress("DEPRECATION")

package com.gladysproject.gladys

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.preference.Preference.OnPreferenceClickListener
import android.app.Fragment
import android.preference.*
import android.support.v7.content.res.AppCompatResources

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.let {supportActionBar!!.setDisplayHomeAsUpEnabled(true)}

        if (fragmentManager.findFragmentById(android.R.id.content) == null) {
            fragmentManager.beginTransaction()
                    .add(android.R.id.content, GeneralSettingsFragment()).commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return false
    }

    companion object {

        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->

            val stringValue = value.toString()

            if (preference is ListPreference) {
                val index = preference.findIndexOfValue(stringValue)
                preference.setSummary(
                        if (index >= 0)
                            preference.entries[index]
                        else
                            null)

            } else {
                if(preference.key == "token" || preference.key == "mqtt_user_password") preference.summary = replaceSumary(stringValue)
                else preference.summary = stringValue
            }
            true
        }

        private fun bindPreferenceSummaryToValue(preference: Preference) {
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, ""))
        }

        private fun replaceSumary(value: String) : String {
            val re = Regex("[A-Za-z0-9!@#\$%^&*(),.?:{}|<> ]")
            return re.replace(value,"*")
        }
    }

    class GeneralSettingsFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.general_settings)

            bindPreferenceSummaryToValue(findPreference("user_name"))
            bindPreferenceSummaryToValue(findPreference("user_firstname"))
            bindPreferenceSummaryToValue(findPreference("user_id"))
            bindPreferenceSummaryToValue(findPreference("house_id"))

            val http = findPreference("http_settings") as Preference
            //val mqtt = findPreference("mqtt_settings") as Preference
            //val notification = findPreference("notification_settings") as Preference
            //val geolocation = findPreference("geolocation_settings") as Preference
            val openSourceLicence = findPreference("open_source_licence_list") as Preference

            http.onPreferenceClickListener = OnPreferenceClickListener {
                openFragment(HttpSettingsFragment())
                true
            }

            //mqtt.onPreferenceClickListener = OnPreferenceClickListener {
            //    openFragment(MqttSettingsFragment())
            //    true
            //}

            //notification.onPreferenceClickListener = OnPreferenceClickListener {
            //    openFragment(NotificationSettingsFragment())
            //    true
            //}

            //geolocation.onPreferenceClickListener = OnPreferenceClickListener {
            //    openFragment(GeolocationSettingsFragment())
            //    true
            //}

            openSourceLicence.onPreferenceClickListener = OnPreferenceClickListener {
                openFragment(OpenSourceLicenceFragment())
                true
            }
        }

        private fun openFragment(fragment: Fragment) {
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(android.R.id.content, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    class HttpSettingsFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.http_settings)

            bindPreferenceSummaryToValue(findPreference("local_ip"))
            bindPreferenceSummaryToValue(findPreference("local_port"))
            bindPreferenceSummaryToValue(findPreference("token"))
            bindPreferenceSummaryToValue(findPreference("dns"))
            bindPreferenceSummaryToValue(findPreference("nat_port"))
        }
    }

    class MqttSettingsFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.mqtt_settings)

            bindPreferenceSummaryToValue(findPreference("mqtt_host"))
            bindPreferenceSummaryToValue(findPreference("mqtt_port"))
            bindPreferenceSummaryToValue(findPreference("mqtt_user"))
            bindPreferenceSummaryToValue(findPreference("mqtt_user_password"))
        }
    }

    class NotificationSettingsFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.notification_settings)
        }
    }

    class GeolocationSettingsFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.geolocation_settings)
        }
    }

    class OpenSourceLicenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.open_source_licence_list)
        }
    }

}
