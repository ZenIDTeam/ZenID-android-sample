package cz.trask.zenid.sample.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.DocumentCountry;
import cz.trask.zenid.sdk.ZenId;
import cz.trask.zenid.sdk.api.ApiService;
import cz.trask.zenid.sdk.api.model.InvestigationResponseJson;
import cz.trask.zenid.sdk.api.model.InvestigationValidatorResponseJson;
import cz.trask.zenid.sdk.api.model.MinedDataJson;
import cz.trask.zenid.sdk.api.model.MinedTextJson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ResultsActivity extends BaseActivity {

    private static final String BUNDLE_SAMPLE_IDS = "sample_ids";

    private static final int VALIDATOR_INSOLVENCY_CODE = 47;
    private static final int VALIDATOR_SELFIE_CODE = 6;
    private static final int VALIDATOR_ID_CARD_RECALLED_CODE = 19;

    @Inject
    ApiService apiService;

    @BindView(R.id.results_textInputLayout_firstName)
    TextInputLayout tilFirstName;
    @BindView(R.id.results_textInputLayout_lastName)
    TextInputLayout tilLastName;
    @BindView(R.id.results_textInputLayout_birthDate)
    TextInputLayout tilBirthDate;
    @BindView(R.id.results_textInputLayout_birthNumber)
    TextInputLayout tilBirthNumber;
    @BindView(R.id.results_textInputLayout_documentNumber)
    TextInputLayout tilDocumentNumber;
    @BindView(R.id.results_textInputLayout_issueDate)
    TextInputLayout tilIssueDate;
    @BindView(R.id.results_textInputLayout_expiryDate)
    TextInputLayout tilExpiryDate;
    @BindView(R.id.results_textInputLayout_address)
    TextInputLayout tilAddress;
    @BindView(R.id.results_textView_checkMv)
    TextView tvCheckMv;
    @BindView(R.id.results_textView_checkIr)
    TextView tvCheckIr;
    @BindView(R.id.results_textView_checkPhoto)
    TextView tvCheckPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMyApplication().getMyApplicationComponent().inject(this);
        getInvestigateSamples(getIntent().getStringArrayListExtra(BUNDLE_SAMPLE_IDS));
    }

    @Override
    protected int layoutRes() {
        return R.layout.activity_results;
    }

    @OnClick(R.id.results_button_done)
    void onDoneClick() {
        startActivity(MainActivity.newIntent(this));
    }

    private void getInvestigateSamples(ArrayList<String> sampleIds) {
        apiService.getInvestigateSamples(sampleIds).enqueue(new Callback<InvestigationResponseJson>() {

            @Override
            public void onResponse(Call<InvestigationResponseJson> call, Response<InvestigationResponseJson> response) {
                Timber.i("investigationId: %s", response.body().getInvestigationId() );
                InvestigationResponseJson investigationResponse = response.body();
                showMinedData(investigationResponse);
                showValidatorResults(investigationResponse);
            }

            @Override
            public void onFailure(Call<InvestigationResponseJson> call, Throwable t) {
                Timber.e(t);
            }
        });
    }

    private void showMinedData(@NonNull InvestigationResponseJson investigationResponse) {
        MinedDataJson minedData = investigationResponse.getMinedData();
        showMinedValue(tilFirstName, minedData.getFirstName());
        showMinedValue(tilLastName, minedData.getLastName());
        showMinedValue(tilBirthDate, minedData.getBirthDate());
        showMinedValue(tilBirthNumber, minedData.getBirthNumber());
        showMinedValue(tilDocumentNumber, minedData.getDocumentNumber());
        showMinedValue(tilIssueDate, minedData.getIssueDate());
        showMinedValue(tilExpiryDate, minedData.getExpiryDate());
        showMinedValue(tilAddress, minedData.getAddress());
    }

    public void showMinedValue(@NonNull TextInputLayout textInputLayout, @Nullable MinedTextJson minedValue) {
        if (minedValue != null && !TextUtils.isEmpty(minedValue.getText())) {
            EditText editText = textInputLayout.getEditText();
            editText.setText(minedValue.getText());
        }
    }

    private void showValidatorResults(@NonNull InvestigationResponseJson investigationResponseJson) {
        for (InvestigationValidatorResponseJson validator : investigationResponseJson.getValidatorResults()) {
            if (validator.getCode() == VALIDATOR_INSOLVENCY_CODE) {
                showValidatorResult(tvCheckIr, validator.getOk());
            }

            if (validator.getCode() == VALIDATOR_SELFIE_CODE) {
                showValidatorResult(tvCheckPhoto, validator.getOk());
            }

            if (validator.getCode() == VALIDATOR_ID_CARD_RECALLED_CODE) {
                showValidatorResult(tvCheckMv, validator.getOk());
            }
        }
    }

    private void showValidatorResult(TextView textView, boolean ok) {
        textView.setVisibility(View.VISIBLE);
        if (ok) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check_box_gray_24dp, 0, 0, 0);
        } else {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check_box_indeterminate_gray_24dp, 0, 0, 0);
        }
    }

    public static Intent newIntent(@NonNull Context context, @NonNull ArrayList<String> sampleIds) {
        Intent intent = new Intent(context, ResultsActivity.class);
        intent.putStringArrayListExtra(BUNDLE_SAMPLE_IDS, sampleIds);
        return intent;
    }
}
