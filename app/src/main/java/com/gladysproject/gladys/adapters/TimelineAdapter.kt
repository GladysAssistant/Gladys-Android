package com.gladysproject.gladys.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import com.gladysproject.gladys.R
import com.gladysproject.gladys.database.entity.Event
import com.gladysproject.gladys.utils.DateTimeUtils
import kotlinx.android.synthetic.main.card_timeline_event.view.*

class TimelineAdapter(private val events: List<Event>) : RecyclerView.Adapter<TimelineAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(View.inflate(parent.context, R.layout.card_timeline_event, null))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(events[position], position)
    }

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(event: Event, position: Int) {

            itemView.event_name.text = event.name
            itemView.event_date.text = DateTimeUtils.getRelativeTimeSpan(event.datetime!!)

            if(position == 0)itemView.time_marker.initLine(0)
            else itemView.time_marker.initLine(itemViewType)

        }
    }

}
