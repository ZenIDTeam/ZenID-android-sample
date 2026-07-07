package cz.trask.zenid.sample

import android.app.Application
import cz.trask.zenid.sdk.ZenId
import cz.trask.zenid.sdk.api.ApiConfig
import cz.trask.zenid.sdk.api.ApiService
import cz.trask.zenid.sdk.internal.verifier.DocumentVerifier
import cz.trask.zenid.sdk.internal.verifier.FaceLivenessVerifier
import cz.trask.zenid.sdk.internal.verifier.HologramVerifier
import cz.trask.zenid.sdk.internal.verifier.LicensePlateVerifier
import cz.trask.zenid.sdk.internal.verifier.MsLivenessVerifier
import cz.trask.zenid.sdk.internal.verifier.SelfieVerifier
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MyApplication : Application() {

    companion object {
        lateinit var appSettings: AppSettings
        var apiService: ApiService? = null
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        appSettings = AppSettings(this)
        initZenId()
        rebuildApiService()
    }

    private fun initZenId() {
        if (ZenId.singletonInstanceExists()) {
            return
        }

        val zenId = ZenId.Builder()
            .applicationContext(applicationContext)
            .verifiers(
                DocumentVerifier(), HologramVerifier(),
                FaceLivenessVerifier(), SelfieVerifier(), MsLivenessVerifier(),
                LicensePlateVerifier()
            )
            .build()

        ZenId.setSingletonInstance(zenId)
        zenId.initialize()
        zenId.setLoggerCallback { module, method, message ->
            Timber.tag(module).d("%s - %s", method, message)
        }
    }

    fun rebuildApiService() {
        val url = appSettings.serverUrl?.takeIf { it.isNotEmpty() }
            ?: BuildConfig.ZENID_URL.takeIf { it.isNotEmpty() }
            ?: return
        val key = appSettings.apiKey?.takeIf { it.isNotEmpty() }
            ?: BuildConfig.ZENID_APIKEY.takeIf { it.isNotEmpty() }
            ?: ""

        val logging = HttpLoggingInterceptor { Timber.tag("OkHttp").d(it) }
            .apply { level = HttpLoggingInterceptor.Level.HEADERS }

        val okHttp = OkHttpClient.Builder()
            .addInterceptor(logging)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(120, TimeUnit.SECONDS)
            .protocols(listOf(Protocol.HTTP_1_1))
            .build()

        apiService = null
        try {
            apiService = ApiService.Builder()
                .apiConfig(ApiConfig.Builder().baseUrl(url).apiKey(key).build())
                .okHttpClient(okHttp)
                .build()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}
