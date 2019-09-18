package cz.trask.zenid.sample;

import android.os.Bundle;
import android.view.View;

import javax.inject.Inject;

import cz.trask.zenid.sdk.DocumentCountry;
import cz.trask.zenid.sdk.DocumentPage;
import cz.trask.zenid.sdk.DocumentRole;
import cz.trask.zenid.sdk.ZenId;
import cz.trask.zenid.sdk.api.ApiService;
import cz.trask.zenid.sdk.api.model.SampleJson;
import dagger.android.support.DaggerAppCompatActivity;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class MyActivity extends DaggerAppCompatActivity {

    @Inject
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_document_verifier).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ZenId.get().startDocumentPictureVerifier(MyActivity.this, DocumentRole.ID, DocumentPage.FRONT_SIDE, DocumentCountry.CZ);
            }
        });

        findViewById(R.id.button_liveness_check).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ZenId.get().startFaceLivenessDetector(MyActivity.this);
            }
        });

        ZenId.get().setCallback(new ZenId.Callback() {

            @Override
            public void onDocumentPictureTaken(DocumentCountry documentCountry, DocumentRole documentRole, Integer documentCode, DocumentPage documentPage, String documentPicturePath) {
                apiService.postDocumentPictureSample(documentCountry, documentRole, documentCode, documentPage, documentPicturePath).enqueue(new retrofit2.Callback<SampleJson>() {

                    @Override
                    public void onResponse(Call<SampleJson> call, Response<SampleJson> response) {
                        String sampleId = response.body().getSampleId();
                        Timber.i("sampleId: %s", sampleId);
                    }

                    @Override
                    public void onFailure(Call<SampleJson> call, Throwable t) {
                        Timber.e(t);
                    }
                });
            }

            @Override
            public void onSelfiePictureTaken(String selfiePicturePath) {
                apiService.postSelfieSample(selfiePicturePath).enqueue(new retrofit2.Callback<SampleJson>() {

                    @Override
                    public void onResponse(Call<SampleJson> call, Response<SampleJson> response) {
                        String sampleId = response.body().getSampleId();
                        Timber.i("sampleId: %s", sampleId);
                    }

                    @Override
                    public void onFailure(Call<SampleJson> call, Throwable t) {
                        Timber.e(t);
                    }
                });
            }

            @Override
            public void onUserLeft() {
                Timber.i("User left the sdk flow without completing it");
            }

        });
    }
}
