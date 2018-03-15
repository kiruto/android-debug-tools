package com.exyui.android.debugbottle.components.dialog.quickviews

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import com.exyui.android.debugbottle.components.R
import com.exyui.android.debugbottle.components.injector.QuickEntry
import com.exyui.android.debugbottle.components.openInBrowser

/**
 * Created by yuriel on 10/13/16.
 */
class __QuickDialogMainView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ScrollView(context, attrs, defStyleAttr) {

    private val rootChild by lazy { findViewById<ViewGroup>(R.id.__dt_content)!! }
    private val emptyView by lazy {
        findViewById<View>(R.id.__dt_empty).apply {
            setOnClickListener {
                val url = "https://github.com/kiruto/debug-bottle/blob/1.0.1/demo/src/main/kotlin/me/chunyu/dev/yuriel/kotdebugtool/ContentInjector.kt"
                (context as Activity).openInBrowser(url)
            }
        }
    }
    private val contents by lazy { findViewById<View>(R.id.__dt_content) }
    private val cb:MutableList<(View) -> Unit> = mutableListOf()

    init {
        bindViews()
    }

    private fun bindViews() {
        inflate(context, R.layout.__view_quick_main, this)
        rootChild.generateItemViews()
    }

    private fun ViewGroup.generateItemViews()/*: List<__QuickDialogItemView>*/ {
        if (QuickEntry.isEmpty()) {
            emptyView.visibility = VISIBLE
            contents.visibility = GONE
            return
        } else {
            emptyView.visibility = GONE
            contents.visibility = VISIBLE
        }
        val items = QuickEntry.getList()
        //val result = mutableListOf<__QuickDialogItemView>()
        for ((title, impl) in items) {
            if (!impl.shouldShowEntry(context as Activity)) continue
            //val current = __QuickDialogItemView(context)
            (inflate(context, R.layout.__item_quick_dialog, null) as __QuickDialogItemView).apply {
                this.title = title
                content = impl.description()
                setOnClickListener {
                    impl.run(context)
                    for (c in cb) {
                        c.invoke(it)
                    }
                }
                //result.add(current)
                addView(this)
            }

        }
        //return result
    }

    fun addOnItemClickListener(callback: (View) -> Unit) { cb.add(callback) }
}