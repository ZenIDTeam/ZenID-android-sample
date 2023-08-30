package cz.trask.zenid.sample.ui;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gemalto.jp2.JP2Decoder;

import cz.trask.zenid.sample.LogUtils;
import cz.trask.zenid.sample.MyApplication;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.DocumentPictureResult;
import cz.trask.zenid.sdk.NfcData;
import cz.trask.zenid.sdk.NfcKey;
import cz.trask.zenid.sdk.api.model.SampleJson;
import cz.trask.zenid.sdk.nfc.FacePictureFormat;
import cz.trask.zenid.sdk.nfc.NfcAccessDeniedException;
import cz.trask.zenid.sdk.nfc.NfcConnectionException;
import cz.trask.zenid.sdk.nfc.NfcResult;
import cz.trask.zenid.sdk.nfc.NfcService;
import cz.trask.zenid.sdk.nfc.NfcState;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class NfcActivity extends AppCompatActivity {

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

        if (nfcService.isSkipNfcAllowed()) {
            bSkipNfc.setVisibility(View.VISIBLE);
        } else {
            bSkipNfc.setVisibility(View.GONE);
        }

        bSkipNfc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                nfcService.skipNfcVerification();
                DocumentPictureResult result = nfcService.getDocumentPictureResult(NfcActivity.this);
                postDocumentPictureSample(result);
                finish();
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Timber.i("New NFC event");
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        nfcService.handleIntent(getApplicationContext(), intent, new NfcService.Callback() {

            @Override
            public void onZenIdNotNfcState() {
                LogUtils.logInfo(getApplicationContext(), "Not NFC state!!!");
            }

            @Override
            public void onStateChanged(NfcState state) {
                tvNfcState.setText("NFC State: " + state.name());
            }

            @Override
            public void onResult(DocumentPictureResult documentPictureResult, NfcResult nfcResult, NfcData nfcData) {
                String picturePath = nfcResult.getFacePicturePath();
                FacePictureFormat format = nfcResult.getFacePictureFormat();
                Bitmap bitmap;
                if (format.equals(FacePictureFormat.JP2)) {
                    bitmap = new JP2Decoder(picturePath).decode();
                } else {
                    bitmap = BitmapFactory.decodeFile(picturePath);
                }
                ivFacePicture.setImageBitmap(bitmap);
                tvDocumentCode.setText("Document code: " + nfcResult.getDocumentCode());
                tvIssuingState.setText("Issuing state: " + nfcResult.getIssuingState());
                tvNationality.setText("Nationality: " + nfcResult.getNationality());
                tvDocumentNumber.setText("Document number: " + nfcResult.getDocumentNumber());
                tvDateOfBirth.setText("Date of birth: " + nfcResult.getDateOfBirth());
                tvDateOfExpiry.setText("Date of expiry: " + nfcResult.getDateOfExpiry());
                tvPrimaryIdentifiery.setText("Primary identifier: " + nfcResult.getPrimaryIdentifier());
                tvSecondaryIdentifier.setText("Secondary identifier: " + nfcResult.getSecondaryIdentifier());
                tvGender.setText("Gender: " + nfcResult.getGender());

                postDocumentPictureSample(documentPictureResult);
            }

            @Override
            public void onAccessDenied(NfcAccessDeniedException accessDeniedException) {
                NfcKey key = nfcService.getNfcKey();
                String msg = String.format("ERROR... birthDate: %s, documentNumber: %s, expiryDate: %s", key.getBirthDate(), key.getDocumentNumber(), key.getExpiryDate());
                tvNfcState.setText(msg);
            }

            @Override
            public void onConnectionFailed(NfcConnectionException exception) {
                tvNfcState.setText("Connection lost.");
            }

            @Override
            public void onGeneralException(Exception exception) {
                tvNfcState.setText("General exception");
            }
        });
    }

    private void postDocumentPictureSample(DocumentPictureResult result) {
        MyApplication.apiService.postDocumentPictureSample(result).enqueue(new retrofit2.Callback<SampleJson>() {

            @Override
            public void onResponse(Call<SampleJson> call, Response<SampleJson> response) {
                LogUtils.logInfo(getApplicationContext(), "...picture has been uploaded!");
                finish();
            }

            @Override
            public void onFailure(Call<SampleJson> call, Throwable t) {
                Timber.e(t);
            }
        });
    }
}
