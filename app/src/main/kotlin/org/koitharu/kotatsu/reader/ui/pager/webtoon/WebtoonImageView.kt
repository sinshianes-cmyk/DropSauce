package org.koitharu.kotatsu.reader.ui.pager.webtoon

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import androidx.core.view.ancestors
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import org.koitharu.kotatsu.core.util.ext.resolveDp
import kotlin.math.roundToInt

class WebtoonImageView @JvmOverloads constructor(
	context: Context,
	attr: AttributeSet? = null,
) : SubsamplingScaleImageView(context, attr) {

	private var debugPaint: Paint? = null

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		if (isDebugDrawingEnabled) {
			drawDebug(canvas)
		}
	}

	// Mihon-style: the page is laid out at its full scaled height and the RecyclerView does all the
	// scrolling, so there is no inner scroll state that could get out of sync and make pages jump.
	fun getScroll() = 0

	fun getScrollRange() = 0

	fun scrollBy(delta: Int) = Unit

	fun scrollTo(y: Int) = Unit

	override fun getSuggestedMinimumHeight(): Int {
		var desiredHeight = super.getSuggestedMinimumHeight()
		if (sHeight == 0) {
			val parentHeight = parentHeight()
			if (desiredHeight < parentHeight) {
				desiredHeight = parentHeight
			}
		}
		return desiredHeight
	}

	override fun onMeasure(widthSpec: Int, heightSpec: Int) {
		val widthMode = MeasureSpec.getMode(widthSpec)
		val heightMode = MeasureSpec.getMode(heightSpec)
		val parentWidth = MeasureSpec.getSize(widthSpec)
		val parentHeight = MeasureSpec.getSize(heightSpec)
		val resizeWidth = widthMode != MeasureSpec.EXACTLY
		val resizeHeight = heightMode != MeasureSpec.EXACTLY
		var desiredWidth = parentWidth
		var desiredHeight = parentHeight
		if (sWidth > 0 && sHeight > 0) {
			if (resizeWidth && resizeHeight) {
				desiredWidth = sWidth
				desiredHeight = sHeight
			} else if (resizeHeight) {
				desiredHeight = (sHeight.toDouble() / sWidth.toDouble() * desiredWidth).roundToInt()
			} else if (resizeWidth) {
				desiredWidth = (sWidth.toDouble() / sHeight.toDouble() * desiredHeight).roundToInt()
			}
		}
		desiredWidth = desiredWidth.coerceAtLeast(suggestedMinimumWidth)
		// No cap by the RecyclerView height: tall strips must be measured at their full height.
		desiredHeight = desiredHeight.coerceAtLeast(suggestedMinimumHeight)
		setMeasuredDimension(desiredWidth, desiredHeight)
	}

	override fun onDownSamplingChanged() {
		super.onDownSamplingChanged()
		if (isReady) {
			fitToWidth()
		}
	}

	override fun onReady() {
		super.onReady()
		fitToWidth()
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)
		if (isReady && w != oldw) {
			fitToWidth()
		}
	}

	private fun fitToWidth() {
		if (width == 0 || sWidth == 0) {
			return
		}
		val fit = width / sWidth.toFloat()
		minScale = fit
		maxScale = fit
		minimumScaleType = SCALE_TYPE_CUSTOM
		setScaleAndCenter(fit, PointF(sWidth / 2f, sHeight / 2f))
		requestLayout()
	}

	private fun parentHeight(): Int {
		return ancestors.firstNotNullOfOrNull { it as? RecyclerView }?.height ?: 0
	}

	private fun drawDebug(canvas: Canvas) {
		val paint = debugPaint ?: Paint(Paint.ANTI_ALIAS_FLAG).apply {
			color = android.graphics.Color.RED
			strokeWidth = context.resources.resolveDp(2f)
			textAlign = Paint.Align.LEFT
			textSize = context.resources.resolveDp(14f)
			debugPaint = this
		}
		paint.style = Paint.Style.STROKE
		canvas.drawRect(1f, 1f, width.toFloat() - 1f, height.toFloat() - 1f, paint)
		paint.style = Paint.Style.FILL
		canvas.drawText("${getScroll()} / ${getScrollRange()}", 100f, 100f, paint)
	}
}
