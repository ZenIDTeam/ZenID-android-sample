package cz.trask.zenid.sample.ui;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import cz.trask.zenid.sample.LogUtils;
import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.api.model.SampleJson;
import cz.trask.zenid.sdk.models.NfcKey;
import cz.trask.zenid.sdk.models.UploadReadyData;
import cz.trask.zenid.sdk.nfc.FacePictureFormat;
import cz.trask.zenid.sdk.nfc.NfcAccessDeniedException;
import cz.trask.zenid.sdk.nfc.NfcConnectionException;
import cz.trask.zenid.sdk.nfc.NfcData;
import cz.trask.zenid.sdk.nfc.NfcResult;
import cz.trask.zenid.sdk.nfc.NfcService;
import cz.trask.zenid.sdk.nfc.NfcState;
import dev.keiji.jp2k.Callback;
import dev.keiji.jp2k.Config;
import dev.keiji.jp2k.Jp2kDecoderAsync;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kotlin.Unit;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class NfcActivity extends AppCompatActivity {

    /*-------------------------*/
    /*         FIELDS          */
    /*-------------------------*/

    private NfcAdapter nfcAdapter;
    private ImageView ivFacePicture;
    private TextView tvDocumentCode;
    private TextView tvIssuingState;
    private TextView tvNationality;
    private TextView tvDocumentNumber;
    private TextView tvDateOfBirth;
    private TextView tvDateOfExpiry;
    private TextView tvPrimaryIdentifiery;
    private TextView tvSecondaryIdentifier;
    private TextView tvNfcState;
    private TextView tvGender;
    private Button bSkipNfc;
    private NfcService nfcService;

    private final NfcService.Callback nfcCallback = new NfcService.Callback() {

        @Override
        public void onZenIdNotNfcState() {
            LogUtils.logInfo(getApplicationContext(), "Not NFC state!!!");
        }

        @Override
        public void onStateChanged(NfcState state) {
            tvNfcState.setText("NFC State: " + state.name());
        }

        @Override
        public void onResult(UploadReadyData uploadReadyData, NfcResult nfcResult, NfcData nfcData) {
            String picturePath = nfcResult.getFacePicturePath();
            if (picturePath != null) {
                if (nfcResult.getFacePictureFormat() == FacePictureFormat.JP2) {
                    loadImage(picturePath);
                } else {
                    ivFacePicture.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                }
                // Both branches above read the file before returning, so it can go now.
                new File(picturePath).delete();
            }
            tvDocumentCode.setText("Document code: " + nfcResult.getDocumentCode());
            tvIssuingState.setText("Issuing state: " + nfcResult.getIssuingState());
            tvNationality.setText("Nationality: " + nfcResult.getNationality());
            tvDocumentNumber.setText("Document number: " + nfcResult.getDocumentNumber());
            tvDateOfBirth.setText("Date of birth: " + nfcResult.getDateOfBirth());
            tvDateOfExpiry.setText("Date of expiry: " + nfcResult.getDateOfExpiry());
            tvPrimaryIdentifiery.setText("Primary identifier: " + nfcResult.getPrimaryIdentifier());
            tvSecondaryIdentifier.setText("Secondary identifier: " + nfcResult.getSecondaryIdentifier());
            tvGender.setText("Gender: " + nfcResult.getGender());

            postSample(NfcActivity.this, uploadReadyData);
        }

        @Override
        public void onAccessDenied(NfcAccessDeniedException accessDeniedException) {
            NfcKey key = nfcService.getNfcKey();
            tvNfcState.setText(key == null
                    ? "Access denied"
                    : String.format("ERROR... birthDate: %s, documentNumber: %s, expiryDate: %s", key.getBirthDate(), key.getDocumentNumber(), key.getExpiryDate()));
        }

        @Override
        public void onConnectionFailed(NfcConnectionException exception) {
            tvNfcState.setText("Connection lost.");
        }

        @Override
        public void onGeneralException(Exception exception) {
            tvNfcState.setText("General exception: " + exception.getMessage());
        }

        @Override
        public void onVerificationFailed(String message) {
            tvNfcState.setText(message);
        }
    };

    /*-------------------------*/
    /*   OVERRIDDEN METHODS    */
    /*-------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        nfcService = new NfcService();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Timber.i("NFC is not available for device");
            finish();
        } else if (!nfcAdapter.isEnabled()) {
            Timber.i("NFC is available for device but not enabled");
            finish();
        }

        ivFacePicture = findViewById(R.id.nfcFacePicture);
        tvDocumentCode = findViewById(R.id.nfcDocumentCode);
        tvIssuingState = findViewById(R.id.nfcIssuingState);
        tvNationality = findViewById(R.id.nfcNationality);
        tvDocumentNumber = findViewById(R.id.nfcDocumentNumber);
        tvDateOfBirth = findViewById(R.id.nfcDateOfBirth);
        tvDateOfExpiry = findViewById(R.id.nfcDateOfExpiry);
        tvPrimaryIdentifiery = findViewById(R.id.nfcPrimaryIdentifiery);
        tvSecondaryIdentifier = findViewById(R.id.nfcSecondaryIdentifier);
        tvGender = findViewById(R.id.nfcGender);
        tvNfcState = findViewById(R.id.nfcState);
        bSkipNfc = findViewById(R.id.nfcSkip);

        try {
            if (nfcService.isSkipNfcAllowed()) {
                bSkipNfc.setVisibility(View.VISIBLE);
            } else {
                bSkipNfc.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        // Registered up front so that a failed skipNfcVerification() is reported even when no tag
        // was ever tapped and handleIntent() never ran.
        nfcService.setCallback(nfcCallback);

        bSkipNfc.setOnClickListener(v -> {
            // No sample means the core refused the skip; it reports the reason through the callback,
            // which has already put it in tvNfcState by the time this returns - do not overwrite it.
            // Finishing here would send the document without the page that carries the chip data.
            UploadReadyData uploadReadyData = nfcService.skipNfcVerification();
            if (uploadReadyData != null) {
                postSample(this, uploadReadyData);
                finish();
            } else {
                Timber.e("Upload ready data is empty");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(NfcActivity.this, NfcActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(NfcActivity.this, 0, intent, PendingIntent.FLAG_MUTABLE);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        Timber.i("New NFC event");
        handleIntent(intent);
    }

    /*-------------------------*/
    /*     PRIVATE METHODS     */
    /*-------------------------*/

    private void handleIntent(Intent intent) {
        nfcService.handleIntent(intent, nfcCallback);
    }

    private void postSample(Context context, UploadReadyData uploadReadyData) {
        if (MyApplication.apiService == null) {
            Timber.e("No API service configured");
            return;
        }
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

    private void loadImage(String picturePath) {
        byte[] jp2kBytes;
        try {
            jp2kBytes = Files.readAllBytes(new File(picturePath).toPath());
        } catch (IOException e) {
            Timber.e(e);
            Toast.makeText(this, "Error reading image file", Toast.LENGTH_SHORT).show();
            return;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Jp2kDecoderAsync decoder = new Jp2kDecoderAsync(executor, new Config());
        decoder.init(this, new Callback<Unit>() {
            @Override
            public void onSuccess(Unit result) {
                decoder.decodeImage(jp2kBytes, new Callback<Bitmap>() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        runOnUiThread(() -> ivFacePicture.setImageBitmap(bitmap));
                        close();
                    }

                    @Override
                    public void onError(@NonNull Exception e) {
                        showDecodingError(e);
                    }
                });
            }

            @Override
            public void onError(@NonNull Exception e) {
                showDecodingError(e);
            }

            private void showDecodingError(Exception e) {
                Timber.e(e);
                runOnUiThread(() -> Toast.makeText(NfcActivity.this, "Error decoding image", Toast.LENGTH_SHORT).show());
                close();
            }

            private void close() {
                decoder.close();
                executor.shutdown();
            }
        });
    }
}