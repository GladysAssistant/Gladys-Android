package com.gladysproject.gladys.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.gladysproject.gladys.R
import com.gladysproject.gladys.database.entity.Task
import com.gladysproject.gladys.utils.AdapterCallback
import kotlinx.android.synthetic.main.card_task.view.*

class TaskAdapter(private val tasks: List<Task>,  private var callbacks: AdapterCallback.AdapterCallbackTask, private var context: Context) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(View.inflate(parent.context, R.layout.card_task, null))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tasks[position], callbacks, context)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun bind(task: Task, callbacks: AdapterCallback.AdapterCallbackTask, context: Context) {

            itemView.task_name.text = task.name
            itemView.setOnClickListener{callbacks.onClickCallbackTask(task)}
            itemView.task_info.text =
                    context.resources.getStringArray(R.array.triggers)[context.resources.getStringArray(R.array.triggers_code).indexOf(task.triggerType)] + " -> " +
                    context.resources.getStringArray(R.array.actions)[context.resources.getStringArray(R.array.actions_code).indexOf(task.actionType)]

        }
    }

}