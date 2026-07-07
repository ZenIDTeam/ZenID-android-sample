package cz.trask.zenid.sample.ui

import android.view.View
import android.widget.ProgressBar
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import cz.trask.sdk.ms_liveness.MSLivenessView
import cz.trask.zenid.sample.viewmodel.MainViewModel
import cz.trask.zenid.sdk.VisualizationSettings
import cz.trask.zenid.sdk.ZenIdException
import cz.trask.zenid.sdk.enums.SupportedLanguages
import cz.trask.zenid.sdk.models.UploadReadyData
import timber.log.Timber
import java.util.Locale

@Composable
fun MsLivenessScreen(
    mainViewModel: MainViewModel,
    navController: NavController
) {
    val challengeToken = mainViewModel.uiState.collectAsStateWithLifecycle().value.challengeToken

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val container = FrameLayout(ctx)
            val msLivenessView = MSLivenessView(ctx)
            val progressBar = ProgressBar(ctx).apply { visibility = View.GONE }
            container.addView(msLivenessView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            container.addView(progressBar, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)

            val visualSettings = VisualizationSettings.Builder()
                .language(msLivenessLanguage())
                .showDebugVisualization(false)
                .showTextInformation(true)
                .build()

            try {
                msLivenessView.enableDefaultVisualization(visualSettings)
            } catch (e: Exception) {
                Timber.e(e)
                navController.popBackStack()
                return@AndroidView container
            }

            msLivenessView.setCallback(object : MSLivenessView.Callback {
                override fun onSetupInProgress(progress: Boolean) {
                    progressBar.visibility = if (progress) View.VISIBLE else View.GONE
                }

                override fun onResult(uploadReadyData: UploadReadyData) {
                    mainViewModel.postSampleAndNavigate(uploadReadyData) {
                        navController.navigate(Routes.RESULT)
                    }
                }

                override fun onError(exception: ZenIdException) {
                    Timber.e(exception)
                    if (challengeToken != null) {
                        msLivenessView.restart(challengeToken)
                    } else {
                        navController.popBackStack()
                    }
                }

                override fun onBackPressed() {
                    navController.popBackStack()
                }
            })

            if (challengeToken != null) {
                msLivenessView.start(challengeToken)
            } else {
                navController.popBackStack()
            }

            container
        }
    )
}

private fun msLivenessLanguage(): SupportedLanguages {
    val locale = Locale.getDefault()
    return try {
        SupportedLanguages.valueOf(locale.getDisplayLanguage(Locale.ENGLISH))
    } catch (_: IllegalArgumentException) {
        when (locale.language) {
            "cs" -> SupportedLanguages.Czech
            "pl" -> SupportedLanguages.Polish
            "de" -> SupportedLanguages.German
            "hr" -> SupportedLanguages.Croatian
            "sk" -> SupportedLanguages.Slovak
            "nl" -> SupportedLanguages.Dutch
            else -> SupportedLanguages.English
        }
    }
}

@Preview(showBackground = true, name = "MS Liveness Screen")
@Composable
private fun MsLivenessScreenPreview() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("MS Liveness View", color = Color.White)
    }
}
