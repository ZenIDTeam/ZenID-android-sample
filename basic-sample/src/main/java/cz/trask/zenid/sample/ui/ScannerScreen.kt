package cz.trask.zenid.sample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import cz.trask.zenid.sample.viewmodel.ANY_COUNTRY
import cz.trask.zenid.sample.viewmodel.MainViewModel
import cz.trask.zenid.sdk.VisualizationSettings
import cz.trask.zenid.sdk.ZenIdException
import cz.trask.zenid.sdk.enums.Country
import cz.trask.zenid.sdk.enums.DocumentRole
import cz.trask.zenid.sdk.enums.PageCodes
import cz.trask.zenid.sdk.enums.SupportedLanguages
import cz.trask.zenid.sdk.models.AcceptableInput
import cz.trask.zenid.sdk.models.DocumentFilter
import cz.trask.zenid.sdk.models.DocumentVerifierSettings
import cz.trask.zenid.sdk.models.FaceLivenessVerifierSettings
import cz.trask.zenid.sdk.models.SelfieVerifierSettings
import cz.trask.zenid.sdk.models.UploadReadyData
import cz.trask.zenid.sdk.view.DocumentView
import cz.trask.zenid.sdk.view.FaceLivenessView
import cz.trask.zenid.sdk.view.HologramView
import cz.trask.zenid.sdk.view.SelfieView
import timber.log.Timber
import java.util.Locale

@Composable
fun ScannerScreen(
    verifierType: String,
    mainViewModel: MainViewModel,
    navController: NavController
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by mainViewModel.uiState.collectAsState()

    val handleResult: (UploadReadyData) -> Unit = remember {
        { uploadReadyData ->
            mainViewModel.postSampleAndNavigate(uploadReadyData) {
                navController.navigate(Routes.RESULT)
            }
        }
    }
    val handleNfc: () -> Unit = remember {
        {
            navController.navigate(Routes.NFC) {
                popUpTo(Routes.MAIN) { inclusive = false }
            }
        }
    }
    val handleError: (ZenIdException) -> Unit = remember {
        { e ->
            Timber.e(e)
            navController.popBackStack()
        }
    }

    val visualSettings = remember { buildVisualizationSettings() }
    val role = remember(uiState.role) { uiState.role?.let { parseRole(it) } }
    val page = remember(uiState.page) { uiState.page?.let { parsePage(it) } }

    if (verifierType == "document" || verifierType == "hologram") {
        AllowLandscapeOrientation()
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            when (verifierType) {
                "document" -> {
                    DocumentView(ctx).apply {
                        setLifecycleOwner(lifecycleOwner)
                        setVerifierSettings(buildDocumentSettings(uiState.country, role, page))
                        enableDefaultVisualization(visualSettings)
                        setCallback(object : DocumentView.DocumentViewCallback {
                            override fun onStateChanged(s: cz.trask.zenid.sdk.models.DocumentVerifierStateContainerForPublicData) = Unit
                            override fun onScanNfc() = handleNfc()
                            override fun onResult(data: UploadReadyData) = handleResult(data)
                            override fun onError(e: ZenIdException) = handleError(e)
                        })
                    }
                }
                "selfie" -> {
                    SelfieView(ctx).apply {
                        setLifecycleOwner(lifecycleOwner)
                        setVerifierSettings(SelfieVerifierSettings())
                        enableDefaultVisualization(visualSettings)
                        setCallback(object : SelfieView.SelfieViewCallback {
                            override fun onStateChanged(s: cz.trask.zenid.sdk.models.SelfieStateContainerForPublicData) = Unit
                            override fun onResult(data: UploadReadyData) = handleResult(data)
                            override fun onError(e: ZenIdException) = handleError(e)
                        })
                    }
                }
                "face_liveness" -> {
                    FaceLivenessView(ctx).apply {
                        setLifecycleOwner(lifecycleOwner)
                        val settings = FaceLivenessVerifierSettings().apply {
                            enableLegacyMode = false
                            showSmileAnimation = true
                        }
                        setVerifierSettings(settings)
                        enableDefaultVisualization(visualSettings)
                        setCallback(object : FaceLivenessView.FaceLivenessViewCallback {
                            override fun onStateChanged(s: cz.trask.zenid.sdk.models.FaceLivenessStateContainerForPublicData) = Unit
                            override fun onResult(data: UploadReadyData) = handleResult(data)
                            override fun onError(e: ZenIdException) = handleError(e)
                        })
                    }
                }
                "hologram" -> {
                    HologramView(ctx).apply {
                        setLifecycleOwner(lifecycleOwner)
                        setVerifierSettings(DocumentVerifierSettings().apply {
                            enableAimingCircle = true
                            drawOutline = true
                            buildAcceptableInput(uiState.country, role, page)?.let { acceptableInput = it }
                        })
                        enableDefaultVisualization(visualSettings)
                        setCallback(object : HologramView.HologramViewCallback {
                            override fun onStateChanged(s: cz.trask.zenid.sdk.models.DocumentVerifierStateContainerForPublicData) = Unit
                            override fun onResult(data: UploadReadyData) = handleResult(data)
                            override fun onError(e: ZenIdException) = handleError(e)
                        })
                    }
                }
                else -> error("Unknown verifier type: $verifierType")
            }
        }
    )
}

private fun buildDocumentSettings(countryStr: String?, role: DocumentRole?, page: PageCodes?): DocumentVerifierSettings {
    val settings = DocumentVerifierSettings().apply {
        enableAimingCircle = true
        showTimer = false
        drawOutline = true
    }
    buildAcceptableInput(countryStr, role, page)?.let { settings.acceptableInput = it }
    return settings
}

private fun buildAcceptableInput(countryStr: String?, role: DocumentRole?, page: PageCodes?): AcceptableInput? {
    val country = countryStr?.let { parseCountry(it) }
    return when {
        country != null ->
            AcceptableInput(listOf(DocumentFilter(role, country, page, null, null)))
        (countryStr == ANY_COUNTRY || countryStr == null) && role != null ->
            AcceptableInput(listOf(DocumentFilter(role, null, page, null, null)))
        countryStr == ANY_COUNTRY || countryStr == null ->
            AcceptableInput(listOf(DocumentFilter(null, null, page, null, null)))
        else -> null
    }
}

private fun buildVisualizationSettings(): VisualizationSettings {
    return VisualizationSettings.Builder()
        .showDebugVisualization(true)
        .language(deviceLanguage())
        .showTextInformation(true)
        .build()
}

private fun deviceLanguage(): SupportedLanguages {
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

private fun parseCountry(name: String): Country? = try { Country.valueOf(name) } catch (_: Exception) { null }

private fun parseRole(name: String): DocumentRole? = try { DocumentRole.valueOf(name) } catch (_: Exception) { null }

private fun parsePage(name: String): PageCodes? = try { PageCodes.valueOf(name) } catch (_: Exception) { null }

@Preview(showBackground = true, name = "Scanner Screen")
@Composable
private fun ScannerScreenPreview() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("Camera Scanner View", color = Color.White)
    }
}
