package org.koitharu.kotatsu.core.prefs

import androidx.annotation.Keep

/**
 * Procedural background art painted behind every screen for a colour scheme.
 * Everything is tinted from the active theme colours, see ThemeBackgroundDrawable.
 */
@Keep
enum class ThemeBackground {
	NONE,

	/** Soft colour blobs and a faint dot field, with drifting leaves and glowing spores (Totoro). */
	MEADOW,

	/** CRT code editor with scanlines and falling glyph rain (Terminal). */
	PHOSPHOR,

	/** Ruled paper with a centre column fold (Broadsheet). */
	PAPER,

	/** Synthwave sunset with a scrolling grid floor, stars and shooting stars (Neon Arcade). */
	SYNTHWAVE,

	/** Tinted wash with sakura petals falling and swaying (Sakura). */
	PETALS,

	/** Halftone dots with hazard-stripe hatching (Brutalist). */
	HALFTONE,
}
