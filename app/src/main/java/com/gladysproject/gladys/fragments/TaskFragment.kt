package com.gladysproject.gladys.fragments

import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ViewFlipper
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.gladysproject.gladys.R
import com.gladysproject.gladys.models.Mode
import com.gladysproject.gladys.utils.ConnectivityAPI
import com.gladysproject.gladys.utils.GladysAPI
import com.gladysproject.gladys.utils.SelfSigningClientBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class TaskFragment : Fragment() {

    private var taskDialog: MaterialDialog? = null
    private var taskName: String = ""
    private var taskTrigger : String = ""
    private var taskTriggerParams : String = ""
    private var taskAction : String = ""
    private var taskActionParams : String = ""
    private var isNfcTask: Boolean = false
    private var modesName = listOf<String>()
    private var modesCode = listOf<String>()
    private lateinit var modes: MutableList<Mode>
    private lateinit var retrofit: Retrofit
    private var token : String = ""

    companion object {
        fun newInstance() = TaskFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        activity?.loadingCircle?.visibility = View.INVISIBLE
        return inflater.inflate(R.layout.fragment_task, container, false)
    }

    override fun onStart() {
        super.onStart()
        retrofit = Retrofit.Builder()
                .baseUrl(ConnectivityAPI.getUrl(context!!)) /** The function getUrl return string address */
                .addConverterFactory(MoshiConverterFactory.create())
                .client(SelfSigningClientBuilder.unsafeOkHttpClient)
                .build()

        token = PreferenceManager.getDefaultSharedPreferences(context).getString("token", "rgdffg")!!

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
            createTaskName()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if(activity?.bottom_navigation?.selectedItemId != R.id.task)activity?.bottom_navigation?.selectedItemId = R.id.task
    }

    private fun createTaskName(){
        getModes()
        taskDialog = MaterialDialog(context!!)

        taskDialog!!
                .title(text = getString(R.string.new_task_name))
                .customView(R.layout.dialog_name)
                .positiveButton(R.string.next){ _ ->
                    createTaskTriggers()
                }
                .negativeButton(R.string.negative_button)
                .show()

        var isValidName = false
        taskDialog!!.setActionButtonEnabled(WhichButton.POSITIVE, isValidName)

        val name = taskDialog!!.getCustomView()!!.findViewById<EditText>(R.id.dialog_name_input)

        name?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p3 == 0 && p1 == 0)isValidName = false
                else if (p3 == 1)isValidName = true
            }
            override fun afterTextChanged(p0: Editable?) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                taskDialog!!.setActionButtonEnabled(WhichButton.POSITIVE, isValidName)
                taskName = name.text.toString()
            }
        })
    }

    private fun createTaskTriggers(){
        taskDialog = MaterialDialog(context!!)
        taskDialog!!
                .title(text = getString(R.string.new_task_trigger))
                .listItemsSingleChoice(R.array.triggers, waitForPositiveButton = false) { dialog, index, _ ->
                    taskTrigger = resources.getStringArray(R.array.triggers_code)[index]
                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                }
                .positiveButton(R.string.next){ _ ->
                    createTaskTriggersFilter()
                }
                .negativeButton(R.string.negative_button)
                .show()

    }

    private fun createTaskTriggersFilter(){
        taskDialog = MaterialDialog(context!!)
        when (taskTrigger){
            "mqtt" -> {
                taskDialog!!
                        .title(text = getString(R.string.new_task_mqtt))
                        .customView(R.layout.dialog_mqtt)
                        .positiveButton(R.string.next){ _ ->
                            createTaskActions()
                        }
                        .negativeButton(R.string.negative_button)
                        .show()

                var isValidTopic = false
                var isValidMessage = false
                taskDialog!!.setActionButtonEnabled(WhichButton.POSITIVE, isValidMessage && isValidTopic)

                val mqttTopic = taskDialog!!.getCustomView()!!.findViewById<EditText>(R.id.mqtt_topic)
                val mqttMessage = taskDialog!!.getCustomView()!!.findViewById<EditText>(R.id.mqtt_message)

                mqttTopic?.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        if (p3 == 0 && p1 == 0)isValidTopic = false
                        else if (p3 == 1)isValidTopic = true
                    }
                    override fun afterTextChanged(p0: Editable?) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        taskDialog!!.setActionButtonEnabled(WhichButton.POSITIVE, isValidMessage && isValidTopic)
                        taskTriggerParams = "${mqttTopic.text};${mqttMessage!!.text}"
                    }
                })

                mqttMessage?.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        if (p3 == 0 && p1 == 0)isValidMessage = false
                        else if (p3 == 1)isValidMessage = true
                    }
                    override fun afterTextChanged(p0: Editable?) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        taskDialog!!.setActionButtonEnabled(WhichButton.POSITIVE, isValidMessage && isValidTopic)
                        taskTriggerParams = "${mqttTopic!!.text};${mqttMessage.text}"
                    }
                })

            }
            else -> {
                isNfcTask = true
                createTaskActions()
            }
        }
    }

    private fun createTaskActions(){
        taskDialog = MaterialDialog(context!!)
        taskDialog!!
                .title(text = getString(R.string.new_task_action))
                .listItemsSingleChoice(R.array.actions, waitForPositiveButton = false) { dialog, index, _ ->
                    taskAction = resources.getStringArray(R.array.actions_code)[index]
                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                }
                .positiveButton(R.string.next){ _ ->
                    createTaskActionsFilter()
                }
                .negativeButton(R.string.negative_button)
                .show()
    }

    private fun createTaskActionsFilter(){
        val positiveButton : Int = if(isNfcTask) R.string.next else R.string.positve_button
        taskDialog = MaterialDialog(context!!)
        when (taskAction){
            "create_event" -> {
                taskDialog!!
                        .title(text = getString(R.string.new_task_event))
                        .listItemsSingleChoice(R.array.events, waitForPositiveButton = false) { dialog, index, _ ->
                            taskActionParams = resources.getStringArray(R.array.events_code)[index]
                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                        }
                        .positiveButton(positiveButton){ _ ->
                            saveTask()
                        }
                        .negativeButton(R.string.negative_button)
                        .show()
            }
            "change_house_mode" -> {
                taskDialog!!
                        .title(text = getString(R.string.new_task_mode))
                        .listItemsSingleChoice(items = modesName, waitForPositiveButton = false) { dialog, index, _ ->
                            taskActionParams = modesCode[index]
                            Log.e("Url", taskActionParams)
                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                        }
                        .positiveButton(positiveButton){ _ ->
                            saveTask()
                        }
                        .negativeButton(R.string.negative_button)
                        .show()
            }
            else -> {
                taskDialog!!
                        .title(text = getString(R.string.new_task_url))
                        .customView(R.layout.dialog_url)
                        .positiveButton(R.string.next){ _ ->
                            saveTask()
                        }
                        .negativeButton(R.string.negative_button)
                        .show()

                var isValidUrl = false
                taskDialog!!.setActionButtonEnabled(WhichButton.POSITIVE, isValidUrl)

                val url = taskDialog!!.getCustomView()!!.findViewById<EditText>(R.id.dialog_url_input)

                url?.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        if (p3 == 0 && p1 == 0)isValidUrl = false
                        else if (p3 == 1)isValidUrl = true
                    }
                    override fun afterTextChanged(p0: Editable?) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        taskDialog!!.setActionButtonEnabled(WhichButton.POSITIVE, isValidUrl)
                        taskActionParams = url.text.toString()
                    }
                })
            }
        }
    }

    private fun saveTask(){
        if(isNfcTask)writeNfcTag()
    }

    private fun writeNfcTag(){
        isNfcTask = false
        taskDialog = MaterialDialog(context!!)
        taskDialog!!
                .title(text = getString(R.string.writing_nfc_tag))
                .customView(R.layout.dialog_nfc)
                .positiveButton(R.string.positve_button){ _ ->
                    saveTask()
                }
                .negativeButton(R.string.negative_button)
                .show()

        taskDialog!!.setActionButtonEnabled(WhichButton.POSITIVE, false)
        val nfcFlipper = taskDialog!!.getCustomView()!!.findViewById<ViewFlipper>(R.id.nfc_flipper)
        Handler().postDelayed({
            nfcFlipper.displayedChild = 1
            taskDialog!!.setActionButtonEnabled(WhichButton.POSITIVE, true)
        }, 3000)
    }

    private fun getModes(){
        retrofit
                .create(GladysAPI::class.java)
                .getModes(token)
                .enqueue(object : Callback<MutableList<Mode>> {
                    override fun onResponse(call: Call<MutableList<Mode>>, response: Response<MutableList<Mode>>) {
                        if(response.code() == 200) {
                            modes = response.body()!!
                            launch {
                                for (mode in modes){
                                    modesCode += mode.code
                                    modesName += mode.name
                                }
                            }
                        }
                    }
                    override fun onFailure(call: Call<MutableList<Mode>>, err: Throwable) {}
                })
    }
}

