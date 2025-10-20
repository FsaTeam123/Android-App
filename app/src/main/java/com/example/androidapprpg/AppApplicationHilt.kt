package com.example.androidapprpg

import android.app.Application
import com.example.androidapprpg.audio.AppAudioLifecycle
import com.example.androidapprpg.audio.MusicManager
import com.example.androidapprpg.audio.MusicProcessController
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp

class AppApplicationHilt : Application() {

    @Inject lateinit var music: MusicManager
    override fun onCreate() {
        super.onCreate()
        MusicProcessController.ensureAttached(music)
    }

}