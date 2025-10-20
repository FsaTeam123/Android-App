package com.example.androidapprpg.audio

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAudioLifecycle @Inject constructor(
    private val music: MusicManager
) : DefaultLifecycleObserver {

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Log.d("AppAudioLifecycle", "registered")
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d("AppAudioLifecycle", "app to FOREGROUND")
        // opcional: se quiser retomar automaticamente quando o app volta
        // if (music.enabled.value) music.resume()
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d("AppAudioLifecycle", "app to BACKGROUND => pause()")
        music.pause()
    }
}
