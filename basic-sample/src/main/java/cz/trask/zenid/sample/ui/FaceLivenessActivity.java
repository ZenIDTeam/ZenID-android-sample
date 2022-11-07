package cz.trask.zenid.sample.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;

import cz.trask.zenid.sample.LogUtils;
import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.VisualizationSettings;
import cz.trask.zenid.sdk.api.model.SampleJson;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessMode;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessResult;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessSettings;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessState;
import cz.trask.zenid.sdk.faceliveness.FaceLivenessView;
import cz.trask.zenid.sdk.Language;
import cz.trask.zenid.sdk.selfie.SelfieResult;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class FaceLivenessActivity extends AppCompatActivity {

    private FaceLivenessView faceLivenessView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_liveness);

        VisualizationSettings visualizationSettings = new VisualizationSettings.Builder()
                .showDebugVisualization(false)
                .language(Language.ENGLISH)
                .build();

        FaceLivenessSettings faceLivenessSettings = new FaceLivenessSettings.Builder()
                .enableLegacyMode(false) // Use the pre-1.6.3 behavior: turn in any direction then smile.
                .maxAuxiliaryImageSize(300) // Auxiliary images will be resized to fit into this size while preserving the aspect ratio.
                .build();

        faceLivenessView = findViewById(R.id.faceLivenessView);
        faceLivenessView.setLifecycleOwner(this);
        faceLivenessView.enableDefaultVisualization(visualizationSettings);
        faceLivenessView.setFaceLivenessSettings(faceLivenessSettings);
        // faceLivenessView.setMode(FaceLivenessMode.VIDEO); // FaceLivenessMode.PICTURE is the default value
        faceLivenessView.setCallback(new FaceLivenessView.Callback() {

            @Override
            public void onStateChanged(FaceLivenessState state, @Nullable String stepParameters) {
                Timber.i("onStateChanged - state: %s, stepParameters: %s", state, stepParameters);
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
        });
    }

    private void postSelfieSample(FaceLivenessResult result) {
        MyApplication.apiService.postSelfieSample(result.getFilePath(), result.getSignature()).enqueue(new retrofit2.Callback<SampleJson>() {

            @Override
            public void onResponse(Call<SampleJson> call, Response<SampleJson> response) {
                LogUtils.logInfo(getApplicationContext(), "...picture has been uploaded!");

            }

            @Override
            public void onFailure(Call<SampleJson> call, Throwable t) {
                Timber.e(t);
            }
        });
    }

    private void postSelfieVideoSample(FaceLivenessResult result) {
        MyApplication.apiService.postSelfieVideoSample(result.getVideoFilePath(), result.getSignature()).enqueue(new retrofit2.Callback<SampleJson>() {

            @Override
            public void onResponse(Call<SampleJson> call, Response<SampleJson> response) {
                LogUtils.logInfo(getApplicationContext(), "...video has been uploaded!");
                finish();
            }

            @Override
            public void onFailure(Call<SampleJson> call, Throwable t) {
                Timber.e(t);
            }
        });
    }
}
