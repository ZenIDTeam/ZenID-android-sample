package cz.trask.zenid.sample.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import cz.trask.zenid.sample.MyApplication
import cz.trask.zenid.sdk.ZenId
import cz.trask.zenid.sdk.ZenIdException
import cz.trask.zenid.sdk.api.model.InitResponseJson
import cz.trask.zenid.sdk.api.model.InvestigationResponseJson
import cz.trask.zenid.sdk.api.model.ProfilesResponseJson
import cz.trask.zenid.sdk.api.model.SampleJson
import cz.trask.zenid.sdk.models.UploadReadyData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

data class MainUiState(
    val isAuthorized: Boolean = false,
    val isAuthorizing: Boolean = false,
    val serverUrl: String = "",
    val profiles: List<String> = emptyList(),
    val selectedProfile: String = "",
    val country: String? = null,
    val role: String? = null,
    val page: String? = null,
    val error: String? = null,
    val challengeToken: String? = null
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _lastResult = MutableStateFlow<String?>(null)
    val lastResult: StateFlow<String?> = _lastResult.asStateFlow()

    private val _isLoadingResult = MutableStateFlow(false)
    val isLoadingResult: StateFlow<Boolean> = _isLoadingResult.asStateFlow()

    private val settings get() = MyApplication.appSettings

    init {
        _uiState.value = _uiState.value.copy(
            serverUrl = settings.serverUrl ?: "",
            selectedProfile = settings.profile,
            country = settings.country,
            role = settings.role,
            page = settings.page
        )
        if (!settings.serverUrl.isNullOrEmpty()) {
            authorize()
        }
    }

    fun authorize() {
        val challengeToken = try {
            ZenId.get().security.challengeToken
        } catch (e: ZenIdException) {
            _uiState.value = _uiState.value.copy(isAuthorizing = false, error = e.message)
            return
        }
        _uiState.value = _uiState.value.copy(isAuthorizing = true, error = null, challengeToken = challengeToken)
        MyApplication.apiService?.initSdk(challengeToken)?.enqueue(object : Callback<InitResponseJson> {
            override fun onResponse(call: Call<InitResponseJson>, response: Response<InitResponseJson>) {
                viewModelScope.launch {
                    val responseToken = response.body()?.response ?: run {
                        _uiState.value = _uiState.value.copy(isAuthorizing = false, error = "Auth response empty")
                        return@launch
                    }
                    try {
                        val authorized = ZenId.get().security.authorize(getApplication(), responseToken)
                        if (authorized) {
                            val profile = settings.profile.ifEmpty { null }
                            if (profile != null) ZenId.get().security.selectProfile(profile)
                            _uiState.value = _uiState.value.copy(isAuthorized = true, isAuthorizing = false)
                            loadProfiles()
                        } else {
                            _uiState.value = _uiState.value.copy(isAuthorizing = false, error = "Authorization failed")
                        }
                    } catch (e: ZenIdException) {
                        _uiState.value = _uiState.value.copy(isAuthorizing = false, error = e.message)
                    }
                }
            }

            override fun onFailure(call: Call<InitResponseJson>, t: Throwable) {
                viewModelScope.launch {
                    _uiState.value = _uiState.value.copy(isAuthorizing = false, error = t.message)
                    Timber.e(t)
                }
            }
        }) ?: run {
            _uiState.value = _uiState.value.copy(isAuthorizing = false, error = "No server configured")
        }
    }

    fun onQrScanned(url: String, apiKey: String) {
        val normalizedUrl = if (url.endsWith("/")) url else "$url/"
        settings.serverUrl = normalizedUrl
        settings.apiKey = apiKey
        (getApplication() as MyApplication).rebuildApiService()
        _uiState.value = _uiState.value.copy(serverUrl = normalizedUrl, isAuthorized = false)
        authorize()
    }

    fun setProfile(name: String) {
        settings.profile = name
        _uiState.value = _uiState.value.copy(selectedProfile = name)
        if (_uiState.value.isAuthorized) try { ZenId.get().security.selectProfile(name) } catch (e: ZenIdException) { Timber.e(e) }
    }

    fun setCountry(c: String?) {
        settings.country = c
        settings.role = null
        settings.page = null
        _uiState.value = _uiState.value.copy(country = c, role = null, page = null)
    }

    fun setRole(r: String?) {
        settings.role = r
        settings.page = null
        _uiState.value = _uiState.value.copy(role = r, page = null)
    }

    fun setPage(p: String?) {
        settings.page = p
        _uiState.value = _uiState.value.copy(page = p)
    }

    fun logout() {
        settings.serverUrl = null
        settings.apiKey = null
        settings.profile = ""
        settings.country = null
        settings.role = null
        settings.page = null
        MyApplication.apiService = null
        _uiState.value = MainUiState()
    }

    fun postSampleAndNavigate(uploadReadyData: UploadReadyData, onNavigate: () -> Unit) {
        _lastResult.value = null
        _isLoadingResult.value = true
        onNavigate()
        MyApplication.apiService?.postSample(uploadReadyData.signedSamplePackage)
            ?.enqueue(object : Callback<SampleJson> {
                override fun onResponse(call: Call<SampleJson>, response: Response<SampleJson>) {
                    viewModelScope.launch {
                        val sample = response.body()
                        val sampleId = sample?.sampleId
                        if (sampleId != null) {
                            investigateAndFinish(listOf(sampleId))
                        } else {
                            _lastResult.value = "Upload failed: ${sample?.errorCode}"
                            _isLoadingResult.value = false
                        }
                    }
                }

                override fun onFailure(call: Call<SampleJson>, t: Throwable) {
                    viewModelScope.launch {
                        Timber.e(t)
                        _lastResult.value = "Upload failed: ${t.message}"
                        _isLoadingResult.value = false
                    }
                }
            }) ?: run {
            _lastResult.value = "No API service"
            _isLoadingResult.value = false
        }
    }

    private fun investigateAndFinish(sampleIds: List<String>) {
        val profile = settings.profile.ifEmpty { null }
        MyApplication.apiService?.investigateSamples(sampleIds, profile)
            ?.enqueue(object : Callback<InvestigationResponseJson> {
                override fun onResponse(
                    call: Call<InvestigationResponseJson>,
                    response: Response<InvestigationResponseJson>
                ) {
                    viewModelScope.launch {
                        _lastResult.value = Gson().toJson(response.body())
                        _isLoadingResult.value = false
                    }
                }

                override fun onFailure(call: Call<InvestigationResponseJson>, t: Throwable) {
                    viewModelScope.launch {
                        Timber.e(t)
                        _lastResult.value = "Investigation failed: ${t.message}"
                        _isLoadingResult.value = false
                    }
                }
            }) ?: run {
            _lastResult.value = "No API service"
            _isLoadingResult.value = false
        }
    }

    private fun loadProfiles() {
        MyApplication.apiService?.profiles?.enqueue(object : Callback<ProfilesResponseJson> {
            override fun onResponse(call: Call<ProfilesResponseJson>, response: Response<ProfilesResponseJson>) {
                viewModelScope.launch {
                    val profiles = response.body()?.results ?: emptyList()
                    _uiState.value = _uiState.value.copy(profiles = profiles)
                }
            }

            override fun onFailure(call: Call<ProfilesResponseJson>, t: Throwable) {
                viewModelScope.launch {
                    Timber.e(t)
                }
            }
        })
    }
}
