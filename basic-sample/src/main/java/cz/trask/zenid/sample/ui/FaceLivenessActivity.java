package cz.trask.zenid.sample.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import cz.trask.zenid.sample.LogUtils;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.VisualizationSettings;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessResult;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessState;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessView;
import cz.trask.zenid.sdk.Language;
import timber.log.Timber;

public class FaceLivenessActivity extends AppCompatActivity {

    private FaceLivenessView faceLivenessView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_liveness);

        VisualizationSettings visualizationSettings = new VisualizationSettings.Builder()
                .showDebugVisualization(true)
                .language(Language.ENGLISH)
                .build();

        faceLivenessView = findViewById(R.id.faceLivenessView);
        faceLivenessView.setLifecycleOwner(this);
        faceLivenessView.enableDefaultVisualization(visualizationSettings);
        faceLivenessView.setCallback(new FaceLivenessView.Callback() {

            @Override
            public void onStateChanged(FaceLivenessState state) {
                Timber.i("onStateChanged %s", state);
            }

            @Override
            public void onPictureTaken(FaceLivenessResult result) {
                LogUtils.logInfo(getApplicationContext(), "onPictureTaken... " + result.getFilePath());
            }
        });
    }
}
