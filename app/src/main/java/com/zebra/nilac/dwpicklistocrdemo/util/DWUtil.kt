package com.zebra.nilac.dwpicklistocrdemo.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

object DWUtil {

    private const val TAG = "DWUtil"
    private const val PROFILE_NAME = "PickList OCR Demo"

    const val DW_SCANNER_INTENT_ACTION = "com.zebra.nilac.dwpicklistocrdemo.SCANNER"

    fun generateDWBaseProfile(context: Context): Intent {
        Log.i(TAG, "Creating DW Profile unless it doesn't exists already")

        val bMain = Bundle().apply {
            putString("PROFILE_NAME", PROFILE_NAME)
            putString("PROFILE_ENABLED", "true")
            putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST")
        }

        val configApplicationList = Bundle().apply {
            putString("PACKAGE_NAME", context.packageName)
            putStringArray("ACTIVITY_LIST", arrayOf("*"))
        }

        val intentModuleParamList = Bundle().apply {
            putString("intent_output_enabled", "true")
            putString("intent_action", DW_SCANNER_INTENT_ACTION)
            putInt("intent_delivery", 2)
        }

        val intentModule = Bundle().apply {
            putString("PLUGIN_NAME", "INTENT")
            putString("RESET_CONFIG", "true")
            putBundle("PARAM_LIST", intentModuleParamList)
        }

        val keystrokeModuleParamList = Bundle().apply {
            putString("keystroke_output_enabled", "false")
        }

        val keystrokeModule = Bundle().apply {
            putString("PLUGIN_NAME", "KEYSTROKE")
            putString("RESET_CONFIG", "true")
            putBundle("PARAM_LIST", keystrokeModuleParamList)
        }

        bMain.putParcelableArrayList("PLUGIN_CONFIG", arrayListOf(intentModule, keystrokeModule))
        bMain.putParcelableArray("APP_LIST", arrayOf(configApplicationList))

        return Intent().apply {
            action = "com.symbol.datawedge.api.ACTION"
            setPackage("com.symbol.datawedge")
            putExtra("com.symbol.datawedge.api.SET_CONFIG", bMain)
        }
    }

    fun enablePickListOCR(): Intent {
        val bMain = Bundle().apply {
            putString("PROFILE_NAME", PROFILE_NAME)
            putString("PROFILE_ENABLED", "true")
            putString("CONFIG_MODE", "OVERWRITE")
        }

        val bConfigWorkflow = Bundle()
        val bundlePluginConfig = ArrayList<Bundle>()

        val bPickListOcr = Bundle().apply {
            putString("module_name", "MlKitExModule")
            putBundle("module_params", Bundle().apply {
                putString("session_timeout", "3000") //Integer Range  0 – 60000
                putString("illumination", "off") //on - off
                putString("output_image", "2") // 0 - Disabled, 2 - Cropped Image
                putString(
                    "script",
                    "0"
                ) // 0 - Latin, 1 - Latin & Chinese, 2 - Latin and Japanese, 3 - Latin and Korean, 4 Latin and Devanagari
                putString("confidence_level", "80") // Integer range 0-100
                putString("text_structure", "0") // 0 - Single Word, 1- Single Line
            })
        }

        val bConfigWorkflowParamList = Bundle().apply {
            putString("workflow_name", "picklist_ocr")
            putString("workflow_input_source", "1")
            putParcelableArrayList("workflow_params", arrayListOf(bPickListOcr))

        }
        bConfigWorkflow.apply {
            putString("PLUGIN_NAME", "WORKFLOW")
            putString("RESET_CONFIG", "true")

            putString("workflow_input_enabled", "true")
            putString("selected_workflow_name", "picklist_ocr")
            putString("workflow_input_source", "1") //1 - Imager 2 - Camera

            putParcelableArrayList("PARAM_LIST", arrayListOf(bConfigWorkflowParamList))
        }
        bundlePluginConfig.add(bConfigWorkflow)

        bMain.putParcelableArrayList("PLUGIN_CONFIG", bundlePluginConfig)

        return Intent().apply {
            action = "com.symbol.datawedge.api.ACTION"
            setPackage("com.symbol.datawedge")
            putExtra("com.symbol.datawedge.api.SET_CONFIG", bMain)
        }
    }
}