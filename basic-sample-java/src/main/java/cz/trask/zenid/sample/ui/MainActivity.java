package cz.trask.zenid.sample.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.ZenId;
import cz.trask.zenid.sdk.ZenIdException;
import cz.trask.zenid.sdk.api.model.InitResponseJson;
import cz.trask.zenid.sdk.api.model.ProfilesResponseJson;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final int MENU_LOGOUT = 1;

    private TextView textServer;
    private TextView textProfile;
    private TextView textCountry;
    private TextView textRole;
    private TextView textPage;
    private View rowProfile;
    private View rowCountry;
    private View rowRole;
    private View rowPage;
    private View verifiersSection;
    private View loadingOverlay;
    private TextView versionText;

    private String challengeToken;
    private final List<String> profiles = new ArrayList<>();

    private final ActivityResultLauncher<Intent> qrScannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String url = result.getData().getStringExtra(QrScannerActivity.EXTRA_URL);
                    String apiKey = result.getData().getStringExtra(QrScannerActivity.EXTRA_API_KEY);
                    if (url != null) {
                        String normalizedUrl = url.endsWith("/") ? url : url + "/";
                        MyApplication.appSettings.setServerUrl(normalizedUrl);
                        MyApplication.appSettings.setApiKey(apiKey);
                        ((MyApplication) getApplication()).rebuildApiService();
                        updateServerText(normalizedUrl);
                        authorize();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> pickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String type = result.getData().getStringExtra(PickerActivity.EXTRA_TYPE);
                    String value = result.getData().getStringExtra(PickerActivity.EXTRA_VALUE);
                    handlePickerResult(type, value);
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) {
                    startMsLiveness();
                } else {
                    Toast.makeText(this, "Camera permission required for MS Liveness", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ZenID Sample");

        textServer = findViewById(R.id.textServer);
        textProfile = findViewById(R.id.textProfile);
        textCountry = findViewById(R.id.textCountry);
        textRole = findViewById(R.id.textRole);
        textPage = findViewById(R.id.textPage);
        rowProfile = findViewById(R.id.rowProfile);
        rowCountry = findViewById(R.id.rowCountry);
        rowRole = findViewById(R.id.rowRole);
        rowPage = findViewById(R.id.rowPage);
        verifiersSection = findViewById(R.id.verifiersSection);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        versionText = findViewById(R.id.versionText);

        initRowListeners();
        refreshSettingsUi();

        try {
            versionText.setText("SDK version " + ZenId.get().getAndroidLibVersion());
        } catch (Exception e) {
            versionText.setText("SDK version N/A");
        }

        String savedUrl = MyApplication.appSettings.getServerUrl();
        if (savedUrl != null && !savedUrl.isEmpty()) {
            authorize();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String serverUrl = MyApplication.appSettings.getServerUrl();
        if (serverUrl != null && !serverUrl.isEmpty()) {
            menu.add(0, MENU_LOGOUT, 0, "Logout")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == MENU_LOGOUT) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("This will clear all saved settings. You will need to scan the QR code again.")
                    .setPositiveButton("Logout", (dialog, which) -> logout())
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        MyApplication.appSettings.setServerUrl(null);
        MyApplication.appSettings.setApiKey(null);
        MyApplication.appSettings.setProfile(null);
        MyApplication.appSettings.setCountry(null);
        MyApplication.appSettings.setRole(null);
        MyApplication.appSettings.setPage(null);
        MyApplication.apiService = null;
        profiles.clear();
        verifiersSection.setVisibility(View.GONE);
        invalidateOptionsMenu();
        refreshSettingsUi();
    }

    private void initRowListeners() {
        findViewById(R.id.rowServer).setOnClickListener(v ->
                qrScannerLauncher.launch(new Intent(this, QrScannerActivity.class)));

        findViewById(R.id.rowProfile).setOnClickListener(v ->
                openPicker("profile", MyApplication.appSettings.getProfile()));

        findViewById(R.id.rowCountry).setOnClickListener(v ->
                openPicker("country", MyApplication.appSettings.getCountry()));

        findViewById(R.id.rowRole).setOnClickListener(v ->
                openPicker("role", MyApplication.appSettings.getRole()));

        findViewById(R.id.rowPage).setOnClickListener(v ->
                openPicker("page", MyApplication.appSettings.getPage()));

        findViewById(R.id.rowDocument).setOnClickListener(v -> {
            Intent intent = new Intent(this, DocumentPictureActivity.class);
            String c = MyApplication.appSettings.getCountry();
            intent.putExtra(DocumentPictureActivity.EXTRA_COUNTRY, c != null ? c : PickerActivity.ANY_COUNTRY);
            intent.putExtra(DocumentPictureActivity.EXTRA_ROLE, MyApplication.appSettings.getRole());
            intent.putExtra(DocumentPictureActivity.EXTRA_PAGE, MyApplication.appSettings.getPage());
            startActivity(intent);
        });

        findViewById(R.id.rowSelfie).setOnClickListener(v ->
                startActivity(new Intent(this, SelfieActivity.class)));

        findViewById(R.id.rowFaceLiveness).setOnClickListener(v ->
                startActivity(new Intent(this, FaceLivenessActivity.class)));

        findViewById(R.id.rowMsLiveness).setOnClickListener(v ->
                requestCameraPermissionAndStartMsLiveness());

        findViewById(R.id.rowHologram).setOnClickListener(v -> {
            Intent intent = new Intent(this, HologramActivity.class);
            String c = MyApplication.appSettings.getCountry();
            intent.putExtra(HologramActivity.EXTRA_COUNTRY, c != null ? c : PickerActivity.ANY_COUNTRY);
            intent.putExtra(HologramActivity.EXTRA_ROLE, MyApplication.appSettings.getRole());
            intent.putExtra(HologramActivity.EXTRA_PAGE, MyApplication.appSettings.getPage());
            startActivity(intent);
        });

    }

    private void openPicker(String type, String selected) {
        Intent intent = new Intent(this, PickerActivity.class);
        intent.putExtra(PickerActivity.EXTRA_TYPE, type);
        intent.putExtra(PickerActivity.EXTRA_SELECTED, selected);
        if ("profile".equals(type)) {
            intent.putStringArrayListExtra(PickerActivity.EXTRA_PROFILES, new ArrayList<>(profiles));
        }
        if ("role".equals(type) || "page".equals(type)) {
            String c = MyApplication.appSettings.getCountry();
            intent.putExtra(PickerActivity.EXTRA_COUNTRY, c != null ? c : PickerActivity.ANY_COUNTRY);
        }
        if ("page".equals(type)) {
            intent.putExtra(PickerActivity.EXTRA_ROLE, MyApplication.appSettings.getRole());
        }
        pickerLauncher.launch(intent);
    }

    private void handlePickerResult(String type, String value) {
        if (type == null) return;
        switch (type) {
            case "country":
                MyApplication.appSettings.setCountry(value);
                MyApplication.appSettings.setRole(null);
                MyApplication.appSettings.setPage(null);
                break;
            case "role":
                MyApplication.appSettings.setRole(value);
                MyApplication.appSettings.setPage(null);
                break;
            case "page":
                MyApplication.appSettings.setPage(value);
                break;
            case "profile":
                MyApplication.appSettings.setProfile(value);
                if (ZenId.get().getSecurity().isAuthorized()) {
                    try {
                        String profile = value != null && !value.isEmpty() ? value : null;
                        if (profile != null) ZenId.get().getSecurity().selectProfile(profile);
                    } catch (ZenIdException e) {
                        Timber.e(e);
                    }
                }
                break;
        }
        refreshSettingsUi();
    }

    private void refreshSettingsUi() {
        String serverUrl = MyApplication.appSettings.getServerUrl();
        updateServerText(serverUrl);

        String profile = MyApplication.appSettings.getProfile();
        textProfile.setText(profile != null && !profile.isEmpty() ? profile : getString(R.string.default_label));

        String country = MyApplication.appSettings.getCountry();
        textCountry.setText(country != null && !country.isEmpty() ? country : PickerActivity.ANY_COUNTRY);

        String role = MyApplication.appSettings.getRole();
        textRole.setText(role != null ? role : getString(R.string.none_label));

        String page = MyApplication.appSettings.getPage();
        textPage.setText(page != null ? page : getString(R.string.none_label));

        boolean serverSet = serverUrl != null && !serverUrl.isEmpty();
        boolean roleSet = role != null && !role.isEmpty();
        rowProfile.setEnabled(serverSet);
        rowProfile.setAlpha(serverSet ? 1f : 0.38f);
        rowCountry.setEnabled(serverSet);
        rowCountry.setAlpha(serverSet ? 1f : 0.38f);
        rowRole.setEnabled(serverSet);
        rowRole.setAlpha(serverSet ? 1f : 0.38f);
        rowPage.setEnabled(roleSet);
        rowPage.setAlpha(roleSet ? 1f : 0.38f);
    }

    private void updateServerText(String url) {
        if (url == null || url.isEmpty()) {
            textServer.setText(getString(R.string.select_server));
            return;
        }
        try {
            String host = Uri.parse(url).getHost();
            textServer.setText((host != null && !host.isEmpty()) ? host : url);
        } catch (Exception e) {
            textServer.setText(url);
        }
    }

    private void authorize() {
        loadingOverlay.setVisibility(View.VISIBLE);
        verifiersSection.setVisibility(View.GONE);
        challengeToken = null;
        try {
            challengeToken = ZenId.get().getSecurity().getChallengeToken();
        } catch (ZenIdException e) {
            Timber.e(e);
            loadingOverlay.setVisibility(View.GONE);
            return;
        }

        if (MyApplication.apiService == null) {
            loadingOverlay.setVisibility(View.GONE);
            return;
        }

        MyApplication.apiService.initSdk(challengeToken).enqueue(new Callback<InitResponseJson>() {
            @Override
            public void onResponse(@NonNull Call<InitResponseJson> call, @NonNull Response<InitResponseJson> response) {
                InitResponseJson body = response.body();
                if (body == null) {
                    Timber.e("Authorization response body is empty");
                    loadingOverlay.setVisibility(View.GONE);
                    return;
                }
                try {
                    boolean authorized = ZenId.get().getSecurity().authorize(getApplicationContext(), body.getResponse());
                    if (authorized) {
                        String profile = MyApplication.appSettings.getProfile();
                        if (profile != null && !profile.isEmpty()) {
                            ZenId.get().getSecurity().selectProfile(profile);
                        }
                        verifiersSection.setVisibility(View.VISIBLE);
                        loadProfiles();
                    }
                } catch (ZenIdException e) {
                    Timber.e(e);
                }
                loadingOverlay.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<InitResponseJson> call, @NonNull Throwable t) {
                Timber.e(t);
                loadingOverlay.setVisibility(View.GONE);
            }
        });
    }

    private void loadProfiles() {
        if (MyApplication.apiService == null) return;
        MyApplication.apiService.getProfiles().enqueue(new Callback<ProfilesResponseJson>() {
            @Override
            public void onResponse(@NonNull Call<ProfilesResponseJson> call, @NonNull Response<ProfilesResponseJson> response) {
                ProfilesResponseJson body = response.body();
                if (body != null && body.getResults() != null) {
                    profiles.clear();
                    profiles.addAll(body.getResults());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfilesResponseJson> call, @NonNull Throwable t) {
                Timber.e(t);
            }
        });
    }

    private void requestCameraPermissionAndStartMsLiveness() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startMsLiveness();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startMsLiveness() {
        startActivity(MSLivenessActivity.newIntent(this, challengeToken));
    }
}
