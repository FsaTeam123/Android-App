package com.example.androidapprpg.audio

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.*

object MusicProcessController : DefaultLifecycleObserver {

    @Volatile private var attached = false
    private lateinit var music: MusicManager
    private var bgJob: Job? = null

    fun ensureAttached(m: MusicManager) {
        if (attached) return
        attached = true
        music = m
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        // app voltou ao foreground
        bgJob?.cancel()
        music.resume(250, force = true)
    }

    override fun onStop(owner: LifecycleOwner) {
        // nenhuma Activity visível; debounce para não pausar durante navegação
        bgJob?.cancel()
        bgJob = MainScope().launch {
            delay(500)
            val state = ProcessLifecycleOwner.get().lifecycle.currentState
            if (!state.isAtLeast(Lifecycle.State.STARTED)) {
                music.pause()
            }
        }
    }
}
