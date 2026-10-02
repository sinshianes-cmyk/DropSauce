package org.koitharu.kotatsu.core.prefs

import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import com.google.android.material.color.DynamicColors
import org.koitharu.kotatsu.R

@Keep
enum class ColorScheme(
	@StyleRes val styleResId: Int,
	@StringRes val titleResId: Int,
	val family: ThemeFamily = ThemeFamily.MATERIAL,
	val background: ThemeBackground = ThemeBackground.NONE,
	/** Picks the layout of the background art, so every colour scheme gets its own composition. */
	val variant: Int = 0,
) {

	DEFAULT(R.style.ThemeOverlay_Kotatsu_Totoro, R.string.theme_name_totoro),
	EXPRESSIVE(R.style.ThemeOverlay_Kotatsu_Expressive, R.string.theme_name_expressive),
	MIKU(R.style.ThemeOverlay_Kotatsu_Miku, R.string.theme_name_miku),
	RENA(R.style.ThemeOverlay_Kotatsu_Asuka, R.string.theme_name_asuka),
	FROG(R.style.ThemeOverlay_Kotatsu_Mion, R.string.theme_name_mion),
	BLUEBERRY(R.style.ThemeOverlay_Kotatsu_Rikka, R.string.theme_name_rikka),
	SAKURA(R.style.ThemeOverlay_Kotatsu_Sakura, R.string.theme_name_sakura),
	MAMIMI(R.style.ThemeOverlay_Kotatsu_Mamimi, R.string.theme_name_mamimi),
	KANADE(R.style.ThemeOverlay_Kotatsu_Kanade, R.string.theme_name_kanade),
	ITSUKA(R.style.ThemeOverlay_Kotatsu_Itsuka, R.string.theme_name_itsuka),
	SHANA(R.style.ThemeOverlay_Kotatsu_Shana, R.string.theme_name_shana),
	LIME(R.style.ThemeOverlay_Kotatsu_Lime, R.string.theme_name_lime),
	AMBER(R.style.ThemeOverlay_Kotatsu_Amber, R.string.theme_name_amber),
	SKY(R.style.ThemeOverlay_Kotatsu_Sky, R.string.theme_name_sky),

	// Totoro family
	TOTORO_CLASSIC(R.style.ThemeOverlay_Kotatsu_Totoro_Classic, R.string.theme_name_classic, ThemeFamily.TOTORO, ThemeBackground.MEADOW, 0),
	TOTORO_DEFAULT(R.style.ThemeOverlay_Kotatsu_Totoro_Totoro, R.string.theme_name_totoro, ThemeFamily.TOTORO, ThemeBackground.MEADOW, 1),
	TOTORO_MIKU(R.style.ThemeOverlay_Kotatsu_Totoro_Miku, R.string.theme_name_miku, ThemeFamily.TOTORO, ThemeBackground.MEADOW, 2),
	TOTORO_RENA(R.style.ThemeOverlay_Kotatsu_Totoro_Asuka, R.string.theme_name_asuka, ThemeFamily.TOTORO, ThemeBackground.MEADOW, 3),
	TOTORO_FROG(R.style.ThemeOverlay_Kotatsu_Totoro_Mion, R.string.theme_name_mion, ThemeFamily.TOTORO, ThemeBackground.MEADOW, 4),
	TOTORO_BLUEBERRY(R.style.ThemeOverlay_Kotatsu_Totoro_Rikka, R.string.theme_name_rikka, ThemeFamily.TOTORO, ThemeBackground.MEADOW, 5),
	TOTORO_SAKURA(R.style.ThemeOverlay_Kotatsu_Totoro_Sakura, R.string.theme_name_sakura, ThemeFamily.TOTORO, ThemeBackground.MEADOW, 6),
	TOTORO_MAMIMI(R.style.ThemeOverlay_Kotatsu_Totoro_Mamimi, R.string.theme_name_mamimi, ThemeFamily.TOTORO, ThemeBackground.MEADOW, 7),
	TOTORO_KANADE(R.style.ThemeOverlay_Kotatsu_Totoro_Kanade, R.string.theme_name_kanade, ThemeFamily.TOTORO, ThemeBackground.MEADOW, 8),
	TOTORO_ITSUKA(R.style.ThemeOverlay_Kotatsu_Totoro_Itsuka, R.string.theme_name_itsuka, ThemeFamily.TOTORO, ThemeBackground.MEADOW, 9),
	TOTORO_SHANA(R.style.ThemeOverlay_Kotatsu_Totoro_Shana, R.string.theme_name_shana, ThemeFamily.TOTORO, ThemeBackground.MEADOW, 10),
	TOTORO_LIME(R.style.ThemeOverlay_Kotatsu_Totoro_Lime, R.string.theme_name_lime, ThemeFamily.TOTORO, ThemeBackground.MEADOW, 11),
	TOTORO_AMBER(R.style.ThemeOverlay_Kotatsu_Totoro_Amber, R.string.theme_name_amber, ThemeFamily.TOTORO, ThemeBackground.MEADOW, 12),
	TOTORO_SKY(R.style.ThemeOverlay_Kotatsu_Totoro_Sky, R.string.theme_name_sky, ThemeFamily.TOTORO, ThemeBackground.MEADOW, 13),

	// Terminal family
	TERMINAL_CLASSIC(R.style.ThemeOverlay_Kotatsu_Terminal_Classic, R.string.theme_name_classic, ThemeFamily.TERMINAL, ThemeBackground.PHOSPHOR, 0),
	TERMINAL_DEFAULT(R.style.ThemeOverlay_Kotatsu_Terminal_Totoro, R.string.theme_name_totoro, ThemeFamily.TERMINAL, ThemeBackground.PHOSPHOR, 1),
	TERMINAL_MIKU(R.style.ThemeOverlay_Kotatsu_Terminal_Miku, R.string.theme_name_miku, ThemeFamily.TERMINAL, ThemeBackground.PHOSPHOR, 2),
	TERMINAL_RENA(R.style.ThemeOverlay_Kotatsu_Terminal_Asuka, R.string.theme_name_asuka, ThemeFamily.TERMINAL, ThemeBackground.PHOSPHOR, 3),
	TERMINAL_FROG(R.style.ThemeOverlay_Kotatsu_Terminal_Mion, R.string.theme_name_mion, ThemeFamily.TERMINAL, ThemeBackground.PHOSPHOR, 4),
	TERMINAL_BLUEBERRY(R.style.ThemeOverlay_Kotatsu_Terminal_Rikka, R.string.theme_name_rikka, ThemeFamily.TERMINAL, ThemeBackground.PHOSPHOR, 5),
	TERMINAL_SAKURA(R.style.ThemeOverlay_Kotatsu_Terminal_Sakura, R.string.theme_name_sakura, ThemeFamily.TERMINAL, ThemeBackground.PHOSPHOR, 6),
	TERMINAL_MAMIMI(R.style.ThemeOverlay_Kotatsu_Terminal_Mamimi, R.string.theme_name_mamimi, ThemeFamily.TERMINAL, ThemeBackground.PHOSPHOR, 7),
	TERMINAL_KANADE(R.style.ThemeOverlay_Kotatsu_Terminal_Kanade, R.string.theme_name_kanade, ThemeFamily.TERMINAL, ThemeBackground.PHOSPHOR, 8),
	TERMINAL_ITSUKA(R.style.ThemeOverlay_Kotatsu_Terminal_Itsuka, R.string.theme_name_itsuka, ThemeFamily.TERMINAL, ThemeBackground.PHOSPHOR, 9),
	TERMINAL_SHANA(R.style.ThemeOverlay_Kotatsu_Terminal_Shana, R.string.theme_name_shana, ThemeFamily.TERMINAL, ThemeBackground.PHOSPHOR, 10),
	TERMINAL_LIME(R.style.ThemeOverlay_Kotatsu_Terminal_Lime, R.string.theme_name_lime, ThemeFamily.TERMINAL, ThemeBackground.PHOSPHOR, 11),
	TERMINAL_AMBER(R.style.ThemeOverlay_Kotatsu_Terminal_Amber, R.string.theme_name_amber, ThemeFamily.TERMINAL, ThemeBackground.PHOSPHOR, 12),
	TERMINAL_SKY(R.style.ThemeOverlay_Kotatsu_Terminal_Sky, R.string.theme_name_sky, ThemeFamily.TERMINAL, ThemeBackground.PHOSPHOR, 13),

	// Broadsheet family
	BROADSHEET_CLASSIC(R.style.ThemeOverlay_Kotatsu_Broadsheet_Classic, R.string.theme_name_classic, ThemeFamily.BROADSHEET, ThemeBackground.PAPER, 0),
	BROADSHEET_DEFAULT(R.style.ThemeOverlay_Kotatsu_Broadsheet_Totoro, R.string.theme_name_totoro, ThemeFamily.BROADSHEET, ThemeBackground.PAPER, 1),
	BROADSHEET_MIKU(R.style.ThemeOverlay_Kotatsu_Broadsheet_Miku, R.string.theme_name_miku, ThemeFamily.BROADSHEET, ThemeBackground.PAPER, 2),
	BROADSHEET_RENA(R.style.ThemeOverlay_Kotatsu_Broadsheet_Asuka, R.string.theme_name_asuka, ThemeFamily.BROADSHEET, ThemeBackground.PAPER, 3),
	BROADSHEET_FROG(R.style.ThemeOverlay_Kotatsu_Broadsheet_Mion, R.string.theme_name_mion, ThemeFamily.BROADSHEET, ThemeBackground.PAPER, 4),
	BROADSHEET_BLUEBERRY(R.style.ThemeOverlay_Kotatsu_Broadsheet_Rikka, R.string.theme_name_rikka, ThemeFamily.BROADSHEET, ThemeBackground.PAPER, 5),
	BROADSHEET_SAKURA(R.style.ThemeOverlay_Kotatsu_Broadsheet_Sakura, R.string.theme_name_sakura, ThemeFamily.BROADSHEET, ThemeBackground.PAPER, 6),
	BROADSHEET_MAMIMI(R.style.ThemeOverlay_Kotatsu_Broadsheet_Mamimi, R.string.theme_name_mamimi, ThemeFamily.BROADSHEET, ThemeBackground.PAPER, 7),
	BROADSHEET_KANADE(R.style.ThemeOverlay_Kotatsu_Broadsheet_Kanade, R.string.theme_name_kanade, ThemeFamily.BROADSHEET, ThemeBackground.PAPER, 8),
	BROADSHEET_ITSUKA(R.style.ThemeOverlay_Kotatsu_Broadsheet_Itsuka, R.string.theme_name_itsuka, ThemeFamily.BROADSHEET, ThemeBackground.PAPER, 9),
	BROADSHEET_SHANA(R.style.ThemeOverlay_Kotatsu_Broadsheet_Shana, R.string.theme_name_shana, ThemeFamily.BROADSHEET, ThemeBackground.PAPER, 10),
	BROADSHEET_LIME(R.style.ThemeOverlay_Kotatsu_Broadsheet_Lime, R.string.theme_name_lime, ThemeFamily.BROADSHEET, ThemeBackground.PAPER, 11),
	BROADSHEET_AMBER(R.style.ThemeOverlay_Kotatsu_Broadsheet_Amber, R.string.theme_name_amber, ThemeFamily.BROADSHEET, ThemeBackground.PAPER, 12),
	BROADSHEET_SKY(R.style.ThemeOverlay_Kotatsu_Broadsheet_Sky, R.string.theme_name_sky, ThemeFamily.BROADSHEET, ThemeBackground.PAPER, 13),

	// Arcade family
	ARCADE_CLASSIC(R.style.ThemeOverlay_Kotatsu_Arcade_Classic, R.string.theme_name_classic, ThemeFamily.ARCADE, ThemeBackground.SYNTHWAVE, 0),
	ARCADE_DEFAULT(R.style.ThemeOverlay_Kotatsu_Arcade_Totoro, R.string.theme_name_totoro, ThemeFamily.ARCADE, ThemeBackground.SYNTHWAVE, 1),
	ARCADE_MIKU(R.style.ThemeOverlay_Kotatsu_Arcade_Miku, R.string.theme_name_miku, ThemeFamily.ARCADE, ThemeBackground.SYNTHWAVE, 2),
	ARCADE_RENA(R.style.ThemeOverlay_Kotatsu_Arcade_Asuka, R.string.theme_name_asuka, ThemeFamily.ARCADE, ThemeBackground.SYNTHWAVE, 3),
	ARCADE_FROG(R.style.ThemeOverlay_Kotatsu_Arcade_Mion, R.string.theme_name_mion, ThemeFamily.ARCADE, ThemeBackground.SYNTHWAVE, 4),
	ARCADE_BLUEBERRY(R.style.ThemeOverlay_Kotatsu_Arcade_Rikka, R.string.theme_name_rikka, ThemeFamily.ARCADE, ThemeBackground.SYNTHWAVE, 5),
	ARCADE_SAKURA(R.style.ThemeOverlay_Kotatsu_Arcade_Sakura, R.string.theme_name_sakura, ThemeFamily.ARCADE, ThemeBackground.SYNTHWAVE, 6),
	ARCADE_MAMIMI(R.style.ThemeOverlay_Kotatsu_Arcade_Mamimi, R.string.theme_name_mamimi, ThemeFamily.ARCADE, ThemeBackground.SYNTHWAVE, 7),
	ARCADE_KANADE(R.style.ThemeOverlay_Kotatsu_Arcade_Kanade, R.string.theme_name_kanade, ThemeFamily.ARCADE, ThemeBackground.SYNTHWAVE, 8),
	ARCADE_ITSUKA(R.style.ThemeOverlay_Kotatsu_Arcade_Itsuka, R.string.theme_name_itsuka, ThemeFamily.ARCADE, ThemeBackground.SYNTHWAVE, 9),
	ARCADE_SHANA(R.style.ThemeOverlay_Kotatsu_Arcade_Shana, R.string.theme_name_shana, ThemeFamily.ARCADE, ThemeBackground.SYNTHWAVE, 10),
	ARCADE_LIME(R.style.ThemeOverlay_Kotatsu_Arcade_Lime, R.string.theme_name_lime, ThemeFamily.ARCADE, ThemeBackground.SYNTHWAVE, 11),
	ARCADE_AMBER(R.style.ThemeOverlay_Kotatsu_Arcade_Amber, R.string.theme_name_amber, ThemeFamily.ARCADE, ThemeBackground.SYNTHWAVE, 12),
	ARCADE_SKY(R.style.ThemeOverlay_Kotatsu_Arcade_Sky, R.string.theme_name_sky, ThemeFamily.ARCADE, ThemeBackground.SYNTHWAVE, 13),

	// Sakura family
	SAKURA_SOFT_CLASSIC(R.style.ThemeOverlay_Kotatsu_Sakura_Classic, R.string.theme_name_classic, ThemeFamily.SAKURA, ThemeBackground.PETALS, 0),
	SAKURA_SOFT_DEFAULT(R.style.ThemeOverlay_Kotatsu_Sakura_Totoro, R.string.theme_name_totoro, ThemeFamily.SAKURA, ThemeBackground.PETALS, 1),
	SAKURA_SOFT_MIKU(R.style.ThemeOverlay_Kotatsu_Sakura_Miku, R.string.theme_name_miku, ThemeFamily.SAKURA, ThemeBackground.PETALS, 2),
	SAKURA_SOFT_RENA(R.style.ThemeOverlay_Kotatsu_Sakura_Asuka, R.string.theme_name_asuka, ThemeFamily.SAKURA, ThemeBackground.PETALS, 3),
	SAKURA_SOFT_FROG(R.style.ThemeOverlay_Kotatsu_Sakura_Mion, R.string.theme_name_mion, ThemeFamily.SAKURA, ThemeBackground.PETALS, 4),
	SAKURA_SOFT_BLUEBERRY(R.style.ThemeOverlay_Kotatsu_Sakura_Rikka, R.string.theme_name_rikka, ThemeFamily.SAKURA, ThemeBackground.PETALS, 5),
	SAKURA_SOFT_SAKURA(R.style.ThemeOverlay_Kotatsu_Sakura_Sakura, R.string.theme_name_sakura, ThemeFamily.SAKURA, ThemeBackground.PETALS, 6),
	SAKURA_SOFT_MAMIMI(R.style.ThemeOverlay_Kotatsu_Sakura_Mamimi, R.string.theme_name_mamimi, ThemeFamily.SAKURA, ThemeBackground.PETALS, 7),
	SAKURA_SOFT_KANADE(R.style.ThemeOverlay_Kotatsu_Sakura_Kanade, R.string.theme_name_kanade, ThemeFamily.SAKURA, ThemeBackground.PETALS, 8),
	SAKURA_SOFT_ITSUKA(R.style.ThemeOverlay_Kotatsu_Sakura_Itsuka, R.string.theme_name_itsuka, ThemeFamily.SAKURA, ThemeBackground.PETALS, 9),
	SAKURA_SOFT_SHANA(R.style.ThemeOverlay_Kotatsu_Sakura_Shana, R.string.theme_name_shana, ThemeFamily.SAKURA, ThemeBackground.PETALS, 10),
	SAKURA_SOFT_LIME(R.style.ThemeOverlay_Kotatsu_Sakura_Lime, R.string.theme_name_lime, ThemeFamily.SAKURA, ThemeBackground.PETALS, 11),
	SAKURA_SOFT_AMBER(R.style.ThemeOverlay_Kotatsu_Sakura_Amber, R.string.theme_name_amber, ThemeFamily.SAKURA, ThemeBackground.PETALS, 12),
	SAKURA_SOFT_SKY(R.style.ThemeOverlay_Kotatsu_Sakura_Sky, R.string.theme_name_sky, ThemeFamily.SAKURA, ThemeBackground.PETALS, 13),

	// Brutal family
	BRUTAL_CLASSIC(R.style.ThemeOverlay_Kotatsu_Brutal_Classic, R.string.theme_name_classic, ThemeFamily.BRUTAL, ThemeBackground.HALFTONE, 0),
	BRUTAL_DEFAULT(R.style.ThemeOverlay_Kotatsu_Brutal_Totoro, R.string.theme_name_totoro, ThemeFamily.BRUTAL, ThemeBackground.HALFTONE, 1),
	BRUTAL_MIKU(R.style.ThemeOverlay_Kotatsu_Brutal_Miku, R.string.theme_name_miku, ThemeFamily.BRUTAL, ThemeBackground.HALFTONE, 2),
	BRUTAL_RENA(R.style.ThemeOverlay_Kotatsu_Brutal_Asuka, R.string.theme_name_asuka, ThemeFamily.BRUTAL, ThemeBackground.HALFTONE, 3),
	BRUTAL_FROG(R.style.ThemeOverlay_Kotatsu_Brutal_Mion, R.string.theme_name_mion, ThemeFamily.BRUTAL, ThemeBackground.HALFTONE, 4),
	BRUTAL_BLUEBERRY(R.style.ThemeOverlay_Kotatsu_Brutal_Rikka, R.string.theme_name_rikka, ThemeFamily.BRUTAL, ThemeBackground.HALFTONE, 5),
	BRUTAL_SAKURA(R.style.ThemeOverlay_Kotatsu_Brutal_Sakura, R.string.theme_name_sakura, ThemeFamily.BRUTAL, ThemeBackground.HALFTONE, 6),
	BRUTAL_MAMIMI(R.style.ThemeOverlay_Kotatsu_Brutal_Mamimi, R.string.theme_name_mamimi, ThemeFamily.BRUTAL, ThemeBackground.HALFTONE, 7),
	BRUTAL_KANADE(R.style.ThemeOverlay_Kotatsu_Brutal_Kanade, R.string.theme_name_kanade, ThemeFamily.BRUTAL, ThemeBackground.HALFTONE, 8),
	BRUTAL_ITSUKA(R.style.ThemeOverlay_Kotatsu_Brutal_Itsuka, R.string.theme_name_itsuka, ThemeFamily.BRUTAL, ThemeBackground.HALFTONE, 9),
	BRUTAL_SHANA(R.style.ThemeOverlay_Kotatsu_Brutal_Shana, R.string.theme_name_shana, ThemeFamily.BRUTAL, ThemeBackground.HALFTONE, 10),
	BRUTAL_LIME(R.style.ThemeOverlay_Kotatsu_Brutal_Lime, R.string.theme_name_lime, ThemeFamily.BRUTAL, ThemeBackground.HALFTONE, 11),
	BRUTAL_AMBER(R.style.ThemeOverlay_Kotatsu_Brutal_Amber, R.string.theme_name_amber, ThemeFamily.BRUTAL, ThemeBackground.HALFTONE, 12),
	BRUTAL_SKY(R.style.ThemeOverlay_Kotatsu_Brutal_Sky, R.string.theme_name_sky, ThemeFamily.BRUTAL, ThemeBackground.HALFTONE, 13),
	;

	companion object {

		val default: ColorScheme
			get() = if (DynamicColors.isDynamicColorAvailable()) {
				EXPRESSIVE
			} else {
				DEFAULT
			}

		fun getAvailableList(): List<ColorScheme> {
			val list = ColorScheme.entries.toMutableList()
			if (!DynamicColors.isDynamicColorAvailable()) {
				list.remove(EXPRESSIVE)
			}
			return list
		}

		/** Available schemes grouped by theme family, Material 3 first, then each family in order. */
		fun getGroupedList(): List<Pair<ThemeFamily, List<ColorScheme>>> {
			val all = getAvailableList()
			return ThemeFamily.entries.mapNotNull { family ->
				all.filter { it.family == family }.takeIf { it.isNotEmpty() }?.let { family to it }
			}
		}
	}
}
