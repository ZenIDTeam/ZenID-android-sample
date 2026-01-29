package cz.trask.zenid.sample.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

import cz.trask.zenid.sample.LanguageUtils;
import cz.trask.zenid.sample.LogUtils;
import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.VisualizationSettings;
import cz.trask.zenid.sdk.ZenId;
import cz.trask.zenid.sdk.ZenIdException;
import cz.trask.zenid.sdk.api.model.SampleJson;
import cz.trask.zenid.sdk.enums.CommonVerifierFeedback;
import cz.trask.zenid.sdk.enums.Country;
import cz.trask.zenid.sdk.enums.DocumentRole;
import cz.trask.zenid.sdk.enums.SupportedLanguages;
import cz.trask.zenid.sdk.internal.verifier.DocumentVerifier;
import cz.trask.zenid.sdk.models.DocumentFilter;
import cz.trask.zenid.sdk.models.DocumentVerifierSettings;
import cz.trask.zenid.sdk.models.DocumentVerifierStateContainerForPublicData;
import cz.trask.zenid.sdk.models.UploadReadyData;
import cz.trask.zenid.sdk.view.DocumentView;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/*
 * This class shows how to create a custom overlay
 */
public class DocumentPictureActivity extends AppCompatActivity {

    /*-------------------------*/
    /*   OVERRIDDEN METHODS    */
    /*-------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_picture);

        DocumentView documentView = findViewById(R.id.documentPictureView);

        try {
            documentView.setLifecycleOwner(this);
            documentView.setVerifierSettings(createDocumentVerifierSettings());
            documentView.enableDefaultVisualization(createVisualizationSettings());
        } catch (Exception e) {
            Timber.e(e);
            finish();
            return;
        }

        documentView.setCallback(new DocumentView.DocumentViewCallback() {

            @Override
            public void onStateChanged(DocumentVerifierStateContainerForPublicData stateContainer) {
                CommonVerifierFeedback feedback = stateContainer.getFeedback();
//                Timber.i("onStateChanged %s", feedback.name());
            }

            @Override
            public void onScanNfc() {
                startActivity(new Intent(getApplicationContext(), NfcActivity.class));
            }

            @Override
            public void onResult(UploadReadyData uploadReadyData) {
                ZenId.get().getVerifier(DocumentVerifier.class).unload();
                postSample(getApplicationContext(), uploadReadyData);
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(ZenIdException exception) {
                LogUtils.logInfo(DocumentPictureActivity.this, exception.getMessage());
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
        documentVerifierSettings.setShowTimer(false);
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
                .language(LanguageUtils.getLanguage())
                .showTextInformation(true)
                .build();
    }
}
