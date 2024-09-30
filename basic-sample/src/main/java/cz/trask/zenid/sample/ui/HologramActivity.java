package cz.trask.zenid.sample.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
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
import cz.trask.zenid.sdk.ZenIdException;
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

        hologramView = findViewById(R.id.hologramView);

        DocumentAcceptableInput.Filter filter1 = new DocumentAcceptableInput.Filter(DocumentRole.ID, DocumentPage.FRONT_SIDE, DocumentCountry.CZ);
        DocumentAcceptableInput documentAcceptableInput = new DocumentAcceptableInput(Arrays.asList(filter1));

        VisualizationSettings visualizationSettings = new VisualizationSettings.Builder()
                .showDebugVisualization(true)
                .language(Language.ENGLISH)
                .build();

        HologramSettings hologramSettings = new HologramSettings.Builder()
                .enableAimingCircle(true)
                .build();

        try {
//            hologramView.setLoggerCallback((module, method, message) -> Timber.tag(module).d("%s - %s", method, message));
            hologramView.setLifecycleOwner(this);
            hologramView.setDocumentAcceptableInput(documentAcceptableInput);
            hologramView.enableDefaultVisualization(visualizationSettings);
            hologramView.setHologramSettings(hologramSettings);
        } catch (Exception e) {
            Timber.e(e);
            finish();
            return;
        }

        hologramView.setCallback(new HologramView.Callback() {

            @Override
            public void onStateChanged(HologramState state) {
//                Timber.i("onStateChanged: %s", state);
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

            @Override
            public void onError(ZenIdException e) {
                Timber.e(e);
            }
        });
    }

    private void postHologramSample(HologramResult result) {

        MyApplication.apiService.postHologramSample(result).enqueue(new retrofit2.Callback<SampleJson>() {

            @Override
            public void onResponse(@NonNull Call<SampleJson> call, @NonNull Response<SampleJson> response) {
                LogUtils.logInfo(getApplicationContext(), "...video has been uploaded!");
            }

            @Override
            public void onFailure(@NonNull Call<SampleJson> call, @NonNull Throwable t) {
                Timber.e(t);
            }
        });
    }
}