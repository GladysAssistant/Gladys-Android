package com.gladysproject.gladys.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gladysproject.gladys.R
import com.gladysproject.gladys.adapters.DeviceTypeAdapter
import com.gladysproject.gladys.database.GladysDb
import com.gladysproject.gladys.database.entity.DeviceType
import com.gladysproject.gladys.database.entity.Rooms
import com.gladysproject.gladys.utils.*
import com.google.android.material.snackbar.Snackbar
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    private lateinit var deviceTypeByRoom: MutableList<Rooms>
    private lateinit var adapter : DeviceTypeAdapter

    companion object {
        fun newInstance() = HomeFragment()
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        activity?.loadingCircle?.visibility = View.VISIBLE
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onStart() {
        super.onStart()
        init()
    }

    private fun init(){
        retrofit = Retrofit.Builder()
                .baseUrl(ConnectivityAPI.getUrl(context!!)) /** The function getUrl return string address */
                .addConverterFactory(MoshiConverterFactory.create())
                .client(SelfSigningClientBuilder.unsafeOkHttpClient)
                .build()

        token = PreferenceManager.getDefaultSharedPreferences(context).getString("token", "dfhdfh")!!

        getAllDeviceTypes()

        socket = ConnectivityAPI.Companion.WebSocket.getInstance(context!!)!!
        socket.on("newDeviceState", onNewDeviceState)
    }

    private fun getAllDeviceTypes(){

        retrofit
                .create(GladysAPI::class.java)
                .getDeviceTypeByRoom(token)
                .enqueue(object : Callback<MutableList<Rooms>> {

                    override fun onResponse(call: Call<MutableList<Rooms>>, response: Response<MutableList<Rooms>>) = runBlocking{
                        if(response.code() == 200){

                            deviceTypeByRoom = response.body()!!

                            /** Remove device type not displayed */
                            GlobalScope.launch {
                                for(room in deviceTypeByRoom){
                                    room.deviceTypes = room.deviceTypes.asSequence().filterIndexed{ _, it -> it.display != 0.toLong() }.toMutableList()
                                }
                            }.join()

                            /** Remove room if after removing device types not displayed her is empty */
                            GlobalScope.launch {
                                for(room in deviceTypeByRoom){
                                    if(room.deviceTypes.isEmpty()){
                                        deviceTypeByRoom.toMutableList().remove(room)
                                    }
                                }
                            }.join()

                            /** Save data in database */
                            GlobalScope.launch{

                                GladysDb.database?.deviceTypeDao()?.deleteDeviceTypes()

                                for(room in deviceTypeByRoom){
                                    val newRoom : Rooms = object : Rooms("", "", 0, false, mutableListOf()){}
                                    newRoom.id = room.id
                                    newRoom.name = room.name
                                    newRoom.house = room.house

                                    /** If room exist update all attributs except isExpanded attribut*/
                                    val existingRoom = GladysDb.database?.roomsDao()?.getRoomsById(newRoom.id)
                                    if(existingRoom != null) {
                                        GladysDb.database?.roomsDao()?.updateRoomWithoutExpand(newRoom.name, newRoom.id)
                                        room.isExpanded = existingRoom.isExpanded
                                    }else GladysDb.database?.roomsDao()?.insertRoom(newRoom)

                                    for (devicetype in room.deviceTypes){
                                        val newDevicetype : DeviceType = object : DeviceType("", 0, "", "", "", "", 0, 0, 1, 0, 0.toFloat(), 0, 0){}
                                        newDevicetype.deviceTypeName = devicetype.deviceTypeName
                                        newDevicetype.id = devicetype.id
                                        newDevicetype.type = devicetype.type
                                        newDevicetype.category = devicetype.category
                                        newDevicetype.tag = devicetype.tag
                                        newDevicetype.unit = devicetype.unit
                                        newDevicetype.min = devicetype.min
                                        newDevicetype.max = devicetype.max
                                        newDevicetype.sensor = devicetype.sensor
                                        newDevicetype.lastValue = devicetype.lastValue
                                        newDevicetype.roomId = room.id
                                        newDevicetype.roomHouse = devicetype.roomHouse

                                        GladysDb.database?.deviceTypeDao()?.insertDeviceType(newDevicetype)
                                    }
                                }
                            }.join()

                            if(deviceTypeByRoom.isNotEmpty())refreshView(deviceTypeByRoom)
                            else showEmptyView()

                        } else {
                            showSnackBar()
                        }
                    }

                    override fun onFailure(call: Call<MutableList<Rooms>>, err: Throwable) = runBlocking {
                        // Log the error for debug
                        Log.e(TAG, err.message)
                        GlobalScope.launch {
                            val rooms : MutableList<Rooms> = GladysDb.database?.roomsDao()?.getAllRooms()!!
                            for (room in rooms){
                                room.deviceTypes = GladysDb.database?.deviceTypeDao()?.getDeviceTypeByRoom(room.id)!!
                            }
                            deviceTypeByRoom = rooms
                        }.join()

                        if(deviceTypeByRoom.isNotEmpty())refreshView(deviceTypeByRoom)
                        else showEmptyView()

                        showSnackBar()
                    }
                })
    }

    fun refreshView(data : MutableList<Rooms>){
        if(home_rv != null){
            home_rv.layoutManager = LinearLayoutManager(context)
            (home_rv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

            val expMgr = RecyclerViewExpandableItemManager(null)
            adapter = DeviceTypeAdapter(data, context!!, this)
            home_rv.adapter = expMgr.createWrappedAdapter(adapter)
            expMgr.attachRecyclerView(home_rv)
            DeviceTypeAdapter.setExpandedGroups(deviceTypeByRoom, expMgr)

            activity?.loadingCircle?.visibility = View.INVISIBLE
        }
    }

    fun showEmptyView(){
        if(home_rv != null) {
            home_rv.visibility = View.INVISIBLE
            activity?.loadingCircle?.visibility = View.INVISIBLE
            empty_state_img_home.visibility = View.VISIBLE
            empty_state_message_home.visibility = View.VISIBLE
        }
    }

    override fun onClickCallbackDeviceState(id: Long?, value: Float?) {
        socket.off("newDeviceState", onNewDeviceState)

        retrofit
                .create(GladysAPI::class.java)
                .changeDeviceState(id, value, token)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        /** The new device state is captured by onNewDeviceState function by websocket connection **/
                        if (response.code() == 200) {
                            socket.on("newDeviceState", onNewDeviceState)
                        }
                        else {
                            adapter.notifyDataSetChanged()
                            socket.on("newDeviceState", onNewDeviceState)
                            showSnackBar()
                        }
                    }
                    override fun onFailure(call: Call<Void>, err: Throwable) {
                        showSnackBar()
                        socket.on("newDeviceState", onNewDeviceState)
                    }
                })
    }

    private val onNewDeviceState = Emitter.Listener { args ->

        val data = args[0] as JSONObject

        activity!!.runOnUiThread {

            for (room in deviceTypeByRoom) {
                for (deviceType in room.deviceTypes) {
                    if (data.getLong("devicetype") == deviceType.id) {
                        deviceType.lastValue = data.getInt("value").toFloat()
                        adapter.notifyDataSetChanged()
                        break
                    }
                }
            }

        }

        /** Update value in database */
        GlobalScope.launch {
            GladysDb.database?.deviceTypeDao()?.updateDeviceTypeLastValue(data.getInt("value").toFloat(), data.getLong("devicetype"))
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

    override fun onResume() {
        super.onResume()
        if(activity?.bottom_navigation?.selectedItemId != R.id.home)activity?.bottom_navigation?.selectedItemId = R.id.home
    }

    fun showSnackBar(){
        try {
            if (ConnectivityAPI.getUrl(this@HomeFragment.context!!) == "http://noconnection") {
                Snackbar.make(home_rv, R.string.no_connection, Snackbar.LENGTH_LONG)
                        .apply {
                            view.layoutParams = (view.layoutParams as CoordinatorLayout.LayoutParams)
                                    .apply { setMargins(leftMargin, topMargin, rightMargin, activity?.bottom_navigation?.height!! + 22) }
                        }.show()
            } else {
                Snackbar.make(home_rv, R.string.error, Snackbar.LENGTH_LONG)
                        .apply {
                            view.layoutParams = (view.layoutParams as CoordinatorLayout.LayoutParams)
                                    .apply { setMargins(leftMargin, topMargin, rightMargin, activity?.bottom_navigation?.height!! + 22) }
                        }.show()
            }
        } catch (er: Exception){}
    }
}
