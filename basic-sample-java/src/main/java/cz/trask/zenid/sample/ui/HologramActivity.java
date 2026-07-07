package cz.trask.zenid.sample.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Collections;
import cz.trask.zenid.sample.LanguageUtils;
import cz.trask.zenid.sample.R;
import cz.trask.zenid.sdk.VisualizationSettings;
import cz.trask.zenid.sdk.ZenIdException;
import cz.trask.zenid.sdk.enums.Country;
import cz.trask.zenid.sdk.enums.DocumentRole;
import cz.trask.zenid.sdk.enums.PageCodes;
import cz.trask.zenid.sdk.models.AcceptableInput;
import cz.trask.zenid.sdk.models.DocumentFilter;
import cz.trask.zenid.sdk.models.DocumentVerifierSettings;
import cz.trask.zenid.sdk.models.DocumentVerifierStateContainerForPublicData;
import cz.trask.zenid.sdk.models.UploadReadyData;
import cz.trask.zenid.sdk.view.HologramView;
import timber.log.Timber;

public class HologramActivity extends AppCompatActivity {

    public static final String EXTRA_COUNTRY = "country";
    public static final String EXTRA_ROLE = "role";
    public static final String EXTRA_PAGE = "page";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hologram);

        String countryName = getIntent().getStringExtra(EXTRA_COUNTRY);
        String roleName = getIntent().getStringExtra(EXTRA_ROLE);
        String pageName = getIntent().getStringExtra(EXTRA_PAGE);

        HologramView hologramView = findViewById(R.id.hologramView);

        DocumentVerifierSettings settings = new DocumentVerifierSettings();
        settings.setEnableAimingCircle(true);
        settings.setDrawOutline(true);
        AcceptableInput input = buildAcceptableInput(countryName, roleName, pageName);
        if (input != null) settings.setAcceptableInput(input);

        try {
            hologramView.setLifecycleOwner(this);
            hologramView.setVerifierSettings(settings);
            hologramView.enableDefaultVisualization(new VisualizationSettings.Builder()
                    .showDebugVisualization(false)
                    .language(LanguageUtils.getLanguage())
                    .showTextInformation(true)
                    .build());
        } catch (Exception e) {
            Timber.e(e);
            finish();
            return;
        }

        hologramView.setCallback(new HologramView.HologramViewCallback() {
            @Override
            public void onStateChanged(DocumentVerifierStateContainerForPublicData stateContainer) {}

            @Override
            public void onResult(UploadReadyData uploadReadyData) {
                startActivity(ResultActivity.newIntent(HologramActivity.this, uploadReadyData.getSignedSamplePackage()));
                finish();
            }

            @Override
            public void onError(ZenIdException exception) {
                Timber.e(exception);
            }
        });
    }

    private AcceptableInput buildAcceptableInput(String countryName, String roleName, String pageName) {
        Country country = parseCountry(countryName);
        DocumentRole role = roleName != null ? parseRole(roleName) : null;
        PageCodes page = pageName != null ? parsePage(pageName) : null;
        if (country != null) {
            return new AcceptableInput(Collections.singletonList(new DocumentFilter(role, country, page, null, null)));
        } else if (PickerActivity.ANY_COUNTRY.equals(countryName) && role != null) {
            return new AcceptableInput(Collections.singletonList(new DocumentFilter(role, null, page, null, null)));
        } else if (PickerActivity.ANY_COUNTRY.equals(countryName)) {
            return new AcceptableInput(Collections.singletonList(new DocumentFilter(null, null, page, null, null)));
        }
        return null;
    }

    private Country parseCountry(String name) {
        if (name == null) return null;
        try { return Country.valueOf(name); } catch (Exception e) { return null; }
    }

    private DocumentRole parseRole(String name) {
        try { return DocumentRole.valueOf(name); } catch (Exception e) { return null; }
    }

    private PageCodes parsePage(String name) {
        try { return PageCodes.valueOf(name); } catch (Exception e) { return null; }
    }
}
