package com.example.androidapprpg.dicecore

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.geometry.Geometry
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Rectangle
import org.dyn4j.geometry.Vector2
import org.dyn4j.world.World

class DicePhysicsView @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : SurfaceView(ctx, attrs), SurfaceHolder.Callback {

    // ----------------------------- Parâmetros tunáveis
    /** Fração do menor lado da view usada como raio base. */
    var sizeFactor: Float = 0.16f

    /** Gravidade vertical. (9 ≈ Terra; 160+ cai bem rápido) */
    var gravityY: Double = 280.00
        set(value) { field = value; world.gravity = Vector2(0.0, value) }

    /** Escala da força/impulso inicial horizontal/vertical. */
    var impulseScale: Double = 3.0
    var torqueScale:  Double = 2.2

    /** Menor damping = menos freio (mais velocidade). */
    var linearDamping:  Double = 0.06
    var angularDamping: Double = 0.06

    /** Critérios para considerar “parado”. */
    var settleLinVel2: Double = 24.0   // (velocidade linear)^2
    var settleAngVel:  Double = 0.9
    var settleFrames:  Int    = 6

    /** Aceleração global do tempo e FPS alvo da simulação. */
    var timeScale: Double = 1.8
    var targetFps: Int = 60

    // ----------------------------- API
    var onRollStarted: ((seed: Long) -> Unit)? = null
    var onAllDiceSettled: ((DiceResult) -> Unit)? = null

    fun isRolling(): Boolean = rolling
    fun lastResult(): DiceResult? = currentResult
    fun stop() { loop?.running = false; loop = null }

    /** Inicia a rolagem (sorteio determinístico + cena física). */
    fun roll(spec: DiceSpec) {
        val logical = DiceLogic.roll(spec)       // resultado “oficial”
        currentResult = logical
        post { onRollStarted?.invoke(logical.seed) }

        resetWorld()
        buildBounds()

        // iteradores por tipo para casar corpos -> valores
        val iters = logical.porTipo.mapValues { it.value.iterator() }.toMutableMap()
        val rng = Random(logical.seed)

        spec.counts.forEach { (tipo, qtd) ->
            repeat(qtd) {
                val (body, lados2d, radius) = makeDieBody(tipo)
                world.addBody(body)

                body.linearDamping = linearDamping
                body.angularDamping = angularDamping

                // impulsos iniciais mais fortes
                body.applyImpulse(
                    Vector2(
                        rng.nextDouble(-15.0, 15.0) * impulseScale,
                        rng.nextDouble(-42.0, -28.0) * impulseScale
                    )
                )
                body.applyTorque(rng.nextDouble(-12.0, 12.0) * torqueScale)

                val valor = iters[tipo]?.next() ?: 1
                dice += DiceBody(body, tipo, lados2d, radius.toFloat(), valor)
            }
        }

        rolling = true
        if (loop == null) { loop = Loop(holder).also { it.running = true; it.start() } }
    }

    // ----------------------------- Física / render
    private val world: World<Body> = World<Body>().apply { gravity = Vector2(0.0, gravityY) }

    private var loop: Loop? = null
    private var rolling = false

    private data class DiceBody(
        val body: Body,
        val kind: TipoDado,
        val sides2D: Int,
        val radius: Float,
        val value: Int,
        var settledFrames: Int = 0
    )

    private val dice = mutableListOf<DiceBody>()
    private var currentResult: DiceResult? = null
    private var notified = false

    // ----------------------------- Estilo
    private val bg = Paint().apply { color = Color.parseColor("#0F1116") }
    private val polyFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val polyStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 3f; color = Color.parseColor("#C8A24A")
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK; textAlign = Paint.Align.CENTER; textSize = 28f
    }
    private val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; alpha = 70 }
    private val tmpPath = Path()

    private fun colorFor(kind: TipoDado) = when (kind) {
        TipoDado.D4   -> Color.parseColor("#F4B400")
        TipoDado.D6   -> Color.parseColor("#B39DDB")
        TipoDado.D8   -> Color.parseColor("#66BB6A")
        TipoDado.D10  -> Color.parseColor("#4DD0E1")
        TipoDado.D12  -> Color.parseColor("#9CCC65")
        TipoDado.D20  -> Color.parseColor("#E57373")
        TipoDado.D100 -> Color.parseColor("#29B6F6")
        TipoDado.FUDGE-> Color.parseColor("#FFEE58")
    }

    // ----------------------------- Lifecycle SV
    init { holder.addCallback(this) }

    override fun surfaceCreated(h: SurfaceHolder) {
        if (loop == null) { loop = Loop(h).also { it.running = true; it.start() } }
    }
    override fun surfaceDestroyed(h: SurfaceHolder) { stop() }
    override fun surfaceChanged(h: SurfaceHolder, format: Int, w: Int, hgt: Int) { buildBounds() }

    // ----------------------------- Mundo / bounds
    private fun resetWorld() {
        world.bodies.toList().forEach(world::removeBody)
        dice.clear()
        notified = false
    }

    private fun buildBounds() {
        val w = width.coerceAtLeast(1).toDouble()
        val h = height.coerceAtLeast(1).toDouble()
        val thick = (min(width, height) * 0.08).coerceAtLeast(16.0) // proporcional

        fun wall(cx: Double, cy: Double, rw: Double, rh: Double) {
            val b = Body()
            b.addFixture(BodyFixture(Rectangle(rw, rh)).apply {
                restitution = 0.18; friction = 0.75; density = 0.0
            })
            b.setMass(MassType.INFINITE)
            b.linearDamping = 1.0
            b.angularDamping = 1.0
            b.translate(cx, cy)
            world.addBody(b)
        }
        wall(w/2, h - thick/2, w, thick) // baixo
        wall(w/2, thick/2,     w, thick) // cima
        wall(thick/2, h/2,     thick, h) // esquerda
        wall(w - thick/2, h/2, thick, h) // direita
    }

    private fun makeDieBody(kind: TipoDado): Triple<Body, Int, Double> {
        val baseR = (min(width, height) * sizeFactor).coerceAtLeast(24f)

        val (n, radius) = when (kind) {
            TipoDado.D4   -> 3  to baseR * 1.10f
            TipoDado.D6   -> 4  to baseR * 1.25f
            TipoDado.D8   -> 8  to baseR * 1.20f
            TipoDado.D10  -> 10 to baseR * 1.20f
            TipoDado.D12  -> 12 to baseR * 1.20f
            TipoDado.D20  -> 20 to baseR * 1.20f
            TipoDado.D100 -> 10 to baseR * 1.30f
            TipoDado.FUDGE-> 6  to baseR * 1.15f
        }

        val verts = Array(n) { i ->
            val a = (2.0 * Math.PI * i) / n
            Vector2(radius * cos(a), radius * sin(a))
        }

        val body = Body().apply {
            addFixture(BodyFixture(Geometry.createPolygon(*verts)).apply {
                restitution = 0.18; friction = 0.65; density = 2.5
            })
            setMass(MassType.NORMAL)
            // solta de mais alto para ganhar velocidade
            val x = (width * 0.20 + Math.random() * width * 0.60)
            translate(x, height * 0.16)
        }
        return Triple(body, n, radius.toDouble())
    }

    // ----------------------------- Game loop (timeScale + FPS)
    private inner class Loop(private val sh: SurfaceHolder) : Thread("DiceLoop") {
        @Volatile var running = true
        override fun run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY)

            var last = System.nanoTime()
            while (running) {
                val now  = System.nanoTime()
                val dtRaw = (now - last) / 1_000_000_000.0
                last = now

                val dt = dtRaw.coerceAtMost(1.0 / targetFps) * timeScale
                world.update(dt)

                var allSettled = true
                dice.forEach { d ->
                    val lv = d.body.linearVelocity
                    val av = d.body.angularVelocity
                    val moving = (lv.x * lv.x + lv.y * lv.y) > settleLinVel2 || abs(av) > settleAngVel
                    d.settledFrames = if (moving) 0 else (d.settledFrames + 1)
                    if (d.settledFrames < settleFrames) allSettled = false
                }

                val c = sh.lockCanvas() ?: continue
                try { drawFrame(c) } finally { sh.unlockCanvasAndPost(c) }

                if (rolling && allSettled && !notified) {
                    currentResult?.let { result -> post { onAllDiceSettled?.invoke(result) } }
                    notified = true
                    rolling = false
                }

                // manter FPS alvo
                val frameMs = 1000.0 / targetFps
                val spentMs = (System.nanoTime() - now) / 1_000_000.0
                val remain  = (frameMs - spentMs).toLong()
                if (remain > 0) try { sleep(remain) } catch (_: InterruptedException) {}
            }
        }
    }

    // ----------------------------- Desenho
    private fun drawFrame(c: Canvas) {
        c.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bg)

        dice.forEach { d ->
            val t = d.body.transform
            val x = t.translationX.toFloat()
            val y = t.translationY.toFloat()
            val angDeg = Math.toDegrees(t.rotationAngle).toFloat()

            // sombra
            c.drawOval(
                x - d.radius, y + d.radius * 0.55f,
                x + d.radius, y + d.radius * 0.9f,
                shadow
            )

            c.save()
            c.translate(x, y)
            c.rotate(angDeg)

            polyFill.color = colorFor(d.kind)
            drawRegularPolygon(c, d.sides2D, d.radius, polyFill, polyStroke)

            if (d.settledFrames >= settleFrames) {
                textPaint.textSize = d.radius * 0.75f
                val label = when (d.kind) {
                    TipoDado.FUDGE -> when (d.value) { -1 -> "−"; 0 -> "0"; else -> "+" }
                    else -> d.value.toString()
                }
                c.drawText(label, 0f, d.radius * 0.28f, textPaint)
            }
            c.restore()
        }
    }

    private fun drawRegularPolygon(c: Canvas, n: Int, r: Float, fill: Paint, stroke: Paint) {
        tmpPath.reset()
        for (i in 0 until n) {
            val a = (2.0 * Math.PI * i) / n
            val px = (r * cos(a)).toFloat()
            val py = (r * sin(a)).toFloat()
            if (i == 0) tmpPath.moveTo(px, py) else tmpPath.lineTo(px, py)
        }
        tmpPath.close()
        c.drawPath(tmpPath, fill)
        c.drawPath(tmpPath, stroke)
    }
}
