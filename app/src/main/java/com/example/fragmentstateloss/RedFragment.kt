package com.example.fragmentstateloss

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class RedFragment(name: String) : BaseFragment(name) {

    constructor() : this(TAG)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_red, container, false)
    }

    override fun onResume() {
        super.onResume()
        ProgressDialog(context).apply {
            setTitle(R.string.loading)
            setMessage(getString(R.string.wait_please))
            setCancelable(true)
            show()
        }
    }

    companion object {
        val TAG = RedFragment::class.java.simpleName

        @JvmStatic
        fun newInstance() = RedFragment()
    }
}
