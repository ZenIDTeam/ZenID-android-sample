package cz.trask.zenid.sample.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import cz.trask.zenid.sample.LogUtils;
import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.VisualizationSettings;
import cz.trask.zenid.sdk.ZenIdException;
import cz.trask.zenid.sdk.api.model.SampleJson;
import cz.trask.zenid.sdk.enums.CommonVerifierFeedback;
import cz.trask.zenid.sdk.enums.SupportedLanguages;
import cz.trask.zenid.sdk.models.SelfieStateContainerForPublicData;
import cz.trask.zenid.sdk.models.SelfieVerifierSettings;
import cz.trask.zenid.sdk.models.UploadReadyData;
import cz.trask.zenid.sdk.view.SelfieView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/*
 * This class shows how to handle camera permission
 */
public class SelfieActivity extends AppCompatActivity {

    /*-------------------------*/
    /*   OVERRIDDEN METHODS    */
    /*-------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie);

        SelfieView selfieView = findViewById(R.id.selfieView);

        try {
            selfieView.setLifecycleOwner(this);
            selfieView.setVerifierSettings(createSelfieVerifierSettings());
            selfieView.enableDefaultVisualization(createVisualizationSettings());
        } catch (Exception e) {
            Timber.e(e);
            finish();
            return;
        }

        selfieView.setCallback(new SelfieView.SelfieViewCallback() {

            @Override
            public void onStateChanged(SelfieStateContainerForPublicData stateContainer) {
                CommonVerifierFeedback feedback = stateContainer.getFeedback();
                Timber.i("onStateChanged: %s", feedback.name());
            }

            @Override
            public void onResult(UploadReadyData uploadReadyData) {
                postSample(getApplicationContext(), uploadReadyData);
                setResult(Activity.RESULT_OK);
                finish();
            }

            @Override
            public void onError(ZenIdException exception) {
                LogUtils.logInfo(SelfieActivity.this, exception.getMessage());
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

                String msg = String.format("Selfie result sent; SampleId: %s", sample.getSampleId());
                LogUtils.logInfo(getApplicationContext(), msg);
            }

            @Override
            public void onFailure(@NonNull Call<SampleJson> call, @NonNull Throwable t) {
                Timber.e(t);
            }
        });
    }

    private SelfieVerifierSettings createSelfieVerifierSettings() {
        return new SelfieVerifierSettings();
    }

    private VisualizationSettings createVisualizationSettings() {
        return new VisualizationSettings.Builder()
                .showDebugVisualization(false)
                .language(SupportedLanguages.English)
                .build();
    }
}
