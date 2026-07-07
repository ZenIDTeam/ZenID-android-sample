package cz.trask.zenid.sample.ui

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.nfc.NfcAdapter
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.trask.zenid.sample.viewmodel.MainViewModel
import cz.trask.zenid.sdk.models.UploadReadyData
import cz.trask.zenid.sdk.nfc.FacePictureFormat
import cz.trask.zenid.sdk.nfc.NfcAccessDeniedException
import cz.trask.zenid.sdk.nfc.NfcConnectionException
import cz.trask.zenid.sdk.nfc.NfcData
import cz.trask.zenid.sdk.nfc.NfcResult
import cz.trask.zenid.sdk.nfc.NfcService
import cz.trask.zenid.sdk.nfc.NfcState
import dev.keiji.jp2k.Callback
import dev.keiji.jp2k.Config
import dev.keiji.jp2k.Jp2kDecoderAsync
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.Executors

data class NfcScreenState(
    val status: String = "Hold the document near NFC reader",
    val faceBitmap: Bitmap? = null,
    val documentInfo: List<Pair<String, String>> = emptyList(),
    val showSkip: Boolean = false,
    val isDone: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcScreen(
    mainViewModel: MainViewModel,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val nfcService = remember { NfcService() }
    var state by remember { mutableStateOf(NfcScreenState()) }

    val showSkip = remember {
        try { nfcService.isSkipNfcAllowed } catch (_: Exception) { false }
    }

    DisposableEffect(Unit) {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        if (nfcAdapter == null || !nfcAdapter.isEnabled) {
            state = state.copy(status = "NFC not available")
            return@DisposableEffect onDispose {}
        }

        state = state.copy(showSkip = showSkip)

        val intent = Intent(activity, activity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_MUTABLE)
        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null)

        val nfcListener: (Intent) -> Unit = { nfcIntent ->
            nfcService.handleIntent(nfcIntent, object : NfcService.Callback {
                override fun onZenIdNotNfcState() {
                    state = state.copy(status = "Not in NFC state")
                }

                override fun onStateChanged(nfcState: NfcState) {
                    state = state.copy(status = "NFC State: ${nfcState.name}")
                }

                override fun onResult(uploadReadyData: UploadReadyData, nfcResult: NfcResult, nfcData: NfcData) {
                    val info = listOf(
                        "Document code" to (nfcResult.documentCode ?: ""),
                        "Issuing state" to (nfcResult.issuingState ?: ""),
                        "Nationality" to (nfcResult.nationality ?: ""),
                        "Document number" to (nfcResult.documentNumber ?: ""),
                        "Date of birth" to (nfcResult.dateOfBirth ?: ""),
                        "Date of expiry" to (nfcResult.dateOfExpiry ?: ""),
                        "Primary identifier" to (nfcResult.primaryIdentifier ?: ""),
                        "Secondary identifier" to (nfcResult.secondaryIdentifier ?: ""),
                        "Gender" to (nfcResult.gender?.name ?: "")
                    )
                    state = state.copy(status = "NFC read successful", documentInfo = info)

                    val picturePath = nfcResult.facePicturePath
                    if (picturePath != null) {
                        if (nfcResult.facePictureFormat == FacePictureFormat.JP2) {
                            decodeJp2k(context, picturePath) { bitmap ->
                                state = state.copy(faceBitmap = bitmap)
                            }
                        } else {
                            state = state.copy(faceBitmap = BitmapFactory.decodeFile(picturePath))
                        }
                    }
                    mainViewModel.postSampleAndNavigate(uploadReadyData) { onDone() }
                }

                override fun onAccessDenied(exception: NfcAccessDeniedException) {
                    val key = nfcService.nfcKey ?: return
                    state = state.copy(
                        status = "Access denied - birth: ${key.birthDate}, doc: ${key.documentNumber}, expiry: ${key.expiryDate}"
                    )
                }

                override fun onConnectionFailed(exception: NfcConnectionException) {
                    state = state.copy(status = "Connection lost")
                }

                override fun onGeneralException(exception: Exception) {
                    state = state.copy(status = "Error: ${exception.message}")
                }
            })
        }
        activity.addOnNewIntentListener(nfcListener)

        onDispose {
            nfcAdapter.disableForegroundDispatch(activity)
            activity.removeOnNewIntentListener(nfcListener)
        }
    }

    NfcScreenContent(state = state, onSkip = {
        val uploadReadyData = nfcService.skipNfcVerification()
        if (uploadReadyData != null) {
            mainViewModel.postSampleAndNavigate(uploadReadyData) { onDone() }
        } else {
            Timber.e("Skip NFC returned no data")
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NfcScreenContent(state: NfcScreenState, onSkip: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NFC Scan") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = state.status, modifier = Modifier.padding(bottom = 16.dp))

            state.faceBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Face picture",
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            state.documentInfo.forEach { (label, value) ->
                if (value.isNotEmpty()) {
                    Text("$label: $value", modifier = Modifier.fillMaxWidth())
                }
            }

            if (state.showSkip) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onSkip) {
                    Text("Skip NFC")
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "NFC Screen")
@Composable
private fun NfcScreenPreview() {
    MaterialTheme {
        NfcScreenContent(
            state = NfcScreenState(
                status = "Hold the document near NFC reader",
                showSkip = true,
                documentInfo = listOf(
                    "Document code" to "IDC1",
                    "Issuing state" to "CZE",
                    "Document number" to "123456789"
                )
            ),
            onSkip = {}
        )
    }
}

private fun decodeJp2k(context: android.content.Context, path: String, onBitmap: (Bitmap) -> Unit) {
    try {
        val bytes = File(path).let { file ->
            FileInputStream(file).use { fis ->
                ByteArray(file.length().toInt()).also { fis.read(it) }
            }
        }
        val executor = Executors.newSingleThreadExecutor()
        val decoder = Jp2kDecoderAsync(executor, Config())
        decoder.init(context, object : Callback<Unit> {
            override fun onSuccess(result: Unit) {
                decoder.decodeImage(bytes, object : Callback<Bitmap> {
                    override fun onSuccess(result: Bitmap) {
                        Handler(Looper.getMainLooper()).post { onBitmap(result) }
                        decoder.close()
                        executor.shutdown()
                    }
                    override fun onError(error: Exception) {
                        Timber.e(error)
                        decoder.close()
                        executor.shutdown()
                    }
                })
            }
            override fun onError(error: Exception) {
                Timber.e(error)
                decoder.close()
                executor.shutdown()
            }
        })
    } catch (e: Exception) {
        Timber.e(e)
    }
}
