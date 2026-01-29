package cz.trask.zenid.sample.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import cz.trask.zenid.sample.LanguageUtils;
import cz.trask.zenid.sample.LogUtils;
import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.VisualizationSettings;
import cz.trask.zenid.sdk.ZenIdException;
import cz.trask.zenid.sdk.api.model.SampleJson;
import cz.trask.zenid.sdk.enums.CommonVerifierFeedback;
import cz.trask.zenid.sdk.enums.SupportedLanguages;
import cz.trask.zenid.sdk.models.FaceLivenessStateContainerForPublicData;
import cz.trask.zenid.sdk.models.FaceLivenessVerifierSettings;
import cz.trask.zenid.sdk.models.UploadReadyData;
import cz.trask.zenid.sdk.view.FaceLivenessView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class FaceLivenessActivity extends AppCompatActivity {

    /*-------------------------*/
    /*   OVERRIDDEN METHODS    */
    /*-------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_liveness);

        FaceLivenessView faceLivenessView = findViewById(R.id.faceLivenessView);

        try {
            faceLivenessView.setLifecycleOwner(this);
            faceLivenessView.setVerifierSettings(createFaceLivenessSettings());
            faceLivenessView.enableDefaultVisualization(createVisualizationSettings());
        } catch (Exception e) {
            Timber.e(e);
            finish();
            return;
        }

        faceLivenessView.setCallback(new FaceLivenessView.FaceLivenessViewCallback() {

            @Override
            public void onStateChanged(FaceLivenessStateContainerForPublicData stateContainer) {
                CommonVerifierFeedback feedback = stateContainer.getFeedback();
//                Timber.i("onStateChanged: %s", feedback.name());
            }

            @Override
            public void onResult(UploadReadyData uploadReadyData) {
                postSample(getApplicationContext(), uploadReadyData);
                setResult(Activity.RESULT_OK);
                finish();
            }

            @Override
            public void onError(ZenIdException exception) {
                LogUtils.logInfo(FaceLivenessActivity.this, exception.getMessage());
            }
        });
    }

    /*-------------------------*/
    /*     PRIVATE METHODS     */
    /*-------------------------*/

    private void postSample(Context context, UploadReadyData uploadReadyData) {
        MyApplication.apiService.postSample(uploadReadyData.getSignedSamplePackage()).enqueue(new Callback<SampleJson>() {

            @Override
            public void onResponse(@NonNull Call<SampleJson> call, @NonNull Response<SampleJson> response) {
                SampleJson sample = response.body();
                if (sample == null) {
                    Timber.e("Response body is empty");
                    return;
                }

                if (sample.getState().equals("NotDone") || sample.getState().equals("Error")) {
                    LogUtils.logInfo(context, "Upload failed... " + sample.getErrorCode());
                    return;
                }

                String msg = String.format("FaceLiveness result sent; SampleId: %s", sample.getSampleId());
                LogUtils.logInfo(getApplicationContext(), msg);
            }

            @Override
            public void onFailure(@NonNull Call<SampleJson> call, @NonNull Throwable t) {
                Timber.e("Response body is empty");
            }
        });
    }

    private FaceLivenessVerifierSettings createFaceLivenessSettings() {
        FaceLivenessVerifierSettings faceLivenessVerifierSettings = new FaceLivenessVerifierSettings();
        faceLivenessVerifierSettings.setEnableLegacyMode(false);
        faceLivenessVerifierSettings.setShowSmileAnimation(true);
        return faceLivenessVerifierSettings;
    }

    private VisualizationSettings createVisualizationSettings() {
        return new VisualizationSettings.Builder()
                .showDebugVisualization(false)
                .language(LanguageUtils.getLanguage())
                .build();
    }
}
