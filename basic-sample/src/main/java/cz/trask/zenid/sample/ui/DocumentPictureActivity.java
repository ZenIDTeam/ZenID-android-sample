package cz.trask.zenid.sample.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.DocumentCountry;
import cz.trask.zenid.sdk.DocumentPage;
import cz.trask.zenid.sdk.DocumentPictureState;
import cz.trask.zenid.sdk.DocumentPictureView;
import cz.trask.zenid.sdk.DocumentResult;
import cz.trask.zenid.sdk.DocumentRole;
import cz.trask.zenid.sdk.api.DocumentPictureResponseValidator;
import cz.trask.zenid.sdk.api.model.SampleJson;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class DocumentPictureActivity extends AppCompatActivity {

    private DocumentPictureView documentPictureView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_picture);

        textView = findViewById(R.id.textView);

        documentPictureView = findViewById(R.id.documentPictureView);
        documentPictureView.setLifecycleOwner(this);
        documentPictureView.setDocumentType(DocumentRole.DRIVING_LICENSE, DocumentPage.FRONT_SIDE, DocumentCountry.CZ);
        // documentPictureView.enableDefaultVizualization(Language.ENGLISH); // enable/disable
        documentPictureView.setCallback(new DocumentPictureView.Callback() {

            @Override
            public void onStateChanged(DocumentPictureState state) {
                Timber.i("onStateChanged %s", state);
                switch (state) {
                    case BLURRY:
                        textView.setText("BLURRY");
                        break;
                    case HOLD_STEADY:
                        textView.setText("HOLD_STEADY");
                        break;
                    case ALIGN_CARD:
                        textView.setText("ALIGN_CARD");
                        break;
                    case OK:
                        textView.setText("OK");
                        break;
                }
            }

            @Override
            public void onPictureTaken(DocumentResult result) {
                postDocumentPictureSample(result);
            }
        });
    }

    private void postDocumentPictureSample(DocumentResult result) {
        MyApplication.apiService.postDocumentPictureSample(result).enqueue(new retrofit2.Callback<SampleJson>() {

            @Override
            public void onResponse(Call<SampleJson> call, Response<SampleJson> response) {
                DocumentPictureResponseValidator.State state = DocumentPictureResponseValidator.validate(result.getRole(), result.getPage(), response);
                if (state == DocumentPictureResponseValidator.State.CORRECT) {
                    Timber.i("Successful - sampleId: %s", response.body().getSampleId());
                } else {
                    Timber.i("Error");
                }
            }

            @Override
            public void onFailure(Call<SampleJson> call, Throwable t) {
                Timber.e(t);
            }
        });
    }

}
