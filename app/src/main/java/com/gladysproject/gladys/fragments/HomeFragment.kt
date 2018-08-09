package com.gladysproject.gladys.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import com.gladysproject.gladys.R
import com.gladysproject.gladys.adapters.DeviceTypeAdapter
import com.gladysproject.gladys.models.DeviceTypeByRoom
import com.gladysproject.gladys.utils.RetrofitAPI
import com.gladysproject.gladys.utils.SelfSigningClientBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import android.support.v7.widget.SimpleItemAnimator
import android.view.*
import com.gladysproject.gladys.utils.AdapterCallback
import com.gladysproject.gladys.utils.Connectivity
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(), AdapterCallback.AdapterCallbackDeviceState{

    private var token : String = ""
    private lateinit var retrofit: Retrofit

    companion object {
        fun newInstance() = HomeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onStart() {
        super.onStart()

        retrofit = Retrofit.Builder()
                .baseUrl(getConnection()) // The function getConnection return string address
                .addConverterFactory(MoshiConverterFactory.create())
                .client(SelfSigningClientBuilder.unsafeOkHttpClient)
                .build()

        token = PreferenceManager.getDefaultSharedPreferences(context).getString("token", "")!!

        getAllDeviceTypes()
    }

    private fun getAllDeviceTypes(){

        retrofit
                .create(RetrofitAPI::class.java)
                .getDeviceTypeByRoom(token)
                .enqueue(object : Callback<List<DeviceTypeByRoom>> {

                    override fun onResponse(call: Call<List<DeviceTypeByRoom>>, response: Response<List<DeviceTypeByRoom>>) {
                        if(response.code() == 200)refreshView(response.body()!!)
                    }

                    override fun onFailure(call: Call<List<DeviceTypeByRoom>>, err: Throwable) {
                        println(err.message)
                    }
                })
    }


    fun refreshView(data : List<DeviceTypeByRoom>){
        if(home_rv != null){
            home_rv.layoutManager = LinearLayoutManager(context)
            (home_rv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

            val expMgr = RecyclerViewExpandableItemManager(null)
            home_rv.adapter = expMgr.createWrappedAdapter(DeviceTypeAdapter(data, context!!, this))
            expMgr.attachRecyclerView(home_rv)
        }
    }

    override fun onClickCallbackDeviceState(id: Long?, value: Float?) {
        retrofit
                .create(RetrofitAPI::class.java)
                .changeDeviceState(id, value,token)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {}
                    override fun onFailure(call: Call<Void>, err: Throwable) {
                        println(err.message)
                    }
                })
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
        super.onCreateOptionsMenu(menu, inflater)
    }
}
