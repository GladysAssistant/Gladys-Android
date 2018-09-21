package com.gladysproject.gladys.fragments

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.*
import com.gladysproject.gladys.R
import kotlinx.android.synthetic.main.activity_main.*

class TaskFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        activity?.loadingCircle?.visibility = View.INVISIBLE
        return inflater.inflate(R.layout.fragment_task, container, false)
    }

    companion object {
        fun newInstance() = TaskFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.toolbar_menu, menu)
        menu.findItem(R.id.add_button).isVisible = true
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        if(activity?.bottom_navigation?.selectedItemId != R.id.task)activity?.bottom_navigation?.selectedItemId = R.id.task
    }
}
