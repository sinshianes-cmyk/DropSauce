package org.koitharu.kotatsu.reader.ui.pager.webtoon

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Ported from Mihon: item prefetch is disabled and extra layout space is used so images of nearby
 * pages get laid out (and their real heights known) before they scroll into view.
 */
@Suppress("unused")
class WebtoonLayoutManager : LinearLayoutManager {

	constructor(context: Context) : super(context)
	constructor(
		context: Context,
		orientation: Int,
		reverseLayout: Boolean,
	) : super(context, orientation, reverseLayout)

	constructor(
		context: Context,
		attrs: AttributeSet?,
		defStyleAttr: Int,
		defStyleRes: Int,
	) : super(context, attrs, defStyleAttr, defStyleRes)

	init {
		isItemPrefetchEnabled = false
	}

	override fun calculateExtraLayoutSpace(state: RecyclerView.State, extraLayoutSpace: IntArray) {
		val space = height * 3 / 4
		extraLayoutSpace[0] = space
		extraLayoutSpace[1] = space
	}
}
