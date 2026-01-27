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
import cz.trask.zenid.sdk.models.DocumentVerifierSettings;
import cz.trask.zenid.sdk.models.DocumentVerifierStateContainerForPublicData;
import cz.trask.zenid.sdk.models.UploadReadyData;
import cz.trask.zenid.sdk.view.HologramView;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class HologramActivity extends AppCompatActivity {

    /*-------------------------*/
    /*   OVERRIDDEN METHODS    */
    /*-------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hologram);

        HologramView hologramView = findViewById(R.id.hologramView);

        try {
            hologramView.setLifecycleOwner(this);
            hologramView.setVerifierSettings(createDocumentVerifierSettings());
            hologramView.enableDefaultVisualization(createVisualizationSettings());
        } catch (Exception e) {
            Timber.e(e);
            finish();
            return;
        }

        hologramView.setCallback(new HologramView.HologramViewCallback() {
            @Override
            public void onStateChanged(DocumentVerifierStateContainerForPublicData stateContainer) {
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
                LogUtils.logInfo(HologramActivity.this, exception.getMessage());
            }
        });
    }

    /*-------------------------*/
    /*     PRIVATE METHODS     */
    /*-------------------------*/

    private void postSample(Context context, UploadReadyData uploadReadyData) {
        MyApplication.apiService.postSample(uploadReadyData.getSignedSamplePackage()).enqueue(new retrofit2.Callback<SampleJson>() {

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

                String msg = String.format("Document result sent; SampleId: %s", sample.getSampleId());
                LogUtils.logInfo(context, msg);
            }

            @Override
            public void onFailure(@NonNull Call<SampleJson> call, @NonNull Throwable t) {
                Timber.e(t);
            }
        });
    }

    private DocumentVerifierSettings createDocumentVerifierSettings() {
        DocumentVerifierSettings documentVerifierSettings = new DocumentVerifierSettings();

        documentVerifierSettings.setEnableAimingCircle(true);
        documentVerifierSettings.setDrawOutline(true);

//        List<DocumentFilter> dbFilters = new ArrayList<>();
//        dbFilters.add(new DocumentFilter(DocumentRole.Idc, Country.Cz, null, null, null));
//        dbFilters.add(new DocumentFilter(DocumentRole.Drv, Country.Cz, PageCodes.F, null, null));
//        documentVerifierSettings.setAcceptableInput(new AcceptableInput(dbFilters));

        return documentVerifierSettings;
    }

    private VisualizationSettings createVisualizationSettings() {
        return new VisualizationSettings.Builder()
                .showDebugVisualization(false)
                .language(SupportedLanguages.English)
                .showTextInformation(true)
                .build();
    }
}