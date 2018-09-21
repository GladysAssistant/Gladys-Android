package com.gladysproject.gladys.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.*
import com.gladysproject.gladys.R
import com.gladysproject.gladys.adapters.DeviceTypeAdapter
import com.gladysproject.gladys.database.GladysDb
import com.gladysproject.gladys.database.entity.DeviceType
import com.gladysproject.gladys.database.entity.Rooms
import com.gladysproject.gladys.utils.AdapterCallback
import com.gladysproject.gladys.utils.ConnectivityAPI
import com.gladysproject.gladys.utils.GladysAPI
import com.gladysproject.gladys.utils.SelfSigningClientBuilder
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.Exception

class HomeFragment : Fragment(), AdapterCallback.AdapterCallbackDeviceState{

    private var token : String = ""
    private lateinit var retrofit: Retrofit
    private lateinit var socket : Socket
    private lateinit var deviceTypeByRoom: MutableList<Rooms>
    private lateinit var adapter : DeviceTypeAdapter
    private var isNotGladysDeviceState : Boolean = false

    companion object {
        fun newInstance() = HomeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        activity?.loadingCircle?.visibility = View.VISIBLE
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onStart() {
        super.onStart()
        bottom_navigation?.selectedItemId = R.id.task
        retrofit = Retrofit.Builder()
                .baseUrl(ConnectivityAPI.getUrl(context!!)) /** The function getUrl return string address */
                .addConverterFactory(MoshiConverterFactory.create())
                .client(SelfSigningClientBuilder.unsafeOkHttpClient)
                .build()

        token = PreferenceManager.getDefaultSharedPreferences(context).getString("token", "dfhdfh")!!

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

                            /** Remove devicetype not displayed */
                            launch {
                                for(room in deviceTypeByRoom){
                                    room.deviceTypes = room.deviceTypes.asSequence().filterIndexed{ _, it -> it.display != 0.toLong() }.toMutableList()
                                }
                            }.join()

                            /** Remove room if after removing devicetypes not displayed her is empty */
                            launch {
                                for(room in deviceTypeByRoom){
                                    if(room.deviceTypes.isEmpty()){
                                        deviceTypeByRoom.toMutableList().remove(room)
                                    }
                                }
                            }.join()

                            /** Save data in database */
                            launch{

                                GladysDb.database?.deviceTypeDao()?.deleteDeviceTypes()

                                for(room in deviceTypeByRoom){
                                    val newRoom : Rooms = object : Rooms("", 0, false, mutableListOf()){}
                                    newRoom.id = room.id
                                    newRoom.name = room.name

                                    /** If room exist update all attributs except isExpanded attribut*/
                                    val existingRoom = GladysDb.database?.roomsDao()?.getRoomsById(newRoom.id)
                                    if(existingRoom != null) {
                                        GladysDb.database?.roomsDao()?.updateRoomWithoutExpand(newRoom.name, newRoom.id)
                                        room.isExpanded = existingRoom.isExpanded
                                    }else GladysDb.database?.roomsDao()?.insertRoom(newRoom)

                                    for (devicetype in room.deviceTypes){
                                        val newDevicetype : DeviceType = object : DeviceType("", 0, "", "", "", 0, 0, 1, 0, 0.toFloat(), 0){}
                                        newDevicetype.deviceTypeName = devicetype.deviceTypeName
                                        newDevicetype.id = devicetype.id
                                        newDevicetype.type = devicetype.type
                                        newDevicetype.tag = devicetype.tag
                                        newDevicetype.unit = devicetype.unit
                                        newDevicetype.min = devicetype.min
                                        newDevicetype.max = devicetype.max
                                        newDevicetype.sensor = devicetype.sensor
                                        newDevicetype.lastValue = devicetype.lastValue
                                        newDevicetype.roomId = room.id

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
                        launch {
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
            empty_state_message_home.visibility = View.VISIBLE
        }
    }

    override fun onClickCallbackDeviceState(id: Long?, value: Float?) {
        retrofit
                .create(GladysAPI::class.java)
                .changeDeviceState(id, value, token)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        /** The new device state is captured by onNewDeviceState function by websocket connection
                         *  But we put the variable a true for can not create a loop between the cursor/switch listeners and event flicker by the socket
                         *  See the onNewDeviceState function for more comprehension
                         * */
                        if (response.code() == 200) isNotGladysDeviceState = true
                        else showSnackBar()
                    }
                    override fun onFailure(call: Call<Void>, err: Throwable) {
                        showSnackBar()
                    }
                })
    }

    private val onNewDeviceState = Emitter.Listener { args ->

        val data = args[0] as JSONObject

        activity!!.runOnUiThread {
            /** If the variable is true then it means that the device state was triggered by the app
             *  So the view is already up to date
             * */
            if (!isNotGladysDeviceState) {
                for (room in deviceTypeByRoom) {
                    for (deviceType in room.deviceTypes) {
                        if (data.getLong("devicetype") == deviceType.id) {
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

        /** Upadte value in database */
        launch {
            GladysDb.database?.deviceTypeDao()?.updateDeviceTypeLastValue( data.getInt("value").toFloat(), data.getLong("devicetype"))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
        getAllDeviceTypes()
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
