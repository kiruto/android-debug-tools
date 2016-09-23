package com.exyui.android.debugbottle.components.floating

import android.content.Intent
import android.os.IBinder
import com.exyui.android.debugbottle.components.RunningFeatureMgr

/**
 * Created by yuriel on 9/19/16.
 */
internal class FloatingTestRunnerService: DTBaseFloatingService() {
    override val floatingViewMgr: DTDragFloatingViewMgr = FloatingTestRunnerViewMgr
    override fun onBind(intent: Intent?): IBinder? = null

    override fun createView() {
        super.createView()
        RunningFeatureMgr.add(RunningFeatureMgr.MONKEY_TEST_RUNNER)
    }

    override fun onDestroy() {
        super.onDestroy()
        RunningFeatureMgr.remove(RunningFeatureMgr.MONKEY_TEST_RUNNER)
    }
}