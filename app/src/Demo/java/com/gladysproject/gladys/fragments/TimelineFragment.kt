package com.gladysproject.gladys.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.gladysproject.gladys.R
import com.gladysproject.gladys.Utils.GetDemoData
import com.gladysproject.gladys.adapters.TimelineAdapter
import com.gladysproject.gladys.database.entity.Event
import com.gladysproject.gladys.utils.DateTimeUtils.getCurrentDate
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_timeline.*

class TimelineFragment : Fragment() {

    private var events = mutableListOf<Event>()
    private lateinit var adapter: TimelineAdapter

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
        init()
    }

    @SuppressLint("SetTextI18n")
    fun init(){

        user_name.text = PreferenceManager.getDefaultSharedPreferences(context).getString("user_firstname", "John")!! + " " + PreferenceManager.getDefaultSharedPreferences(context).getString("user_name", "Pepperwood")!!
        datetime.text = getCurrentDate()

        events = GetDemoData.getTimelineData(context!!)
        refreshView(events)

    }

    private fun createEvent(){}

    private fun refreshView(data : List<Event>){
        if(timeline_rv != null){
            timeline_appbar.visibility = View.VISIBLE
            timeline_rv.layoutManager = LinearLayoutManager(context)
            adapter = TimelineAdapter(data)
            timeline_rv.adapter = adapter

            activity?.loadingCircle?.visibility = View.INVISIBLE
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
                    .listItemsSingleChoice(R.array.events) { _, _, _ ->
                        createEvent()
                    }
                    .positiveButton(R.string.validate)
                    .negativeButton(R.string.cancel)
                    .show()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if(activity?.bottom_navigation?.selectedItemId != R.id.timeline)activity?.bottom_navigation?.selectedItemId = R.id.timeline
    }

}

