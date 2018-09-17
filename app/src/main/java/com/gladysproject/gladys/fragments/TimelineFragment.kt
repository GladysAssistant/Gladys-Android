package com.gladysproject.gladys.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.afollestad.materialdialogs.MaterialDialog
import com.gladysproject.gladys.R
import com.gladysproject.gladys.adapters.TimelineAdapter
import com.gladysproject.gladys.models.Event
import com.gladysproject.gladys.utils.ConnectivityAPI
import com.gladysproject.gladys.utils.DateTimeUtils.getCurrentDate
import com.gladysproject.gladys.utils.GladysAPI
import com.gladysproject.gladys.utils.SelfSigningClientBuilder
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.fragment_timeline.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class TimelineFragment : Fragment() {

    private var token : String = ""
    private var houseId : String = "1"
    private var userId : String = "1"
    private lateinit var retrofit: Retrofit
    private lateinit var socket : Socket
    private lateinit var events: MutableList<Event>
    private lateinit var adapter: TimelineAdapter

    /** Initialize new event variable */
    private var newEvent : Event = object : Event("", "", "", 1){}

    companion object {
        fun newInstance() = TimelineFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
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

        token = PreferenceManager.getDefaultSharedPreferences(context).getString("token", "")!!
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
                        if(response.code() == 200)
                            events = response.body()!!
                            refreshView(events)
                    }

                    override fun onFailure(call: Call<MutableList<Event>>, err: Throwable) {
                        println(err.message)
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
                    }

                    override fun onFailure(call: Call<Event>, err: Throwable) {
                        println(err.message)
                    }
                })
    }

    private val onNewEvent = Emitter.Listener { args ->
        activity!!.runOnUiThread {
            val data = args[0] as JSONObject

            newEvent.datetime = data.getString("datetime")
            newEvent.name = data.getString("name")

            events.add(0, newEvent)
            adapter.notifyItemInserted(0)
            timeline_rv.scrollToPosition(0)
        }
    }

    fun refreshView(data : List<Event>){
        if(timeline_rv != null){
            timeline_rv.layoutManager = LinearLayoutManager(context)
            adapter = TimelineAdapter(data)
            timeline_rv.adapter = adapter
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

            MaterialDialog.Builder(activity!!)
                    .title(getString(R.string.trigger_an_event))
                    .items(R.array.events)
                    .itemsCallbackSingleChoice(0) { _, _, which, _ ->
                        createEvent(resources.getStringArray(R.array.events_code)[which])
                        true
                    }
                    .positiveText(R.string.positve_button)
                    .negativeText(R.string.negative_button)
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
}

