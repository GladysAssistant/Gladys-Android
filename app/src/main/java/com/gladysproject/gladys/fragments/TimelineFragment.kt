package com.gladysproject.gladys.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.gladysproject.gladys.R
import com.gladysproject.gladys.adapters.TimelineAdapter
import com.gladysproject.gladys.database.GladysDb
import com.gladysproject.gladys.database.entity.Event
import com.gladysproject.gladys.utils.ConnectivityAPI
import com.gladysproject.gladys.utils.DateTimeUtils.getCurrentDate
import com.gladysproject.gladys.utils.GladysAPI
import com.gladysproject.gladys.utils.SelfSigningClientBuilder
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_timeline.*
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
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

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()

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
                            launch {
                                GladysDb.database?.eventDao()?.deleteEvents()
                                GladysDb.database?.eventDao()?.insertEvents(events)
                            }

                        }else {
                            showSnackBar()
                        }
                    }

                    override fun onFailure(call: Call<MutableList<Event>>, err: Throwable) = runBlocking {

                        launch {
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

        val data = args[0] as JSONObject
        newEvent.datetime = data.getString("datetime")
        newEvent.name = data.getString("name")

        /** Insert new event in database */
        launch {
            GladysDb.database?.eventDao()?.insertEvent(newEvent)
        }

        /** Insert new event in UI */
        activity!!.runOnUiThread {
            events.add(0, newEvent)
            adapter.notifyItemInserted(0)
            timeline_rv.scrollToPosition(0)

            if(events.size == 1) {
                refreshView(events)
                timeline_rv.visibility = View.VISIBLE
                empty_state_message_timeline.visibility = View.INVISIBLE
            }
        }

    }

    fun refreshView(data : List<Event>){
        if(timeline_rv != null){
            timeline_rv.layoutManager = LinearLayoutManager(context)
            adapter = TimelineAdapter(data)
            timeline_rv.adapter = adapter

            activity?.loadingCircle?.visibility = View.INVISIBLE
        }
    }

    fun showEmptyView(){
        if(timeline_rv != null) {
            timeline_rv.visibility = View.INVISIBLE
            activity?.loadingCircle?.visibility = View.INVISIBLE
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
        val id = item!!.itemId
        if (id == R.id.add_button) {
            MaterialDialog(context!!)
                    .title(text = getString(R.string.trigger_an_event))
                    .listItemsSingleChoice(R.array.events) { _, index, _ ->
                        createEvent(resources.getStringArray(R.array.events_code)[index])
                    }
                    .positiveButton(R.string.positve_button)
                    .negativeButton(R.string.negative_button)
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
            } else {
                Snackbar.make(timeline_cl, R.string.error, Snackbar.LENGTH_LONG).show()
            }
        } catch (er: Exception){}
    }
}

