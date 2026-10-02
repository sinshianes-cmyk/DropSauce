package org.koitharu.kotatsu.list.ui.model

import org.koitharu.kotatsu.core.ui.model.MangaOverride
import org.koitharu.kotatsu.core.ui.widgets.ChipsView
import org.koitharu.kotatsu.list.domain.ReadingProgress
import org.koitharu.kotatsu.list.ui.ListModelDiffCallback.Companion.PAYLOAD_ANYTHING_CHANGED
import org.koitharu.kotatsu.list.ui.ListModelDiffCallback.Companion.PAYLOAD_PROGRESS_CHANGED
import org.koitharu.kotatsu.parsers.model.Manga

data class MangaDetailedListModel(
	override val manga: Manga,
	override val override: MangaOverride?,
	val subtitle: String?,
	override val counter: Int,
	val progress: ReadingProgress?,
	val isFavorite: Boolean,
	val isSaved: Boolean,
	val tags: List<ChipsView.ChipModel>,
	val isPinned: Boolean = false,
	val historyInfo: HistoryRowInfo? = null,
) : MangaListModel() {

	override fun getChangePayload(previousState: ListModel): Any? = when {
		previousState !is MangaDetailedListModel || previousState.manga != manga -> null

		previousState.progress != progress || previousState.historyInfo != historyInfo -> PAYLOAD_PROGRESS_CHANGED
		previousState.isFavorite != isFavorite ||
			previousState.isSaved != isSaved ||
			previousState.isPinned != isPinned -> PAYLOAD_ANYTHING_CHANGED

		else -> super.getChangePayload(previousState)
	}
}

/** Extra data shown by the History screen's Details rows instead of source / author / tags. */
data class HistoryRowInfo(
	val chapterName: String?,
	val chapterNumber: String?,
	val percent: Float,
	val updatedAt: Long,
	val unreadCount: Int = 0,
)
