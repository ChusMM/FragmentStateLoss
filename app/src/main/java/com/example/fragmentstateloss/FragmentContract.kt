package com.example.fragmentstateloss

import androidx.fragment.app.Fragment
import java.lang.RuntimeException

fun newFragmentInstanceFromName(name: String): BaseFragment {
    return when(name) {
        RedFragment.TAG -> RedFragment.newInstance()
        BlueFragment.TAG -> BlueFragment.newInstance()
        else -> throw RuntimeException("Not situable fragment from name")
    }
}

open class BaseFragment(val name: String): Fragment()