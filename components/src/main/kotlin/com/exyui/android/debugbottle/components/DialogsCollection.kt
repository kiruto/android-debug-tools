package com.exyui.android.debugbottle.components

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.exyui.android.debugbottle.components.widgets.DebugToolEditText
import java.util.regex.Pattern

/**
 * Created by yuriel on 8/16/16.
 */
internal object DialogsCollection {
    class RunActivityDialogFragment: DialogFragment() {

        companion object {
            fun newInstance(intent: Intent): RunActivityDialogFragment {
                return (RunActivityDialogFragment()).apply {
                    this.intent = intent
                }
            }
        }

        private var intent: Intent? = null
        private val intentExtras: MutableMap<String, Any> = mutableMapOf()
        private val inflater by lazy {
            activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
            if (null == intent) return null
            for (i in intent?.extras?.keySet()?: setOf()) {
                intentExtras.put(i, intent!!.extras.get(i))
            }
            val content = inflater.inflate(R.layout.__dialog_run_activity, null)
            val builder = AlertDialog.Builder(activity)
                    .setView(content)
                    .setPositiveButton(R.string.__run) { dialog, _ ->
                        for ((k, v) in intentExtras) {
                            when (v) {
                                is Boolean -> intent!!.putExtra(k, v)
                                is Int -> intent!!.putExtra(k, v)
                                is Float -> intent!!.putExtra(k, v)
                                else -> intent!!.putExtra(k, v.toString())
                            }
                        }
                        activity.startActivity(intent)
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.__cancel) { dialog, view ->
                        dialog.dismiss()
                    }
            content.init()

            return builder.create()
        }

        private fun View.init() {
            val content = findViewById<ViewGroup>(R.id.__dt_intent_content)
            val key = findViewById<TextView>(R.id.__dt_intent_key)


            findViewById<TextView>(R.id.__dt_activity_name).apply {
                text = intent?.component?.className?: ""
            }
            findViewById<TextView>(R.id.__dt_title).apply {
                text = getString(R.string.__run_activity_with_intent)
            }

            fun DebugToolEditText.putExtra() {
                if (!key.text.isEmpty() && !text.isEmpty()) {
                    intentExtras.apply {
                        text.toString().let {
                            when {
                                it.equals("true", true) || it.equals("false", true) -> put(key.text.toString(), it.toBoolean())
                                it.isInteger() -> put(key.text.toString(), it.toInt())
                                it.isNumeric() -> put(key.text.toString(), it.toFloat())
                                else -> put(key.text.toString(), it)
                            }
                        }
                    }
                    key.text = ""
                    setText("")
                    updateContentGroup(content)
                }
            }

            val et = findViewById<DebugToolEditText>(R.id.__dt_edit_text).apply {
                addTextChangedListener(object: TextWatcher {
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, after: Int) {
                        if (key.text.isEmpty()) {
                            if (s?.contains(" ") ?: false) {
                                val string = s!!.split(" ")
                                key.text = string[0]
                                setText(string[1])
                            }
                        }
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                    }

                    override fun afterTextChanged(s: Editable?) {

                    }
                })

                onBackPressed {
                    if (key.text.isEmpty()) return@onBackPressed
                    if (text.isEmpty()) {
                        setText(key.text.toString())
                        key.text = ""
                    }
                }

                onSpacePressed {
                    if (!key.text.isEmpty()) return@onSpacePressed
                    key.text = text
                    setText("")
                }

                onEnterPressed {
                    putExtra()
                }
            }

            findViewById<Button>(R.id.__dt_edit_submit).setOnClickListener { et.putExtra() }
        }

        @SuppressLint("SetTextI18n")
        private fun updateContentGroup(parent: ViewGroup) {
            parent.removeAllViews()
            val onDelClickListener = { v: View ->
                @Suppress("UNCHECKED_CAST")
                val p = v.tag as Pair<String, Any>
                intentExtras.remove(p.first)
                updateContentGroup(parent)
            }
            for ((k, v) in intentExtras) {
                val view = inflater.inflate(R.layout.__item_intents_content, parent, false)
                view.findViewById<View>(R.id.__dt_delete).apply {
                    tag = Pair(k, v)
                    setOnClickListener(onDelClickListener)
                }
                view.findViewById<TextView>(R.id.__dt_intent_content_kv).apply { text = "$k: $v" }
                val anyBtn = view.findViewById<RadioButton>(R.id.__dt_radio_any)
//                val stringBtn = view.findViewById(R.id.__dt_radio_string) as RadioButton
                view.findViewById<RadioGroup>(R.id.__dt_radio_group).apply {
                    visibility = View.VISIBLE
                    if (v !is String) {
                        check(R.id.__dt_radio_any)
                        anyBtn.text = v.javaClass.simpleName
                        setOnCheckedChangeListener { _, id ->
                            when(id) {
                                R.id.__dt_radio_any -> intentExtras.put(k, v)
                                R.id.__dt_radio_string -> intentExtras.put(k, v.toString())
                            }
                        }
                    } else {
                        check(R.id.__dt_radio_string)
                        if (v.toUpperCase() == "TRUE" || v.toUpperCase() == "FALSE") {
                            anyBtn.text = "Boolean"
                            setOnCheckedChangeListener { _, id ->
                                when(id) {
                                    R.id.__dt_radio_any -> intentExtras.put(k, v.toBoolean())
                                    R.id.__dt_radio_string -> intentExtras.put(k, v.toString())
                                }
                            }
                        } else if (v.isInteger()) {
                            anyBtn.text = "Int"
                            setOnCheckedChangeListener { _, id ->
                                when(id) {
                                    R.id.__dt_radio_any -> intentExtras.put(k, v.toInt())
                                    R.id.__dt_radio_string -> intentExtras.put(k, v.toString())
                                }
                            }
                        } else if (v.isNumeric()) {
                            anyBtn.text = "Float"
                            setOnCheckedChangeListener { _, id ->
                                when(id) {
                                    R.id.__dt_radio_any -> intentExtras.put(k, v.toFloat())
                                    R.id.__dt_radio_string -> intentExtras.put(k, v.toString())
                                }
                            }
                        } else {
                            visibility = View.GONE
                        }
                    }
                }

                parent.addView(view)
            }
        }

        private fun String.isNumeric(): Boolean {
            val pattern = Pattern.compile("[0-9]*(\\.?)[0-9]*")
            return pattern.matcher(this).matches()
        }

        private fun String.isInteger(): Boolean {
            val pattern = Pattern.compile("^[-\\+]?[\\d]*$")
            return pattern.matcher(this).matches()
        }
    }

    class EditSPDialogFragment: DialogFragment() {

        companion object {
            fun newInstance(key: String, sp: SharedPreferences): EditSPDialogFragment {
                val dialog = EditSPDialogFragment()
                dialog.sp = sp
                dialog.key = key
                return dialog
            }
        }

        private var sp: SharedPreferences? = null
        private var key: String? = null
        private var action: SPDialogAction? = null

        private val inflater by lazy {
            activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
            if (null == sp || null == key) return null
            val content = inflater.inflate(R.layout.__dialog_sp_edit, null)

            val editView = content.findViewById<TextView>(R.id.__dt_sp_value)
            val radio = content.findViewById<RadioGroup>(R.id.__dt_radio_group)

            var value: Any? = null

            // TODO: improve
            for ((k, v) in sp?.all?: mapOf<String, Any>()) {
                if (k == key) {
                    value = v
                    break
                }
            }

            if (value is Boolean) {
                editView.visibility = View.GONE
                radio.visibility = View.VISIBLE
                when(value) {
                    true -> radio.check(R.id.__dt_radio_true)
                    false -> radio.check(R.id.__dt_radio_false)
                }
            } else {
                editView.visibility = View.VISIBLE
                radio.visibility = View.GONE
                editView.text = value.toString()
            }

            when(value) {
                is Int,
                is Float,
                is Double -> editView.setRawInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL)
            }

            content.findViewById<TextView>(R.id.__dt_sp_key).apply { text = key }

            return AlertDialog.Builder(activity)
                    .setView(content)
                    .setNeutralButton(R.string.__delete) { dialog, _ ->
                        sp?.edit()?.remove(key)
                        dialog.dismiss()
                        action?.updateSPViews()
                    }
                    .setNegativeButton(R.string.__cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.__save) { dialog, _ ->
                        try {
                            sp?.edit()?.apply {
                                when (value) {
                                    is Boolean -> {
                                        putBoolean(key, radio.checkedRadioButtonId == R.id.__dt_radio_true)
                                    }
                                    is Int -> putInt(key, editView.text.toString().toInt())
                                    is Float -> putFloat(key, editView.text.toString().toFloat())
                                    is String -> putString(key, editView.text.toString())
                                }
                            }?.apply()?: return@setPositiveButton
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        dialog.dismiss()
                        action?.updateSPViews()
                    }
                    .create()
        }

        @Suppress("DEPRECATION", "OverridingDeprecatedMember")
        override fun onAttach(activity: Activity?) {
            super.onAttach(activity)
            action = activity as SPDialogAction
        }
    }

    interface SPDialogAction {
        fun updateSPViews()
    }
}