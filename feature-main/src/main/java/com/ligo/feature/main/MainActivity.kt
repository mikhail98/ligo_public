package com.ligo.feature.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ligo.common.ViewContainer
import com.ligo.core.PermissionsManager
import com.ligo.feature.main.databinding.ActivityMainBinding
import com.ligo.google.api.IAuthManager
import com.ligo.navigator.api.Target
import com.ligo.tools.api.ILocalizationManager
import com.ligo.tools.api.INotificationManager
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class MainActivity : AppCompatActivity(), ViewContainer {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel by inject<MainActivityViewModel>()

    override val localizationManager by inject<ILocalizationManager>()
    private val googleAuthManager by inject<IAuthManager>()
    private val notificationManager by inject<INotificationManager>()

    private val permissionsManager by lazy { PermissionsManager(this) }

    private val createdCompositeDisposable = CompositeDisposable()
    private val startedCompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        loadKoinModules(MainModule)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        googleAuthManager.createSignInClient(this)
        notificationManager.createNotificationChannel()
        permissionsManager.requestNotificationsPermissions()

        viewModel.setupActivity(this)

        viewModel.navigator.open(Target.Splash)
    }

    override fun onStop() {
        super.onStop()
        startedCompositeDisposable.clear()
    }

    override fun onDestroy() {
        unloadKoinModules(MainModule)
        super.onDestroy()
        viewModel.onDestroy()
        createdCompositeDisposable.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleAuthManager.handleGoogleAuth(requestCode, data)
    }
}