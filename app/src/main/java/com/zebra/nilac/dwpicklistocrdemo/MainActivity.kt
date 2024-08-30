package com.zebra.nilac.dwpicklistocrdemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.zebra.nilac.dwpicklistocrdemo.databinding.ActivityMainBinding
import com.zebra.nilac.dwpicklistocrdemo.util.AppConstants
import com.zebra.nilac.dwpicklistocrdemo.util.DWUtil

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        registerReceivers()

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
                            //FIXME remove this once the result is successful
                            binding.scanButton.isEnabled = true
                        }
                    }
                }
            } else if (extras != null && intent.hasExtra("RESULT_INFO")) {
                val result = intent.getStringExtra("RESULT")
                val info = intent.getBundleExtra("RESULT_INFO")

                if (result.equals("FAILURE")) {
                    resultInfo += "Result info: $info\n"
                } else {
                    resultInfo += "Result: $result\n"

                    Log.d(TAG, "Picklist OCR successfully enabled")
                }
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}