package cz.trask.zenid.sample.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.OnClick;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.DocumentCountry;
import cz.trask.zenid.sdk.DocumentPage;
import cz.trask.zenid.sdk.DocumentRole;
import cz.trask.zenid.sdk.ZenId;
import cz.trask.zenid.sdk.api.ApiService;
import cz.trask.zenid.sdk.api.model.SampleJson;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class MainActivity extends BaseActivity {

    @Inject
    ApiService apiService;

    private ArrayList<String> sampleIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerZenIdCallback();
    }

    @Override
    protected int layoutRes() {
        return R.layout.activity_main;
    }

    @OnClick(R.id.button_start_flow)
    void onStartFlowClick() {
        sampleIds = new ArrayList<>();
        ZenId.get().startDrivingLicenseVerifier(MainActivity.this, DocumentCountry.CZ);
    }

    private void registerZenIdCallback() {
        ZenId.get().setCallback(new ZenId.Callback() {

            @Override
            public void onDocumentPictureTaken(DocumentCountry documentCountry, DocumentRole documentRole, Integer documentCode, DocumentPage documentPage, String documentPicturePath) {
                postDocumentPictureSample(documentCountry, documentRole, documentCode, documentPage, documentPicturePath);
            }

            @Override
            public void onSelfiePictureTaken(String selfiePicturePath) {
                postSelfieSample(selfiePicturePath);
            }

            @Override
            public void onUserLeft() {
                Timber.i("User left the sdk flow without completing it");
            }
        });
    }

    private void postDocumentPictureSample(DocumentCountry documentCountry, DocumentRole documentRole, Integer documentCode, DocumentPage documentPage, String documentPicturePath) {
        apiService.postDocumentPictureSample(documentCountry, documentRole, documentCode, documentPage, documentPicturePath).enqueue(new retrofit2.Callback<SampleJson>() {

            @Override
            public void onResponse(Call<SampleJson> call, Response<SampleJson> response) {
                String sampleId = response.body().getSampleId();
                Timber.i("sampleId: %s", sampleId);
                sampleIds.add(sampleId);
                ZenId.get().startFaceLivenessDetector(MainActivity.this);
            }

            @Override
            public void onFailure(Call<SampleJson> call, Throwable t) {
                Timber.e(t);
            }
        });
    }

    private void postSelfieSample(String selfiePicturePath) {
        apiService.postSelfieSample(selfiePicturePath).enqueue(new retrofit2.Callback<SampleJson>() {

            @Override
            public void onResponse(Call<SampleJson> call, Response<SampleJson> response) {
                String sampleId = response.body().getSampleId();
                Timber.i("sampleId: %s", sampleId);
                sampleIds.add(sampleId);
                startActivity(ResultsActivity.newIntent(MainActivity.this, sampleIds));
            }

            @Override
            public void onFailure(Call<SampleJson> call, Throwable t) {
                Timber.e(t);
            }
        });
    }

    public static Intent newIntent(@NonNull Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }
}
