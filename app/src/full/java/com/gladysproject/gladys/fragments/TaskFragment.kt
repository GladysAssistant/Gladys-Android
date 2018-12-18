package com.gladysproject.gladys.fragments

import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.gladysproject.gladys.R
import com.gladysproject.gladys.adapters.TaskAdapter
import com.gladysproject.gladys.database.GladysDb
import com.gladysproject.gladys.database.entity.Task
import com.gladysproject.gladys.models.Mode
import com.gladysproject.gladys.utils.AdapterCallback
import com.gladysproject.gladys.utils.ConnectivityAPI
import com.gladysproject.gladys.utils.GladysAPI
import com.gladysproject.gladys.utils.SelfSigningClientBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_task.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class TaskFragment : Fragment(), AdapterCallback.AdapterCallbackTask {

    private var taskDialog: MaterialDialog? = null
    private var isNfcTask: Boolean = false
    private var modesName = listOf<String>()
    private var modesCode = listOf<String>()
    private lateinit var modes: MutableList<Mode>
    private lateinit var retrofit: Retrofit
    private var token : String = ""
    private var tasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter

    /** Initialize new event variable */
    private var newTask : Task = object : Task("", "", "", "",""){}

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

        getTasks()
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
        //if(activity?.bottom_navigation?.selectedItemId != R.id.task)activity?.bottom_navigation?.selectedItemId = R.id.task
    }

    private fun createTaskName(){

        /** Reset fields of new task if the user has create other task before */
        newTask.name = ""
        newTask.triggerType = ""
        newTask.triggerParam = ""
        newTask.actionType = ""
        newTask.actionParam = ""

        getModes()
        taskDialog = MaterialDialog(context!!)

        taskDialog!!
                .title(text = getString(R.string.new_task_name))
                .customView(R.layout.dialog_name)
                .positiveButton(R.string.next){
                    createTaskTriggers()
                }
                .negativeButton(R.string.cancel)
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
                newTask.name = name.text.toString()
            }
        })
    }

    private fun createTaskTriggers(){
        taskDialog = MaterialDialog(context!!)
        taskDialog!!
                .title(text = getString(R.string.new_task_trigger))
                .listItemsSingleChoice(R.array.triggers, waitForPositiveButton = false) { dialog, index, _ ->
                    newTask.triggerType = resources.getStringArray(R.array.triggers_code)[index]
                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                }
                .positiveButton(R.string.next){
                    createTaskTriggersFilter()
                }
                .negativeButton(R.string.cancel)
                .show()

    }

    private fun createTaskTriggersFilter(){
        taskDialog = MaterialDialog(context!!)
        when (newTask.triggerType){
            "mqtt" -> {
                taskDialog!!
                        .title(text = getString(R.string.new_task_mqtt))
                        .customView(R.layout.dialog_mqtt)
                        .positiveButton(R.string.next){
                            createTaskActions()
                        }
                        .negativeButton(R.string.cancel)
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
                        newTask.triggerParam = "${mqttTopic.text};${mqttMessage!!.text}"
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
                        newTask.triggerParam = "${mqttTopic!!.text};${mqttMessage.text}"
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
                    newTask.actionType = resources.getStringArray(R.array.actions_code)[index]
                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                }
                .positiveButton(R.string.next){
                    createTaskActionsFilter()
                }
                .negativeButton(R.string.cancel)
                .show()
    }

    private fun createTaskActionsFilter(){
        val positiveButton : Int = if(isNfcTask) R.string.next else R.string.validate
        taskDialog = MaterialDialog(context!!)
        when (newTask.actionType){
            "create_event" -> {
                taskDialog!!
                        .title(text = getString(R.string.new_task_event))
                        .listItemsSingleChoice(R.array.events, waitForPositiveButton = false) { dialog, index, _ ->
                            newTask.actionParam = resources.getStringArray(R.array.events_code)[index]
                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                        }
                        .positiveButton(positiveButton){
                            if(isNfcTask) writeNfcTag() else saveTask()
                        }
                        .negativeButton(R.string.cancel)
                        .show()
            }
            "change_house_mode" -> {
                taskDialog!!
                        .title(text = getString(R.string.new_task_mode))
                        .listItemsSingleChoice(items = modesName, waitForPositiveButton = false) { dialog, index, _ ->
                            newTask.actionParam = modesCode[index]
                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                        }
                        .positiveButton(positiveButton){
                            if(isNfcTask) writeNfcTag() else saveTask()
                        }
                        .negativeButton(R.string.cancel)
                        .show()
            }
            else -> {
                taskDialog!!
                        .title(text = getString(R.string.new_task_url))
                        .customView(R.layout.dialog_url)
                        .positiveButton(R.string.next){
                            if(isNfcTask) writeNfcTag() else saveTask()
                        }
                        .negativeButton(R.string.cancel)
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
                        newTask.actionParam = url.text.toString()
                    }
                })
            }
        }
    }

    private fun saveTask() {
        GlobalScope.launch {
            GladysDb.database?.taskDao()?.insertTask(newTask)
        }

        tasks.add(tasks.size, newTask)
        adapter.notifyItemInserted(tasks.size)

        if(tasks.size == 1) {
            refreshView(tasks)
            task_rv.visibility = View.VISIBLE
            empty_state_img_task.visibility = View.INVISIBLE
            empty_state_message_task.visibility = View.INVISIBLE
        }
    }

    private fun deleteTask(task: Task){
        GlobalScope.launch {
            GladysDb.database?.taskDao()?.deleteTask(task.id)
        }
        tasks.remove(task)
        adapter.notifyDataSetChanged()
    }

    private fun getTasks() = runBlocking {
        GlobalScope.launch {
            tasks = GladysDb.database?.taskDao()?.getAllTasks()!!
        }.join()

        if(tasks.isNotEmpty()) refreshView(tasks)
        else showEmptyView()
    }

    private fun writeNfcTag(){
        isNfcTask = false
        taskDialog = MaterialDialog(context!!)
        taskDialog!!
                .title(text = getString(R.string.writing_nfc_tag))
                .customView(R.layout.dialog_nfc)
                .positiveButton(R.string.validate){
                    saveTask()
                }
                .negativeButton(R.string.cancel)
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
                            GlobalScope.launch {
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

    private fun refreshView(data : List<Task>){
        if(task_rv != null){
            task_rv.layoutManager = LinearLayoutManager(context)
            adapter = TaskAdapter(data, this, context!!)
            task_rv.adapter = adapter

            activity?.loadingCircle?.visibility = View.INVISIBLE
        }
    }

    private fun showEmptyView(){
        if(task_rv != null) {
            task_rv.visibility = View.INVISIBLE
            activity?.loadingCircle?.visibility = View.INVISIBLE
            empty_state_img_task.visibility = View.VISIBLE
            empty_state_message_task.visibility = View.VISIBLE
        }
    }

    override fun onClickCallbackTask(task: Task) {
        taskDialog = MaterialDialog(context!!)
        taskDialog!!
                .title(text = task.name)
                .customView(R.layout.dialog_task)
                .positiveButton(R.string.delete){
                    deleteTask(task)
                }
                .show()

        taskDialog!!.getCustomView()!!.findViewById<TextView>(R.id.dialog_trigger_type).text = task.triggerType
        taskDialog!!.getCustomView()!!.findViewById<TextView>(R.id.dialog_trigger_param).text = task.triggerParam
        taskDialog!!.getCustomView()!!.findViewById<TextView>(R.id.dialog_action_type).text = task.actionType
        taskDialog!!.getCustomView()!!.findViewById<TextView>(R.id.dialog_action_param).text = task.actionParam
    }
}

