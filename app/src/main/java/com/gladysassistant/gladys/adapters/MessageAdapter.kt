package com.gladysassistant.gladys.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gladysassistant.gladys.R
import com.gladysassistant.gladys.database.entity.Message
import com.gladysassistant.gladys.utils.AdapterCallback
import com.gladysassistant.gladys.utils.DateTimeUtils
import com.gladysassistant.gladys.utils.DateTimeUtils.convertStringToDate
import com.gladysassistant.gladys.utils.DateTimeUtils.getDate
import com.gladysassistant.gladys.utils.DateTimeUtils.getTime
import kotlinx.android.synthetic.main.card_message_gladys.view.*
import kotlinx.android.synthetic.main.card_message_user.view.*

class MessageAdapter(private val messages: MutableList<Message>, private var callback: AdapterCallback.AdapterCallbackMessage) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            0 -> GladysMessagesVH(LayoutInflater.from(parent.context).inflate(R.layout.card_message_gladys, parent, false))
            else -> UserMessagesVH(LayoutInflater.from(parent.context).inflate(R.layout.card_message_user, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)){
            0 -> (holder as GladysMessagesVH).bind(messages, position, callback)
            else -> (holder as UserMessagesVH).bind(messages, position, callback)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            messages[position].sender == null -> 0
            else -> 1
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    class GladysMessagesVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(messages: MutableList<Message>, position: Int, callback: AdapterCallback.AdapterCallbackMessage) {
            itemView.time_message_gladys.text = getTime(messages[position].datetime!!)

            itemView.card_message_gladys

            itemView.card_message_gladys.setOnLongClickListener {
                callback.onClickCallbackMessage(messages[position].text!!, true)
                return@setOnLongClickListener true
            }

            itemView.gladys_message_text.text = messages[position].text
            if (position == 0){
                itemView.gladys_date.text = getDate(messages[position].datetime!!)
                itemView.gladys_date_indicator.visibility = View.VISIBLE
                itemView.gladys_date_indicator.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            } else if(position + 1 != messages.size) {
                if (DateTimeUtils.isAfterDay(convertStringToDate(messages[position].datetime!!), convertStringToDate(messages[position - 1].datetime!!))) {
                    itemView.gladys_date.text = getDate(messages[position].datetime!!)
                    itemView.gladys_date_indicator.visibility = View.VISIBLE
                    itemView.gladys_date_indicator.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    itemView.gladys_date_indicator.visibility = View.INVISIBLE
                    itemView.gladys_date_indicator.layoutParams.height = 0
                }
            }

        }
    }

    class UserMessagesVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(messages: MutableList<Message>, position: Int, callback: AdapterCallback.AdapterCallbackMessage){

            itemView.user_message_text.text = messages[position].text

            itemView.card_message_user.setOnLongClickListener {
                callback.onClickCallbackMessage(messages[position].text!!, messages[position].datetime != "error")
                return@setOnLongClickListener true
            }

            if (position == 0){
                itemView.user_date.text = getDate(messages[position].datetime!!)
                itemView.user_date_indicator.visibility = View.VISIBLE
                itemView.user_date_indicator.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            } else if(position + 1 != messages.size) {
                if (DateTimeUtils.isAfterDay(convertStringToDate(messages[position].datetime!!), convertStringToDate(messages[position - 1].datetime!!))) {
                    itemView.user_date.text = getDate(messages[position].datetime!!)
                    itemView.user_date_indicator.visibility = View.VISIBLE
                    itemView.user_date_indicator.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    itemView.user_date_indicator.visibility = View.INVISIBLE
                    itemView.user_date_indicator.layoutParams.height = 0
                }
            }

            if(messages[position].datetime != null && messages[position].datetime != "" && messages[position].datetime != "error") {
                itemView.time_message_user.text = getTime(messages[position].datetime!!)
                itemView.check_message_user.displayedChild = 1
            } else if(messages[position].datetime == "error"){
                itemView.check_message_user.displayedChild = 2
            } else {
                itemView.check_message_user.displayedChild = 0
            }

        }
    }

}
