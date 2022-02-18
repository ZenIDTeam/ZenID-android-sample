package cz.trask.zenid.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.ZenId;
import cz.trask.zenid.sdk.api.model.InitResponseJson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initAuthorizeButton();

        initDocumentVerifierButton();

        initSelfieButton();

        initFaceLivenessButton();

        initHologramButton();
    }

    private void initAuthorizeButton() {
        findViewById(R.id.button_authorize).setOnClickListener(v -> {
            String challengeToken = ZenId.get().getSecurity().getChallengeToken();
            Timber.i("challengeToken: %s", challengeToken);
            MyApplication.apiService.getInitSdk(challengeToken).enqueue(new Callback<InitResponseJson>() {

                @Override
                public void onResponse(Call<InitResponseJson> call, Response<InitResponseJson> response) {
                    String responseToken = response.body().getResponse();
                    Timber.i("responseToken: %s", response);
                    boolean authorized = ZenId.get().getSecurity().authorize(getApplicationContext(), responseToken);
                    Toast.makeText(getApplicationContext(), "Authorized: " + authorized, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<InitResponseJson> call, Throwable t) {
                    Timber.e(t);
                }
            });
        });
    }

    private void initDocumentVerifierButton() {
        findViewById(R.id.button_documentPicture).setOnClickListener(v -> {
            if (ZenId.get().getSecurity().isAuthorized()) {
                startActivity(new Intent(getApplicationContext(), DocumentPictureActivity.class));
            } else {
                logNotAuthorizedError();
            }
        });
    }

    private void initSelfieButton() {
        findViewById(R.id.button_selfie).setOnClickListener(v -> {
            if (ZenId.get().getSecurity().isAuthorized()) {
                startActivity(new Intent(getApplicationContext(), SelfieActivity.class));
            } else {
                logNotAuthorizedError();
            }
        });
    }

    private void initFaceLivenessButton() {
        findViewById(R.id.button_faceLiveness).setOnClickListener(v -> {
            if (ZenId.get().getSecurity().isAuthorized()) {
                startActivity(new Intent(getApplicationContext(), FaceLivenessActivity.class));
            } else {
                logNotAuthorizedError();
            }
        });
    }

    private void initHologramButton() {
        findViewById(R.id.button_hologram).setOnClickListener(v -> {
            if (ZenId.get().getSecurity().isAuthorized()) {
                startActivity(new Intent(getApplicationContext(), HologramActivity.class));
            } else {
                logNotAuthorizedError();
            }
        });
    }

    private void logNotAuthorizedError() {
        String msg = "Your application " + getApplicationContext().getPackageName() + " is not yet authorized.";
        Timber.i(msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
