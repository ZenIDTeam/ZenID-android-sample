## ZenID Android SDK

Android sample app that shows how to use the ZenID Android SDK. The SDK can help you with performing the following operations on documents:

* OCR and data extraction
* verification of authenticity
* real-time face liveness detection
* Hologram verification

The SDK supports API level 21 and above.
The SDK targets Android API level 35.

Apps need to use a single NDK and STL for all native code and dependencies - [one STL per app](https://developer.android.com/ndk/guides/cpp-support#one_stl_per_app).
We use NDK 26.3.11579264 and STL c++_shared by default. If you already rely on an another native library, please do mutual compatibility check as soon as possible.

#### Compatibility assumptions
To ensure technical compatibility, version of SDK must not be newer than Version of ZenID backend.
We strongly recommend to do regular upgrades of SDK libraries along with ZenID backend, to prevent irregularities in controls and other common functionalities of ZenID backend and SDK. Minor version of SDK libraries should not be older than one versions from ZenID backend. For example with ZenID backend installed in version 4.4.x, SDK libraries should not be older than 4.3.x, etc.

### Migration

#### 4.4.14 -> 4.4.15
- Copy and paste libraries

#### 4.4.7 -> 4.4.14
- Copy and paste libraries

#### 4.4.6 -> 4.4.7
- Copy and paste libraries
- Change NDK version to `26.3.11579264`

#### 4.3.10 -> 4.4.6
- Copy and paste libraries
- Remove maven credentials to CameraView maven repository from your project level gradle file

  ```
  maven {
    url 'https://maven.pkg.github.com/ZenIDTeam/CameraView'
    credentials {
      username = "ZenIDTeam"
      password = "YOUR_PERSONAL_ACCESS_TOKEN"
    }
  }
  ```

- Remove dependency: `com.otaliastudios:cameraview:2.7.4` from your app level gradle file
- Add dependencies:
  `com.google.android.gms:play-services-tasks:18.1.0`
  `com.otaliastudios.opengl:egloo:0.6.1`
- You will be forced to implement new callback functions `onError(ZenIdException exception)` in each view callback, e.g.:

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

#### 4.2.17 -> 4.3.10
- Copy and paste libraries

#### 4.1.17 -> 4.2.17
- Copy and paste libraries

#### 1.23.1 -> 4.1.17
- Copy and paste libraries

#### 1.23.0 -> 1.23.1
- Copy and paste libraries

#### 1.22.0 -> 1.23.0
- Copy and paste libraries
- Update library `net.sf.scuba:scuba-sc-android` to version `0.0.23` and library `org.jmrtd:jmrtd` to version `0.7.21`
- If you want to use updated version of CameraView library, follow these steps:
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
  You can find more information about Github personal access tokens here: https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens

  Change CameraView library version to (2.7.4): `com.otaliastudios:cameraview:2.7.4`
  Sync project with gradle files and updated CameraView library will be downloaded.
- The DONT_SMILE state is now used in the FaceLivenessView. It is triggered when the user continually smiles during the face liveness check. During the face liveness process, there is a step where the user is asked to smile. However, this could be defeated by a fraudster who uses a mask with a permanent smile. The "Don't Smile" check is designed to counter this. Typically, this check runs in the background. If the user stops smiling even briefly, the DONT_SMILE state is not activated. This is the most common scenario. However, if the user consistently smiles, we eventually prompt them to stop smiling and then return the DONT_SMILE state.
- Added a new validator that checks the MRZ to the SDK. To preserve the previous behavior, disable it the backend "Sensitivity" page in the MRZ validator.
- The TEXT_NOT_READABLE state in the DocumentView should now be handled if the MRZ validator is enabled. It was previously used just for NFC, and now it's used by the MRZ validator too.

#### 1.21.0 -> 1.22.0
- Copy and paste libraries
- Remove method setVideoSettings. Local change of video settings is no longer supported. You can set video settings on backend side.

#### 1.20.0 -> 1.21.0
- Copy and paste libraries

#### 1.19.0 -> 1.20.0
- Copy and paste libraries
- Set targetSdkVersion 31 at least
- Send ZENID signature as a multipart request [here](https://github.com/ZenIDTeam/ZenID-android-sample#sdk-signature)
- Implement profile feature [here](https://github.com/ZenIDTeam/ZenID-android-sample#select-profile)
- Implement new states for DocumentPicture and FaceLiveness
- Remove readBarcode, specularAcceptableScore, documentBlurAcceptableScore fields from DocumentPictureSettings. These values are set on backend side.

### Package sizes

|  Package name  |  Size (MB)  |  Note  |
|----------|:-------------:|------|
|  sdk-core  |  24.6  |  Can be reduced by using abi-filters or abi-split  |
|  sdk-core-models-at  |  7.5  |  |
|  sdk-core-models-cz-*  |  10  | Can be reduced by card type |
|  sdk-core-models-de  |  2.6  |  |
|  sdk-core-models-hr  |  2.7  |  |
|  sdk-core-models-hu  |  12.9  |  |
|  sdk-core-models-it  |  5.7  |  |
|  sdk-core-models-pl  |  9.9  |  |
|  sdk-core-models-sk  |  4.4  |  |
|  sdk-faceliveness  |  2.4  |  |
|  sdk-selfie  |  2.4  |  |
|  sdk-api-zenid  | 0.05  |  |

You can find full list of models [here](https://github.com/ZenIDTeam/ZenID-android-sample/tree/master/libs).

###  Supported screen orientations

|  View name |  Required mode  |
|----------|:-------------:|
|  DocumentPictureView |  no limitations  |  
|  SelfieView  |  portrait  |  
|  FaceLivenessView  |  portrait  |  
|  HologramView  |  landscape  |  

### Installation

* Put our *.aar files in the libs folder of your app
* Edit the build.gradle file of your app and add

```
ext {
    okHttpVersion = '3.14.9'
    retrofitVersion = '2.6.2'
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.2.0'
    implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
    implementation "com.squareup.okhttp3:okhttp:$okHttpVersion"
    implementation "com.squareup.okhttp3:logging-interceptor:$okHttpVersion"
    implementation "com.squareup.retrofit2:retrofit:$retrofitVersion"
    implementation "com.squareup.retrofit2:converter-gson:$retrofitVersion"
    implementation 'com.jakewharton.timber:timber:4.7.1'
    implementation 'com.otaliastudios:cameraview:2.6.4'
    implementation fileTree(include: ['*.aar'], dir: '../libs')
}
```
* Rebuild the project

### Multi-APK split

The C++ code needs to be compiled for each of the CPU architectures (known as "ABIs") present on the Android environment. Currently, the SDK supports the following ABIs:

* `armeabi-v7a`
* `arm64-v8a`
* `x86`
* `x86_64`

The SDK binary contains a copy of the native `.so` file for each of these four platforms.
You can considerably reduce the size of your `.apk` by applying APK split by ABI, editing your `build.gradle` as the following:

```
android {

  splits {
    abi {
        enable true
        reset()
        include 'x86', 'x86_64', 'arm64-v8a', 'armeabi-v7a'
        universalApk false
    }
  }
}
```

More information on the [Android documentation](https://developer.android.com/studio/build/configure-apk-splits.html)

### Initialization

The ZenID Android SDK is a collection of four modules. The first one (sdk-core) is the core module for offline document processing. Selfie and face-liveness modules are optional. And the last one (sdk-api-zenid) is an API integration to our backend system.

```
ZenId zenId = new ZenId.Builder()
        .applicationContext(getApplicationContext())
        .modules(new DocumentModule(), new SelfieModule(), new FaceLivenessModule())
        .build();

// Make the instance globally accessible.
ZenId.setSingletonInstance(zenId);

// This may take a few seconds. Please do it as soon as possible.
zenId.initialize();
```

`zenId.initialize();` is supposed to be called only once per application lifetime. You can use helper method `ZenId.isSingletonInstanceExists` to check whether singleton instance already exists or not.

In order to start integration, you will need to get an **API key** and **URL** of the backend system.

```
ApiConfig apiConfig = new ApiConfig.Builder()
        .baseUrl("http://your.frauds.zenid.cz/api/") 
        .apiKey("your_api_key")
        .build();
```

```
OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
        // .addInterceptor() if you want to add a logging interceptor and so on
        .build();
```

```
ApiService apiService = new ApiService.Builder()
        .apiConfig(apiConfig)
        .okHttpClient(okHttpClient)
        .build();
```

You can check our implementation in Sample app (MyApplication class)

#### Init callback

You can use ZenId.InitCallback to be notified when ZENID SDK is ready for use. Initialization itself might take a few seconds. It all depends on count of models (documents).

```
zenId.initialize(new ZenId.InitCallback() {
    @Override
    public void onInitialized() {
        // Initialized
    }

    @Override
    public void onInitializationFailed(ZenIdException e) {
        // Initialization failed - handle exception
    }
});
```

Please, check out our samples and you will get the whole picture on how it works.

#### Logger callback

In previous versions of Android SDK there was no option to turn on/off logs. Starting from Android SDK version 4.4.3 you can set a LoggerCallback to intercept logs from SDK and print logs according to your preferences.
Use function setLoggerCallback with parameter LoggerCallback interface to receive logs. You will receive 3 string parameters (module, method, message) which might be helpful during debugging and solving issues.

Global logger:

In this callback you will receive logs from Core library, from various processes and threads.
Use security module function setLoggerCallback to set listener:

```
zenId.getSecurity().setLoggerCallback(new LoggerCallback() {
    @Override
    public void logMessage(String module, String method, String message) {
        // Handle logging here (Write the log to console, file, Firebase or some other system you use) e.g.
        Timber.tag(module).d("%s - %s", method, message)
    }
});
```

Logger for specific view:

You can also set LoggerCallback on each view provided by our SDK DocumentPictureView, FacelivenessView, SelfieView, ...) to catch and print longs from Android SDK. You will receive 3 string parameters (module, method, message) which might be helpful during debugging and solving issues.
Use view function setLoggerCallback to set listener:

```
documentPictureView.setLoggerCallback((module, method, message) -> {
    // Handle logging here e.g.
    Timber.tag(module).d("%s - %s", method, message);
});
```

### Authorization

Starting from 0.21.5, mobile apps need to be authorized by the backend. Here is how you can do it.

Before you start, make sure "Android Package Name" are filled in the settings page of the website.

* Step 1. Get the challenge token.
* Step 2. Get the response token by calling /api/initSdk in the backend.
* Step 3. Call authorize using the response token obtained in the previous step. It will return a bool representing success or failure.

These steps need to be performed before any operation on documents, otherwise you will get a RecogLibCException with "Security Error" in the message.
All these steps need to be performed together as one action.

> [!IMPORTANT]
> getChallengeToken() and getInitSdk() should always be called together as getChallengeToken() resets the session.

> [!IMPORTANT]
> getChallengeToken() and getInitSdk() should not be called while a card or face is being processed.
```
String challengeToken = ZenId.get().getSecurity().getChallengeToken();
Timber.i("challengeToken: %s", challengeToken);
apiService.getInitSdk(challengeToken).enqueue(new Callback<InitResponseJson>() {

    @Override
    public void onResponse(Call<InitResponseJson> call, Response<InitResponseJson> response) {
        String responseToken = response.body().getResponse();
        Timber.i("responseToken: %s", responseToken);
        boolean authorized = ZenId.get().getSecurity().authorize(getApplicationContext(), responseToken);
        Timber.i("Authorized: %s", authorized);
    }

    @Override
    public void onFailure(Call<InitResponseJson> call, Throwable t) {
        Timber.e(t);
    }
});

private void authorize() {
    String challengeToken = null;
    try {
        challengeToken = ZenId.get().getSecurity().getChallengeToken();
    } catch (ZenIdException e) {
        Timber.e(e);
    }
    Timber.i("challengeToken: %s", challengeToken);
    apiService.getInitSdk(challengeToken).enqueue(new Callback<InitResponseJson>() {

        @Override
        public void onResponse(@NonNull Call<InitResponseJson> call, @NonNull Response<InitResponseJson> response) {
            InitResponseJson initResponseJson = response.body();
            if (initResponseJson == null) {
                Timber.e("Authorization response body is empty!");
                return;
            }
            String responseToken = initResponseJson.getResponse();
            Timber.i("responseToken: %s", responseToken);
            try {
                boolean authorized = ZenId.get().getSecurity().authorize(getApplicationContext(), responseToken);
                Timber.i("Authorized: %s", authorized);
            } catch (ZenIdException e) {
                Timber.e(e);
            }
        }

        @Override
        public void onFailure(@NonNull Call<InitResponseJson> call, @NonNull Throwable t) {
            Timber.e(t);
        }
    });
}
```

### Get enabled features

SDK provides list of supported countries and documents based on licence activated on the server.
```
ZenId.get().getSecurity().getEnabledFeatures()
```
This method returns object `EnabledFeatures` with list of enabled countries, document roles, document pages and verifiers.

### Select profile

This allows customers to set frontend validator configs on the backend. On Init() call the SDK receives a list of profiles and their respective configs.
Calling SelectProfile() sets what profile will be used for subsequent verifier usage.
```
ZenId.get().getSecurity().selectProfile("profile-name");
```
You must set explicitly profile when investigation samples over `ApiService.getInvestigateSamples()`.

### Architectural overview of the SDK

Every each use-case has its own view class:
- cz.trask.zenid.sdk.DocumentPictureView for document picture verification
- cz.trask.zenid.sdk.HologramView for the hologram verification
- cz.trask.zenid.sdk.faceliveness.FaceLivenessView for the real-time face liveness check
- cz.trask.zenid.sdk.selfie.SelfieView to take a good selfie picture
- cz.trask.zenid.sdk.nfc.NfcService to read data from chip of documents with dual interface (NFC antenna) via NFC

To use the DocumentPictureView for instance, simply add a DocumentPictureView to your layout.

```
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <cz.trask.zenid.sdk.DocumentPictureView
        android:id="@+id/documentPictureView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</FrameLayout>
```

Those views are lifecycle-aware. Set lifecycleOwner as soon as possible.

```
// For activities
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    DocumentPictureView documentPictureView = findViewById(R.id.documentPictureView);
    documentPictureView.setLifecycleOwner(this);
}

// For fragments
@Override
public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    DocumentPictureView documentPictureView = findViewById(R.id.documentPictureView);
    documentPictureView.setLifecycleOwner(getViewLifecycleOwner());
}
```

Register a callback to notify about its events.

```
documentPictureView.setCallback(new DocumentPictureView.Callback() {

    @Override
    public void onStateChanged(DocumentPictureState state) {
        Timber.i("state: %s", state);
    }

    @Override
    public void onPictureTaken(DocumentPictureResult result, NfcStatus nfcStatus) {
        Timber.i("result: %s", result);
    }

    @Override
    public void onError(ZenIdException e) {
        Timber.e(e);
    }
});
```

To take a blurry etc. document pictures (just before it is considered as a perfect match by our SDK) you can use this method `documentPictureView.activateTakeNextDocumentPicture()`.
This method will take the next video frame which matches requested document type and return it as a picture through `onPictureTaken` callback.

DocumentPictureView offers two different scale types - CENTER_CROP and CENTER_INSIDE - the same behavior as described at [Android documentation](https://developer.android.com/reference/android/widget/ImageView.ScaleType).
In addition, but not necessary, you can use `documentPictureView.adjustPreviewStreamSize()` for CENTER_INSIDE scale type which will set the camera aspect ratio as close as possible to the viewport with min. required size of the picture.

Please see the sample app for more details.

### Camera permission

Camera permission is automatically requested when lifecycleOwner is set. You can use `setAutoRequestPermissions` to disable this auto request feature. Recommended way (a code snippet from the sample SelfieActivity) to handle the camera permission:

```
@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (isCameraPermissionDenied(grantResults)) {
        if (shouldShowRequestPermissionRationale()) {
            Timber.i("Camera permission denied but selfieView will immediately request camera permission again.");
        } else {
            selfieView.setAutoRequestPermissions(false);
            Timber.i("Camera permission denied with -don't ask again- option.");
        }
    } else {
        Timber.i("Camera permission granted!");
    }
}

private boolean isCameraPermissionDenied(int[] grantResults) {
    return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED;
}

private boolean shouldShowRequestPermissionRationale() {
    return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
}
```

### Overview of states

DocumentPictureState:
- NO_MATCH_FOUND = No document matching input parameters was found.
- ALIGN_CARD = Align card in front of your back camera.
- HOLD_STEADY = Hold the document steady for a while.
- BLURRY = The picture is too blurry.
- REFLECTION_PRESENT = The reflection is present on text.
- OK = The picture is ok.
- DARK = The picture is dark.
- BARCODE = The barcode is unreadable.
- TEXT_NOT_READABLE = Text on the document is not readable.
- HOLOGRAM = Hologram verification required.
- NFC = NFC verification required.

HologramState:
- CENTER = Align the document to the center of the screen.
- TILT_LEFT_AND_RIGHT = Tilt your phone left or right.
- TILT_UP_AND_DOWN = Tilt your phone up or down.
- OK = Scanning done, the hologram is ok.

SelfieState:
- OK = The picture is ok.
- NO_FACE_FOUND = No face was found.
- BLURRY = The picture is too blurry.
- DARK = The picture is dark.
- CONFIRMING_FACE = Face was confirmed.
- BAD_FACE_ANGLE = When the user's head is turned away from the camera.

FaceLivenessState:
- LOOK_AT_ME = Turn head towards camera.
- TURN_HEAD = Turn head slowly towards arrow.
- SMILE = Smile to the camera or move your mouth.
- OK = The scanning is done.
- BLURRY = The picture is too blurry.
- DARK = The picture is dark.
- HOLD_STILL = Hold still.
- DONT_SMILE = Don't smile.
- RESETING = Face is not verified, please try again. Move to better light conditions. Minimize sudden movements.

NfcState:
- START = Connection established.
- PERSONAL_DATA = Reading personal data.
- PHOTO = Reading photo.
- OK = Reading is done, all data retrieved.

### Document picture feature

#### Document picture settings

```
DocumentPictureSettings documentPictureSettings = new DocumentPictureSettings.Builder()
        .enableAimingCircle(true)
        .showTimer(true)
        .drawOutline(true)
        .build();

documentPictureView.setDocumentPictureSettings(documentPictureSettings);
```

```
// Toggles displaying an aiming circle while searching for cards.
Boolean enableAimingCircle;

// Toggles displaying timer that shows seconds remaining for the validators to become max tolerant.
Boolean showTimer;

// The card outline will be drawn on the video preview if this is true. Default: true
Boolean drawOutline;
```

#### Document acceptable input

Use `documentAcceptableInput` to specify subset of documents which are allowed for scanning. For example, if you want to scan both
sides of Czech identity card and front side of Slovak driving license.

```
DocumentAcceptableInput.Filter filter1 = new DocumentAcceptableInput.Filter(DocumentRole.ID, null, DocumentCountry.CZ);
DocumentAcceptableInput.Filter filter2 = new DocumentAcceptableInput.Filter(DocumentRole.DRIVING_LICENSE, DocumentPage.FRONT_SIDE, DocumentCountry.SK);
DocumentAcceptableInput documentAcceptableInput = new DocumentAcceptableInput(Arrays.asList(filter1, filter2));

documentPictureView.setDocumentAcceptableInput(documentAcceptableInput);
```

#### Document front/back side check

Use methods `hasFrontSide` and `hasBackSide` to check if document front/back side is available for scanning.

```
DocumentVerifier verifier = (DocumentVerifier) ZenId.get().getVerifier(DocumentVerifier.class);
verifier.hasFrontSide(documentRole, documentCountry)
verifier.hasBackSide(documentRole, documentCountry)
```

#### NFC

It is up to you to use this package. Optional. First of all, you need to set targetSdkVersion 31 at least.
When you decide to use this package, please add these dependencies.
```
implementation 'net.sf.scuba:scuba-sc-android:0.0.23'
implementation 'org.jmrtd:jmrtd:0.7.21'
implementation 'com.gemalto.jp2:jp2-android:1.0.3'
```

Method `DocumentPictureView.onPictureTaken()` can be implemented like this. Based on NfcStatus you must decide what to do.
```
public void onPictureTaken(DocumentPictureResult result, NfcStatus nfcStatus) {
    if (NfcStatus.NFC_REQUIRED.equals(nfcStatus)) {
        startActivity(new Intent(getApplicationContext(), NfcActivity.class));
    } else {
        postDocumentPictureSample(result);
    }
    finish();
}
```

```
public enum NfcStatus {
    DEVICE_DOES_NOT_SUPPORT_NFC, // Device does not have NFC reader.
    SAMPLE_WITHOUT_CHIP, // Sample does not have NFC tag.
    NFC_VALIDATOR_DISABLED, // NFC validator is disabled on backend side.
    NFC_REQUIRED; // You must proceed with NFC check.
}
```

DON'T upload samples on backend when NFC step is required. Implement NfcActivity instead and let's do NFC check first.
Please check the sample codebase.

Our SDK provides `NfcService` class to wrap interaction with NFC tag

```
public interface NfcService.Callback {

    // DocumentPictureState must be NFC otherwice NfcService will not work properly.
    void onZenIdNotNfcState();  

    void onStateChanged(NfcState nfcState); 

    // DocumentPictureState is OK, result is signed and can be send to backend.
    void onResult(DocumentPictureResult documentPictureResult, NfcResult nfcResult, NfcData nfcData);

    // For instance when MRZ data are not correct and authentication is failing.
    void onAccessDenied(NfcAccessDeniedException accessDeniedException);

    // For instance when use more with his phone to far away.
    void onConnectionFailed(NfcConnectionException connectionException);

    void onGeneralException(Exception exception);
}
```

```
public enum NfcState {
    START, // Connection established.
    PERSONAL_DATA, // Reading personal data.
    PHOTO, // Reading photo.
    OK; // All data retrieved.
}
```

Display photo from NFC. Unfortunately some documents use JPEG2000 image format. If you need to display photo from NFC, please, use this snippet.

```
String picturePath = nfcResult.getFacePicturePath();
FacePictureFormat format = nfcResult.getFacePictureFormat();
Bitmap bitmap;
if (format.equals(FacePictureFormat.JP2)) {
    bitmap = new JP2Decoder(picturePath).decode();
} else {
    bitmap = BitmapFactory.decodeFile(picturePath);
}
```

To get number of reading attempts set it your verifier use `nfcService.getNumberOfReadingAttempts()`
To check if it is allowed to skip this step use `nfcService.isSkipNfcAllowed()`
If you want to skip NFC verification use `nfcService.skipNfcVerification()` get your result object `DocumentPictureResult result = nfcService.getDocumentPictureResult(NfcActivity.this)` and at this point you can upload the result.

Please check our implementation in Sample app (NfcActivity)

### Hologram feature

#### Hologram settings

```
HologramSettings hologramSettings = new HologramSettings.Builder()
        .enableAimingCircle(true)
        .drawOutline(true)
        .build();

hologramView.setHologramSettings(hologramSettings);
```

### Face liveness feature

The face liveness user instructions are now shuffled to prevent passing the checks with recorded videos. To preserve the previous
behavior of face liveness, the enableLegacyMode option can be set to true in the `FaceLivenessSettings`.

#### Step pictures

The `onResult(FaceLivenessResult result)` callback now returns images of the user at the time each step was completed. They are in the auxiliaryImages field.
The auxiliary images are an array of blobs containing images compressed in jpeg format. For example they can be process like this:

```
for (byte[] arr : result.getAuxiliaryImages()) {
    File file = new File("picture_" + System.currentTimeMillis() + ".jpg");
    try (FileOutputStream fos = new FileOutputStream(file)) {
        file.createNewFile();
        fos.write(arr);
    }
}
```

FaceLivenessResult also returns metadata (JSON) for the step images in the auxiliaryImageMetadata field. The metadata is an array of objects with the following properties:

- checkName: the name of the check. It can be LegacyAngle, Angle, or Smile. Angle and Smile are the default checks, LegacyAngle and Smile will be used instead of legacy mode is enabled.
- parameter: a parameter for the check. Angle is the only check with a parameter. It can be either Left, Right, Up or Down.
- isoTimeUtc: ISO 8601 timestamp of the moment the check was passed.
- unixEpoch: Unix epoch of the moment the check was passed, in seconds.

For example:
```
[
   {
      "checkName":"Angle",
      "isoTimeUtc":"2021-11-11T19:20:10.826Z",
      "parameter":"Up",
      "unixEpoch":"1636658410"
   },
   {
      "checkName":"Smile",
      "isoTimeUtc":"2021-11-11T19:20:15.146Z",
      "parameter":"",
      "unixEpoch":"1636658415"
   },
   {
      "checkName":"Angle",
      "isoTimeUtc":"2021-11-11T19:20:19.536Z",
      "parameter":"Right",
      "unixEpoch":"1636658419"
   },
   {
      "checkName":"Angle",
      "isoTimeUtc":"2021-11-11T19:20:23.787Z",
      "parameter":"Left",
      "unixEpoch":"1636658423"
   },
   {
      "checkName":"Angle",
      "isoTimeUtc":"2021-11-11T19:20:28.145Z",
      "parameter":"Down",
      "unixEpoch":"1636658428"
   }
]      
```

#### Step parameters

During the face liveness check, additional parameters for the current check can be accessed from the callback.

```
faceLivenessView.setCallback(new FaceLivenessView.Callback() {

    @Override
    public void onStateChanged(FaceLivenessState state, @Nullable FaceLivenessStepParams stepParams) {
        Timber.i("onStateChanged - state: %s, stepParams: %s", state, stepParams.getName());
    }
```

The stepParams object is only available during the liveness part of the process, when checkboxes are visible. It is null during the preliminary quality check.

The object has the following properties:

|  Property  |  Description  |
|----------|-------------|
|  name  |  Name of the step. It can be "CenteredCheck", "AngleCheck Left", "AngleCheck Right", "AngleCheck Up", "AngleCheck Down", "LegacyAngleCheck", "UpPerspectiveCheck" or "SmileCheck". CenteredCheck requires the user to look at the camera. The AngleCheck steps require the user to turn their head in a specific direction. The LegacyAngleCheck requires the user to turn his head in any direction. It's only used when legacy mode is enabled. SmileCheck requires the user to smile, UpPerspective requires turn up and sometimes also turn down or repeat movement.  |
|  totalCheckCount  |  The total number of the checks the user has to pass, including the ones that were already passed.  |
|  passedCheckCount  |  The number of checks the user has passed.  |
|  hasFailed  |  Flag that is true if the user has failed the most recent check. After the failed check, a few seconds pass and the check process is restarted - the flag is set to false and passedCheckCount goes back to 0.  |
|  headYaw/headPitch/headRoll  |  Euler angles of the head in degrees. Only defined if a face is visible.  |
|  faceCenterX/faceCenterY  |  Coordinates of the center of the face in relative units. Multiply by the width or height of the camera preview to get absolute units. Only defined if a face is visible.  |
|  faceWidth/faceHeight  |  Size of the face in relative units. Multiply by the width or height of the camera preview to get absolute units. Only defined if a face is visible.  |

For example:
```
{
   "faceCenterX":0.57113116979599,
   "faceCenterY":0.6632745265960693,
   "faceHeight":0.2726851999759674,
   "faceWidth":0.5453703999519348,
   "hasFailed":false,
   "headPitch":-5.794335842132568,
   "headRoll":2.2474241256713867,
   "headYaw":9.96286392211914,
   "name":"SmileCheck",
   "passedCheckCount":1,
   "totalCheckCount":5
}
```

#### Face liveness mode

PICTURE:
```
faceLivenessView.setMode(FaceLivenessMode.PICTURE);
```
FaceLivenessResult returns selfie picture file. Path to the selfie picture file is in parameter filePath. To post selfie picture to our backend system use method postSelfieSample from module sdk-api-zenid.</br>For more details about api calls see chapter "More details on the sdk-api-zenid module".

VIDEO:
```
faceLivenessView.setMode(FaceLivenessMode.VIDEO);
```
FaceLivenessResult returns selfie picture file and also selfie video file.</br>Path to the selfie picture file is in parameter filePath. To post selfie picture to our backend system use method postSelfieSample from module sdk-api-zenid.</br>Path to the selfie video file is in parameter videoFilePath. To post selfie video file to our backend system use method postSelfieVideoSample from module sdk-api-zenid.</br>For more details about api calls see chapter "More details on the sdk-api-zenid module".

#### Face liveness settings

```
FaceLivenessSettings faceLivenessSettings = new FaceLivenessSettings.Builder()
        .enableLegacyMode(false) 
        .maxAuxiliaryImageSize(300)
        .showSmileAnimation(true)
        .build();

faceLivenessView.setFaceLivenessSettings(faceLivenessSettings);
```

```
// Use the pre-1.6.3 behavior: turn in any direction then smile. Default value is false.
boolean enableLegacyMode;

// Auxiliary images will be resized to fit into this size while preserving the aspect ratio. Default value is 300.
int maxAuxiliaryImageSize;
```

### Visualization settings

```
VisualizationSettings visualizationSettings = new VisualizationSettings.Builder()
        .showDebugVisualization(false) // Enables debug visulisation rendered directly to camera feed. 
        .language(Language.ENGLISH) // Language of UI
        .showTextInformation(true) // Enables text information rendering
        .build();

documentPictureView.enableDefaultVisualization(visualizationSettings);
```

### Video settings

You can set video settings on backend side.

### Preview stream size selection

When you need more control over the frame processing, you can use this method `documentPictureView.setPreviewStreamSize()`. The same thing for face liveness or selfie.
For instance when you want to have as big resolution as possible:
```
documentPictureView.setPreviewStreamSize(SizeSelectors.biggest());
```
Please take a look into the cameraView documentation here https://natario1.github.io/CameraView/docs/capture-size - SizeSelectors utilities and configure your size selector as you wish.

### SDK Signature

The SDK now generates a signature for the snapshots it takes. The backend uses the signature to verify picture origin and integrity.

The SDK returns signature within `DocumentPictureResult / SelfieResult / FaceLivenessResult` objects. Manual snapshots don't have signatures since they bypass SDK checks.

You can then send the signature to the backend with the /api/sample request.

Signatures now contain "--ZENID_SIGNATURE--" prefix. The new recommended way of sending `signature` is by adding it as a second file to multipart upload of sample (first file being the image or video itself). Alternative method, if you are uploading image/file as binary body in POST request is to append signature to binary data of image/video as is. Old way of sending signature as request parameter in URL still works, but you can encounter issues due to URL size limits so it is recommended to switch to the new method.

Example using `okhttp3` and `retrofit2` libraries:

```
public interface RetrofitService {

    @Multipart
    @POST("sample")
    Call<SampleJson> postSample(
            @Part MultipartBody.Part picture,
            @Part MultipartBody.Part signature,
            @Query("expectedSampleType") String expectedSampleType,
            ...
    );
}    
```

```
private MultipartBody.Part buildJpegPart(@NonNull String picturePath) {
    File file = new File(picturePath);
    RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), file);
    MultipartBody.Part formData = MultipartBody.Part.createFormData("picture", "my picture", requestBody);
    return formData;
}

private MultipartBody.Part buildSignaturePart(@NonNull String signature) {
    RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), signature.getBytes());
    MultipartBody.Part formData = MultipartBody.Part.createFormData("signature", "my signature", requestBody);
    return formData;
}

public Call<SampleJson> postDocumentPictureSample(@NonNull DocumentPictureResult result) {
    MultipartBody.Part jpegPart = buildJpegPart(result.getFilePath());
    MultipartBody.Part signaturePart = buildSignaturePart(result.getSignature());
    return retrofitService.postSample(jpegPart, signaturePart, "DocumentPicture", ...);
}
```

### More details on the sdk-api-zenid module

To run optical character recognition (OCR) and investigate documents (please follow the link at http://your.frauds.zenid.cz/Sensitivity/Validators to get more details what investigation is about), you need to connect to our backend systems. To do so, you need to use our `ApiService` and and make appropriate calls to upload documents, for instance:
```
apiService.postDocumentPictureSample(documentPictureResult, true).enqueue(new retrofit2.Callback<SampleJson>() {

    @Override
    public void onResponse(@NonNull Call<SampleJson> call, @NonNull Response<SampleJson> response) {
        String sampleId = response.body().getSampleId();
        Timber.i("sampleId: %s", sampleId);
    }

    @Override
    public void onFailure(@NonNull Call<SampleJson> call, @NonNull Throwable t) {
        Timber.e(t);
    }
});

apiService.postHologramSample(hologramResult, true).enqueue(new retrofit2.Callback<SampleJson>() {

    @Override
    public void onResponse(@NonNull Call<SampleJson> call, @NonNull Response<SampleJson> response) {
        String sampleId = response.body().getSampleId();
        Timber.i("sampleId: %s", sampleId);
    }

    @Override
    public void onFailure(@NonNull Call<SampleJson> call, @NonNull Throwable t) {
        Timber.e(t);
    }
});

apiService.postSelfieSample(selfieResult.getFilePath(), selfieResult.getSignature(), true).enqueue(new retrofit2.Callback<SampleJson>() {

    @Override
    public void onResponse(@NonNull Call<SampleJson> call, @NonNull Response<SampleJson> response) {
        String sampleId = response.body().getSampleId();
        Timber.i("sampleId: %s", sampleId);
    }

    @Override
    public void onFailure(@NonNull Call<SampleJson> call, @NonNull Throwable t) {
        Timber.e(t);
    }
});

apiService.postSelfieVideoSample(faceLivenessResult.getVideoFilePath(), faceLivenessResult.getSignature(), true).enqueue(new Callback<SampleJson>() {

    @Override
    public void onResponse(@NonNull Call<SampleJson> call, @NonNull Response<SampleJson> response) {
        String sampleId = response.body().getSampleId();
        Timber.i("sampleId: %s", sampleId);
    }

    @Override
    public void onFailure(@NonNull Call<SampleJson> call, @NonNull Throwable t) {
        Timber.e(t);
    }
});
```

To run OCR or investigation on more documents altogether (both sides of ID card and/or driving license and so on), you should keep safe `sampleId` of each POST call and then do:

```
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
```

You can find out more detail inside the sample app.

### Sample App

Don't forget to add the right package name into the backend system if you want to do end-to-end test. The build system adds .debug to the package name for debug builds.

- cz.trask.zenid.sample.debug for DEBUG builds
- cz.trask.zenid.sample for RELEASE builds

### Open Source

Zenid is powered by Open Source libraries.

 * AHEasing (WTFPL)
 * Catch2 (Boost Software License 1.0)
 * cppcodec (MIT License)
 * cvui (MIT License)
 * cxxopts (MIT License)
 * fmt (MIT License)
 * gmath (MIT License)
 * hedley (Creative Commons Zero v1.0 Universal)
 * imgui (MIT License)
 * implot (MIT License)
 * JSON for Modern C++ (Creative Commons Zero v1.0 Universal)
 * Magic Enum C++ (MIT License)
 * nameof (MIT License)
 * OpenCV  (Apache License 2.0)
 * PicoSHA2 (MIT License)
 * plusaes (Boost Software License 1.0)
 * Rapidcsv (BSD 3-Clause license)
 * Tensorflow Lite (Apache License 2.0)
 * TooJpeg (zlib License)
 * JMRTD (LGPL License)
 * CameraView (MIT License)
