package com.gladysproject.gladys.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gladysproject.gladys.R
import com.gladysproject.gladys.database.entity.Message
import kotlinx.android.synthetic.main.card_message_gladys.view.*
import kotlinx.android.synthetic.main.card_message_user.view.*

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            0 -> GladysMessagesVH(LayoutInflater.from(parent.context).inflate(R.layout.card_message_gladys, parent, false))
            else -> UserMessagesVH(LayoutInflater.from(parent.context).inflate(R.layout.card_message_user, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)){
            0 -> (holder as GladysMessagesVH).bind(messages[position])
            else -> (holder as UserMessagesVH).bind(messages[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(messages[position].sender == null) 0
        else 1
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    class GladysMessagesVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(message: Message) {

            itemView.gladysText.text = message.text
        }
    }

    class UserMessagesVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(message: Message){

            itemView.userText.text = message.text
        }
    }

}
