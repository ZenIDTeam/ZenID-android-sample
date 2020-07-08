package cz.trask.zenid.sample;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cz.trask.zenid.sdk.DocumentCountry;
import cz.trask.zenid.sdk.DocumentPage;
import cz.trask.zenid.sdk.DocumentRole;
import cz.trask.zenid.sdk.ZenId;
import cz.trask.zenid.sdk.api.model.InitResponseJson;
import cz.trask.zenid.sdk.api.model.SampleJson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initAuthorizeButton();

        initDocumentVerifierButton();

        initLivenessCheckButton();

        setZenIdCallback();
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
        findViewById(R.id.button_document_verifier).setOnClickListener(v -> {
            if (ZenId.get().getSecurity().isAuthorized()) {
                ZenId.get().startIdentityDocumentVerifier(MyActivity.this, DocumentPage.FRONT_SIDE, DocumentCountry.CZ);
            } else {
                throwUnauthorizedException();
            }
        });
    }

    private void initLivenessCheckButton() {
        findViewById(R.id.button_liveness_check).setOnClickListener(v -> {
            if (ZenId.get().getSecurity().isAuthorized()) {
                ZenId.get().startFaceLivenessDetector(MyActivity.this);
            } else {
                throwUnauthorizedException();
            }
        });
    }

    private void setZenIdCallback() {
        ZenId.get().setCallback(new ZenId.Callback() {

            @Override
            public void onDocumentPictureTaken(DocumentCountry documentCountry, DocumentRole documentRole, Integer documentCode, DocumentPage documentPage, String documentPicturePath) {
                MyApplication.apiService.postDocumentPictureSample(documentCountry, documentRole, documentCode, documentPage, documentPicturePath).enqueue(new retrofit2.Callback<SampleJson>() {

                    @Override
                    public void onResponse(Call<SampleJson> call, Response<SampleJson> response) {
                        String sampleId = response.body().getSampleId();
                        Timber.i("sampleId: %s", sampleId);
                    }

                    @Override
                    public void onFailure(Call<SampleJson> call, Throwable t) {
                        Timber.e(t);
                    }
                });
            }

            @Override
            public void onDocumentVideoTaken(DocumentCountry documentCountry, DocumentRole documentRole, Integer documentCode, DocumentPage documentPage, String documentVideoPath) {
                // Not used yet
            }

            @Override
            public void onSelfiePictureTaken(String selfiePicturePath) {
                MyApplication.apiService.postSelfieSample(selfiePicturePath).enqueue(new retrofit2.Callback<SampleJson>() {

                    @Override
                    public void onResponse(Call<SampleJson> call, Response<SampleJson> response) {
                        String sampleId = response.body().getSampleId();
                        Timber.i("sampleId: %s", sampleId);
                    }

                    @Override
                    public void onFailure(Call<SampleJson> call, Throwable t) {
                        Timber.e(t);
                    }
                });
            }

            @Override
            public void onUserLeft() {
                Timber.i("User left the sdk flow without completing it");
            }
        });
    }

    private void throwUnauthorizedException() {
        throw new IllegalStateException("Your application " + getApplicationContext().getPackageName() + " is not yet authorized.");
    }
}
