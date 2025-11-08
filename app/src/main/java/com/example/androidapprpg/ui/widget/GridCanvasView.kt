package com.example.androidapprpg.ui.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.*

private val TAG = "GridCanvasView"

class GridCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // ================= Aparência do grid =================
    private var cellSizePx = 64f
    private val bgPaint = Paint().apply { color = Color.parseColor("#121212") }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#592E2A28")
        strokeWidth = 1.5f
    }
    private val majorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#99C8A24A")
        strokeWidth = 2f
    }

    // ================= Transformação (pan/zoom) =================
    private var scale = 1.0f
    private var minScale = 0.5f
    private var maxScale = 5.0f
    private var offsetX = 0f
    private var offsetY = 0f

    // =================== Persistência do Canvas ===================
    data class CanvasStyleDTO(
        val strokeColor: Int,
        val strokeWidth: Float,
        val fillColor: Int,
        val textColor: Int,
        val textSize: Float
    )
    data class Pt(val x: Float, val y: Float)

    sealed class ShapeDTO {
        data class Pen(val points: List<Pt>, val style: CanvasStyleDTO): ShapeDTO()
        data class Line(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val style: CanvasStyleDTO): ShapeDTO()
        data class Rect(val left: Float, val top: Float, val right: Float, val bottom: Float, val style: CanvasStyleDTO): ShapeDTO()
        data class Circle(val cx: Float, val cy: Float, val r: Float, val style: CanvasStyleDTO): ShapeDTO()
        data class Text(val x: Float, val y: Float, val text: String, val style: CanvasStyleDTO): ShapeDTO()
    }
    data class CanvasState(val shapes: List<ShapeDTO>)

    // =================== Camada de MAPA ===================
    private var mapBitmap: Bitmap? = null
    private val mapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = 255 }
    private val mapMatrix = Matrix()

    /** Opacidade do mapa (0–255). */
    var mapAlpha: Int
        get() = mapPaint.alpha
        set(value) { mapPaint.alpha = value.coerceIn(0, 255); invalidate() }

    /** Se true, a **grade** é desenhada por cima do mapa (padrão). */
    var drawGridOnTop: Boolean = true

    // ================= Ferramentas =================
    enum class Tool { PAN, PEN, LINE, RECT, CIRCLE, TEXT, SELECT, ERASER }
    private var tool: Tool = Tool.PAN

    // ================= Pincéis globais usados no draw =================
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C8A24A")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33C8A24A")
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EED7A1")
        textSize = 28f
        typeface = Typeface.DEFAULT_BOLD
    }

    // ======== ESTILO ATUAL ESCOLHIDO PELO USUÁRIO ========
    private var currStrokeColor = Color.parseColor("#C8A24A")
    private var currStrokeWidth = 4f
    private var currFillColor   = Color.parseColor("#33C8A24A")
    private var currTextColor   = Color.parseColor("#EED7A1")
    private var currTextSize    = 28f

    // ================= Estilo por forma =================
    private data class Style(
        val strokeColor: Int,
        val strokeWidth: Float,
        val fillColor: Int,
        val textColor: Int,
        val textSize: Float
    ) {
        fun applyTo(stroke: Paint, fill: Paint, tPaint: Paint) {
            stroke.color = strokeColor
            stroke.strokeWidth = strokeWidth
            fill.color = fillColor
            tPaint.color = textColor
            tPaint.textSize = textSize
        }
    }
    private fun currentStyle() = Style(
        currStrokeColor, currStrokeWidth, currFillColor, currTextColor, currTextSize
    )

    // ================= Modelos de formas =================
    private sealed class Shape {
        abstract fun draw(c: Canvas, stroke: Paint, fill: Paint, tPaint: Paint)
        open fun hit(x: Float, y: Float, tol: Float): Boolean = false
        open fun translate(dx: Float, dy: Float) {}
        open fun getBounds(out: RectF): Boolean = false

        data class PenPath(val path: Path, val style: Style, val points: MutableList<Pt>) : Shape() {
            private val bounds = RectF()
            override fun draw(c: Canvas, stroke: Paint, fill: Paint, tPaint: Paint) {
                style.applyTo(stroke, fill, tPaint); c.drawPath(path, stroke)
            }
            override fun hit(x: Float, y: Float, tol: Float): Boolean {
                path.computeBounds(bounds, true); bounds.inset(-tol, -tol); return bounds.contains(x, y)
            }
            override fun translate(dx: Float, dy: Float) { path.offset(dx, dy) }
            override fun getBounds(out: RectF): Boolean { path.computeBounds(out, true); return true }
        }

        data class Line(var x1: Float, var y1: Float, var x2: Float, var y2: Float, val style: Style) : Shape() {
            override fun draw(c: Canvas, stroke: Paint, fill: Paint, tPaint: Paint) {
                style.applyTo(stroke, fill, tPaint); c.drawLine(x1, y1, x2, y2, stroke)
            }
            override fun hit(x: Float, y: Float, tol: Float): Boolean {
                val dx = x2 - x1; val dy = y2 - y1; val len2 = dx*dx + dy*dy
                if (len2 == 0f) return hypot(x - x1, y - y1) <= tol
                var t = ((x - x1) * dx + (y - y1) * dy) / len2; t = t.coerceIn(0f, 1f)
                val px = x1 + t*dx; val py = y1 + t*dy; return hypot(x - px, y - py) <= tol
            }
            override fun translate(dx: Float, dy: Float) { x1 += dx; y1 += dy; x2 += dx; y2 += dy }
            override fun getBounds(out: RectF): Boolean {
                val l = min(x1,x2); val r = max(x1,x2); val t = min(y1,y2); val b = max(y1,y2)
                out.set(l,t,r,b); return true
            }
        }

        data class RectBox(var left: Float, var top: Float, var right: Float, var bottom: Float, val style: Style) : Shape() {
            override fun draw(c: Canvas, stroke: Paint, fill: Paint, tPaint: Paint) {
                style.applyTo(stroke, fill, tPaint)
                val l = min(left,right); val r = max(left,right); val t = min(top,bottom); val b = max(top,bottom)
                c.drawRect(l,t,r,b, fill); c.drawRect(l,t,r,b, stroke)
            }
            override fun hit(x: Float, y: Float, tol: Float): Boolean {
                val l = min(left,right); val r = max(left,right); val t = min(top,bottom); val b = max(top,bottom)
                val nearH = (abs(x-l)<=tol || abs(x-r)<=tol) && y in (t - tol)..(b + tol)
                val nearV = (abs(y-t)<=tol || abs(y-b)<=tol) && x in (l - tol)..(r + tol)
                val inside = x in l..r && y in t..b
                return nearH || nearV || inside
            }
            override fun translate(dx: Float, dy: Float) { left+=dx; right+=dx; top+=dy; bottom+=dy }
            override fun getBounds(out: RectF): Boolean {
                out.set(min(left,right), min(top,bottom), max(left,right), max(top,bottom)); return true
            }
        }

        data class Circle(var cx: Float, var cy: Float, var r: Float, val style: Style) : Shape() {
            override fun draw(c: Canvas, stroke: Paint, fill: Paint, tPaint: Paint) {
                style.applyTo(stroke, fill, tPaint); c.drawCircle(cx, cy, r, fill); c.drawCircle(cx, cy, r, stroke)
            }
            override fun hit(x: Float, y: Float, tol: Float): Boolean {
                val d = hypot(x - cx, y - cy); return abs(d - r) <= tol || d < r
            }
            override fun translate(dx: Float, dy: Float) { cx += dx; cy += dy }
            override fun getBounds(out: RectF): Boolean { out.set(cx - r, cy - r, cx + r, cy + r); return true }
        }

        data class TextRun(var x: Float, var y: Float, val text: String, val style: Style) : Shape() {
            override fun draw(c: Canvas, stroke: Paint, fill: Paint, tPaint: Paint) {
                style.applyTo(stroke, fill, tPaint); c.drawText(text, x, y, tPaint)
            }
            override fun hit(x: Float, y: Float, tol: Float): Boolean {
                val w = text.length * style.textSize * 0.6f; val h = style.textSize
                return x in (this.x - tol)..(this.x + w + tol) && y in (this.y - h - tol)..(this.y + tol)
            }
            override fun translate(dx: Float, dy: Float) { x += dx; y += dy }
            override fun getBounds(out: RectF): Boolean {
                val w = text.length * style.textSize * 0.6f; val h = style.textSize
                out.set(x, y - h, x + w, y); return true
            }
        }
    }

    private val shapes = mutableListOf<Shape>()
    private val redoStack = ArrayDeque<Shape>()
    private var tempShape: Shape? = null
    private var currentPath: Path? = null
    private var lastX = 0f
    private var lastY = 0f

    // ====== Texto: callback para pedir conteúdo
    interface OnRequestTextListener { fun onRequestText(x: Float, y: Float) }
    var requestTextListener: OnRequestTextListener? = null
    private var pendingTextPoint: PointF? = null

    // ====== Seleção ======
    private var selectedIndex: Int? = null
    private var dragLastX = 0f
    private var dragLastY = 0f

    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(12f, 12f), 0f)
    }

    // ================= Gestos =================
    private val scaleDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val prev = scale
                scale = (scale * detector.scaleFactor).coerceIn(minScale, maxScale)
                val fx = detector.focusX; val fy = detector.focusY
                offsetX = (offsetX - fx) * (scale / prev) + fx
                offsetY = (offsetY - fy) * (scale / prev) + fy
                Log.d(TAG, "onScale() scale=$scale off=($offsetX,$offsetY)")
                invalidate()
                notifyTransform()
                return true
            }
        })

    private val gestureDetector = GestureDetector(context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = true
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                if (tool == Tool.PAN) {
                    offsetX -= distanceX; offsetY -= distanceY
                    invalidate()
                    Log.d(TAG, "onScroll() off=($offsetX,$offsetY)")
                    notifyTransform(); return true
                }
                return false
            }
            override fun onDoubleTap(e: MotionEvent): Boolean {
                zoomTo((scale * 1.6f).coerceAtMost(maxScale), e.x, e.y)
                return true
            }
        })

    private fun zoomTo(targetScale: Float, focusX: Float, focusY: Float) {
        val prev = scale
        scale = targetScale
        offsetX = (offsetX - focusX) * (scale / prev) + focusX
        offsetY = (offsetY - focusY) * (scale / prev) + focusY
        Log.d(TAG, "zoomTo() scale=$scale off=($offsetX,$offsetY)")
        invalidate()
        notifyTransform()
    }

    // ================= Touch =================
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        if (scaleDetector.isInProgress || event.pointerCount > 1) {
            if (event.actionMasked == MotionEvent.ACTION_UP) performClick()
            return true
        }

        val wx = screenToWorldX(event.x)
        val wy = screenToWorldY(event.y)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                redoStack.clear()
                when (tool) {
                    Tool.PEN -> {
                        currentPath = Path().apply { moveTo(wx, wy) }
                        tempShape = Shape.PenPath(currentPath!!, currentStyle(), mutableListOf(Pt(wx, wy)))
                        selectedIndex = null
                    }
                    Tool.LINE   -> { tempShape = Shape.Line(wx, wy, wx, wy, currentStyle()); selectedIndex = null }
                    Tool.RECT   -> { tempShape = Shape.RectBox(wx, wy, wx, wy, currentStyle()); selectedIndex = null }
                    Tool.CIRCLE -> { tempShape = Shape.Circle(wx, wy, 0f, currentStyle()); selectedIndex = null }
                    Tool.TEXT -> {
                        selectedIndex = null
                        pendingTextPoint = PointF(wx, wy)
                        requestTextListener?.onRequestText(wx, wy)
                    }
                    Tool.SELECT -> {
                        tempShape = null
                        selectedIndex = findShapeAt(wx, wy)
                        dragLastX = wx; dragLastY = wy
                        invalidate()
                    }
                    Tool.ERASER -> {
                        tempShape = null
                        val idx = findShapeAt(wx, wy)
                        if (idx != null) {
                            shapes.removeAt(idx)
                            selectedIndex = null
                            invalidate()
                        }
                    }
                    Tool.PAN -> { tempShape = null }
                }
                lastX = wx; lastY = wy
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                when (val t = tempShape) {
                    is Shape.PenPath -> {
                        val mx = (lastX + wx) / 2f
                        val my = (lastY + wy) / 2f
                        currentPath?.quadTo(lastX, lastY, mx, my)
                        t.points.add(Pt(wx, wy))
                        lastX = wx; lastY = wy
                    }
                    is Shape.Line   -> { t.x2 = wx; t.y2 = wy }
                    is Shape.RectBox-> { t.right = wx; t.bottom = wy }
                    is Shape.Circle -> { t.r = hypot(wx - t.cx, wy - t.cy) }
                    else -> {
                        if (tool == Tool.SELECT && selectedIndex != null) {
                            val dx = wx - dragLastX; val dy = wy - dragLastY
                            shapes[selectedIndex!!].translate(dx, dy)
                            dragLastX = wx; dragLastY = wy
                        }
                    }
                }
                invalidate()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                when (tool) {
                    Tool.PEN, Tool.LINE, Tool.RECT, Tool.CIRCLE -> {
                        tempShape?.let { shapes.add(it) }
                        tempShape = null
                        currentPath = null
                    }
                    Tool.SELECT, Tool.ERASER -> Unit
                    else -> Unit
                }
                invalidate()
                performClick()
            }
        }
        return true
    }

    override fun performClick(): Boolean = super.performClick()

    // ================= Desenho =================
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // fundo
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scale, scale)

        // --- ordem de camadas ---
        if (!drawGridOnTop) drawGrid(canvas)

        // mapa (defensivo: checar recycled)
        mapBitmap?.let { bmp ->
            if (!bmp.isRecycled) {
                canvas.drawBitmap(bmp, mapMatrix, mapPaint)
            }
        }

        if (drawGridOnTop) drawGrid(canvas)

        // shapes
        shapes.forEach { it.draw(canvas, strokePaint, fillPaint, textPaint) }
        tempShape?.draw(canvas, strokePaint, fillPaint, textPaint)

        // highlight da seleção
        selectedIndex?.let { idx ->
            val r = RectF()
            if (idx in shapes.indices && shapes[idx].getBounds(r)) {
                canvas.drawRect(r, selectionPaint)
            }
        }

        canvas.restore()
    }

    private fun drawGrid(canvas: Canvas) {
        val viewW = width / scale
        val viewH = height / scale
        val left = -offsetX / scale
        val top  = -offsetY / scale

        val startCol = floor(left / cellSizePx).toInt() - 2
        val endCol   = ceil((left + viewW) / cellSizePx).toInt() + 2
        val startRow = floor(top / cellSizePx).toInt() - 2
        val endRow   = ceil((top + viewH) / cellSizePx).toInt() + 2

        for (c in startCol..endCol) {
            val x = c * cellSizePx
            val p = if (c % 5 == 0) majorPaint else gridPaint
            canvas.drawLine(x, startRow * cellSizePx, x, endRow * cellSizePx, p)
        }
        for (r in startRow..endRow) {
            val y = r * cellSizePx
            val p = if (r % 5 == 0) majorPaint else gridPaint
            canvas.drawLine(startCol * cellSizePx, y, endCol * cellSizePx, y, p)
        }
    }

    // ================= APIs públicas =================
    fun setGridSize(dp: Float) {
        val density = resources.displayMetrics.density
        cellSizePx = dp * density
        invalidate()
    }

    fun setScaleLimits(min: Float, max: Float) {
        minScale = min; maxScale = max
    }

    fun resetView() {
        scale = 1f; offsetX = 0f; offsetY = 0f
        Log.d(TAG, "resetView()")
        invalidate()
        notifyTransform()
    }

    fun setTool(t: Tool) { tool = t; invalidate() }
    fun getTool(): Tool = tool

    fun setStrokeColor(color: Int) { currStrokeColor = color; invalidate() }
    fun setFillColor(color: Int)   { currFillColor   = color; invalidate() }
    fun setTextColor(color: Int)   { currTextColor   = color; invalidate() }
    fun setStrokeWidth(px: Float)  { currStrokeWidth = px;    invalidate() }
    fun setTextSize(px: Float)     { currTextSize    = px;    invalidate() }

    /** Cópia defensiva para evitar bitmaps reciclados por loaders (Glide/Coil/etc). */
    private fun defensivelyCopy(bmp: Bitmap): Bitmap {
        val cfg = bmp.config ?: Bitmap.Config.ARGB_8888
        return bmp.copy(cfg, /* mutable = */ false)
    }

    /** Define/atualiza o bitmap do mapa e calcula um fit centralizado no viewport atual. */
    fun setMapBitmap(bmp: Bitmap?) {
        Log.d(TAG, "setMapBitmap() hasBmp=${bmp!=null}, view=$width x $height")
        mapBitmap = bmp?.let { defensivelyCopy(it) }

        val safe = mapBitmap
        if (safe == null || safe.isRecycled) { invalidate(); return }

        if (width == 0 || height == 0) {
            post { refitMapToView() }
            return
        }
        computeMapMatrixForViewport(safe)
        invalidate()
    }

    /** Recalcula o encaixe do mapa considerando o viewport atual (útil após mudanças programáticas de zoom/pan). */
    fun refitMapToView() {
        mapBitmap?.let { if (!it.isRecycled) { computeMapMatrixForViewport(it); invalidate() } }
    }

    private fun computeMapMatrixForViewport(bmp: Bitmap) {
        if (bmp.isRecycled) return
        // viewport em coordenadas DO MUNDO
        val worldW = width  / scale
        val worldH = height / scale
        val worldLeft = -offsetX / scale
        val worldTop  = -offsetY / scale

        val s = min(worldW / bmp.width, worldH / bmp.height).coerceAtLeast(0f)
        val drawW = bmp.width * s
        val drawH = bmp.height * s
        val cx = worldLeft + worldW / 2f
        val cy = worldTop  + worldH / 2f
        val left = cx - drawW / 2f
        val top  = cy - drawH / 2f

        mapMatrix.reset()
        mapMatrix.postScale(s, s)
        mapMatrix.postTranslate(left, top)
    }

    /** Chamado pelo Fragment após o usuário digitar o texto. */
    fun commitText(text: String) {
        val p = pendingTextPoint ?: return
        shapes.add(Shape.TextRun(p.x, p.y, text, currentStyle()))
        pendingTextPoint = null
        invalidate()
    }

    fun undo() {
        if (shapes.isNotEmpty()) {
            val last = shapes.removeAt(shapes.lastIndex)
            redoStack.addLast(last)
            selectedIndex = null
            invalidate()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val last = redoStack.removeLast()
            shapes.add(last)
            selectedIndex = null
            invalidate()
        }
    }

    fun clearCanvas() {
        shapes.clear()
        redoStack.clear()
        tempShape = null
        currentPath = null
        selectedIndex = null
        invalidate()
    }

    /** Libera referência para GC (não chame recycle). */
    fun releaseMap() {
        mapBitmap = null
        invalidate()
    }

    // ================= Helpers =================
    private fun screenToWorldX(x: Float) = (x - offsetX) / scale
    private fun screenToWorldY(y: Float) = (y - offsetY) / scale
    private fun hitTolerance(): Float = 12f / scale

    private fun findShapeAt(wx: Float, wy: Float): Int? {
        val tol = hitTolerance()
        for (i in shapes.size - 1 downTo 0) {
            if (shapes[i].hit(wx, wy, tol)) return i
        }
        return null
    }

    /** Encaixa o mapa na tela com a ORIGEM DO MUNDO (0,0) no CENTRO do bitmap. */
    fun fitMapCenteredOrigin() {
        val bmp = mapBitmap ?: return
        if (bmp.isRecycled) return
        if (width == 0 || height == 0) {
            post { fitMapCenteredOrigin() }
            return
        }

        val s = min(width.toFloat() / bmp.width, height.toFloat() / bmp.height).coerceAtLeast(0.0001f)
        scale = s
        offsetX = width  / 2f
        offsetY = height / 2f

        mapMatrix.reset()
        mapMatrix.postTranslate(-bmp.width / 2f, -bmp.height / 2f)
        Log.d(TAG, "fitMapCenteredOrigin() scale=$scale off=($offsetX,$offsetY)")
        invalidate()
        notifyTransform()
    }

    /** Define o bitmap e já posiciona com a origem do mundo no centro do mapa. */
    fun setMapBitmapCenteredOrigin(bmp: Bitmap?) {
        Log.d(TAG, "setMapBitmapCenteredOrigin() hasBmp=${bmp!=null}, view=$width x $height")
        mapBitmap = bmp?.let { defensivelyCopy(it) }

        val safe = mapBitmap
        if (safe == null || safe.isRecycled) { invalidate(); return }
        if (width == 0 || height == 0) {
            post { fitMapCenteredOrigin() }
            return
        }
        fitMapCenteredOrigin()
    }

    // --- listener de transform ---
    interface OnTransformChangedListener {
        fun onTransformChanged(scale: Float, offsetX: Float, offsetY: Float)
    }
    var transformListener: OnTransformChangedListener? = null

    private fun notifyTransform() {
        Log.d(TAG, "notifyTransform() scale=$scale, off=($offsetX,$offsetY)")
        transformListener?.onTransformChanged(scale, offsetX, offsetY)
    }

    // --- aplicar transform programaticamente (opcional notificar) ---
    fun setTransform(newScale: Float, ox: Float, oy: Float, notify: Boolean = false) {
        Log.d(TAG, "setTransform(newScale=$newScale, ox=$ox, oy=$oy, notify=$notify)")
        scale = newScale.coerceIn(minScale, maxScale)
        offsetX = ox
        offsetY = oy
        invalidate()
        if (notify) notifyTransform()
    }

    // ===== persistência: export/import =====
    private fun Style.toDTO() = CanvasStyleDTO(strokeColor, strokeWidth, fillColor, textColor, textSize)
    private fun CanvasStyleDTO.toStyle() = Style(strokeColor, strokeWidth, fillColor, textColor, textSize)

    fun exportState(): CanvasState {
        val list = shapes.map { s ->
            when (s) {
                is Shape.PenPath -> ShapeDTO.Pen(s.points.toList(), s.style.toDTO())
                is Shape.Line    -> ShapeDTO.Line(s.x1, s.y1, s.x2, s.y2, s.style.toDTO())
                is Shape.RectBox -> ShapeDTO.Rect(s.left, s.top, s.right, s.bottom, s.style.toDTO())
                is Shape.Circle  -> ShapeDTO.Circle(s.cx, s.cy, s.r, s.style.toDTO())
                is Shape.TextRun -> ShapeDTO.Text(s.x, s.y, s.text, s.style.toDTO())
            }
        }
        Log.d(TAG, "exportState() shapes=${list.size}")
        return CanvasState(list)
    }

    fun importState(state: CanvasState?) {
        shapes.clear()
        redoStack.clear()
        tempShape = null
        currentPath = null
        selectedIndex = null

        state?.shapes?.forEach { dto ->
            when (dto) {
                is ShapeDTO.Pen -> {
                    val p = Path()
                    if (dto.points.isNotEmpty()) {
                        p.moveTo(dto.points.first().x, dto.points.first().y)
                        for (i in 1 until dto.points.size) {
                            val pt = dto.points[i]
                            p.lineTo(pt.x, pt.y)
                        }
                    }
                    shapes.add(Shape.PenPath(p, dto.style.toStyle(), dto.points.toMutableList()))
                }
                is ShapeDTO.Line   -> shapes.add(Shape.Line(dto.x1, dto.y1, dto.x2, dto.y2, dto.style.toStyle()))
                is ShapeDTO.Rect   -> shapes.add(Shape.RectBox(dto.left, dto.top, dto.right, dto.bottom, dto.style.toStyle()))
                is ShapeDTO.Circle -> shapes.add(Shape.Circle(dto.cx, dto.cy, dto.r, dto.style.toStyle()))
                is ShapeDTO.Text   -> shapes.add(Shape.TextRun(dto.x, dto.y, dto.text, dto.style.toStyle()))
            }
        }
        Log.d(TAG, "importState() shapes=${shapes.size}")
        invalidate()
    }
}
