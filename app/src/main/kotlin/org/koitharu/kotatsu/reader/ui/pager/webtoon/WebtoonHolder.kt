package org.koitharu.kotatsu.reader.ui.pager.webtoon

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.LifecycleOwner
import org.koitharu.kotatsu.core.exceptions.resolve.ExceptionResolver
import org.koitharu.kotatsu.core.os.NetworkState
import org.koitharu.kotatsu.databinding.ItemPageWebtoonBinding
import org.koitharu.kotatsu.reader.domain.PageLoader
import org.koitharu.kotatsu.reader.ui.config.ReaderSettings
import org.koitharu.kotatsu.reader.ui.pager.BasePageHolder

class WebtoonHolder(
	owner: LifecycleOwner,
	binding: ItemPageWebtoonBinding,
	loader: PageLoader,
	readerSettingsProducer: ReaderSettings.Producer,
	networkState: NetworkState,
	exceptionResolver: ExceptionResolver,
) : BasePageHolder<ItemPageWebtoonBinding>(
	binding = binding,
	loader = loader,
	readerSettingsProducer = readerSettingsProducer,
	networkState = networkState,
	exceptionResolver = exceptionResolver,
	lifecycleOwner = owner,
) {

	override val ssiv = binding.ssiv

	override val requiresAlpha: Boolean = true

	init {
		bindingInfo.progressBar.setVisibilityAfterHide(View.GONE)
	}

	override fun onReady() {
		binding.ssiv.colorFilter = settings.colorFilter?.toColorFilter()
	}

	/** Offset (in px) of the page's top edge above the viewport top, used to save reading position. */
	fun getScrollY() = (-itemView.top).coerceAtLeast(0)

	fun restoreScroll(scroll: Int) {
		if (scroll == 0) return
		val rv = itemView.parent as? RecyclerView ?: return
		val position = bindingAdapterPosition
		if (position == RecyclerView.NO_POSITION) return
		(rv.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(position, -scroll)
	}
}
