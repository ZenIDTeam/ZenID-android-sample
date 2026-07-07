package cz.trask.zenid.sample.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import cz.trask.zenid.sample.LanguageUtils;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.VisualizationSettings;
import cz.trask.zenid.sdk.ZenIdException;
import cz.trask.zenid.sdk.models.FaceLivenessStateContainerForPublicData;
import cz.trask.zenid.sdk.models.FaceLivenessVerifierSettings;
import cz.trask.zenid.sdk.models.UploadReadyData;
import cz.trask.zenid.sdk.view.FaceLivenessView;
import timber.log.Timber;

public class FaceLivenessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_liveness);

        FaceLivenessView faceLivenessView = findViewById(R.id.faceLivenessView);

        FaceLivenessVerifierSettings settings = new FaceLivenessVerifierSettings();
        settings.setEnableLegacyMode(false);
        settings.setShowSmileAnimation(true);

        try {
            faceLivenessView.setLifecycleOwner(this);
            faceLivenessView.setVerifierSettings(settings);
            faceLivenessView.enableDefaultVisualization(new VisualizationSettings.Builder()
                    .showDebugVisualization(false)
                    .language(LanguageUtils.getLanguage())
                    .build());
        } catch (Exception e) {
            Timber.e(e);
            finish();
            return;
        }

        faceLivenessView.setCallback(new FaceLivenessView.FaceLivenessViewCallback() {
            @Override
            public void onStateChanged(FaceLivenessStateContainerForPublicData stateContainer) {}

            @Override
            public void onResult(UploadReadyData uploadReadyData) {
                startActivity(ResultActivity.newIntent(FaceLivenessActivity.this, uploadReadyData.getSignedSamplePackage()));
                finish();
            }

            @Override
            public void onError(ZenIdException exception) {
                Timber.e(exception);
            }
        });
    }
}
