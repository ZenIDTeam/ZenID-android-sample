package cz.trask.zenid.sample.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cz.trask.zenid.sdk.models.ZenidModels
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PickerType { COUNTRY, ROLE, PAGE, PROFILE }

const val ANY_COUNTRY = "Any"

class PickerViewModel(app: Application) : AndroidViewModel(app) {

    private val _options = MutableStateFlow<List<String>>(emptyList())
    val options: StateFlow<List<String>> = _options.asStateFlow()

    fun loadOptions(type: PickerType, profiles: List<String>, country: String?, role: String?) {
        _options.value = when (type) {
            PickerType.COUNTRY -> {
                val countries = ZenidModels.getModels().map { it.country.name }.distinct()
                if (countries.size > 1) listOf(ANY_COUNTRY) + countries else countries
            }
            PickerType.ROLE -> {
                val models = ZenidModels.getModels()
                val filtered = if (country == ANY_COUNTRY || country == null) models else models.filter { it.country.name == country }
                filtered.map { it.documentRole.name }.distinct()
            }
            PickerType.PAGE -> {
                val models = ZenidModels.getModels()
                val filtered = if (country == ANY_COUNTRY || country == null)
                    models.filter { it.documentRole.name == role }
                else
                    models.filter { it.country.name == country && it.documentRole.name == role }
                filtered.map { it.pageCode.name }.distinct()
            }
            PickerType.PROFILE -> profiles
        }
    }
}
