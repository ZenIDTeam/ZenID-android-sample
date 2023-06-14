package cz.trask.zenid.sample.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import cz.trask.zenid.sample.LogUtils;
import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.DocumentAcceptableInput;
import cz.trask.zenid.sdk.DocumentCountry;
import cz.trask.zenid.sdk.DocumentPage;
import cz.trask.zenid.sdk.DocumentRole;
import cz.trask.zenid.sdk.HologramResult;
import cz.trask.zenid.sdk.HologramSettings;
import cz.trask.zenid.sdk.HologramState;
import cz.trask.zenid.sdk.HologramView;
import cz.trask.zenid.sdk.Language;
import cz.trask.zenid.sdk.VisualizationSettings;
import cz.trask.zenid.sdk.api.model.SampleJson;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class HologramActivity extends AppCompatActivity {

    private HologramView hologramView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hologram);

        DocumentAcceptableInput.Filter filter1 = new DocumentAcceptableInput.Filter(DocumentRole.ID, DocumentPage.FRONT_SIDE, DocumentCountry.CZ);
        DocumentAcceptableInput documentAcceptableInput = new DocumentAcceptableInput(Arrays.asList(filter1));


        VisualizationSettings visualizationSettings = new VisualizationSettings.Builder()
                .showDebugVisualization(true)
                .language(Language.ENGLISH)
                .build();

        HologramSettings hologramSettings = new HologramSettings.Builder()
                .enableAimingCircle(true)
                .build();

        hologramView = findViewById(R.id.hologramView);
        hologramView.setLifecycleOwner(this);
        hologramView.setDocumentAcceptableInput(documentAcceptableInput);
        hologramView.enableDefaultVisualization(visualizationSettings);
        hologramView.setHologramSettings(hologramSettings);
        hologramView.setCallback(new HologramView.Callback() {

            @Override
            public void onStateChanged(HologramState state) {
                Timber.i("onStateChanged: %s", state);
            }

            @Override
            public void onVideoRecordingStart() {
                LogUtils.logInfo(getApplicationContext(), "onVideoRecordingStart... ");
            }

            @Override
            public void onVideoTaken(HologramResult result) {
                LogUtils.logInfo(getApplicationContext(), "onVideoTaken... " + result.getVideoFilePath());
                postHologramSample(result);
                finish();
            }
        });
    }

    private void postHologramSample(HologramResult result) {

        MyApplication.apiService.postHologramSample(result).enqueue(new retrofit2.Callback<SampleJson>() {

            @Override
            public void onResponse(Call<SampleJson> call, Response<SampleJson> response) {
                LogUtils.logInfo(getApplicationContext(), "...video has been uploaded!");
            }

            @Override
            public void onFailure(Call<SampleJson> call, Throwable t) {
                Timber.e(t);
            }
        });
    }
}