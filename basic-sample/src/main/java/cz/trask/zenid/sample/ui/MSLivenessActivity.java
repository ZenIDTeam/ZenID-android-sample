package cz.trask.zenid.sample.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import cz.trask.sdk.ms_liveness.MSLivenessView;
import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sdk.VisualizationSettings;
import cz.trask.zenid.sdk.ZenIdException;
import cz.trask.zenid.sdk.api.model.SampleJson;
import cz.trask.zenid.sdk.enums.SupportedLanguages;
import cz.trask.zenid.sdk.models.UploadReadyData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
import cz.trask.zenid.sample.R;

public class MSLivenessActivity extends AppCompatActivity {

    public static final String EXTRA_RESTART = "EXTRA_RESTART";

    /*-------------------------*/
    /*        CONSTANTS        */
    /*-------------------------*/

    private static final String BUNDLE_CHALLENGE_TOKEN = "challenge_token";

    /*-------------------------*/
    /*         FIELDS          */
    /*-------------------------*/

    private MSLivenessView msLivenessView;
    private ProgressBar progressBar;
    private String challengeToken;

    /*-------------------------*/
    /*       CONSTRUCTORS      */
    /*-------------------------*/

    public static Intent newIntent(@NonNull Context context, String challengeToken) {
        Intent intent = new Intent(context, MSLivenessActivity.class);
        intent.putExtra(BUNDLE_CHALLENGE_TOKEN, challengeToken);
        return intent;
    }

    public static Intent newIntent(@NonNull Context context, String challengeToken, boolean restart) {
        Intent intent = new Intent(context, MSLivenessActivity.class);
        intent.putExtra(BUNDLE_CHALLENGE_TOKEN, challengeToken);
        intent.putExtra(EXTRA_RESTART, challengeToken);
        return intent;
    }

    /*-------------------------*/
    /*   OVERRIDDEN METHODS    */
    /*-------------------------*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ms_liveness);

        challengeToken = null;
        if (getIntent().hasExtra(BUNDLE_CHALLENGE_TOKEN)) {
            challengeToken = getIntent().getStringExtra(BUNDLE_CHALLENGE_TOKEN);
        }

        boolean restart = getIntent().getBooleanExtra(MSLivenessActivity.EXTRA_RESTART, false);

        msLivenessView = findViewById(R.id.msLivenessView);
        progressBar = findViewById(R.id.progressBar);

        try {
            msLivenessView.enableDefaultVisualization(createDefaultVisualizationSettings());
        } catch (Exception exception) {
            Timber.e(exception);
            return;
        }

        msLivenessView.setCallback(new MSLivenessView.Callback() {

            @Override
            public void onSetupInProgress(boolean progress) {
                if (progress) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onResult(@NonNull UploadReadyData uploadReadyData) {
                postSample(uploadReadyData);
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(@NonNull ZenIdException exception) {
                Timber.e(exception);
                restartActivity();
            }

            @Override
            public void onBackPressed() {
                finish();
            }
        });

        if (restart) {
            msLivenessView.restart(challengeToken);
        } else {
            msLivenessView.start(challengeToken);
        }
    }

    /*-------------------------*/
    /*     PRIVATE METHODS     */
    /*-------------------------*/

    private void postSample(UploadReadyData uploadReadyData) {
        MyApplication.apiService.postSample(uploadReadyData.getSignedSamplePackage()).enqueue(new Callback<SampleJson>() {

            @Override
            public void onResponse(@NonNull Call<SampleJson> call, @NonNull Response<SampleJson> response) {
                SampleJson sample = response.body();
                if (sample == null) {
                    Timber.e("Response body is empty");
                    return;
                }

                if (sample.getState().equals("NotDone") || sample.getState().equals("Error")) {
                    Timber.e("Upload failed... %s", sample.getErrorCode());
                    return;
                }

                String msg = String.format("MS Faceliveness result sent; SampleId: %s", sample.getSampleId());
                Timber.e(msg);
            }

            @Override
            public void onFailure(@NonNull Call<SampleJson> call, @NonNull Throwable t) {
                Timber.e(t);
            }
        });
    }

    private void restartActivity() {
        finish();
        startActivity(newIntent(this, challengeToken,true));
        overridePendingTransition(0, 0);
    }

    private VisualizationSettings createDefaultVisualizationSettings() {
        return new VisualizationSettings.Builder()
                .language(SupportedLanguages.English)
                .showDebugVisualization(false)
                .showTextInformation(true)
                .build();
    }
}
