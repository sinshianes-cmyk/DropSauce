package org.koitharu.kotatsu.list.ui.adapter

import android.text.format.DateUtils
import androidx.core.view.isVisible
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.list.domain.ReadingProgress
import kotlin.math.roundToInt
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.koitharu.kotatsu.core.model.getTitle
import org.koitharu.kotatsu.core.ui.list.AdapterDelegateClickListenerAdapter
import org.koitharu.kotatsu.core.ui.list.OnListItemClickListener
import org.koitharu.kotatsu.list.ui.model.MangaListModel
import org.koitharu.kotatsu.core.util.ext.textAndVisible
import org.koitharu.kotatsu.databinding.ItemMangaListDetailsBinding
import org.koitharu.kotatsu.list.ui.ListModelDiffCallback
import org.koitharu.kotatsu.list.ui.model.ListModel
import org.koitharu.kotatsu.list.ui.model.MangaDetailedListModel

fun mangaListDetailedItemAD(
	clickListener: MangaDetailsClickListener,
	titleClickListener: OnListItemClickListener<MangaListModel>? = null,
) = adapterDelegateViewBinding<MangaDetailedListModel, ListModel, ItemMangaListDetailsBinding>(
	{ inflater, parent -> ItemMangaListDetailsBinding.inflate(inflater, parent, false) },
) {

	AdapterDelegateClickListenerAdapter(this, clickListener)
		.attach(itemView)
	// Details rows: the text box resumes reading, the cover (and the rest of the row) opens the title page.
	if (titleClickListener != null) {
		binding.buttonContinue.setOnClickListener { view ->
			titleClickListener.onItemClick(item, view)
		}
		binding.layoutText.setOnClickListener { view ->
			titleClickListener.onItemClick(item, view)
		}
		binding.layoutText.setOnLongClickListener { itemView.performLongClick() }
	}

	// Remember the normal colours so recycled rows can be restored after being greyed out.
	val normalTitleColors = binding.textViewTitle.textColors
	val normalChapterColors = binding.textViewChapterName.textColors
	val normalButtonBg = binding.buttonContinue.backgroundTintList
	val normalButtonText = binding.buttonContinue.textColors
	val normalButtonIcon = binding.buttonContinue.iconTint

	bind { payloads ->
		binding.textViewTitle.text = item.title
		val info = item.historyInfo
		// Finished titles (everything read, no next chapter) are shown in grey.
		val isDone = info != null && info.unreadCount == 0 && ReadingProgress.isCompleted(info.percent)
		val grey = com.google.android.material.color.MaterialColors.getColor(
			itemView, com.google.android.material.R.attr.colorOnSurfaceVariant,
		)
		val greyFill = android.content.res.ColorStateList.valueOf(
			androidx.core.graphics.ColorUtils.setAlphaComponent(grey, 40),
		)
		val greyText = android.content.res.ColorStateList.valueOf(
			androidx.core.graphics.ColorUtils.setAlphaComponent(grey, 170),
		)
		binding.textViewTitle.setTextColor(if (isDone) greyText else normalTitleColors)
		binding.textViewChapterName.setTextColor(if (isDone) greyText else normalChapterColors)
		binding.buttonContinue.backgroundTintList = if (isDone) greyFill else normalButtonBg
		binding.buttonContinue.setTextColor(if (isDone) greyText else normalButtonText)
		binding.buttonContinue.iconTint = if (isDone) greyText else normalButtonIcon
		binding.progressHistory.alpha = if (isDone) 0.45f else 1f
		binding.imageViewCover.alpha = if (isDone) 0.7f else 1f
		val isHistory = info != null
		binding.layoutSource.isVisible = !isHistory
		binding.textViewAuthor.isVisible = !isHistory
		binding.textViewTags.isVisible = !isHistory
		binding.textViewChapterName.isVisible = isHistory
		binding.layoutHistoryMeta.isVisible = isHistory
		binding.progressHistory.isVisible = isHistory
		if (info != null) {
			binding.textViewChapterName.text = info.chapterName ?: context.getString(R.string.unknown)
			binding.buttonContinue.text = info.chapterNumber
				?.let { context.getString(R.string.continue_chapter, it) }
				?: context.getString(R.string.continue_reading)
			val lastOpened = DateUtils.getRelativeTimeSpanString(
				info.updatedAt,
				System.currentTimeMillis(),
				DateUtils.MINUTE_IN_MILLIS,
			)
			binding.textViewLastOpened.text = if (info.unreadCount > 0) {
				context.getString(R.string.last_opened_unread, lastOpened, info.unreadCount)
			} else {
				lastOpened
			}
			binding.progressHistory.setProgressCompat(
				if (ReadingProgress.isValid(info.percent)) (info.percent * 100f).roundToInt() else 0,
				true,
			)
		} else {
			binding.textViewSource.text = item.source.getTitle(context)
			binding.imageViewFavicon.setImageAsync(item.source)
			binding.textViewAuthor.textAndVisible = item.manga.authors.joinToString(", ")
			binding.textViewTags.text = item.tags.joinToString(separator = ", ") { it.title ?: "" }
		}
		binding.corners.progressView.setProgress(
			value = item.progress,
			animate = ListModelDiffCallback.PAYLOAD_PROGRESS_CHANGED in payloads,
		)
		binding.iconsViewBottom.setMangaBadges(item.isSaved, item.isFavorite)
		binding.corners.imageViewPin.isVisible = item.isPinned
		binding.imageViewCover.setImageAsync(item.coverUrl, item.manga)
		binding.corners.badge.number = item.counter
		binding.corners.badge.isVisible = item.counter > 0
	}
}
