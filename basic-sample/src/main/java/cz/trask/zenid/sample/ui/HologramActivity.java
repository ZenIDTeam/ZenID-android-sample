package cz.trask.zenid.sample.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import cz.trask.zenid.sample.LogUtils;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.DocumentAcceptableInput;
import cz.trask.zenid.sdk.DocumentCountry;
import cz.trask.zenid.sdk.DocumentPage;
import cz.trask.zenid.sdk.DocumentRole;
import cz.trask.zenid.sdk.HologramResult;
import cz.trask.zenid.sdk.HologramState;
import cz.trask.zenid.sdk.HologramView;
import cz.trask.zenid.sdk.Language;
import cz.trask.zenid.sdk.VisualizationSettings;
import timber.log.Timber;

public class HologramActivity extends AppCompatActivity {

    private HologramView hologramView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hologram);

        DocumentAcceptableInput.Filter filter1 = new DocumentAcceptableInput.Filter(DocumentRole.ID,  DocumentPage.FRONT_SIDE, DocumentCountry.CZ);
        DocumentAcceptableInput documentAcceptableInput = new DocumentAcceptableInput(Arrays.asList(filter1));


        VisualizationSettings visualizationSettings = new VisualizationSettings.Builder()
                .showDebugVisualization(true)
                .language(Language.ENGLISH)
                .build();

        hologramView = findViewById(R.id.hologramView);
        hologramView.setLifecycleOwner(this);
        hologramView.setDocumentAcceptableInput(documentAcceptableInput);
        hologramView.enableDefaultVisualization(visualizationSettings);
        hologramView.setCallback(new HologramView.Callback() {

            @Override
            public void onStateChanged(HologramState state) {
                Timber.i("onStateChanged: %s", state);
            }

            @Override
            public void onVideoTaken(HologramResult result) {
                LogUtils.logInfo(getApplicationContext(), "onVideoTaken... " + result.getVideoFilePath());
            }
        });
    }
}
