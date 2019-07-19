package com.example.fragmentstateloss

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class BlueFragment(name: String) : BaseFragment(name) {
    constructor() : this(TAG)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blue, container, false)
    }

    companion object {
        val TAG = BlueFragment::class.java.simpleName

        @JvmStatic
        fun newInstance() = BlueFragment()
    }
}
