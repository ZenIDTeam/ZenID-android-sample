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
import cz.trask.zenid.sample.LanguageUtils;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.VisualizationSettings;
import cz.trask.zenid.sdk.ZenIdException;
import cz.trask.zenid.sdk.models.UploadReadyData;
import timber.log.Timber;

public class MSLivenessActivity extends AppCompatActivity {

    public static final String EXTRA_RESTART = "EXTRA_RESTART";

    private static final String BUNDLE_CHALLENGE_TOKEN = "challenge_token";

    private MSLivenessView msLivenessView;
    private ProgressBar progressBar;
    private String challengeToken;

    public static Intent newIntent(@NonNull Context context, String challengeToken) {
        Intent intent = new Intent(context, MSLivenessActivity.class);
        intent.putExtra(BUNDLE_CHALLENGE_TOKEN, challengeToken);
        return intent;
    }

    public static Intent newIntent(@NonNull Context context, String challengeToken, boolean restart) {
        Intent intent = new Intent(context, MSLivenessActivity.class);
        intent.putExtra(BUNDLE_CHALLENGE_TOKEN, challengeToken);
        intent.putExtra(EXTRA_RESTART, restart);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ms_liveness);

        challengeToken = getIntent().getStringExtra(BUNDLE_CHALLENGE_TOKEN);
        boolean restart = getIntent().getBooleanExtra(EXTRA_RESTART, false);

        msLivenessView = findViewById(R.id.msLivenessView);
        progressBar = findViewById(R.id.progressBar);

        try {
            msLivenessView.enableDefaultVisualization(new VisualizationSettings.Builder()
                    .language(LanguageUtils.getLanguage())
                    .showDebugVisualization(false)
                    .showTextInformation(true)
                    .build());
        } catch (Exception e) {
            Timber.e(e);
            return;
        }

        msLivenessView.setCallback(new MSLivenessView.Callback() {
            @Override
            public void onSetupInProgress(boolean progress) {
                progressBar.setVisibility(progress ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onResult(@NonNull UploadReadyData uploadReadyData) {
                startActivity(ResultActivity.newIntent(MSLivenessActivity.this, uploadReadyData.getSignedSamplePackage()));
                finish();
            }

            @Override
            public void onError(@NonNull ZenIdException exception) {
                Timber.e(exception);
                restartActivity();
            }

            @Override
            public void onBackPressed(@NonNull String action) {
                finish();
            }
        });

        if (restart) {
            msLivenessView.restart(challengeToken);
        } else {
            msLivenessView.start(challengeToken);
        }
    }

    private void restartActivity() {
        finish();
        startActivity(newIntent(this, challengeToken, true));
        overridePendingTransition(0, 0);
    }
}
