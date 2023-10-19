package media.uqab.toolbox

import android.content.res.Resources.getSystem

/**
 * Convert Px to Dp
 */
val Int.dp: Int get() = (this / getSystem().displayMetrics.density).toInt()

/**
 * Convert Dp to Px
 */
val Int.px: Int get() = (this * getSystem().displayMetrics.density).toInt()
