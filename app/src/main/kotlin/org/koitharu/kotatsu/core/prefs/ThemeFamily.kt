package org.koitharu.kotatsu.core.prefs

import androidx.annotation.Keep
import androidx.annotation.StringRes
import org.koitharu.kotatsu.R

/**
 * Groups colour schemes in the picker. MATERIAL holds the original Material 3 schemes; every other
 * family offers the same colours restyled with its own corner scale and background art.
 */
@Keep
enum class ThemeFamily(@StringRes val titleResId: Int) {
	MATERIAL(R.string.theme_family_material),
	TOTORO(R.string.theme_family_totoro),
	TERMINAL(R.string.theme_family_terminal),
	BROADSHEET(R.string.theme_family_broadsheet),
	ARCADE(R.string.theme_family_arcade),
	SAKURA(R.string.theme_family_sakura),
	BRUTAL(R.string.theme_family_brutal),
}
