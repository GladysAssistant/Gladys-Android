package com.gladysproject.gladys.fragments

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.getColor
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.OnTouchListener
import android.widget.TextView
import com.gladysproject.gladys.R
import com.gladysproject.gladys.Utils.GetDemoData
import com.gladysproject.gladys.adapters.MessageAdapter
import com.gladysproject.gladys.database.entity.Message
import com.gladysproject.gladys.utils.AdapterCallback
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment : Fragment(), AdapterCallback.AdapterCallbackMessage {

    private var messages = mutableListOf<Message>()
    private lateinit var adapter : MessageAdapter

    /** Initialize new message variable */
    private var newMessage : Message = object : Message("", 0, "", "", 0){}

    companion object {
        fun newInstance() = ChatFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        activity?.loadingCircle?.visibility = View.VISIBLE
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onStart() {
        super.onStart()
        init()
    }

    fun init(){

        messages = GetDemoData.getChatData(context!!)
        refreshView(messages)

        /** Set drawable manually because API 21 and lower not support the vector drawable */
        message.setCompoundDrawablesWithIntrinsicBounds(null, null, AppCompatResources.getDrawable(context!!, R.drawable.ic_send_24dp), null)

        message.setOnTouchListener(OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= message.right - message.compoundDrawables[2].bounds.width()) {
                    if (message.text.isNotEmpty() || message.text != null) sendMessage()
                    return@OnTouchListener false
                }
            }
            false
        })

        message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p3 == 0 && p1 == 0) {setTextViewDrawableColor(message, R.color.secondaryDarkColor)}
                else if (p3 == 1) {setTextViewDrawableColor(message, R.color.primaryColor)}
            }
            override fun afterTextChanged(p0: Editable?) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        /** Scrolling to bottom when the keyboard is opened */
        chat_rv.addOnLayoutChangeListener{_: View, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int ->
            if(::adapter.isInitialized) chat_rv.scrollToPosition(adapter.itemCount - 1)
        }

    }

    private fun sendMessage(){}

    fun refreshView(data : MutableList<Message>){
        if(chat_rv != null){
            chat_rv.layoutManager = LinearLayoutManager(context)
            adapter = MessageAdapter(data, this)
            chat_rv.adapter = adapter
            chat_rv.scrollToPosition(adapter.itemCount -1)

            activity?.loadingCircle?.visibility = View.INVISIBLE
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

    override fun onClickCallbackMessage(text: String, isSend: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.toolbar_menu, menu)
        //menu.findItem(R.id.minimize_button).isVisible = true
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        if(activity?.bottom_navigation?.selectedItemId != R.id.chat)activity?.bottom_navigation?.selectedItemId = R.id.chat
    }
}
