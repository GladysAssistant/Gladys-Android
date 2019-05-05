package com.gladysassistant.gladys.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.gladysassistant.gladys.R
import com.gladysassistant.gladys.adapters.TimelineAdapter
import com.gladysassistant.gladys.database.GladysDb
import com.gladysassistant.gladys.database.entity.Event
import com.gladysassistant.gladys.utils.ConnectivityAPI
import com.gladysassistant.gladys.utils.DateTimeUtils.getCurrentDate
import com.gladysassistant.gladys.utils.GladysAPI
import com.gladysassistant.gladys.utils.SelfSigningClientBuilder
import com.google.android.material.snackbar.Snackbar
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_timeline.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.Exception

class TimelineFragment : Fragment() {

    private var token : String = ""
    private var houseId : String = "1"
    private var userId : String = "1"
    private lateinit var retrofit: Retrofit
    private lateinit var socket : Socket
    private var events = mutableListOf<Event>()
    private lateinit var adapter: TimelineAdapter
    private var eventsTypeName = listOf<String>()
    private var eventsTypeCode = listOf<String>()
    private lateinit var eventsType: MutableList<Event>

    /** Initialize new event variable */
    private var newEvent : Event = object : Event("", "", "", 1){}

    companion object {
        fun newInstance() = TimelineFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        activity?.loadingCircle?.visibility = View.VISIBLE
        return inflater.inflate(R.layout.fragment_timeline, container, false)
    }

    override fun onStart() {
        super.onStart()
        init()
    }

    @SuppressLint("SetTextI18n")
    fun init(){
        retrofit = Retrofit.Builder()
                .baseUrl(ConnectivityAPI.getUrl(context!!)) /** The function getUrl return string address */
                .addConverterFactory(MoshiConverterFactory.create())
                .client(SelfSigningClientBuilder.unsafeOkHttpClient)
                .build()

        token = PreferenceManager.getDefaultSharedPreferences(context).getString("token", "rgdffg")!!
        userId = PreferenceManager.getDefaultSharedPreferences(context).getString("user_id", "1")!!
        houseId = PreferenceManager.getDefaultSharedPreferences(context).getString("house_id", "1")!!
        user_name.text = PreferenceManager.getDefaultSharedPreferences(context).getString("user_firstname", "John")!! + " " + PreferenceManager.getDefaultSharedPreferences(context).getString("user_name", "Pepperwood")!!
        datetime.text = getCurrentDate()

        getEvents()
        getEventsType()

        socket = ConnectivityAPI.Companion.WebSocket.getInstance(context!!)!!
        socket.on("newEvent", onNewEvent)
    }

    private fun getEvents(){
        retrofit
                .create(GladysAPI::class.java)
                .getEvents(token)
                .enqueue(object : Callback<MutableList<Event>> {

                    override fun onResponse(call: Call<MutableList<Event>>, response: Response<MutableList<Event>>) {
                        if(response.code() == 200) {
                            events = response.body()!!

                            if(events.isNotEmpty()) refreshView(events)
                            else showEmptyView()

                            /** Insert events in database */
                            GlobalScope.launch {
                                GladysDb.database?.eventDao()?.deleteEvents()
                                GladysDb.database?.eventDao()?.insertEvents(events)
                            }

                        }else {
                            showSnackBar()
                        }
                    }

                    override fun onFailure(call: Call<MutableList<Event>>, err: Throwable) = runBlocking {

                        GlobalScope.launch {
                            events = GladysDb.database?.eventDao()?.getAllEvents()!!
                        }.join()

                        if(events.isNotEmpty()) refreshView(events)
                        else showEmptyView()

                        showSnackBar()

                    }
                })
    }

    private fun createEvent(event : String){
        retrofit
                .create(GladysAPI::class.java)
                .createEvents(event, houseId, userId, token)
                .enqueue(object : Callback<Event> {
                    override fun onResponse(call: Call<Event>, response: Response<Event>) {
                        /** The new event is captured by onNewEvent function by websocket connection */
                        if(response.code() != 201) showSnackBar()
                    }

                    override fun onFailure(call: Call<Event>, err: Throwable) {
                        showSnackBar()
                    }
                })
    }

    private val onNewEvent = Emitter.Listener { args ->
        runBlocking {

            val data = args[0] as JSONObject

            newEvent.datetime = data.getString("datetime")
            newEvent.name = data.getString("name")

            /** Insert new event in database */
            GlobalScope.launch {
                GladysDb.database?.eventDao()?.insertEvent(newEvent)
            }.join()

            /** Get list with new event
             * This is dirty sorry but it solve a bug of duplicate events in UI
             **/
            GlobalScope.launch {
                events = GladysDb.database?.eventDao()?.getAllEvents()!!
            }.join()

            /** Insert new event in UI */
            activity!!.runOnUiThread {
                if (::adapter.isInitialized) {
                    refreshView(events)
                }

                if (events.size == 1) {
                    refreshView(events)
                    timeline_appbar.visibility = View.VISIBLE
                    timeline_rv.visibility = View.VISIBLE
                    empty_state_img_timeline.visibility = View.INVISIBLE
                    empty_state_message_timeline.visibility = View.INVISIBLE
                }
            }

        }
    }


    private fun getEventsType(){
        retrofit
                .create(GladysAPI::class.java)
                .getEventsType(token)
                .enqueue(object : Callback<MutableList<Event>> {
                    override fun onResponse(call: Call<MutableList<Event>>, response: Response<MutableList<Event>>) {
                        if(response.code() == 200) {
                            eventsType = response.body()!!
                            GlobalScope.launch {
                                for (eventType in eventsType){
                                    eventsTypeCode += eventType.code!!
                                    eventsTypeName += eventType.name!!
                                }
                            }
                        }
                    }
                    override fun onFailure(call: Call<MutableList<Event>>, err: Throwable) {}
                })
    }

    fun refreshView(data : List<Event>){
        if(timeline_rv != null){
            timeline_appbar.visibility = View.VISIBLE
            timeline_rv.layoutManager = LinearLayoutManager(context)
            adapter = TimelineAdapter(data)
            timeline_rv.adapter = adapter

            activity?.loadingCircle?.visibility = View.INVISIBLE
        }
    }

    fun showEmptyView(){
        if(timeline_rv != null) {
            timeline_appbar.visibility = View.INVISIBLE
            timeline_rv.visibility = View.INVISIBLE
            activity?.loadingCircle?.visibility = View.INVISIBLE
            empty_state_img_timeline.visibility = View.VISIBLE
            empty_state_message_timeline.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.toolbar_menu, menu)
        menu.findItem(R.id.add_button).isVisible = true
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var eventTypeIndex = 0
        val id = item!!.itemId
        if (id == R.id.add_button) {
            MaterialDialog(context!!)
                    .title(text = getString(R.string.trigger_an_event))
                    .listItemsSingleChoice(items = eventsTypeName, waitForPositiveButton = false) { dialog, index, _ ->
                        eventTypeIndex = index
                        dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                    }
                    .positiveButton(R.string.validate){
                        createEvent(eventsTypeCode[eventTypeIndex])
                    }
                    .negativeButton(R.string.cancel)
                    .show()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()

        /** Remove listener on new event when the fragment is paused */
        socket.off("newEvent", onNewEvent)
    }

    override fun onResume() {
        super.onResume()
        if(activity?.bottom_navigation?.selectedItemId != R.id.timeline)activity?.bottom_navigation?.selectedItemId = R.id.timeline
    }

    fun showSnackBar(){
        try {
            if (ConnectivityAPI.getUrl(this@TimelineFragment.context!!) == "http://noconnection") {
                Snackbar.make(timeline_cl, R.string.no_connection, Snackbar.LENGTH_LONG).show()
            } else if (ConnectivityAPI.isPreferencesSet(this@TimelineFragment.context!!)) {
                Snackbar.make(timeline_cl, R.string.error, Snackbar.LENGTH_LONG).show()
            }
        } catch (er: Exception){}
    }
}

