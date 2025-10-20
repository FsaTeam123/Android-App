package com.example.androidapprpg.audio

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.RawRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.example.androidapprpg.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@Singleton
class MusicManager @Inject constructor(
    private val player: ExoPlayer,
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val ctx: Context
) {
    enum class Track { MENU, BATTLE }

    private val KEY_MUSIC = booleanPreferencesKey("music_enabled")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * ÚNICA fonte da verdade vinda do DataStore.
     * initialValue=false para iniciar mudo até carregar; default lógico = true.
     */
    val enabled: StateFlow<Boolean> =
        dataStore.data
            .map { it[KEY_MUSIC] ?: true }
            .stateIn(scope, SharingStarted.Eagerly, false)

    private var current: Track? = null
    private var fadeJob: Job? = null
    private val defaultTrack = Track.MENU

    init {
        // Reagir automaticamente quando o usuário (re)ativar ou desativar
        scope.launch {
            enabled.drop(1).collect { on ->
                if (on) resume(250, force = true) else fadeOutStop(300)
            }
        }

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                val s = when (state) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> state.toString()
                }
                Log.d("MusicManager", "state=$s enabled=${enabled.value} cur=$current vol=${player.volume}")
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("MusicManager", "Player error: ${error.errorCodeName}", error)
            }
        })
    }

    /** Persiste no DataStore e aplica ação correspondente (fade-out/retomar). */
    fun setEnabled(on: Boolean) {
        scope.launch {
            dataStore.edit { it[KEY_MUSIC] = on }
            // não precisa chamar resume aqui pois o init já observa enabled e reage
        }
    }

    /** Toca a trilha pedida, só se enabled = true. Crossfade simples. */
    fun play(track: Track, loop: Boolean = true, fadeMs: Long = 500) {
        if (!enabled.value) {
            Log.d("MusicManager", "play($track) ignorado: enabled=false (carregando ou desligado)")
            return
        }
        if (current == track && player.isPlaying) return
        current = track

        fadeJob?.cancel()
        fadeJob = scope.launch {
            // mini fade-out antes de trocar
            fadeTo(0f, fadeMs / 2)

            setAndStart(track, loop)

            // volta ao volume suavemente
            player.volume = 0f
            fadeTo(1f, fadeMs / 2)
        }
    }

    fun pause() {
        Log.w("MusicManager", "pause() chamado", Throwable())
        if (player.isPlaying) player.pause()
    }

    /**
     * Retoma a reprodução. Se não houver mídia carregada, toca a trilha atual ou a default.
     * Se force=true, ignora o enabled.value (já garantido por quem chama/observador).
     */
    fun resume(fadeMs: Long = 250, force: Boolean = false) {
        if (!force && !enabled.value) return
        if (player.mediaItemCount == 0 || current == null) {
            play(current ?: defaultTrack, loop = true, fadeMs = fadeMs)
            return
        }
        if (!player.isPlaying) player.play()
        fadeTo(1f, fadeMs)
    }

    fun stop() = fadeOutStop(250)

    // ---------- helpers ----------
    private fun raw(@RawRes id: Int): Uri =
        RawResourceDataSource.buildRawResourceUri(id)

    private fun setAndStart(track: Track, loop: Boolean) {
        val resId = when (track) {
            Track.MENU   -> R.raw.menu_theme
            Track.BATTLE -> R.raw.battle_theme
        }
        val uri = raw(resId)
        player.setMediaItem(MediaItem.fromUri(uri))
        player.repeatMode = if (loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        player.prepare()
        player.play()
    }

    private fun fadeTo(target: Float, durationMs: Long) {
        fadeJob?.cancel()
        fadeJob = scope.launch {
            val steps = 20
            val start = player.volume
            val delta = (target - start) / steps
            repeat(steps) {
                player.volume = (player.volume + delta).coerceIn(0f, 1f)
                delay(maxOf(1L, durationMs / steps))
            }
            player.volume = target
        }
    }

    private fun fadeOutStop(durationMs: Long) {
        fadeJob?.cancel()
        fadeJob = scope.launch {
            val steps = 16
            val start = player.volume
            val delta = start / steps
            repeat(steps) {
                player.volume = (player.volume - delta).coerceIn(0f, 1f)
                delay(maxOf(1L, durationMs / steps))
            }
            player.pause()
            player.seekTo(0)
            // opcional: esquecer a mídia
            // player.clearMediaItems(); current = null
        }
    }
}
