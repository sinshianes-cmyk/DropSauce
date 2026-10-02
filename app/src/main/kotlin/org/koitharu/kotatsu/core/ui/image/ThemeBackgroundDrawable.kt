package org.koitharu.kotatsu.core.ui.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.animation.ValueAnimator
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.Choreographer
import androidx.annotation.RequiresApi
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.google.android.material.color.MaterialColors
import org.koitharu.kotatsu.core.prefs.ThemeBackground
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Window background: a flat colour plus theme-tinted art. Every [ThemeBackground] style has its own
 * composition and the [variant] reshuffles it (positions, sizes, spacing), so each colour scheme in a
 * family gets its own picture. The art is built from gradients and tiny repeating bitmap shaders,
 * so it costs almost no memory and nothing is re-rendered while scrolling.
 *
 * The random numbers come from a fixed LCG, which keeps the layout stable between launches and
 * identical to the Theme Lab preview.
 */
class ThemeBackgroundDrawable(
	private val style: ThemeBackground,
	private val variant: Int,
	@ColorInt private val bg: Int,
	@ColorInt private val accent: Int,
	@ColorInt private val on: Int,
	@ColorInt private val container: Int,
	private val density: Float,
	/** Blur the art a little and dim it so text stays readable (used for the real window background). */
	private val soften: Boolean = false,
) : Drawable(), Animatable {

	private val ops = ArrayList<(Canvas, Rect) -> Unit>()
	private var animation: ((Canvas, Float, Float) -> Unit)? = null
	private var node: RenderNode? = null
	private var nodeDirty = true
	private var lastFrameNanos = 0L
	private val localBounds = Rect()
	private var running = false
	private var startNanos = 0L
	private var lastTime = 0f
	private val choreographer by lazy { Choreographer.getInstance() }
	private val frameCallback = object : Choreographer.FrameCallback {
		override fun doFrame(frameTimeNanos: Long) {
			if (!running) return
			// ~30 fps is plenty for slow ambient motion and halves the redraw cost of the whole window.
			if (frameTimeNanos - lastFrameNanos >= FRAME_INTERVAL_NANOS) {
				lastFrameNanos = frameTimeNanos
				invalidateSelf()
			}
			choreographer.postFrameCallback(this)
		}
	}

	private class Rng(seed: Int) {
		private var s = (seed * 7919 + 13) and 0x7fffffff

		init {
			repeat(3) { next() }
		}

		fun next(): Float {
			s = ((s.toLong() * 1103515245L + 12345L) and 0x7fffffffL).toInt()
			return s / 2147483648f
		}

		fun range(from: Float, to: Float) = from + (to - from) * next()
	}

	private fun dp(v: Float) = v * density

	private fun alpha(@ColorInt color: Int, a: Float) =
		ColorUtils.setAlphaComponent(color, (a * 255f).roundToInt().coerceIn(0, 255))

	override fun onBoundsChange(bounds: Rect) {
		nodeDirty = true
		ops.clear()
		animation = null
		if (bounds.isEmpty || style == ThemeBackground.NONE) return
		val w = bounds.width().toFloat()
		val h = bounds.height().toFloat()
		val r = Rng(variant * 31 + style.ordinal * 101)
		when (style) {
			ThemeBackground.NONE -> Unit
			ThemeBackground.MEADOW -> meadow(w, h, r)
			ThemeBackground.PHOSPHOR -> phosphor(w, h, r)
			ThemeBackground.PAPER -> paper(w, h, r)
			ThemeBackground.SYNTHWAVE -> synthwave(w, h, r)
			ThemeBackground.PETALS -> petals(w, h, r)
			ThemeBackground.HALFTONE -> halftone(w, h, r)
		}
		animation = buildAnimation(w, h)
	}

	// ---- compositions ---------------------------------------------------------------------

	/** Soft colour blobs, drifting spores, big leaves and a faint dot field. */
	private fun meadow(w: Float, h: Float, r: Rng) {
		val reach = hypot(w, h)
		for (i in 0..2) {
			val cx = w * r.range(0.08f, 0.92f)
			val cy = h * r.range(0.05f, 0.95f)
			val radius = reach * r.range(0.28f, 0.50f)
			blob(cx, cy, radius, if (i % 2 == 0) alpha(container, 0.85f) else alpha(accent, 0.15f))
		}
		for (i in 0..2) {
			ovalAt(w * r.range(0.1f, 0.9f), h * r.range(0.1f, 0.9f), dp(r.range(40f, 70f)), dp(r.range(16f, 26f)), r.range(-70f, 70f), alpha(accent, 0.07f))
		}
		tile(dotTile(dp(22f + (r.next() * 10f).toInt()).roundToInt(), dp(1.4f), alpha(accent, 0.22f)), 0f, 0f)
		for (i in 0..5) {
			circleAt(w * r.next(), h * r.next(), dp(r.range(3f, 9f)), alpha(accent, 0.18f))
		}
	}

	/** CRT code editor: line-number gutter, faint shell output, scanlines and a vignette (no grid). */
	private fun phosphor(w: Float, h: Float, r: Rng) {
		val lines = listOf(
			"\$ ./dropsauce --sync", "[ OK ] library loaded", "[ OK ] 48 titles indexed", "\$ fetch --source all",
			"> chapter 112 ... done", "> chapter 113 ... queued", "\$ tail -f history.log", "read  Lantern Tides   62%",
			"read  Orbit Cafe      34%", "\$ cat theme.conf", "accent = #" + String.format("%06X", accent and 0xFFFFFF),
			"radius = 0", "border = 1px solid", "\$ _",
		)
		val lh = dp(18f)
		val count = ceil(h / lh).toInt() + 1
		val offsets = IntArray(count) { (r.next() * 3f).toInt() }
		rectAt(dp(34f), 0f, density, h, alpha(accent, 0.22f))
		ops += { c, b ->
			val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.MONOSPACE; textSize = dp(11f) }
			for (i in 0 until count) {
				val y = b.top + dp(8f) + i * lh + p.textSize
				p.color = alpha(accent, 0.16f)
				c.drawText("%02d".format((i + 1) % 100), b.left + dp(8f), y, p)
				p.color = alpha(accent, if (i % 3 == 0) 0.20f else 0.14f)
				c.drawText(lines[(i + offsets[i]) % lines.size], b.left + dp(42f), y, p)
			}
		}
		tile(rowTile(dp(3f).roundToInt(), 0, alpha(on, 0.08f)), 0f, 0f)
		ops += { c, b ->
			val cx = b.left + b.width() / 2f
			val cy = b.top + b.height() / 2f
			val radius = hypot(w, h) * 0.6f
			val inner = (min(w, h) * 0.25f / radius).coerceIn(0f, 0.9f)
			val shader = RadialGradient(
				cx, cy, radius,
				intArrayOf(alpha(on, 0f), alpha(on, 0f), alpha(on, 0.18f)),
				floatArrayOf(0f, inner, 1f), Shader.TileMode.CLAMP,
			)
			c.drawRect(b, Paint().apply { this.shader = shader })
		}
	}

	/** Ruled paper with either a column fold or a notebook margin, plus ink stains. */
	private fun paper(w: Float, h: Float, r: Rng) {
		ellipse(w * 0.5f, 0f, w * 0.9f, h * 0.35f, alpha(accent, 0.08f))
		val gap = dp(22f + (r.next() * 6f).toInt())
		tile(rowTile(gap.roundToInt(), gap.roundToInt() - 1, alpha(on, 0.08f)), 0f, dp(8f))
		if (r.next() < 0.6f) {
			rectAt(w / 2f - density, 0f, density * 2f, h, alpha(on, 0.07f))
		} else {
			val mx = w * r.range(0.10f, 0.16f)
			rectAt(mx, 0f, density * 1.5f, h, alpha(accent, 0.25f))
			rectAt(mx + dp(4f), 0f, density, h, alpha(accent, 0.12f))
		}
		for (i in 0..1) {
			circleAt(w * r.next(), h * r.next(), dp(r.range(24f, 60f)), alpha(on, 0.04f))
		}
	}

	/** Synthwave sunset: striped sun, mountains and a glowing horizon. The grid floor is animated. */
	private fun synthwave(w: Float, h: Float, r: Rng) {
		val hy = h * 0.52f
		ops += { c, b ->
			val p = Paint()
			p.shader = LinearGradient(0f, b.top.toFloat(), 0f, b.top + hy, alpha(accent, 0.34f), alpha(container, 0.28f), Shader.TileMode.CLAMP)
			c.drawRect(b.left.toFloat(), b.top.toFloat(), b.right.toFloat(), b.top + hy, p)
		}
		val sr = w * 0.30f
		val scy = hy - sr * 0.35f
		ops += { c, b ->
			c.save()
			c.clipRect(b.left.toFloat(), b.top.toFloat(), b.right.toFloat(), b.top + hy)
			val p = Paint(Paint.ANTI_ALIAS_FLAG)
			p.shader = LinearGradient(0f, b.top + scy - sr, 0f, b.top + scy + sr, alpha(accent, 0.95f), alpha(container, 0.95f), Shader.TileMode.CLAMP)
			c.drawCircle(b.left + w / 2f, b.top + scy, sr, p)
			val cut = Paint().apply { color = bg }
			for (i in 0..6) {
				val sy = scy + sr * (0.05f + i * 0.14f)
				val sh = dp(2f + i * 1.6f)
				c.drawRect(b.left + w / 2f - sr, b.top + sy, b.left + w / 2f + sr, b.top + sy + sh, cut)
			}
			c.restore()
		}
		val peaks = ArrayList<Float>()
		var mx = 0f
		while (mx < w) {
			peaks += dp(r.range(16f, 48f))
			mx += dp(28f)
		}
		ops += { c, b ->
			val path = Path()
			val base = b.top + hy
			path.moveTo(b.left.toFloat(), base)
			peaks.forEachIndexed { i, peak ->
				path.lineTo(b.left + i * dp(28f) + dp(14f), base - peak)
				path.lineTo(b.left + (i + 1) * dp(28f), base)
			}
			path.lineTo(b.right.toFloat(), base)
			path.close()
			c.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = alpha(accent, 0.55f) })
			c.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = alpha(on, 0.35f); style = Paint.Style.STROKE; strokeWidth = density })
		}
		rectAt(0f, hy, w, h - hy, alpha(on, 0.07f))
		ellipse(w * 0.5f, hy, w * 0.7f, dp(26f), alpha(accent, 0.55f))
	}

	/** A pastel wash with scattered petals. */
	private fun petals(w: Float, h: Float, r: Rng) {
		ops += { c, b ->
			val p = Paint()
			p.shader = LinearGradient(0f, b.top.toFloat(), 0f, b.top + b.height() * 0.6f, alpha(container, 0.70f), alpha(container, 0f), Shader.TileMode.CLAMP)
			c.drawRect(b, p)
		}
		val count = 9 + (r.next() * 6f).toInt()
		for (i in 0 until count) {
			val x = w * r.next()
			val y = h * r.next()
			val rad = dp(r.range(7f, 17f))
			val angle = r.range(-60f, 60f)
			ovalAt(x, y, rad * 0.9f, rad * 0.55f, angle, alpha(accent, r.range(0.10f, 0.22f)))
		}
	}

	/** Halftone dots, hazard stripes and an offset slab. */
	private fun halftone(w: Float, h: Float, r: Rng) {
		tile(dotTile(dp(8f + (r.next() * 3f).toInt()).roundToInt(), dp(0.6f), alpha(on, 0.20f)), 0f, 0f)
		tile(hazardTile(dp(24f).roundToInt(), dp(2f), alpha(accent, 0.14f), r.next() < 0.5f), 0f, 0f)
		rectAt(w * r.range(0.45f, 0.75f), h * r.range(0.68f, 0.88f), w * 0.4f, h * 0.12f, alpha(accent, 0.10f))
	}

	// ---- animation ------------------------------------------------------------------------

	/** One falling petal / leaf / glyph column / star, depending on the style that owns it. */
	private class Particle {
		var x0 = 0f
		var y = 0f
		var s = 0f
		var vy = 0f
		var amp = 0f
		var f = 0f
		var ph = 0f
		var rot = 0f
		var rs = 0f
		var fl = 0f
		var fs = 0f
		var a = 0f
		var tw = 0f
		var kind = 0
		var color = 0
	}

	private fun buildAnimation(w: Float, h: Float): ((Canvas, Float, Float) -> Unit)? = when (style) {
		ThemeBackground.PETALS -> petalsAnim(w, h)
		ThemeBackground.MEADOW -> meadowAnim(w, h)
		ThemeBackground.PHOSPHOR -> phosphorAnim(w, h)
		ThemeBackground.SYNTHWAVE -> synthwaveAnim(w, h)
		else -> null
	}

	/** Single sakura petals (notched tips) drifting down, swaying and tumbling behind the content. */
	private fun petalsAnim(w: Float, h: Float): (Canvas, Float, Float) -> Unit {
		val r = Rng(variant * 977 + 5)
		val items = List(34) {
			Particle().apply {
				x0 = r.range(0f, w); y = r.range(-dp(20f), h); s = dp(r.range(9f, 19f)); vy = dp(r.range(28f, 58f))
				amp = dp(r.range(10f, 30f)); f = r.range(0.6f, 1.5f); ph = r.range(0f, TAU); rot = r.range(0f, TAU)
				rs = r.range(-1.4f, 1.4f); fl = r.range(0f, TAU); fs = r.range(1f, 2.6f); a = r.range(0.45f, 0.85f)
				color = if (r.next() < 0.6f) accent else container
			}
		}
		val fill = Paint(Paint.ANTI_ALIAS_FLAG)
		val vein = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = density * 0.7f }
		val path = Path()
		return { c, t, dt ->
			for (p in items) {
				p.y += p.vy * dt
				p.x0 += dp(9f) * dt
				p.rot += p.rs * dt
				if (p.y > h + dp(24f)) {
					p.y = -dp(24f)
					p.x0 = r.range(-dp(30f), w)
				}
				if (p.x0 > w + dp(30f)) p.x0 = -dp(30f)
				val s = p.s
				c.save()
				c.translate(p.x0 + sin(t * p.f + p.ph) * p.amp, p.y)
				c.rotate(p.rot * RAD)
				c.scale(0.55f + 0.45f * abs(cos(t * p.fs + p.fl)), 1f)
				fill.color = alpha(p.color, p.a)
				path.reset()
				path.moveTo(0f, s * 0.6f)
				path.cubicTo(-s * 0.75f, s * 0.2f, -s * 0.5f, -s * 0.6f, -s * 0.1f, -s * 0.6f)
				path.lineTo(0f, -s * 0.42f)
				path.lineTo(s * 0.1f, -s * 0.6f)
				path.cubicTo(s * 0.5f, -s * 0.6f, s * 0.75f, s * 0.2f, 0f, s * 0.6f)
				path.close()
				c.drawPath(path, fill)
				vein.color = alpha(on, p.a * 0.3f)
				c.drawLine(0f, s * 0.5f, 0f, -s * 0.3f, vein)
				c.restore()
			}
		}
	}

	/** Camphor leaves drifting down and glowing forest spores floating up (Totoro). */
	private fun meadowAnim(w: Float, h: Float): (Canvas, Float, Float) -> Unit {
		val r = Rng(variant * 977 + 5)
		val leaves = List(16) {
			Particle().apply {
				x0 = r.range(0f, w); y = r.range(-dp(20f), h); s = dp(r.range(14f, 26f)); vy = dp(r.range(22f, 44f))
				amp = dp(r.range(14f, 34f)); f = r.range(0.4f, 1f); ph = r.range(0f, TAU); rot = r.range(0f, TAU)
				rs = r.range(-0.8f, 0.8f); a = r.range(0.5f, 0.85f); color = if (r.next() < 0.55f) accent else container
			}
		}
		val spores = List(22) {
			Particle().apply {
				x0 = r.range(0f, w); y = r.range(0f, h); s = dp(r.range(3f, 7f)); vy = -dp(r.range(8f, 22f))
				amp = dp(r.range(6f, 18f)); f = r.range(0.3f, 0.9f); ph = r.range(0f, TAU); a = r.range(0.3f, 0.8f)
				tw = r.range(0.8f, 2f)
			}
		}
		val fill = Paint(Paint.ANTI_ALIAS_FLAG)
		val vein = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = density * 0.7f }
		val path = Path()
		return { c, t, dt ->
			for (p in leaves) {
				p.y += p.vy * dt
				p.x0 += dp(6f) * dt
				p.rot += p.rs * dt
				if (p.y > h + dp(24f)) {
					p.y = -dp(24f)
					p.x0 = r.range(-dp(20f), w)
				}
				if (p.x0 > w + dp(30f)) p.x0 = -dp(30f)
				val s = p.s
				c.save()
				c.translate(p.x0 + sin(t * p.f + p.ph) * p.amp, p.y)
				c.rotate((p.rot + sin(t * p.f + p.ph) * 0.5f) * RAD)
				fill.color = alpha(p.color, p.a)
				path.reset()
				path.moveTo(0f, -s)
				path.quadTo(s * 0.75f, -s * 0.15f, 0f, s)
				path.quadTo(-s * 0.75f, -s * 0.15f, 0f, -s)
				c.drawPath(path, fill)
				vein.color = alpha(on, 0.5f * p.a)
				c.drawLine(0f, -s * 0.9f, 0f, s * 0.9f, vein)
				c.restore()
			}
			for (p in spores) {
				p.y += p.vy * dt
				if (p.y < -dp(10f)) {
					p.y = h + dp(10f)
					p.x0 = r.range(0f, w)
				}
				val x = p.x0 + sin(t * p.f + p.ph) * p.amp
				val al = p.a * (0.5f + 0.5f * sin(t * p.tw + p.ph))
				fill.color = alpha(accent, al * 0.20f); c.drawCircle(x, p.y, p.s * 4f, fill)
				fill.color = alpha(accent, al * 0.35f); c.drawCircle(x, p.y, p.s * 2.4f, fill)
				fill.color = alpha(on, al * 0.80f); c.drawCircle(x, p.y, p.s * 0.45f, fill)
			}
		}
	}

	/** Falling glyph columns, a sweeping scan band and a blinking cursor (Terminal). */
	private fun phosphorAnim(w: Float, h: Float): (Canvas, Float, Float) -> Unit {
		val r = Rng(variant * 977 + 5)
		val glyphs = "01{}[]<>/\\|;:\$#%=+*abcdef0123456789"
		val fs = dp(15f)
		val cell = fs + density
		val cols = ceil(w / fs).toInt()
		val rows = ceil(h / cell).toInt() + 2
		val cells = CharArray(rows * cols) { glyphs[(r.next() * glyphs.length).toInt()] }
		val head = FloatArray(cols) { r.range(-rows.toFloat(), rows.toFloat()) }
		val speed = FloatArray(cols) { r.range(5f, 15f) }
		val length = IntArray(cols) { r.range(7f, 20f).toInt() }
		val active = BooleanArray(cols) { r.next() < 0.55f }
		val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.MONOSPACE; textSize = fs }
		val band = dp(160f)
		val bandShader = LinearGradient(
			0f, 0f, 0f, band,
			intArrayOf(alpha(accent, 0f), alpha(accent, 0.09f), alpha(accent, 0.22f)),
			floatArrayOf(0f, 0.85f, 1f), Shader.TileMode.CLAMP,
		)
		val bandPaint = Paint().apply { shader = bandShader }
		val bandMatrix = Matrix()
		val cursor = Paint().apply { color = alpha(accent, 0.55f) }
		return { c, t, dt ->
			repeat(6) { cells[(r.next() * cells.size).toInt().coerceIn(0, cells.size - 1)] = glyphs[(r.next() * glyphs.length).toInt()] }
			for (ci in 0 until cols) {
				if (!active[ci]) continue
				head[ci] += speed[ci] * dt
				if (head[ci] - length[ci] > rows) {
					head[ci] = r.range(-12f, 0f)
					speed[ci] = r.range(5f, 15f)
					length[ci] = r.range(7f, 20f).toInt()
					active[ci] = r.next() < 0.8f
				}
				val hd = floor(head[ci]).toInt()
				for (j in 0 until length[ci]) {
					val rw = hd - j
					if (rw < 0 || rw >= rows) continue
					text.color = if (j == 0) alpha(on, 0.8f) else alpha(accent, 0.55f * (1f - j / length[ci].toFloat()))
					c.drawText(cells, rw * cols + ci, 1, ci * fs, rw * cell + fs, text)
				}
			}
			val by = (t * dp(70f)) % (h + band) - band
			bandMatrix.setTranslate(0f, by)
			bandShader.setLocalMatrix(bandMatrix)
			c.drawRect(0f, by, w, by + band, bandPaint)
			if (floor(t * 1.6f).toInt() % 2 == 0) {
				c.drawRect(dp(12f), h * 0.5f, dp(20f), h * 0.5f + dp(15f), cursor)
			}
		}
	}

	/** A scrolling synthwave floor, twinkling stars, rising pixels and a shooting star (Neon Arcade). */
	private fun synthwaveAnim(w: Float, h: Float): (Canvas, Float, Float) -> Unit {
		val r = Rng(variant * 977 + 5)
		val hy = h * 0.52f
		val stars = List(46) {
			Particle().apply { x0 = r.range(0f, w); y = r.range(0f, hy); s = dp(r.range(1.5f, 3f)); ph = r.range(0f, TAU); f = r.range(1f, 3.5f) }
		}
		val pixels = List(16) {
			Particle().apply {
				x0 = r.range(0f, w); y = r.range(0f, h); s = dp(r.range(3f, 6f)); vy = dp(r.range(10f, 30f))
				a = r.range(0.5f, 0.9f); color = if (r.next() < 0.5f) accent else on
			}
		}
		val line = Paint().apply { style = Paint.Style.STROKE; strokeWidth = density * 1.5f }
		val fill = Paint()
		var shootT = -1f
		var shootX = 0f
		var shootY = 0f
		var shootNext = 2f
		val n = 11
		return { c, t, dt ->
			val off = (t * 0.35f) % 1f
			for (k in 0..n) {
				val u = (k + off) / n
				val y = hy + (h - hy) * u * u
				line.color = alpha(accent, (0.15f + 0.7f * u).coerceAtMost(1f))
				c.drawLine(0f, y, w, y, line)
			}
			line.color = alpha(accent, 0.55f)
			for (k in -9..9) c.drawLine(w / 2f + k * w * 0.012f, hy, w / 2f + k * w * 0.2f, h, line)
			fill.color = alpha(accent, 0.7f)
			c.drawRect(0f, hy, w, hy + density * 1.5f, fill)
			for (p in stars) {
				fill.color = alpha(on, 0.15f + 0.6f * abs(sin(t * p.f + p.ph)))
				c.drawRect(p.x0, p.y, p.x0 + p.s, p.y + p.s, fill)
			}
			for (p in pixels) {
				p.y -= p.vy * dt
				if (p.y < -dp(6f)) {
					p.y = h + dp(6f)
					p.x0 = r.range(0f, w)
				}
				fill.color = alpha(p.color, p.a)
				c.drawRect(p.x0, p.y, p.x0 + p.s, p.y + p.s, fill)
			}
			shootNext -= dt
			if (shootNext <= 0f && shootT < 0f) {
				shootT = 0f; shootX = r.range(w * 0.3f, w); shootY = r.range(dp(10f), hy * 0.6f); shootNext = r.range(4f, 8f)
			}
			if (shootT >= 0f) {
				shootT += dt
				val q = shootT / 0.7f
				if (q >= 1f) {
					shootT = -1f
				} else {
					val sx = shootX - q * dp(180f)
					val sy = shootY + q * dp(80f)
					line.strokeWidth = density * 2f
					for (i in 0 until 6) {
						val a0 = i / 6f
						val a1 = (i + 1) / 6f
						line.color = alpha(on, 0.9f * (1f - q) * (1f - a0))
						c.drawLine(sx + dp(60f) * a0, sy - dp(27f) * a0, sx + dp(60f) * a1, sy - dp(27f) * a1, line)
					}
					line.strokeWidth = density * 1.5f
				}
			}
		}
	}

	// ---- draw ops -------------------------------------------------------------------------

	private fun blob(cx: Float, cy: Float, radius: Float, @ColorInt color: Int) {
		ops += { c, b ->
			val p = Paint()
			p.shader = RadialGradient(b.left + cx, b.top + cy, max(radius, 1f), color, ColorUtils.setAlphaComponent(color, 0), Shader.TileMode.CLAMP)
			c.drawRect(b, p)
		}
	}

	private fun ellipse(cx: Float, cy: Float, rx: Float, ry: Float, @ColorInt color: Int) {
		ops += { c, b ->
			val g = RadialGradient(0f, 0f, 1f, color, ColorUtils.setAlphaComponent(color, 0), Shader.TileMode.CLAMP)
			g.setLocalMatrix(Matrix().apply { postScale(max(rx, 1f), max(ry, 1f)); postTranslate(b.left + cx, b.top + cy) })
			c.drawRect(b, Paint().apply { shader = g })
		}
	}

	private fun tile(bitmap: Bitmap, dx: Float, dy: Float) {
		ops += { c, b ->
			val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
			shader.setLocalMatrix(Matrix().apply { setTranslate(b.left + dx, b.top + dy) })
			c.drawRect(b, Paint().apply { isFilterBitmap = true; this.shader = shader })
		}
	}

	private fun rectAt(x: Float, y: Float, rw: Float, rh: Float, @ColorInt color: Int) {
		ops += { c, b -> c.drawRect(b.left + x, b.top + y, b.left + x + rw, b.top + y + rh, Paint().apply { this.color = color }) }
	}

	private fun circleAt(x: Float, y: Float, radius: Float, @ColorInt color: Int) {
		ops += { c, b -> c.drawCircle(b.left + x, b.top + y, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }) }
	}

	private fun ovalAt(x: Float, y: Float, rx: Float, ry: Float, angle: Float, @ColorInt color: Int) {
		ops += { c, b ->
			c.save()
			c.rotate(angle, b.left + x, b.top + y)
			c.drawOval(RectF(b.left + x - rx, b.top + y - ry, b.left + x + rx, b.top + y + ry), Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
			c.restore()
		}
	}

	// ---- tiles ----------------------------------------------------------------------------

	private fun newTile(w: Int, h: Int): Pair<Bitmap, Canvas> {
		val bmp = Bitmap.createBitmap(max(w, 1), max(h, 1), Bitmap.Config.ARGB_8888)
		return bmp to Canvas(bmp)
	}

	private fun dotTile(size: Int, radius: Float, @ColorInt color: Int): Bitmap {
		val (bmp, c) = newTile(size, size)
		c.drawCircle(size / 2f, size / 2f, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
		return bmp
	}

	private fun gridTile(size: Int, @ColorInt color: Int): Bitmap {
		val (bmp, c) = newTile(size, size)
		val line = max(1f, density * 0.75f)
		val p = Paint().apply { this.color = color }
		c.drawRect(0f, 0f, line, size.toFloat(), p)
		c.drawRect(0f, 0f, size.toFloat(), line, p)
		return bmp
	}

	/** One horizontal line of colour at [row] inside a column [height] pixels tall. */
	private fun rowTile(height: Int, row: Int, @ColorInt color: Int): Bitmap {
		val (bmp, c) = newTile(1, height)
		c.drawRect(0f, row.toFloat(), 1f, row + 1f, Paint().apply { this.color = color })
		return bmp
	}

	private fun hazardTile(size: Int, stroke: Float, @ColorInt color: Int, flip: Boolean): Bitmap {
		val (bmp, c) = newTile(size, size)
		val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
			this.color = color
			strokeWidth = stroke
			style = Paint.Style.STROKE
		}
		val s = size.toFloat()
		// One diagonal that wraps cleanly when the tile repeats.
		if (flip) {
			c.drawLine(-s, -s, s * 2f, s * 2f, p)
		} else {
			c.drawLine(s * 2f, -s, -s, s * 2f, p)
		}
		return bmp
	}

	// ---- Drawable -------------------------------------------------------------------------

	override fun draw(canvas: Canvas) {
		canvas.drawColor(bg)
		val b = bounds
		if (b.isEmpty) return
		if (soften && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && canvas.isHardwareAccelerated) {
			drawStaticBlurred(canvas, b)
		} else {
			drawOps(canvas, b)
		}
		drawAnimation(canvas, b)
		if (soften) canvas.drawColor(alpha(bg, SCRIM_ALPHA))
	}

	private fun drawOps(canvas: Canvas, b: Rect) {
		for (op in ops) op(canvas, b)
	}

	private fun drawAnimation(canvas: Canvas, b: Rect) {
		val frame = animation
		if (frame != null && running) {
			val t = (System.nanoTime() - startNanos) / 1_000_000_000f
			val dt = (t - lastTime).coerceIn(0f, 0.05f)
			lastTime = t
			canvas.save()
			canvas.clipRect(b)
			canvas.translate(b.left.toFloat(), b.top.toFloat())
			frame(canvas, t, dt)
			canvas.restore()
		}
	}

	/** The static art is recorded and blurred once per size change, not on every animation frame. */
	@RequiresApi(Build.VERSION_CODES.S)
	private fun drawStaticBlurred(canvas: Canvas, b: Rect) {
		val n = node ?: RenderNode("theme-background").also {
			val radius = dp(BLUR_DP)
			it.setRenderEffect(RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP))
			node = it
		}
		if (nodeDirty) {
			n.setPosition(0, 0, b.width(), b.height())
			localBounds.set(0, 0, b.width(), b.height())
			val rc = n.beginRecording(b.width(), b.height())
			try {
				drawOps(rc, localBounds)
			} finally {
				n.endRecording()
			}
			nodeDirty = false
		}
		canvas.save()
		canvas.clipRect(b)
		canvas.translate(b.left.toFloat(), b.top.toFloat())
		canvas.drawRenderNode(n)
		canvas.restore()
	}

	/** Starts the moving layer. Does nothing when the system has animations turned off. */
	override fun start() {
		if (running || style == ThemeBackground.NONE || !ValueAnimator.areAnimatorsEnabled()) return
		running = true
		startNanos = System.nanoTime()
		lastTime = 0f
		choreographer.postFrameCallback(frameCallback)
	}

	override fun stop() {
		running = false
		choreographer.removeFrameCallback(frameCallback)
	}

	override fun isRunning() = running

	override fun setAlpha(alpha: Int) = Unit

	override fun setColorFilter(colorFilter: ColorFilter?) = Unit

	@Deprecated("Deprecated in Java")
	override fun getOpacity(): Int = PixelFormat.OPAQUE

	companion object {

		private const val TAU = 6.2831855f
		private const val BLUR_DP = 3.5f
		private const val FRAME_INTERVAL_NANOS = 41_000_000L
		private const val SCRIM_ALPHA = 0.30f
		private const val RAD = 57.29578f

		fun create(context: Context, style: ThemeBackground, variant: Int): ThemeBackgroundDrawable? {
			if (style == ThemeBackground.NONE) return null
			val bg = MaterialColors.getColor(context, android.R.attr.colorBackground, 0xFF000000.toInt())
			val accent = MaterialColors.getColor(context, androidx.appcompat.R.attr.colorPrimary, bg)
			val on = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, accent)
			val container = MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimaryContainer, accent)
			return ThemeBackgroundDrawable(style, variant, bg, accent, on, container, context.resources.displayMetrics.density, soften = true)
		}
	}
}
