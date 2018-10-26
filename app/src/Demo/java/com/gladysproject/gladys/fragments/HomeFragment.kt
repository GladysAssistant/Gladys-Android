package com.gladysproject.gladys.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.*
import com.gladysproject.gladys.R
import com.gladysproject.gladys.Utils.GetDemoData
import com.gladysproject.gladys.adapters.DeviceTypeAdapter
import com.gladysproject.gladys.database.entity.Rooms
import com.gladysproject.gladys.utils.AdapterCallback
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(), AdapterCallback.AdapterCallbackDeviceState{

    private lateinit var deviceTypeByRoom: MutableList<Rooms>
    private lateinit var adapter : DeviceTypeAdapter

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
        bottom_navigation?.selectedItemId = R.id.home
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

    override fun onClickCallbackDeviceState(id: Long?, value: Float?) {}

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

        deviceTypeByRoom = GetDemoData.getHomeData(context!!)
        refreshView(deviceTypeByRoom)
    }

    override fun onResume() {
        super.onResume()
        if(activity?.bottom_navigation?.selectedItemId != R.id.home)activity?.bottom_navigation?.selectedItemId = R.id.home
    }
}
