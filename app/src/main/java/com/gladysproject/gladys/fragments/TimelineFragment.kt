package com.gladysproject.gladys.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gladysproject.gladys.R
import com.gladysproject.gladys.adapters.TimelineAdapter
import com.gladysproject.gladys.models.Event
import com.gladysproject.gladys.models.User
import com.gladysproject.gladys.utils.Connectivity
import com.gladysproject.gladys.utils.RetrofitAPI
import com.gladysproject.gladys.utils.SelfSigningClientBuilder
import kotlinx.android.synthetic.main.fragment_timeline.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

class TimelineFragment : Fragment() {

    private val token :String = ""
    private val retrofit: Retrofit

    companion object {
        fun newInstance() = TimelineFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_timeline, container, false)


    init {
        retrofit = Retrofit.Builder()
                .baseUrl(getConnection()) // The function getConnection return string address
                .addConverterFactory(MoshiConverterFactory.create())
                .client(SelfSigningClientBuilder.unsafeOkHttpClient)
                .build()

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
        return when(Connectivity.getTypeOfConnection(context!!)){
            1 -> Connectivity.getLocalPreferences(context!!)
            2 -> Connectivity.getNatPreferences(context!!)
            else -> ""
        }
    }
}

