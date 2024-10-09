# Changelog

### 4.4.6 (2024-10-04) (RecogLibC v4.4.6)
- New: Android SDK targets Android API level 35
- New: Android SDK uses own implementation of CameraView (Camera API v2):
  Remove maven credentials to CameraView maven repository from your project level gradle file

  ```
  maven {
    url 'https://maven.pkg.github.com/ZenIDTeam/CameraView'
    credentials {
      username = "ZenIDTeam"
      password = "YOUR_PERSONAL_ACCESS_TOKEN"
    }
  }
  ```

  Remove dependency: `com.otaliastudios:cameraview:2.7.4` from your app level gradle file
  Add dependencies:
  `com.google.android.gms:play-services-tasks:18.1.0`
  `com.otaliastudios.opengl:egloo:0.6.1`
- New: You can set Frame processing resolution (it will change quality of result images).
  To retrieve available resolutions use function `getFrameProcessingAvailableSizes`
  ```
  CameraView cameraView = new CameraView(context);
  List<Size> sizeList = cameraView.getFrameProcessingAvailableSizes(Facing.BACK);
  ```
  To set Frame processing resolution use view (DocumentPictureView, FacelivenessView, SelfieView, ...) function `documentPictureView.setFrameProcessingSize(size)`
  You can also set your own size and SDK will automatically choose closest from available resolutions to the one you selected.
- New: Android SDK will no longer print logs automatically, instead you can set LoggerCallback to intercept logs from SDK and print logs according to your preferences. For more information check README.md (Section - Logger callback)
- New: Android SDK has improved error handling. You will be forced to implement new callback functions `onError(ZenIdException exception)` in each view callback, e.g.:

  ```
  documentPictureView.setCallback(new DocumentPictureView.Callback() {

    @Override
    public void onStateChanged(DocumentPictureState state) { }

    @Override
    public void onPictureTaken(DocumentPictureResult result, NfcStatus nfcStatus) { }

    @Override
    public void onError(ZenIdException e) {
      // Handle exception here
      Timber.e(e);
    }
  });
  ```

  Also you will be forced to surround some functions with try...catch(ZenIdException exception), e.g.:

  ```
  try {
      documentPictureView.setLoggerCallback((module, method, message) -> Timber.tag(module).d("%s - %s", method, message));
      documentPictureView.setLifecycleOwner(this);
      documentPictureView.setDocumentAcceptableInput(documentAcceptableInput);
      documentPictureView.setPreviewStreamSize(SizeSelectors.biggest());
      documentPictureView.setDocumentPictureSettings(documentPictureSettings);
      documentPictureView.enableDefaultVisualization(visualizationSettings); // enable/disable
  } catch (Exception e) {
      // Handle exception here
      Timber.e(e);
  }
  ```
- Improvement: Introduced DocumentCode, PageCode, and OriginalAcceptableInput to the SDK Signature. TRASKZENIDPV-2973

### 4.3.10 (2024-07-15) (RecogLibC v4.3.10)
- Improvement: Removed Time limit for reading data from the NFC chip from settings. TRASKZENIDPV-1859
- Improvement: Decreased risk of misclassifying documents. SZENID-2633, SZENID-2634, TRASKZENIDPV-2269, TRASKZENIDPV-2689
- Fix: If one document is replaced by another during the scanning process using the SDK, all validators are reset, and only the data related to the last scanned document is sent to the backend. SZENID-2666

### 4.2.17 (2024-07-15) (RecogLibC v4.2.17)
- Fix: Fixed the DocumentVerifier getting stuck in TextNotReadable state while trying to scan the MRZ. TRASKZENIDPV-2567 TRASKZENIDPV-2577 TRASKZENIDPV-1769
- Fix: Fixed behavior when sometimes focus and reflection validators fail on backend for images uploaded from SDK. TRASKZENIDPV-2556
- Improvement: Blur and specular validator writes note to backend validator results if accept score was lower then defined threshold because of downcreasing required sensitivity on SDK during processing of cards. TRASKZENIDPV-2556

### 4.1.17 (2024-07-15) (RecogLibC v4.1.17)
- Versioning for Android SDK and Core library is unified from now on

### 1.23.1 (2024-05-30) (RecogLibC v4.1.11)
- Fix (SZENID-2625) java.lang.NullPointerException while calling:
  - cz.trask.zenid.faceliveness.FaceLivenessStepParams.isHasFailed()
  - cz.trask.zenid.faceliveness.FaceLivenessStepParams.getPassedCheckCount()
  - cz.trask.zenid.faceliveness.FaceLivenessStepParams.getTotalCheckCount()
- Fix: Lost face in face liveness causes the process to be restarted when "Face must be stable and detectable all the time" is set to true. TRASKZENIDPV-2555 (Backported to 4.1.9)

### 1.23.0 (2024-04-10) (RecogLibC v4.1.7)
- New: Support for passports of USA v2006/2017 (NFC only). TRASKZENIDPV-2009
- New: Support for Pakistanese passports v2022 (NFC only). TRASKZENIDPV-2009
- New: Support for Japanese passports v2013 (NFC only). TRASKZENIDPV-2009
- New: PACE protocol support for NFC communication.
- New: SDK provides list of supported countries and documents based on licence activated on the server. Method `ZenId.get().getSecurity().getEnabledFeatures()`
- New: Check if document has a front/back side. Methods `DocumentVerifier.hasFrontSide(documentRole, documentCountry)` and `DocumentVerifier.hasBackSide(documentRole, documentCountry)`
- New: Get profiles from backend. Method `Apiservice.getProfiles(apiKey)`
- New: Step for FaceLiveness `DONT_SMILE` TRASKZENIDPV-1822
- New: The outline is drawn when the model is known in advance. TRASKZENIDPV-2261
- Improvement: Added a new validator that checks the MRZ to the SDK. It is disabled by default but can be enabled in the MRZ Validator settings in the backend Sensitivity page. "MRZ successfully read" is no longer shown in feedback. Use the validator debug view instead. The TEXT_NOT_READABLE state is now used by the MRZ validator, not just by the NFC feature. TRASKZENIDPV-1769
- Fix: CameraView prevents application to lock the screen (updated CameraView library). 
  If you want to use updated version of CameraView library, follow these steps:
  Add URL and credentials to updated CameraView maven repository to your project level gradle file:
  ```
  maven {
    url 'https://maven.pkg.github.com/ZenIDTeam/CameraView'
    credentials {
      username = "ZenIDTeam"
      password = "YOUR_PERSONAL_ACCESS_TOKEN"
    }
  }
  ```
  Generate your own personal access token and use it as password (YOUR_PERSONAL_ACCESS_TOKEN). Github packages requires credentials even for public packages. Minimal required scope for personal access token is read:packages
  You can find more information about Github personal access tokens and how to generate them here: https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens
- Fix: Fixed vaticanese MRZ structure, and update default MRZ parsing for it. TRASKZENIDPV-1974
- Fix: Fixed The number of steps in the face liveness verifier (Index out of range error in backend). TRASKZENIDPV-2331
- Fix: Fixed various crashes and "Unknown Document" upload issues. TRASKZENIDPV-2099 TRASKZENIDPV-2065 TRASKZENIDPV-2356
- Fix: It is possible to change Profile in face liveness. TRASKZENIDPV-2343
- Improvement: Dominican ID Card MRZ checksum handling updated also on client. TRASKZENIDPV-2182
- Improvement: Improved smile detection with a new ML model. TRASKZENIDPV-1822
- Fix: Fixed empty path while getting the ImagePreview event in NFC state.
  

  Change CameraView library version to (2.7.4): `com.otaliastudios:cameraview:2.7.4`
  Sync project with gradle files and updated CameraView library will be downloaded.

### 1.22.0 (2023-10-30) (RecogLibC v3.9.6)
- New: Support for Andorran passports v2017 (NFC only). TRASKZENIDPV-1572
- New: Support for Sanmarinese passports v2006 (NFC only). TRASKZENIDPV-1572
- New: Support for Vaticanian passports v2013 (NFC only). TRASKZENIDPV-1974
- New: Support for Liechtensteiner passports v2006 (NFC only). TRASKZENIDPV-1974
- New: System updated to enable support for documents with just extracting its NFC data. TRASKZENIDPV-1572
- Improvement: Italian ID cards suppord broadened: Italian ID cards v2016/2022. TRASKZENIDPV-1708
- Improvement: SDK checks if barcode has predefined format. SZENID-2256
- New: Support for Belgian Passport v2022. TRASKZENIDPV-1358
- Remove method `setVideoSettings`. These values are set on backend side.
- Profile name can not contain diacritics (it was causing init SDK crashes). TRASKZENIDPV-1744
- Fix: Fixed checksum computing for Slovak Driver licenses. TRASKZENIDPV-1981 

### 1.21.0 (2023-09-27) (RecogLibC v3.8.4)
- New: Support for Dominican ID cards v2014.
- Improvement: Dominican ID card MRZ reading.

### 1.20.0 (2023-08-30) (RecogLibC v3.8.1)
- New `sdk-nfc` package. 
- Set targetSdkVersion 31 at least.
- Method `getInvestigateSamples` requires you to set profile.
- Remove readBarcode, specularAcceptableScore, documentBlurAcceptableScore fields from DocumentPictureSettings. These values are set on backend side.

### 1.20.0-beta (2023-08-09) (RecogLibC v3.7.15)
- Method `DocumentPictureView.Callback::onPictureTaken()` has got `boolean nfcRequired` argument for future usage. 
- Face liveness improvements.
- New SelectProfile feature that allows customers to set frontend validator configs on the backend.
- New states for DocumentPicture and FaceLiveness.

### 1.19.0 (2023-06-13) (RecogLibC v3.7.0)
- Improvement: Hologram video length reducing. Start recording video when a card is detected. 
- Improvement: `HologramView.Callback` has got `onVideoRecordingStart` method to notify you when video recording has started.

### 1.18.1 (2023-05-10) (RecogLibC v3.5.2)
- Fix parsing BirthNumberChecksum value from MRZ

### 1.18.0 (2023-05-10) (RecogLibC v3.5.2)
- Add missing MRZ data into `sdk-api-zenid` package.
- Fixed a crash that could happen when resetting face liveness workflow with debug visualization enabled.

### 1.17.2 (2023-04-28) (RecogLibC v3.3.1)
- Preview stream size selection. 

### 1.17.1 (2023-03-28) (RecogLibC v3.3.1)
- Fix face liveness (legacy mode) crash  

### 1.17.0 (2023-03-09) (RecogLibC v3.3.1)
- The face liveness check takes a selfie picture at a random point while performing the steps.
- The face liveness check retakes a new selfie picture after a failed attempt.

### 1.16.0 (2023-02-23) (RecogLibC v3.2.4)

- Fix tracking issues when the card is far from the center. 
- Improvement: Support for new variant of Montenegrin passport. 
- New: Support for Swedish ID cards v2022.
- New: Support for Swedish passports v2022.
- New: Support for Indian passports v2000/2013.
- New: Support for Danish passports v2021.
- New: Support for Swiss passports v2022.
- New: Support for Vietnamese passports v2022. 

### 1.15.0 (2023-01-20) (RecogLibC v2.12.3)

- Improvement: Video parameters (FPS, width, height) are configurable via backend. Use this option `VideoSettings.useVideoParamsFromBackend(true)` to override local settings.
- New: Support for Slovak ID cards v2022.

### 1.14.0 (2022-11-24) (RecogLibC v2.11.3)

- Fix issue with Birth Certificate tracking.
- Additional parameters for face liveness check are returned as java object instead of JSON string.
- Video parameters (FPS, width, height) are configurable.

### 1.13.1 (2022-11-15) (RecogLibC v2.11.1)

- New: Support for Slovak residency permits v2011. 
- New: Support for Lithuanian ID cards (v2021).

### 1.13.0 (2022-11-07) (RecogLibC v2.11.1)

- Improvement: Additional parameters during the face liveness check.
- New: InitCallback when ZENID SDK is initialized and ready for use.

### 1.12.0 (2022-10-17) (RecogLibC v2.9.1.0)

- Improvement: Updated Polish passport - support for v2022. 
- Improvement: Removed support for the oldest slovak driver license (v1993), it will be invalid soon. 
- New: Support for Montenegrin passports (v2008).
- New: Support for Norwegian passports (v2011/2015 and v2020).
- New: Support for Polish passport version 2011.
- New: Support for Croatian passport version 2009/2015. 
- New: Support for Moldovan passports (v2011/2014/2018). 
- New: Support for Icelandic passports (v2019 and v2006). 
- New: Support for Serbian passports (v2008).
- New: Support for Bosnian passports (v2014).
- New: Support for Albanian passports (v2009).
- New: Support for Macedonian passports (v2007).
- New: Support for Belarussian passports (v2006 and v2021).

### 1.11.0 (2022-08-26) (RecogLibC v2.8.3)

- Add more countries (DK EE, ES, FI, FR, GR, LT, LU, LV, RO, SE, SI, VN)
- New customization options `showSmileAnimation` for FaceLivenessVerifier 
- New look for FaceLivenessVerifier checkmarks

### 1.9.0 (2022-06-30) (RecogLibC v2.6.3)

- Add models for Austria (AT), Belgium (BE), Bulgaria (BG), Ukraine (UA) and Netherlands (NL) 
- Add extra module for the European Union `sdk-core-models-eu`
- Split CZ module into more granular modules by card type
- Added read barcode option into `DocumentPictureSettings`

### 1.8.1 (2022-03-22) (RecogLibC v2.3.11)

- Allow hologram verification in portrait mode
- Add new state for document picture - `Barcode`

### 1.8.0

- Add face liveness video mode
- Add hologram verification (Czech and Croatia ID cards) - `HologramView`
- Bump com.otaliastudios:cameraview dependecy version to 2.7.2

### 1.7.0

- New face tracker - The selfie and face livenes features use a more accurate face tracker with much smaller model downloads (37MB -> 2.5MB).
- BREAKING CHANGE! The face liveness user instructions are now shuffled to prevent passing the checks with recorded videos. To preserve the previous
  behavior of face liveness, the enableLegacyMode option can be set to true in the `FaceLivenessSettings`.
- Add new state for face liveness - `HoldStill`                  
- Add new state for selfie - `BadFaceAngle`

### 1.6.5

- Add `ZenId.isSingletonInstanceExists` helper method to check whether singleton instance already exists or not.

### 1.6.3

- Add Croatia and Hungary models.
- Fix missing fields in `SampleJson` class.
- Fix and depredecate `DocumentPictureResponseValidator` class.

### 1.6.2

- Fix document code wasn't use when scanning the second page of document. It will be a bit faster. 

### 1.6.1

- The FaceLiveness verifier also checks for darkness and blur. It can return "Dark" and "Blurry" states.
- The FaceLiveness verifier no longer takes pictures when the user is looking sideways or is outside the frame.

### 1.6.0

- Add picture signature feature, which in conjunction with the backend SDK signature validator, can be used to verify the image. 
- BREAKING CHANGE! Objects `DocumentPictureResult / SelfieResult / FaceLivenessResult` were unified to return the picture path `getFilePath` and the signature `getSignature`. 
Use these objects to get the picture signature and send it to the backend.

### 1.5.0

- BREAKING CHANGE! `DocumentPictureVisualizationSettings` was renamed to `DocumentPictureSettings` plus three new options:
  - specularAcceptableScore - Can be used for fine tuning the sensitivity of the specular validator. Value range 0-100. Default value is 50.
  - documentBlurAcceptableScore - Can be used for fine tuning the sensitivity of the document blur validator. Value range 0-100. Default value is 50.
  - timeToBlurMaxToleranceInSeconds - The time delay for the blur validator to become max tolerant. Default value is 10.
- New package `sdk-core-models-cz-extended` includes RESIDENCY_PERMIT, GUN_LICENSE and EUROPEAN_HEALTH_INSURANCE document roles

### 1.4.0

- BREAKING CHANGE! ZenId.Builder refactoring. We have moved each verifier into its own module. The goal under the hood is to declare what we consider as a library-private API and what can be changed without warning. 
  Everything inside `internal` package is not a published API and you should be very careful when using it.

  ```
  ZenId zenId = new ZenId.Builder()
    .applicationContext(context)
    .modules(new DocumentModule(), new SelfieModule(), new FaceLivenessModule())
    .build();
  ```

- BREAKING CHANGE! Use `visualizationSettings` for our default visualization. 

  ```
  VisualizationSettings visualizationSettings = new VisualizationSettings.Builder()
    .language(Language.CZECH)
    .showDebugVisualization(true)
    .build();
  documentPictureView.enableDefaultVisualization(visualizationSettings);
  ```

- BREAKING CHANGE! Class `DocumentResult` has been splitted into two separate classes: `DocumentPictureResult` and `HologramResult`.


### 1.3.6

- Fix exception when NULL document country is provided for `documentPictureView.setDocumentType()`

### 1.3.5

- Fix scan a document in landscape mode  
- Bump OkHttp version. Use at least OkHttp 3.14.7 see https://square.github.io/okhttp/changelog_3x/#version-3147

### 1.3.4

-  Add option to show or hide default text messages when using our default visualization. Use `showTextInformation` parameter which is set to `true` by default.

### 1.3.3

- The blur, reflection and darkness validators are now more tolerant and reach their maximum tolerance faster.

### 1.3.2

- Fix handle camera permission. New method setAutoRequestPermissions for each CameraView. More details in README, section: Camera permission.

### 1.3.0

- Support auto-rotate setting for document picture processing. DocumentPictureView doesn't require landscape mode anymore. 
- Bump com.otaliastudios:cameraview dependecy version to 2.6.4

### 1.2.1

- Change minimum API level from 22 to 21.

### 1.2.0
- BREAKING CHANGE! Split document models by country codes into separate packages to optimize SDK size. 

  - Add only model packages on classpath which you really need. Keep the rest out.
  ```
  implementation "cz.trask.zenid.sdk:sdk-core:$version"
  implementation "cz.trask.zenid.sdk:sdk-core-models-cz:$version"
  implementation "cz.trask.zenid.sdk:sdk-core-models-sk:$version"
  ```

### 1.1.5
- Fix blur detection

### 1.1.4
- Fix CENTER_CROP scale type. Using this method `documentPictureView.adjustPreviewStreamSize()` for CENTER_CROP scale type is not needed anymore.
- Fix document role/country/page are now optional. 

### 1.1.1

- BREAKING CHANGE! Selfie and faceliveness are optional modules now. You can save some MBs if you don't include unused modules. See the docs. 
Migration should we very straightforward.

   - Add all modules you need.
   ```
   implementation "cz.trask.zenid.sdk:sdk-core:$version"
   implementation "cz.trask.zenid.sdk:sdk-selfie:$version"
   implementation "cz.trask.zenid.sdk:sdk-faceliveness:$version"
   implementation "cz.trask.zenid.sdk:sdk-api-zenid:$version"
   ```

   - Pass extra verifiers (selfie and/or face-liveness) you want to use.
   ```
   ZenId zenId = new ZenId.Builder()
        .applicationContext(getApplicationContext())
        .extraVerifiers(new SelfieVerifier(), new FaceLivenessVerifier())
        .build();
   ```

   - Change package names in java and xml.
   ```
   cz.trask.zenid.sdk.SelfieView -> cz.trask.zenid.sdk.faceliveness.SelfieView
   cz.trask.zenid.sdk.FaceLivenessView -> cz.trask.zenid.sdk.faceliveness.FaceLivenessView
   ```

- New states (DARK, CONFIRMING_FACE) for the selfie module.
- Fix *fault addr* crash on some devices

### 1.0.6

- Fix debug (offline) tokens for authorization
- Fix selfie timer issue

### 1.0.5

- Fix (Nexus 5X) reverse camera orientation

### 1.0.3

- Fix aspect ratio issue. 

### 1.0.2

- An option to take a document picture

### 1.0.1

- Total refactoring in order to support UI customization

### 0.22.3

- Default theme
- Set the default language. It is a new mandatory field - BREAKING CHANGE.

### 0.22.0

- Authorization

### 0.19.1

- Hologram verification
