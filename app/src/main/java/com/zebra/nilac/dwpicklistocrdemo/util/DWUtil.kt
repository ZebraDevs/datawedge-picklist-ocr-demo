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
            putString("module", "MlKitExModule")
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
                putString(
                    "picklist_mode",
                    "0"
                ) // 0 - OCR or Barcode, 1 - OCR Only, 2 - Barcode Only

                putParcelableArrayList("rules",
                    arrayListOf(
                        Bundle().apply {
                            putParcelableArrayList("rule_list", createOCRRules())
                            putString("rule_param_id", "report_ocr_data")
                        }
                    )
                )
            })
        }

        val bPickListBarcode = Bundle().apply {
            putString("module", "BarcodeDecoderModule")
            putBundle("module_params", Bundle().apply {
                putParcelableArrayList("rules",
                    arrayListOf(
                        Bundle().apply {
                            putParcelableArrayList("rule_list", createBarcodeRules())
                            putString("rule_param_id", "report_barcode_data")
                        }
                    )
                )
            })
        }

        val bConfigWorkflowParamList = Bundle().apply {
            putString("workflow_name", "picklist_ocr")
            putString("workflow_input_source", "1")
            putParcelableArrayList("workflow_params", arrayListOf(bPickListOcr, bPickListBarcode))

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

    private fun createBarcodeRules(): ArrayList<Bundle> {
        val ean8Rule = Bundle().apply {
            putString("rule_name", "EAN8")
            putBundle("criteria", Bundle().apply {
                putParcelableArrayList(
                    "identifier", arrayListOf(
                        Bundle().apply {
                            putString("criteria_key", "starts_with")
                            putString("criteria_value", "58")
                        }
                    ))
                putStringArray("symbology", arrayOf("decoder_ean8"))
            })
            putParcelableArrayList("actions", arrayListOf(
                Bundle().apply {
                    putString("action_key", "report")
                    putString("action_value", "")
                }
            ))
        }
        return arrayListOf(ean8Rule)
    }

    private fun createOCRRules(): ArrayList<Bundle> {
        val testOcrRule = Bundle().apply {
            putString("rule_name", "TestOCR")
            putBundle("criteria", Bundle().apply {
                putParcelableArrayList(
                    "identifier", arrayListOf(
                        Bundle().apply {
                            putString("criteria_key", "min_length")
                            putString("criteria_value", "3")
                        },
                        Bundle().apply {
                            putString("criteria_key", "max_length")
                            putString("criteria_value", "7")
                        },
                        Bundle().apply {
                            putString("criteria_key", "starts_with")
                            putString("criteria_value", "A")
                        },

                        Bundle().apply {
                            putString("criteria_key", "contains")
                            putString("criteria_value", "BA")
                        },

                        Bundle().apply {
                            putString("criteria_key", "ignore_case")
                            putString("criteria_value", "true")
                        })
                )
            })
            putParcelableArrayList("actions", arrayListOf(
                Bundle().apply {
                    putString("action_key", "report")
                    putString("action_value", "")
                }
            ))
        }
        return arrayListOf(testOcrRule)
    }
}