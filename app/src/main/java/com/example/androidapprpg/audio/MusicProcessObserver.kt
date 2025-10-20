package com.example.androidapprpg.audio

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MusicProcessObserver(private val music: MusicManager,private val ctx: CoroutineContext = Dispatchers.Main.immediate) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(SupervisorJob() + ctx)

    override fun onStart(owner: LifecycleOwner) {

        scope.launch {
            if (music.enabled.value) music.resume(250) else music.stop()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        music.pause()
    }
}