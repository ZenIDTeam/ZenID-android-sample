package cz.trask.zenid.sample

import android.content.Context
import android.content.Context.MODE_PRIVATE
import kotlin.reflect.KProperty
import androidx.core.content.edit

class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("zenid_prefs", MODE_PRIVATE)

    var serverUrl: String? by NullableStringPref("server_url")
    var apiKey: String? by NullableStringPref("api_key")
    var profile: String by StringPref("profile", "")
    var country: String? by NullableStringPref("country")
    var role: String? by NullableStringPref("role")
    var page: String? by NullableStringPref("page")

    inner class StringPref(private val key: String, private val default: String) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String =
            prefs.getString(key, default) ?: default

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) =
            prefs.edit { putString(key, value) }
    }

    inner class NullableStringPref(private val key: String) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String? =
            prefs.getString(key, null)

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
            if (value == null) prefs.edit { remove(key) }
            else prefs.edit { putString(key, value) }
        }
    }
}
