package cz.trask.zenid.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import cz.trask.zenid.sample.LogUtils;
import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.DocumentPictureResult;
import cz.trask.zenid.sdk.DocumentPictureSettings;
import cz.trask.zenid.sdk.DocumentPictureState;
import cz.trask.zenid.sdk.DocumentPictureView;
import cz.trask.zenid.sdk.Language;
import cz.trask.zenid.sdk.NfcStatus;
import cz.trask.zenid.sdk.VisualizationSettings;
import cz.trask.zenid.sdk.ZenIdException;
import cz.trask.zenid.sdk.api.model.SampleJson;
import cz.trask.zenid.sdk.camera.size.SizeSelectors;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/*
 * This class shows how to create a custom overlay
 */
public class DocumentPictureActivity extends AppCompatActivity {

    private DocumentPictureView documentPictureView;
    private ImageView imageView;
    private boolean activateCameraButton = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_picture);

        documentPictureView = findViewById(R.id.documentPictureView);
        imageView = findViewById(R.id.imageView_camera);

        imageView.setOnClickListener(view -> {
            try {
                documentPictureView.activateTakeNextDocumentPicture();
            } catch (Exception e) {
                Timber.e(e);
                finish();
            }
        });

//        DocumentAcceptableInput.Filter filter1 = new DocumentAcceptableInput.Filter(DocumentRole.ID, null, DocumentCountry.CZ);
//        DocumentAcceptableInput.Filter filter2 = new DocumentAcceptableInput.Filter(DocumentRole.DRIVING_LICENSE, DocumentPage.FRONT_SIDE, DocumentCountry.SK);
//        DocumentAcceptableInput documentAcceptableInput = new DocumentAcceptableInput(Arrays.asList(filter1, filter2));

        VisualizationSettings visualizationSettings = new VisualizationSettings.Builder()
                .showDebugVisualization(false)
                .language(Language.ENGLISH)
                .showTextInformation(true)
                .build();

        DocumentPictureSettings documentPictureSettings = new DocumentPictureSettings.Builder()
                .enableAimingCircle(true)
                .build();

        try {
            documentPictureView.setLoggerCallback((module, method, message) -> Timber.tag(module).d("%s - %s", method, message));
            documentPictureView.setLifecycleOwner(this);
//            documentPictureView.setDocumentAcceptableInput(documentAcceptableInput);
            documentPictureView.setPreviewStreamSize(SizeSelectors.biggest());
            documentPictureView.setDocumentPictureSettings(documentPictureSettings);
            documentPictureView.enableDefaultVisualization(visualizationSettings); // enable/disable
        } catch (Exception e) {
            Timber.e(e);
            finish();
            return;
        }

        documentPictureView.setCallback(new DocumentPictureView.Callback() {

            @Override
            public void onStateChanged(DocumentPictureState state) {
//                Timber.i("onStateChanged %s", state);
                if (state.isMatchFound() && activateCameraButton) {
                    activateCameraButton = false;
                    imageView.postDelayed(() -> imageView.setVisibility(View.VISIBLE), 5000);
                }
            }

            @Override
            public void onPictureTaken(DocumentPictureResult result, NfcStatus nfcStatus) {
                LogUtils.logInfo(getApplicationContext(), "onPictureTaken... " + result.getFilePath());
                if (NfcStatus.NFC_REQUIRED.equals(nfcStatus)) {
                    startActivity(new Intent(getApplicationContext(), NfcActivity.class));
                } else {
                    postDocumentPictureSample(result);
                }
                finish();
            }

            @Override
            public void onError(ZenIdException e) {

            }
        });

        activateCameraButton = true;
    }

    private void postDocumentPictureSample(DocumentPictureResult result) {
        MyApplication.apiService.postDocumentPictureSample(result).enqueue(new retrofit2.Callback<SampleJson>() {

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

}
