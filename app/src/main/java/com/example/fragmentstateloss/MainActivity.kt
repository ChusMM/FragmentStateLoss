package com.example.fragmentstateloss

import android.content.res.Configuration
import android.os.Bundle
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue

interface AsyncCallback {
    fun loadFragment(fragment: BaseFragment)
}

class MainActivity : AppCompatActivity(), AsyncCallback {
    companion object {
        private const val QUEUE_EXTRA = "queue_extra"
    }

    @Volatile
    private var isSafeToCommitTransaction = false
    private val listenerReference = WeakReference<AsyncCallback>(this)

    private var transactionsQueue: ConcurrentLinkedQueue<String> = ConcurrentLinkedQueue()

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sync_button.setOnClickListener {
            performTransactionSync()
        }

        async_button.setOnClickListener {
            performTransactionAsync()
        }

        isSafeToCommitTransaction = true
    }

    @Suppress("UNCHECKED_CAST")
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        (savedInstanceState.getSerializable(QUEUE_EXTRA) as? ConcurrentLinkedQueue<String>)?.let { queue ->
            transactionsQueue.addAll(queue)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        isSafeToCommitTransaction = false
        outState.putSerializable(QUEUE_EXTRA, transactionsQueue)
    }

    override fun onResume() {
        super.onResume()
        isSafeToCommitTransaction = true

        this.commitPendingTransactions()
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerReference.clear()
    }

    private fun performTransactionSync() {
        loadFragment(BlueFragment.newInstance())
    }

    private fun performTransactionAsync() {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                sleep()
            }
            listenerReference.get()?.loadFragment(RedFragment.newInstance()) }
    }

    override fun loadFragment(fragment: BaseFragment) {
        when (BuildConfig.COMMIT_FRAGMENT_CONFIG) {
            "sec" -> this.tryToCommitFragmentTransactionSafely(fragment)
            "loss" -> this.commitFragmentTransactionAllowingStateLoss(fragment)
            "non_sec" -> this.commitFragmentTransactionUnsafely(fragment)
        }
    }

    private fun tryToCommitFragmentTransactionSafely(fragment: BaseFragment) {
        val transaction = buildTransaction(fragment, fragment.name)
        if (isSafeToCommitTransaction) {
            transaction.commit()
        } else {
            transactionsQueue.offer(fragment.name)
        }
    }

    private fun commitPendingTransactions() {
        while (transactionsQueue.isNotEmpty() && isSafeToCommitTransaction) {

            transactionsQueue.poll()?.let { name ->
                buildTransaction(newFragmentInstanceFromName(name), name).commit()
            }
        }
    }

    private fun commitFragmentTransactionAllowingStateLoss(fragment: BaseFragment) {
        buildTransaction(fragment, fragment.name).apply {
            commitAllowingStateLoss()
        }
    }

    private fun commitFragmentTransactionUnsafely(fragment: BaseFragment) {
        buildTransaction(fragment, fragment.name).apply {
            commit()
        }
    }

    private fun buildTransaction(fragment: Fragment, tag: String?): FragmentTransaction {
       return supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(null)
    }

    @WorkerThread
    private fun sleep() {
        Thread.sleep(7000)
    }
}
