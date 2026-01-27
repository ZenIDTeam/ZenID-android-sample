package cz.trask.zenid.sample.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.ZenId;
import cz.trask.zenid.sdk.ZenIdException;
import cz.trask.zenid.sdk.api.model.InitResponseJson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
import android.Manifest;

public class MainActivity extends AppCompatActivity {

    /*-------------------------*/
    /*         FIELDS          */
    /*-------------------------*/

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    private String challengeToken;

    /*-------------------------*/
    /*   OVERRIDDEN METHODS    */
    /*-------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDocumentVerifierButton();

        initSelfieButton();

        initFaceLivenessButton();

        initMsLivenessButton();

        initHologramButton();

        Timber.i("Android lib version: %s", ZenId.get().getAndroidLibVersion());
        Timber.i("Core lib version: %s", ZenId.get().getCoreLibVersion());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ZenId.get().getSecurity().isAuthorized()) {
            authorize();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(MSLivenessActivity.newIntent(getApplicationContext(), challengeToken));
            } else {
                Toast.makeText(this, "Camera permission is required for face liveness", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*-------------------------*/
    /*     PRIVATE METHODS     */
    /*-------------------------*/

    private void authorize() {
        challengeToken = null;
        try {
            challengeToken = ZenId.get().getSecurity().getChallengeToken();
        } catch (ZenIdException e) {
            Timber.e(e);
        }
        Timber.i("challengeToken: %s", challengeToken);
        MyApplication.apiService.initSdk(challengeToken).enqueue(new Callback<InitResponseJson>() {

            @Override
            public void onResponse(@NonNull Call<InitResponseJson> call, @NonNull Response<InitResponseJson> response) {
                InitResponseJson initResponseJson = response.body();
                if (initResponseJson == null) {
                    Timber.e("Authorization response body is empty!");
                    return;
                }
                String responseToken = initResponseJson.getResponse();
                Timber.i("responseToken: %s", responseToken);
                try {
                    boolean authorized = ZenId.get().getSecurity().authorize(getApplicationContext(), responseToken);
                    Timber.i("Authorized: %s", authorized);
                    if (authorized) {
                        Toast.makeText(getApplicationContext(), "Authorized: " + authorized, Toast.LENGTH_SHORT).show();
                        // This is part of a new feature that allows customers to set frontend validator configs on the backend.
                        // On Init() call the SDK receives a list of profiles and their respective configs.
                        // Calling SelectProfile() sets what profile will be used for subsequent verifier usage.
                        // ZenId.get().getSecurity().selectProfile(Security.DEFAULT_PROFILE_NAME);
                        ZenId.get().getSecurity().selectProfile("NFC");
                    }

                } catch (ZenIdException e) {
                    Timber.e(e);
                }
            }

            @Override
            public void onFailure(@NonNull Call<InitResponseJson> call, @NonNull Throwable t) {
                Timber.e(t);
            }
        });
    }

    private void initDocumentVerifierButton() {
        findViewById(R.id.button_documentPicture).setOnClickListener(v -> {
            if (isAuthorized()) {
                startActivity(new Intent(getApplicationContext(), DocumentPictureActivity.class));
            } else {
                logNotAuthorizedError();
            }
        });
    }

    private void initSelfieButton() {
        findViewById(R.id.button_selfie).setOnClickListener(v -> {
            if (isAuthorized()) {
                startActivity(new Intent(getApplicationContext(), SelfieActivity.class));
            } else {
                logNotAuthorizedError();
            }
        });
    }

    private void initFaceLivenessButton() {
        findViewById(R.id.button_faceLiveness).setOnClickListener(v -> {
            if (isAuthorized()) {
                startActivity(new Intent(getApplicationContext(), FaceLivenessActivity.class));
            } else {
                logNotAuthorizedError();
            }
        });
    }

    private void initMsLivenessButton() {
        findViewById(R.id.button_ms_faceLiveness).setOnClickListener(v -> {
            if (isAuthorized()) {
                requestCameraPermissionAndStartMsLiveness();
            } else {
                logNotAuthorizedError();
            }
        });
    }

    private void initHologramButton() {
        findViewById(R.id.button_hologram).setOnClickListener(v -> {
            if (isAuthorized()) {
                startActivity(new Intent(getApplicationContext(), HologramActivity.class));
            } else {
                logNotAuthorizedError();
            }
        });
    }

    private boolean isAuthorized() {
        return ZenId.get().getSecurity().isAuthorized();
    }

    private void logNotAuthorizedError() {
        String msg = "Your application " + getApplicationContext().getPackageName() + " is not yet authorized.";
        Timber.i(msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void requestCameraPermissionAndStartMsLiveness() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startActivity(MSLivenessActivity.newIntent(getApplicationContext(), challengeToken));
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }
}
