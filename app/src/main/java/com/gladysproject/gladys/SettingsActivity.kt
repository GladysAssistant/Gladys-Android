@file:Suppress("DEPRECATION")

package com.gladysproject.gladys

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.preference.Preference.OnPreferenceClickListener
import android.app.Fragment
import android.preference.*

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
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                val index = preference.findIndexOfValue(stringValue)

                // Set the summary to reflect the new value.
                preference.setSummary(
                        if (index >= 0)
                            preference.entries[index]
                        else
                            null)

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.summary = stringValue
            }
            true
        }

        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, ""))
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
            val mqtt = findPreference("mqtt_settings") as Preference
            //val notification = findPreference("notification_settings") as Preference
            //val geolocation = findPreference("geolocation_settings") as Preference
            val openSourceLicence = findPreference("open_source_licence_list") as Preference

            http.onPreferenceClickListener = OnPreferenceClickListener {
                openFragment(HttpSettingsFragment())
                true
            }

            mqtt.onPreferenceClickListener = OnPreferenceClickListener {
                openFragment(MqttSettingsFragment())
                true
            }

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
