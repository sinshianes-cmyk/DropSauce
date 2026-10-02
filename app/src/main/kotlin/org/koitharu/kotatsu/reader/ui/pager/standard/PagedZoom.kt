package org.koitharu.kotatsu.reader.ui.pager.standard

import android.graphics.PointF
import android.view.animation.DecelerateInterpolator
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kotlin.math.abs

private const val MAX_ZOOM_SCALE = 5f
private const val DOUBLE_TAP_ZOOM_SCALE = 2f
private const val PAN_DURATION = 250L

/**
 * Zoom behaviour ported from Mihon's ReaderPageImageView: up to 5x zoom, a two-level double tap
 * (fit <-> 2x) that zooms around the tapped point, and panning that stays inside the image.
 *
 * Must be called after the zoom mode has set the initial scale. [keepStart] pages start zoomed in,
 * so their max scale is left untouched.
 */
internal fun SubsamplingScaleImageView.applyPagedZoom(keepStart: Boolean) {
	if (sWidth <= 0 || sHeight <= 0 || width == 0 || height == 0) return
	panLimit = SubsamplingScaleImageView.PAN_LIMIT_INSIDE
	doubleTapZoomStyle = SubsamplingScaleImageView.ZOOM_FOCUS_FIXED
	if (keepStart) return
	val fit = minOf(width / sWidth.toFloat(), height / sHeight.toFloat())
	val base = scale.takeIf { it > 0f } ?: fit
	maxScale = maxOf(maxScale, base * MAX_ZOOM_SCALE)
	doubleTapZoomScale = base * DOUBLE_TAP_ZOOM_SCALE
}

/** True if the image extends beyond the view on the side the user wants to move to. */
internal fun SubsamplingScaleImageView.canPan(direction: Int): Boolean {
	if (!isReady || direction == 0) return false
	val c = getCenter() ?: return false
	val scale = scale
	val halfViewWidth = width / (2f * scale)
	val hiddenLeft = (c.x - halfViewWidth) * scale
	val hiddenRight = (sWidth - (c.x + halfViewWidth)) * scale
	return if (direction > 0) hiddenRight > 1f else hiddenLeft > 1f
}

/** Pans by one screen width in [direction] (>0 right, <0 left). Returns false if it can't pan. */
internal fun SubsamplingScaleImageView.panScreen(direction: Int): Boolean {
	if (!canPan(direction)) return false
	val c = getCenter() ?: return false
	val step = width / scale * (if (direction > 0) 1 else -1)
	val target = PointF(c.x + step, c.y)
	animateScaleAndCenter(scale, target)?.apply {
		withDuration(PAN_DURATION)
		withInterpolator(DecelerateInterpolator())
		start()
	}
	return abs(step) > 0f
}
