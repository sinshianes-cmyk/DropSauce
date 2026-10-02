package org.koitharu.kotatsu.list.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.util.ext.HapticEffect
import org.koitharu.kotatsu.core.util.ext.getItem
import org.koitharu.kotatsu.core.util.ext.hapticFeedback
import org.koitharu.kotatsu.list.ui.model.MangaDetailedListModel
import org.koitharu.kotatsu.list.ui.model.MangaListModel
import kotlin.math.abs

/**
 * Swipe actions for Details rows. Nothing is ever "committed" in ItemTouchHelper's sense: the action
 * fires on release and the row always settles back (removal arrives through the content flow).
 *
 * Swipe right: remove. Swipe left: mark as read; pull further and it becomes "add to favourites"
 * (only when [allowFavourite] is set).
 */
class MangaSwipeCallback(
	context: Context,
	private val removeIcon: Int = R.drawable.ic_delete,
	private val allowFavourite: Boolean,
	private val onAction: (item: MangaListModel, action: Action) -> Unit,
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

	enum class Action { REMOVE, MARK_READ, FAVOURITE }

	private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
	private val removeDrawable = ContextCompat.getDrawable(context, removeIcon)?.mutate()
	private val readDrawable = ContextCompat.getDrawable(context, R.drawable.ic_eye_check)?.mutate()
	private val favDrawable = ContextCompat.getDrawable(context, R.drawable.ic_heart)?.mutate()

	private var pending: Action? = null
	private var currentItem: MangaListModel? = null
	private var lastZone: Action? = null

	override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
		val item = viewHolder.getItem(MangaListModel::class.java)
		return if (item is MangaDetailedListModel) super.getMovementFlags(recyclerView, viewHolder) else 0
	}

	override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

	// never let ItemTouchHelper dismiss the row
	override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = 10f
	override fun getSwipeEscapeVelocity(defaultValue: Float) = Float.MAX_VALUE
	override fun getSwipeVelocityThreshold(defaultValue: Float) = Float.MAX_VALUE
	override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

	override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
		super.onSelectedChanged(viewHolder, actionState)
		if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && viewHolder != null) {
			currentItem = viewHolder.getItem(MangaListModel::class.java)
			pending = null
			lastZone = null
		} else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
			val item = currentItem
			val action = pending
			if (item != null && action != null) {
				onAction(item, action)
			}
			pending = null
			currentItem = null
		}
	}

	override fun onChildDraw(
		c: Canvas,
		recyclerView: RecyclerView,
		viewHolder: RecyclerView.ViewHolder,
		dX: Float,
		dY: Float,
		actionState: Int,
		isCurrentlyActive: Boolean,
	) {
		val view = viewHolder.itemView
		if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX != 0f) {
			val ratio = abs(dX) / view.width
			val isLeft = dX < 0f
			val zone: Action? = when {
				!isLeft -> if (ratio >= REMOVE_THRESHOLD) Action.REMOVE else null
				allowFavourite && ratio >= FAVOURITE_THRESHOLD -> Action.FAVOURITE
				ratio >= READ_THRESHOLD -> Action.MARK_READ
				else -> null
			}
			if (isCurrentlyActive) {
				pending = zone
				if (zone != null && zone != lastZone) {
					view.hapticFeedback(HapticEffect.CONFIRM)
				}
				lastZone = zone
			}
			val shownAction = zone ?: if (isLeft) Action.MARK_READ else Action.REMOVE
			bgPaint.color = when {
				zone == null -> INACTIVE_BG
				shownAction == Action.REMOVE -> 0xFFD32F2F.toInt()
				shownAction == Action.MARK_READ -> 0xFF1976D2.toInt()
				else -> 0xFFE91E63.toInt()
			}
			val top = view.top.toFloat()
			val bottom = view.bottom.toFloat()
			val left = if (isLeft) view.right + dX else view.left.toFloat()
			val right = if (isLeft) view.right.toFloat() else view.left + dX
			if (right - left > 0f) {
				val radius = (bottom - top) / 2f
				c.drawRoundRect(left, top, right, bottom, radius, radius, bgPaint)
				val icon = when (shownAction) {
					Action.REMOVE -> removeDrawable
					Action.MARK_READ -> readDrawable
					Action.FAVOURITE -> favDrawable
				}
				if (icon != null && right - left >= icon.intrinsicWidth) {
					icon.setTint(android.graphics.Color.WHITE)
					icon.alpha = ((ratio * 4f).coerceAtMost(1f) * 255).toInt()
					val cx = ((left + right) / 2f).toInt()
					val cy = ((top + bottom) / 2f).toInt()
					icon.setBounds(
						cx - icon.intrinsicWidth / 2,
						cy - icon.intrinsicHeight / 2,
						cx + icon.intrinsicWidth / 2,
						cy + icon.intrinsicHeight / 2,
					)
					icon.draw(c)
				}
			}
		}
		super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
	}

	private companion object {
		const val REMOVE_THRESHOLD = 0.4f
		const val READ_THRESHOLD = 0.3f
		const val FAVOURITE_THRESHOLD = 0.65f
		const val INACTIVE_BG = 0xFF757575.toInt()
	}
}
