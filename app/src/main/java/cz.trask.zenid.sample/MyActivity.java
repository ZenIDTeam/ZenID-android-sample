package cz.trask.zenid.sample;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import cz.trask.zenid.sdk.DocumentCountry;
import cz.trask.zenid.sdk.DocumentPage;
import cz.trask.zenid.sdk.DocumentRole;
import cz.trask.zenid.sdk.MatcherResponseValidator;
import cz.trask.zenid.sdk.ZenId;
import timber.log.Timber;

public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_document_verifier).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ZenId.get().startMatcherActivity(MyActivity.this, DocumentRole.ID, DocumentPage.FRONT_SIDE, DocumentCountry.CZ);
            }
        });

        findViewById(R.id.button_liveness_check).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ZenId.get().startFaceLivenessDetectorActivity(MyActivity.this);
            }
        });

        ZenId.get().setCallback(new ZenId.Callback() {

            @Override
            public void onMatcherPictureTaken(DocumentRole documentRole, DocumentPage documentPage, String picturePath) {
                Timber.i("Match - documentRole: %s, documentPage: %s, picturePath: %s", documentRole, documentPage, picturePath);
            }

            @Override
            public void onMatcherResult(DocumentRole documentRole, DocumentPage documentPage, String sampleId) {
                Timber.i("Match & Succesful response - documentRole: %s, documentPage: %s, sampleId: %s", documentRole, documentPage, sampleId);
            }

            @Override
            public void onMatcherError(DocumentRole documentRole, DocumentPage documentPage, MatcherResponseValidator.State state) {
                Timber.i("Match & Error response - documentRole: %s, documentPage: %s", documentRole, documentPage);
            }

            @Override
            public void onFaceLivenessDetectorPictureTaken(String picturePath) {
                Timber.i("Selfie - picturePath: %s", picturePath);
            }

            @Override
            public void onFaceLivenessDetectorResult(String picturePath, String sampleId) {
                Timber.i("Selfie detected - picturePath: %s, sampleId: %s", picturePath, sampleId);
            }

            @Override
            public void userExited() {
                Timber.i("User left the sdk flow without completing it");
            }
        });
    }
}
