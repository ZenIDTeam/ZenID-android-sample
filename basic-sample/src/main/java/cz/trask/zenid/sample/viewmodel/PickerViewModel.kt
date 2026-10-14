package cz.trask.zenid.sample.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cz.trask.zenid.sdk.ZenId
import cz.trask.zenid.sdk.enums.Country
import cz.trask.zenid.sdk.enums.DocumentRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

enum class PickerType { COUNTRY, ROLE, PAGE, PROFILE }

const val ANY_COUNTRY = "Any"

internal fun countryOf(name: String?): Country? =
    if (name.isNullOrEmpty() || name == ANY_COUNTRY) null else Country.valueOf(name)

class PickerViewModel(app: Application) : AndroidViewModel(app) {

    private val _options = MutableStateFlow<List<String>>(emptyList())
    val options: StateFlow<List<String>> = _options.asStateFlow()

    fun loadOptions(type: PickerType, profiles: List<String>, country: String?, role: String?) {
        if (type == PickerType.PROFILE) {
            _options.value = profiles
            return
        }
        // Offer only what the license allows.
        _options.value = try {
            val zenId = ZenId.get()
            when (type) {
                PickerType.COUNTRY -> {
                    val countries = zenId.supportedCountries.map { it.name }
                    if (countries.size > 1) listOf(ANY_COUNTRY) + countries else countries
                }
                PickerType.ROLE -> zenId.getSupportedDocuments(countryOf(country)).map { it.name }
                else -> {
                    val roleFilter = role?.let { DocumentRole.valueOf(it) }
                    zenId.getSupportedDocumentPageCodes(countryOf(country), roleFilter).map { it.name }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            emptyList()
        }
    }
}
