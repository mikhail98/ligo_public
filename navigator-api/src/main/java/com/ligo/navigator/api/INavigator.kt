package com.ligo.navigator.api

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

interface INavigator {

    companion object {
        const val EXTRA_PUSH_ARGS = "INavigator.EXTRA_PUSH_ARGS"
    }

    val topLevelFeature: Target

    fun setupActivity(activity: AppCompatActivity)

    fun open(target: Target)

    fun <T : Target> close(target: Class<out T>)

    fun <T : Fragment> closeFragment(fragment: Class<out T>)

    fun provideMainAppIntent(context: Context): Intent
}