package com.example.androidapprpg.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.androidapprpg.ui.widget.GridCanvasView
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GameCanvasViewModel @Inject constructor() : ViewModel() {
    private val TAG = "GameCanvasVM"

    data class Transform(val scale: Float, val ox: Float, val oy: Float)

    // Preferências simples
    var tool: GridCanvasView.Tool = GridCanvasView.Tool.PAN; private set
    var mapAlpha: Int = 255; private set
    var drawGridOnTop: Boolean = true; private set

    // Mapa atualmente aplicado no canvas
    var currentMapId: String? = null; private set

    // Estado por mapa
    private val transforms = mutableMapOf<String, Transform>()
    private val canvasStates = mutableMapOf<String, GridCanvasView.CanvasState>()

    fun setTool(t: GridCanvasView.Tool) { tool = t }
    fun setMapAlpha(a: Int) { mapAlpha = a.coerceIn(0,255) }
    fun setGridOnTop(b: Boolean) { drawGridOnTop = b }

    fun setCurrentMap(id: String?) {
        currentMapId = id
        Log.d(TAG, "setCurrentMap($id)")
    }

    fun saveTransform(mapId: String?, scale: Float, ox: Float, oy: Float) {
        if (mapId == null) return
        transforms[mapId] = Transform(scale, ox, oy)
        Log.d(TAG, "saveTransform(mapId=$mapId, s=$scale, off=($ox,$oy))")
    }

    fun getTransform(mapId: String?): Transform? = transforms[mapId]

    fun saveCanvasState(mapId: String?, state: GridCanvasView.CanvasState) {
        if (mapId == null) return
        canvasStates[mapId] = state
        Log.d(TAG, "saveCanvasState(mapId=$mapId, shapes=${state.shapes.size})")
    }

    fun getCanvasState(mapId: String?): GridCanvasView.CanvasState? =
        if (mapId == null) null else canvasStates[mapId]
}