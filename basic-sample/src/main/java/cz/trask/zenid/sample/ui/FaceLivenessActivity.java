package cz.trask.zenid.sample.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import cz.trask.zenid.sample.LogUtils;
import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.VisualizationSettings;
import cz.trask.zenid.sdk.ZenIdException;
import cz.trask.zenid.sdk.api.model.SampleJson;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessMode;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessResult;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessSettings;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessState;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessStepParams;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessView;
import cz.trask.zenid.sdk.Language;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class FaceLivenessActivity extends AppCompatActivity {

    private FaceLivenessView faceLivenessView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_liveness);

        faceLivenessView = findViewById(R.id.faceLivenessView);

        VisualizationSettings visualizationSettings = new VisualizationSettings.Builder()
                .showDebugVisualization(false)
                .language(Language.ENGLISH)
                .build();

        FaceLivenessSettings faceLivenessSettings = new FaceLivenessSettings.Builder()
                .enableLegacyMode(false) // Use the pre-1.6.3 behavior: turn in any direction then smile.
                .maxAuxiliaryImageSize(300) // Auxiliary images will be resized to fit into this size while preserving the aspect ratio.
                .build();

        try {
//            faceLivenessView.setLoggerCallback((module, method, message) -> Timber.tag(module).d("%s - %s", method, message));
            faceLivenessView.setLifecycleOwner(this);
            faceLivenessView.enableDefaultVisualization(visualizationSettings);
            faceLivenessView.setFaceLivenessSettings(faceLivenessSettings);
            faceLivenessView.setMode(FaceLivenessMode.VIDEO); // FaceLivenessMode.PICTURE is the default value
        } catch (Exception e) {
            Timber.e(e);
            finish();
            return;
        }

        faceLivenessView.setCallback(new FaceLivenessView.Callback() {

            @Override
            public void onStateChanged(FaceLivenessState state, @Nullable FaceLivenessStepParams stepParams) {
//                Timber.i("onStateChanged - state: %s", state);
//                if (stepParams != null) {
//                    Timber.i("onStateChanged - stepParams.name: %s", stepParams.getName());
//                    Timber.i("onStateChanged - stepParams.isHasFailed: %s", stepParams.isHasFailed());
//                    Timber.i("onStateChanged - stepParams.getPassedCheckCount: %s", stepParams.getPassedCheckCount());
//                    Timber.i("onStateChanged - stepParams.getTotalCheckCount: %s", stepParams.getTotalCheckCount());
//                }
            }

            @Override
            public void onResult(FaceLivenessResult faceLivenessResult) {
                FaceLivenessMode faceLivenessMode = faceLivenessResult.getMode();
                if (FaceLivenessMode.VIDEO.equals(faceLivenessMode)) {
                    LogUtils.logInfo(getApplicationContext(), "Uploading video...");
                    postSelfieVideoSample(faceLivenessResult);
                } else {
                    LogUtils.logInfo(getApplicationContext(), "Uploading picture...");
                    postSelfieSample(faceLivenessResult);
                }
                finish();
            }

            @Override
            public void onError(ZenIdException e) {
                Timber.e(e);
            }
        });
    }

    private void postSelfieSample(FaceLivenessResult result) {
        MyApplication.apiService.postSelfieSample(result.getFilePath(), result.getSignature()).enqueue(new retrofit2.Callback<SampleJson>() {

            @Override
            public void onResponse(@NonNull Call<SampleJson> call, @NonNull Response<SampleJson> response) {
                LogUtils.logInfo(getApplicationContext(), "...picture has been uploaded!");

            }

            @Override
            public void onFailure(@NonNull Call<SampleJson> call, @NonNull Throwable t) {
                Timber.e(t);
            }
        });
    }

    private void postSelfieVideoSample(FaceLivenessResult result) {
        MyApplication.apiService.postSelfieVideoSample(result.getVideoFilePath(), result.getSignature()).enqueue(new retrofit2.Callback<SampleJson>() {

            @Override
            public void onResponse(@NonNull Call<SampleJson> call, @NonNull Response<SampleJson> response) {
                LogUtils.logInfo(getApplicationContext(), "...video has been uploaded!");
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<SampleJson> call, @NonNull Throwable t) {
                Timber.e(t);
            }
        });
    }
}
