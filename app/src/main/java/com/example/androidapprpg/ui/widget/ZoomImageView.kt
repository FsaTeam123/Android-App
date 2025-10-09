package com.example.androidapprpg.ui.widget

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

class ZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val matrixValues = FloatArray(9)
    private val imageMatrixInternal = Matrix()
    private val startPoint = PointF()
    private var mode = Mode.NONE

    private val scaleDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scaleFactor = detector.scaleFactor
                val current = currentScale()
                val target = (current * scaleFactor).coerceIn(MIN_SCALE, MAX_SCALE)
                val factor = target / current
                imageMatrixInternal.postScale(factor, factor, detector.focusX, detector.focusY)
                fixTranslation()
                imageMatrix = imageMatrixInternal
                return true
            }
        })

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                reset(animate = false)
                return true
            }
        })

    init {
        scaleType = ScaleType.MATRIX
        imageMatrix = imageMatrixInternal
        post { fitToCenter() }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startPoint.set(event.x, event.y)
                mode = Mode.DRAG
            }
            MotionEvent.ACTION_MOVE -> if (mode == Mode.DRAG && !scaleDetector.isInProgress) {
                val dx = event.x - startPoint.x
                val dy = event.y - startPoint.y
                startPoint.set(event.x, event.y)
                imageMatrixInternal.postTranslate(dx, dy)
                fixTranslation()
                imageMatrix = imageMatrixInternal
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mode = Mode.NONE
            }
        }
        return true
    }

    // Público: zoom pelos botões
    fun zoomIn(step: Float = 1.15f) {
        zoom(step)
    }

    fun zoomOut(step: Float = 1.15f) {
        zoom(1f / step)
    }

    fun reset(animate: Boolean = false) {
        fitToCenter()
    }

    // Helpers
    private fun zoom(multiplier: Float) {
        val current = currentScale()
        val target = (current * multiplier).coerceIn(MIN_SCALE, MAX_SCALE)
        val factor = target / current
        val px = width / 2f
        val py = height / 2f
        imageMatrixInternal.postScale(factor, factor, px, py)
        fixTranslation()
        imageMatrix = imageMatrixInternal
    }

    private fun currentScale(): Float {
        imageMatrixInternal.getValues(matrixValues)
        return matrixValues[Matrix.MSCALE_X]
    }

    private fun fitToCenter() {
        drawable?.let { d ->
            val viewW = width.toFloat()
            val viewH = height.toFloat()
            val imgW = d.intrinsicWidth.toFloat()
            val imgH = d.intrinsicHeight.toFloat()

            imageMatrixInternal.reset()

            // scale mínimo que faz caber por completo
            val scale = min(viewW / imgW, viewH / imgH)
            val dx = (viewW - imgW * scale) / 2f
            val dy = (viewH - imgH * scale) / 2f

            imageMatrixInternal.postScale(scale, scale)
            imageMatrixInternal.postTranslate(dx, dy)
            imageMatrix = imageMatrixInternal
        }
    }


    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (drawable != null && width > 0 && height > 0) post { fitToCenter() }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (drawable != null && w > 0 && h > 0) post { fitToCenter() }
    }

    private fun fixTranslation() {
        // impede “sair” demais da tela
        val rectW = width.toFloat()
        val rectH = height.toFloat()
        val d = drawable ?: return

        // tamanho atual da imagem
        val values = FloatArray(9)
        imageMatrixInternal.getValues(values)
        val scale = values[Matrix.MSCALE_X]
        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]

        val imgW = d.intrinsicWidth * scale
        val imgH = d.intrinsicHeight * scale

        val minX = min(0f, rectW - imgW)
        val maxX = max(0f, rectW - imgW)
        val minY = min(0f, rectH - imgH)
        val maxY = max(0f, rectH - imgH)

        var tx = transX
        var ty = transY
        if (tx < minX) tx = minX
        if (tx > maxX) tx = maxX
        if (ty < minY) ty = minY
        if (ty > maxY) ty = maxY

        values[Matrix.MTRANS_X] = tx
        values[Matrix.MTRANS_Y] = ty
        imageMatrixInternal.setValues(values)
    }

    private enum class Mode { NONE, DRAG }

    companion object {
        private const val MIN_SCALE = 1f
        private const val MAX_SCALE = 5f
    }
}
