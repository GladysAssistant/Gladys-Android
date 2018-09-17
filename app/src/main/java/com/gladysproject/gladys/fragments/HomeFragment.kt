package com.gladysproject.gladys.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.*
import com.gladysproject.gladys.R
import com.gladysproject.gladys.adapters.DeviceTypeAdapter
import com.gladysproject.gladys.database.entity.DeviceTypeByRoom
import com.gladysproject.gladys.utils.AdapterCallback
import com.gladysproject.gladys.utils.ConnectivityAPI
import com.gladysproject.gladys.utils.GladysAPI
import com.gladysproject.gladys.utils.SelfSigningClientBuilder
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class HomeFragment : Fragment(), AdapterCallback.AdapterCallbackDeviceState{

    private var token : String = ""
    private lateinit var retrofit: Retrofit
    private lateinit var socket : Socket
    private lateinit var deviceTypeByRoom: List<DeviceTypeByRoom>
    private lateinit var adapter : DeviceTypeAdapter
    private var isNotGladysDeviceState : Boolean = false

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
                .baseUrl(ConnectivityAPI.getUrl(context!!)) /** The function getUrl return string address */
                .addConverterFactory(MoshiConverterFactory.create())
                .client(SelfSigningClientBuilder.unsafeOkHttpClient)
                .build()

        token = PreferenceManager.getDefaultSharedPreferences(context).getString("token", "")!!

        getAllDeviceTypes()

        socket = ConnectivityAPI.Companion.WebSocket.getInstance(context!!)!!
        socket.on("newDeviceState", onNewDeviceState)
    }

    private fun getAllDeviceTypes(){

        retrofit
                .create(GladysAPI::class.java)
                .getDeviceTypeByRoom(token)
                .enqueue(object : Callback<List<DeviceTypeByRoom>> {

                    override fun onResponse(call: Call<List<DeviceTypeByRoom>>, response: Response<List<DeviceTypeByRoom>>) = runBlocking{
                        if(response.code() == 200){

                            launch {
                                for(room in response.body()!!){
                                    room.deviceTypes = room.deviceTypes.filterIndexed{_, it -> it.display != 0.toLong() }.toMutableList()
                                }
                            }.join()

                            deviceTypeByRoom = response.body()!!
                            refreshView(deviceTypeByRoom)

                        }
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

            adapter = DeviceTypeAdapter(data, context!!, this)
            val expMgr = RecyclerViewExpandableItemManager(null)
            home_rv.adapter = expMgr.createWrappedAdapter(adapter)
            expMgr.attachRecyclerView(home_rv)
        }
    }

    override fun onClickCallbackDeviceState(id: Long?, value: Float?) {
        retrofit
                .create(GladysAPI::class.java)
                .changeDeviceState(id, value,token)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        /** The new device state is captured by onNewDeviceState function by websocket connection
                         *  But we put the variable a true for can not create a loop between the cursor/switch listeners and event flicker by the socket
                         *  See the onNewDeviceState function for more comprehension
                         * */
                        isNotGladysDeviceState = true
                    }
                    override fun onFailure(call: Call<Void>, err: Throwable) {
                        println(err.message)
                    }
                })
    }

    private val onNewDeviceState = Emitter.Listener { args ->
        activity!!.runOnUiThread {
            val data = args[0] as JSONObject

            /** If the variable is true then it means that the device state was triggered by the app
             *  So the view is already up to date
             * */
            if (!isNotGladysDeviceState) {
                for (room in deviceTypeByRoom) {
                    for (deviceType in room.deviceTypes) {
                        if (data.getLong("devicetype") == deviceType.deviceTypeId) {
                            deviceType.lastValue = data.getInt("value").toFloat()
                            adapter.notifyDataSetChanged()
                            break
                        }
                    }
                }
            }else {
                isNotGladysDeviceState = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPause() {
        super.onPause()

        /** Remove listener on new device state when the fragment is paused */
        socket.off("newDeviceState", onNewDeviceState)
    }
}
