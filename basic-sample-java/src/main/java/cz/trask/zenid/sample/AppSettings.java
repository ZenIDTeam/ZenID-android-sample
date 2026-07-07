package cz.trask.zenid.sample;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {
    private final SharedPreferences prefs;

    public AppSettings(Context context) {
        prefs = context.getSharedPreferences("zenid_prefs", Context.MODE_PRIVATE);
    }

    public String getServerUrl() { return prefs.getString("server_url", null); }
    public void setServerUrl(String value) {
        if (value == null) prefs.edit().remove("server_url").apply();
        else prefs.edit().putString("server_url", value).apply();
    }

    public String getApiKey() { return prefs.getString("api_key", null); }
    public void setApiKey(String value) {
        if (value == null) prefs.edit().remove("api_key").apply();
        else prefs.edit().putString("api_key", value).apply();
    }

    public String getProfile() { return prefs.getString("profile", ""); }
    public void setProfile(String value) { prefs.edit().putString("profile", value != null ? value : "").apply(); }

    public String getCountry() { return prefs.getString("country", null); }
    public void setCountry(String value) {
        if (value == null) prefs.edit().remove("country").apply();
        else prefs.edit().putString("country", value).apply();
    }

    public String getRole() { return prefs.getString("role", null); }
    public void setRole(String value) {
        if (value == null) prefs.edit().remove("role").apply();
        else prefs.edit().putString("role", value).apply();
    }

    public String getPage() { return prefs.getString("page", null); }
    public void setPage(String value) {
        if (value == null) prefs.edit().remove("page").apply();
        else prefs.edit().putString("page", value).apply();
    }
}
