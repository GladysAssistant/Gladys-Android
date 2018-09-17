package com.gladysproject.gladys.fragments

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.getColor
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.OnTouchListener
import android.widget.TextView
import com.gladysproject.gladys.R
import com.gladysproject.gladys.adapters.MessageAdapter
import com.gladysproject.gladys.database.entity.Message
import com.gladysproject.gladys.utils.ConnectivityAPI
import com.gladysproject.gladys.utils.GladysAPI
import com.gladysproject.gladys.utils.SelfSigningClientBuilder
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.fragment_chat.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ChatFragment : Fragment() {

    private var token : String = ""
    private lateinit var retrofit : Retrofit
    private lateinit var socket : Socket
    private lateinit var messages : MutableList<Message>
    private lateinit var adapter : MessageAdapter

    /** Initialize new message variable */
    private var newMessage : Message = object : Message("", 0, "", "", 0){}

    companion object {
        fun newInstance() = ChatFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onStart() {
        super.onStart()

        retrofit = Retrofit.Builder()
                .baseUrl(ConnectivityAPI.getUrl(context!!)) /** The function getUrl return string address */
                .addConverterFactory(MoshiConverterFactory.create())
                .client(SelfSigningClientBuilder.unsafeOkHttpClient)
                .build()

        token = PreferenceManager.getDefaultSharedPreferences(context).getString("token", "")!!

        getMessages()

        message.setOnTouchListener(OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= message.right - message.compoundDrawables[2].bounds.width()) {
                    if (message.text.isNotEmpty() || message.text != null) sendMessage(message.text.toString())
                    return@OnTouchListener false
                }
            }
            false
        })

        message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p3 == 0) {setTextViewDrawableColor(message, R.color.secondaryDarkColor)}
                else if (p3 == 1) {setTextViewDrawableColor(message, R.color.primaryColor)}
            }
            override fun afterTextChanged(p0: Editable?) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        /** Scrolling to bottom when the keyboard is opened */
        chat_rv.addOnLayoutChangeListener{_: View, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int ->
            if(::adapter.isInitialized) chat_rv.scrollToPosition(adapter.itemCount - 1)
        }

        socket = ConnectivityAPI.Companion.WebSocket.getInstance(context!!)!!
        socket.on("message", onNewMessage)
    }

    private fun getMessages(){
        retrofit
                .create(GladysAPI::class.java)
                .getMessages(token)
                .enqueue(object : Callback<MutableList<Message>> {

                    override fun onResponse(call: Call<MutableList<Message>>, response: Response<MutableList<Message>>) {
                        if(response.code() == 200)
                            messages = response.body()!!
                            refreshView(messages)
                    }

                    override fun onFailure(call: Call<MutableList<Message>>, err: Throwable) {
                        println(err.message)
                    }
                })
    }

    private fun sendMessage(text : String){
        retrofit
                .create(GladysAPI::class.java)
                .sendMessage(text,null ,token)
                .enqueue(object : Callback<Message> {
                    override fun onResponse(call: Call<Message>, response: Response<Message>) {
                        if(response.code() == 200) {
                            message.setText("")
                            messages.add(messages.size ,response.body()!!)
                            adapter.notifyItemInserted(messages.size)
                            chat_rv.scrollToPosition(adapter.itemCount - 1)
                        }
                    }
                    override fun onFailure(call: Call<Message>, err: Throwable) {
                        println(err.message)
                    }
                })
    }

    private val onNewMessage = Emitter.Listener { args ->
        activity!!.runOnUiThread {
            val data = args[0] as JSONObject

            newMessage.text = data.getString("text")
            newMessage.sender = null /** The null sender is Gladys */

            messages.add(messages.size, newMessage)
            adapter.notifyItemInserted(messages.size)
            chat_rv.scrollToPosition(adapter.itemCount - 1)
        }
    }

    fun refreshView(data : List<Message>){
        if(chat_rv != null){
            chat_rv.layoutManager = LinearLayoutManager(context)
            adapter = MessageAdapter(data)
            chat_rv.adapter = adapter
            chat_rv.scrollToPosition(adapter.itemCount -1)
        }
    }

    private fun setTextViewDrawableColor(textView: TextView, color: Int) {
        for (drawable in textView.compoundDrawables) {
            if (drawable != null){
                drawable.clearColorFilter()
                drawable.colorFilter = PorterDuffColorFilter(getColor(context!!, color), PorterDuff.Mode.SRC_IN)
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

        /** Remove listener on message event when the fragment is paused */
        socket.off("message", onNewMessage)
    }
}
