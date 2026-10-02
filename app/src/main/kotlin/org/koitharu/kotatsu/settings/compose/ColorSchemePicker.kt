package org.koitharu.kotatsu.settings.compose

import android.content.Context
import android.content.res.TypedArray
import androidx.appcompat.R as appcompatR
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.border
import kotlin.math.roundToInt
import org.koitharu.kotatsu.core.prefs.ThemeBackground
import org.koitharu.kotatsu.core.ui.image.ThemeBackgroundDrawable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.prefs.ColorScheme
import org.koitharu.kotatsu.core.util.ext.HapticEffect
import org.koitharu.kotatsu.core.util.ext.rememberHapticEffect
import com.google.android.material.R as materialR

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Inline horizontal color-scheme picker that mirrors the legacy ThemeChooserPreference:
 * each entry is a small themed preview card (with a tiny "Abc" + primary/secondary swatches),
 * tap-to-select, check mark on the active one.
 *
 * Lives inline within the AppearanceScreen (not a dialog) — matching the legacy widget's
 * placement on the page.
 */
@Composable
fun ColorSchemePickerRow(
	title: String,
	selectedValue: String,
	onValueChange: (String) -> Unit,
	shape: Shape = MaterialTheme.shapes.medium,
	enabled: Boolean = true,
) {
	val context = LocalContext.current
	val haptic = rememberHapticEffect()
	val groups = remember { ColorScheme.getGroupedList() }
	// Resolving ~80 theme overlays on the main thread froze the page on open; do it off-thread.
	val previews by produceState<Map<ColorScheme, SchemePreviewColors>?>(null, groups) {
		value = withContext(Dispatchers.Default) {
			groups.flatMap { it.second }.associateWith { resolveSchemeColors(context, it) }
		}
	}
	val resolved = previews ?: return

	Surface(
		modifier = Modifier.fillMaxWidth(),
		shape = shape,
		color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.86f),
	) {
		Column(
			modifier = Modifier.padding(vertical = 12.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium,
				modifier = Modifier.padding(horizontal = 16.dp),
			)
			val current = remember(selectedValue) { groups.flatMap { it.second }.firstOrNull { it.name == selectedValue } }
			var pickedFamily by remember { mutableStateOf(current?.family ?: groups.first().first) }
			// Follow external changes (e.g. restored settings) but keep the user's browsing choice otherwise.
			LaunchedEffect(current?.family) { current?.family?.let { pickedFamily = it } }
			val familySchemes = groups.firstOrNull { it.first == pickedFamily }?.second.orEmpty()

			Text(
				text = stringResource(R.string.theme_style_label),
				style = MaterialTheme.typography.labelLarge,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.padding(start = 16.dp, top = 4.dp),
			)
			LazyRow(
				contentPadding = PaddingValues(horizontal = 12.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				items(groups, key = { it.first.name }) { (family, schemes) ->
					// Preview the family with the palette that is currently active, so switching style keeps your colour.
					val rep = schemes.firstOrNull { current != null && it.titleResId == current.titleResId } ?: schemes.first()
					ColorSchemeCard(
						scheme = rep,
						colors = resolved.getValue(rep),
						selected = family == pickedFamily,
						enabled = enabled,
						label = stringResource(family.titleResId),
						onClick = {
							haptic(HapticEffect.CONFIRM)
							pickedFamily = family
							if (current?.family != family) onValueChange(rep.name)
						},
					)
				}
			}

			Text(
				text = stringResource(R.string.theme_colors_label),
				style = MaterialTheme.typography.labelLarge,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.padding(start = 16.dp, top = 4.dp),
			)
			LazyRow(
				contentPadding = PaddingValues(horizontal = 12.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				items(familySchemes, key = { it.name }) { scheme ->
					ColorSchemeCard(
						scheme = scheme,
						colors = resolved.getValue(scheme),
						selected = scheme.name == selectedValue,
						enabled = enabled,
						onClick = {
							haptic(HapticEffect.CONFIRM)
							onValueChange(scheme.name)
						},
					)
				}
			}
		}
	}
}

@Composable
private fun ColorSchemeCard(
	scheme: ColorScheme,
	colors: SchemePreviewColors,
	selected: Boolean,
	enabled: Boolean,
	onClick: () -> Unit,
	label: String? = null,
) {
	val cardShape = RoundedCornerShape(16.dp)
	val borderColor by animateColorAsState(
		targetValue = if (selected) colors.primary else colors.onSurface.copy(alpha = 0.16f),
		label = "cardBorderColor",
	)
	val borderWidth = if (selected) 2.5.dp else 1.dp

	Column(
		modifier = Modifier
			.width(96.dp)
			.clickable(interactionSource = null, indication = null, enabled = enabled, onClick = onClick),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		val art = remember(scheme, colors) { colors.art(scheme) }
		Surface(
			modifier = Modifier.size(width = 88.dp, height = 110.dp),
			shape = cardShape,
			color = if (art != null) Color.Transparent else colors.surface,
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.drawWithCache {
						// Render the art once per size instead of on every frame of a scroll.
						val w = size.width.roundToInt().coerceAtLeast(1)
						val h = size.height.roundToInt().coerceAtLeast(1)
						val bmp = if (art != null) {
							android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888).also {
								art.setBounds(0, 0, w, h)
								art.draw(android.graphics.Canvas(it))
							}.asImageBitmap()
						} else null
						onDrawBehind { if (bmp != null) drawImage(bmp) }
					}
					// Border is drawn above the art so it is always crisp and fully visible.
					.border(borderWidth, borderColor, cardShape)
					.padding(8.dp),
			) {
				Text(
					text = "Abc",
					color = colors.onSurface,
					style = MaterialTheme.typography.titleSmall,
					modifier = Modifier.align(Alignment.TopStart),
				)
				Column(
					modifier = Modifier.align(Alignment.BottomStart),
					verticalArrangement = Arrangement.spacedBy(4.dp),
				) {
					Box(
						modifier = Modifier
							.fillMaxWidth(0.4f)
							.height(6.dp)
							.background(colors.secondary, RoundedCornerShape(4.dp)),
					)
					Box(
						modifier = Modifier
							.fillMaxWidth(0.7f)
							.height(6.dp)
							.background(colors.secondary, RoundedCornerShape(4.dp)),
					)
				}
				Box(
					modifier = Modifier
						.align(Alignment.BottomEnd)
						.size(16.dp)
						.background(colors.primary, RoundedCornerShape(6.dp)),
				)
				if (selected) {
					Icon(
						painter = painterResource(R.drawable.ic_check),
						contentDescription = null,
						tint = colors.primary,
						modifier = Modifier
							.align(Alignment.TopEnd)
							.size(18.dp),
					)
				}
			}
		}
		Spacer(Modifier.height(6.dp))
		Text(
			text = label ?: stringResource(scheme.titleResId),
			style = MaterialTheme.typography.labelSmall,
			fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
			maxLines = 1,
			color = if (enabled) MaterialTheme.colorScheme.onSurface
			else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
		)
	}
}

@Immutable
private data class SchemePreviewColors(
	val primary: Color,
	val secondary: Color,
	val surface: Color,
	val onSurface: Color,
	val background: Color,
	val container: Color,
) {

	/** The scheme's background art, rendered on the card in the scheme's own colours. */
	fun art(scheme: ColorScheme): ThemeBackgroundDrawable? {
		if (scheme.background == ThemeBackground.NONE) return null
		return ThemeBackgroundDrawable(
			style = scheme.background,
			variant = scheme.variant,
			bg = background.toArgb(),
			accent = primary.toArgb(),
			on = onSurface.toArgb(),
			container = container.toArgb(),
			density = 2.5f,
		)
	}
}

/** Resolve a ColorScheme's preview colors by inflating its theme overlay. */
@android.annotation.SuppressLint("ResourceType")
private fun resolveSchemeColors(context: Context, scheme: ColorScheme): SchemePreviewColors {
	val themed = ContextThemeWrapper(context, scheme.styleResId)
	val attrs = intArrayOf(
		appcompatR.attr.colorPrimary,
		materialR.attr.colorSecondary,
		materialR.attr.colorSurfaceContainer,
		materialR.attr.colorOnSurface,
		android.R.attr.colorBackground,
		materialR.attr.colorPrimaryContainer,
	)
	val ta: TypedArray = themed.obtainStyledAttributes(attrs)
	try {
		val primary = ta.getColor(0, 0xFF000000.toInt())
		return SchemePreviewColors(
			primary = Color(primary),
			secondary = Color(ta.getColor(1, primary)),
			surface = Color(ta.getColor(2, 0xFFFFFFFF.toInt())),
			onSurface = Color(ta.getColor(3, 0xFF000000.toInt())),
			background = Color(ta.getColor(4, 0xFFFFFFFF.toInt())),
			container = Color(ta.getColor(5, primary)),
		)
	} finally {
		ta.recycle()
	}
}
