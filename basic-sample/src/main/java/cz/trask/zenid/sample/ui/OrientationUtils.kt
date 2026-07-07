package cz.trask.zenid.sample.ui

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

private var landscapeRequestCount = 0

@Composable
fun AllowLandscapeOrientation() {
    val activity = LocalActivity.current
    DisposableEffect(activity) {
        landscapeRequestCount++
        val previousOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {
            landscapeRequestCount--
            if (landscapeRequestCount == 0) {
                activity?.requestedOrientation = previousOrientation ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }
}
