package cz.trask.zenid.sample.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import cz.trask.zenid.sample.LanguageUtils;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.VisualizationSettings;
import cz.trask.zenid.sdk.ZenIdException;
import cz.trask.zenid.sdk.models.SelfieStateContainerForPublicData;
import cz.trask.zenid.sdk.models.SelfieVerifierSettings;
import cz.trask.zenid.sdk.models.UploadReadyData;
import cz.trask.zenid.sdk.view.SelfieView;
import timber.log.Timber;

public class SelfieActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie);

        SelfieView selfieView = findViewById(R.id.selfieView);

        try {
            selfieView.setLifecycleOwner(this);
            selfieView.setVerifierSettings(new SelfieVerifierSettings());
            selfieView.enableDefaultVisualization(new VisualizationSettings.Builder()
                    .showDebugVisualization(false)
                    .language(LanguageUtils.getLanguage())
                    .build());
        } catch (Exception e) {
            Timber.e(e);
            finish();
            return;
        }

        selfieView.setCallback(new SelfieView.SelfieViewCallback() {
            @Override
            public void onStateChanged(SelfieStateContainerForPublicData stateContainer) {}

            @Override
            public void onResult(UploadReadyData uploadReadyData) {
                startActivity(ResultActivity.newIntent(SelfieActivity.this, uploadReadyData.getSignedSamplePackage()));
                finish();
            }

            @Override
            public void onError(ZenIdException exception) {
                Timber.e(exception);
            }
        });
    }
}
