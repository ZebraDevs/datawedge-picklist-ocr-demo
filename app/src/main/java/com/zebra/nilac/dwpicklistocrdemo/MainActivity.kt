package com.zebra.nilac.dwpicklistocrdemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.zebra.nilac.dwpicklistocrdemo.databinding.ActivityMainBinding
import com.zebra.nilac.dwpicklistocrdemo.util.AppConstants
import com.zebra.nilac.dwpicklistocrdemo.util.DWUtil
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var resultsAdapter: ResultsAdapter

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        registerReceivers()
        setUpAdapter()

        binding.scanButton.setOnClickListener {
            launchScanningSession()
        }

        mainViewModel.processedOutputResult.observe(this, processedResultObserver)

        //Create DW Profile if it doesn't exist already
        sendBroadcast(DWUtil.generateDWBaseProfile(this))
    }

    private fun registerReceivers() {
        val filter = IntentFilter()
        filter.addAction("com.symbol.datawedge.api.RESULT_ACTION")
        filter.addAction(AppConstants.DW_SCANNER_INTENT_ACTION)
        filter.addCategory("android.intent.category.DEFAULT")
        registerReceiver(dwReceiver, filter)
    }

    private fun setUpAdapter() {
        resultsAdapter = ResultsAdapter()
        binding.resultsList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            itemAnimator = DefaultItemAnimator()
            adapter = resultsAdapter
        }
    }

    private fun launchScanningSession() {
        sendBroadcast(Intent().apply {
            setPackage(AppConstants.DATAWEDGE_PACKAGE)
            setAction(AppConstants.DATAWEDGE_API_ACTION)
            putExtra(AppConstants.EXTRA_SOFT_SCAN_TRIGGER, "TOGGLE_SCANNING")
        })
    }

    private val dwReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val extras = intent.extras
            var resultInfo = ""

            if (extras != null && intent.hasExtra("RESULT_LIST")) {
                if (extras.getString(AppConstants.COMMAND_IDENTIFIER_EXTRA)
                        .equals(AppConstants.PROFILE_CREATION_COMMAND_IDENTIFIER)
                ) {
                    val resultList: ArrayList<Bundle> =
                        extras.get("RESULT_LIST") as ArrayList<Bundle>

                    if (resultList.size > 0) {
                        var allSuccess = true

                        // Iterate through the result list for each module
                        for (result in resultList) {
                            val module = result.getString("MODULE")
                            val resultCode = result.getString("RESULT_CODE")
                            val subResultCode = result.getString("SUB_RESULT_CODE")

                            if (result.getString("RESULT").equals("FAILURE")
                                && !module.equals("APP_LIST")
                            ) {
                                // Profile creation failed for the module.
                                // Getting more information on what failed
                                allSuccess = false

                                resultInfo = "Module: $module\n" // Name of the module that failed
                                resultInfo += "Result code: $resultCode\n" // Information on the type of the failure
                                if (!subResultCode.isNullOrEmpty()) // More Information on the failure if exists
                                    resultInfo += "\tSub Result code: $subResultCode\n"
                                break
                            } else {
                                // Profile creation success for the module.
                                resultInfo = "Module: " + result.getString("MODULE") + "\n"
                                resultInfo += "Result: " + result.getString("RESULT") + "\n"
                            }
                        }
                        if (allSuccess) {
                            Log.d(TAG, "Profile created successfully")
                            binding.scanButton.isEnabled = true
                        } else {
                            Log.e(TAG, "Profile creation failed!\n\n$resultInfo")
                        }
                    }
                }
            } else if (extras != null &&
                action.equals(AppConstants.DW_SCANNER_INTENT_ACTION, ignoreCase = true)
            ) {
                val jsonData: String = extras.getString(AppConstants.DATA_TAG)!!
                mainViewModel.parseOCRResult(jsonData)
            }
        }
    }

    private val processedResultObserver: Observer<OutputResult> =
        Observer { result ->
            resultsAdapter.notifyAdapter(result)
            binding.resultsList.scrollToPosition(0)
        }

    companion object {
        const val TAG = "MainActivity"
    }
}