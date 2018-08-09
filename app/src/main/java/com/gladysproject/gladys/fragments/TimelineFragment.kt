package com.gladysproject.gladys.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.afollestad.materialdialogs.MaterialDialog
import com.gladysproject.gladys.R
import com.gladysproject.gladys.adapters.TimelineAdapter
import com.gladysproject.gladys.models.Event
import com.gladysproject.gladys.utils.Connectivity
import com.gladysproject.gladys.utils.RetrofitAPI
import com.gladysproject.gladys.utils.SelfSigningClientBuilder
import kotlinx.android.synthetic.main.fragment_timeline.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class TimelineFragment : Fragment() {

    private var token : String = ""
    private var house_id : String = "1"
    private var user_id : String = "1"
    private lateinit var retrofit: Retrofit

    companion object {
        fun newInstance() = TimelineFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_timeline, container, false)
    }

    override fun onStart() {
        super.onStart()

        retrofit = Retrofit.Builder()
                .baseUrl(getConnection()) // The function getConnection return string address
                .addConverterFactory(MoshiConverterFactory.create())
                .client(SelfSigningClientBuilder.unsafeOkHttpClient)
                .build()

        token = PreferenceManager.getDefaultSharedPreferences(context).getString("token", "")!!
        user_id = PreferenceManager.getDefaultSharedPreferences(context).getString("user_id", "1")!!
        house_id = PreferenceManager.getDefaultSharedPreferences(context).getString("house_id", "1")!!

        getEvents()
    }

    private fun getEvents(){
        retrofit
                .create(RetrofitAPI::class.java)
                .getEvents(token)
                .enqueue(object : Callback<List<Event>> {

                    override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                        if(response.code() == 200) refreshView(response.body()!!)
                    }

                    override fun onFailure(call: Call<List<Event>>, err: Throwable) {
                        println(err.message)
                    }
                })
    }

    private fun createEvent(event : String){
        retrofit
                .create(RetrofitAPI::class.java)
                .createEvents(event, house_id, user_id, token)
                .enqueue(object : Callback<Event> {
                    override fun onResponse(call: Call<Event>, response: Response<Event>) {
                        if(response.code() == 201) getEvents()
                    }

                    override fun onFailure(call: Call<Event>, err: Throwable) {
                        println(err.message)
                    }
                })
    }

    fun refreshView(data : List<Event>){
        if(timeline_rv != null){
            timeline_rv.layoutManager = LinearLayoutManager(context)
            timeline_rv.adapter = TimelineAdapter(data)
        }
    }

    /*
     Get type of connection
     0 for no connection
     1 for local connection
     2 for external connection

     See Connectivity file in utils folder for more info
    */
    private fun getConnection(): String {
        return when(context?.let { Connectivity.getTypeOfConnection(it) }){
            1 -> Connectivity.getLocalPreferences(context!!)
            2 -> Connectivity.getNatPreferences(context!!)
            else -> "http://fakeurl"
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
}

