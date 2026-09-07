package com.oxygen.weather.app

import android.animation.ValueAnimator

fun interface MotionPreferenceSource {
    fun areAnimationsEnabled(): Boolean
}

object AndroidMotionPreferenceSource : MotionPreferenceSource {
    override fun areAnimationsEnabled(): Boolean = ValueAnimator.areAnimatorsEnabled()
}

object EnabledMotionPreferenceSource : MotionPreferenceSource {
    override fun areAnimationsEnabled(): Boolean = true
}
