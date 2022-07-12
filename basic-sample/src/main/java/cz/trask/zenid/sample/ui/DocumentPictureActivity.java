package cz.trask.zenid.sample.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import cz.trask.zenid.sample.LogUtils;
import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.DocumentAcceptableInput;
import cz.trask.zenid.sdk.DocumentCountry;
import cz.trask.zenid.sdk.DocumentPage;
import cz.trask.zenid.sdk.DocumentPictureResult;
import cz.trask.zenid.sdk.DocumentPictureSettings;
import cz.trask.zenid.sdk.DocumentPictureState;
import cz.trask.zenid.sdk.DocumentPictureView;
import cz.trask.zenid.sdk.DocumentRole;
import cz.trask.zenid.sdk.HologramSettings;
import cz.trask.zenid.sdk.Language;
import cz.trask.zenid.sdk.VisualizationSettings;
import cz.trask.zenid.sdk.api.model.SampleJson;
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

        imageView = findViewById(R.id.imageView_camera);
        imageView.setOnClickListener(view -> documentPictureView.activateTakeNextDocumentPicture());

        DocumentAcceptableInput.Filter filter1 = new DocumentAcceptableInput.Filter(DocumentRole.ID, null, DocumentCountry.CZ);
        DocumentAcceptableInput.Filter filter2 = new DocumentAcceptableInput.Filter(DocumentRole.DRIVING_LICENSE, DocumentPage.FRONT_SIDE, DocumentCountry.SK);
        DocumentAcceptableInput documentAcceptableInput = new DocumentAcceptableInput(Arrays.asList(filter1, filter2));

        VisualizationSettings visualizationSettings = new VisualizationSettings.Builder()
                .showDebugVisualization(false)
                .language(Language.ENGLISH)
                .build();

        DocumentPictureSettings documentPictureSettings = new DocumentPictureSettings.Builder()
                .enableAimingCircle(true)
                .build();

        documentPictureView = findViewById(R.id.documentPictureView);
        documentPictureView.setLifecycleOwner(this);
        documentPictureView.setDocumentAcceptableInput(documentAcceptableInput);
        documentPictureView.setDocumentPictureSettings(documentPictureSettings);
        documentPictureView.enableDefaultVisualization(visualizationSettings); // enable/disable
        documentPictureView.setCallback(new DocumentPictureView.Callback() {

            @Override
            public void onStateChanged(DocumentPictureState state) {
                Timber.i("onStateChanged %s", state);

                if (state.isMatchFound() && activateCameraButton) {
                    activateCameraButton = false;
                    imageView.postDelayed(() -> imageView.setVisibility(View.VISIBLE), 5000);
                }
            }

            @Override
            public void onPictureTaken(DocumentPictureResult result) {
                Timber.i("onPictureTaken... " + result.getFilePath());
                postDocumentPictureSample(result);
                finish();
            }
        });

        activateCameraButton = true;
    }

    private void postDocumentPictureSample(DocumentPictureResult result) {
        MyApplication.apiService.postDocumentPictureSample(result).enqueue(new retrofit2.Callback<SampleJson>() {

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

}
