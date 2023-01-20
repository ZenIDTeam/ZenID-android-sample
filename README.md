## ZenID Android SDK

Android sample app that shows how to use the ZenID Android SDK. The SDK can help you with performing the following operations on documents:

* OCR and data extraction
* verification of authenticity
* real-time face liveness detection
* Hologram verification

The SDK supports API level 21 and above.

Apps need to use a single NDK and STL for all native code and dependencies - [one STL per app](https://developer.android.com/ndk/guides/cpp-support#one_stl_per_app).
We use NDK 21.3.6528147 and STL c++_shared by default. If you already rely on an another native library, please do mutual compatibility check as soon as possible.

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
    okHttpVersion = '3.14.4'
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

#### Init callback

You can use ZenId.InitCallback to be notified when ZENID SDK is ready for use. Initialization itself might take a few seconds. It all depends on count of models (documents).

```
zenId.initialize(new ZenId.InitCallback() {

    @Override
    public void onInitialized() {
        // Initialized
    }
});
```

Please, check out our samples and you will get the whole picture on how it works.

### Authorization

Starting from 0.21.5, mobile apps need to be authorized by the backend. Here is how you can do it.

Before you start, make sure "Android Package Name" are filled in the settings page of the website.

* Step 1. Get the challenge token.
* Step 2. Get the response token by calling /api/initSdk in the backend.
* Step 3. Call authorize using the response token obtained in the previous step. It will return a bool representing success or failure.

These steps need to be performed before any operation on documents, otherwise you will get a RecogLibCException with "Security Error" in the message.

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
```

If you want to speed up authorization process during development, just ask us. We can provide offline response tokens to you. But only for development purposes. Leak into production would undermine security!

### Architectural overview of the SDK

Every each use-case has its own view class:
  - cz.trask.zenid.sdk.DocumentPictureView for document picture verification
  - cz.trask.zenid.sdk.HologramView for the hologram verification
  - cz.trask.zenid.sdk.faceliveness.FaceLivenessView for the real-time face liveness check 
  - cz.trask.zenid.sdk.selfie.SelfieView to take a good selfie picture

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
    public void onPictureTaken(DocumentResult result) {
        Timber.i("result: %s", result);
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

HologramState:
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
- LOOK_AT_ME =  Turn head towards camera.
- TURN_HEAD = Turn head slowly towards arrow.
- SMILE = Smile to the camera or move your mouth.
- DONE = The scanning is done.
- BLURRY = The picture is too blurry.
- DARK = The picture is dark.
- HOLD_STILL = Hold still.

### Document picture feature

#### Document picture settings

```
DocumentPictureSettings documentPictureSettings = new DocumentPictureSettings.Builder()
        .specularAcceptableScore(15)
        .enableAimingCircle(true)
        .readBarcode(true)
        .build();

documentPictureView.setDocumentPictureSettings(documentPictureSettings);
```

```
// Toggles displaying an aiming circle while searching for cards.
Boolean enableAimingCircle;

// Toggles displaying timer that shows seconds remaining for the validators to become max tolerant.
Boolean showTimer;

// Can be used for fine tuning the sensitivity of the specular validator. Value range 0-100. Default value is 50.
Integer specularAcceptableScore;

// Can be used for fine tuning the sensitivity of the document blur validator. Value range 0-100. Default value is 50.
Integer documentBlurAcceptableScore;

// The time delay for the blur validator to become max tolerant. Default value is 10.
Integer timeToBlurMaxToleranceInSeconds;

// The card outline will be drawn on the video preview if this is true. Default: true
Boolean drawOutline;

// Setting it to false can be used to disable the barcode check. Default: false
Boolean readBarcode;
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

### Hologram feature

#### Hologram settings

```
HologramSettings hologramSettings = new HologramSettings.Builder()
        .enableAimingCircle(true)
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
|  name  |  Name of the step. It can be "CenteredCheck", "AngleCheck Left", "AngleCheck Right", "AngleCheck Up", "AngleCheck Down", "LegacyAngleCheck", or "SmileCheck". CenteredCheck requires the user to look at the camera. The AngleCheck steps require the user to turn their head in a specific direction. The LegacyAngleCheck requires the user to turn his head in any direction. It's only used when legacy mode is enabled. SmileCheck requires the user to smile.  |
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

```
VideoSettings videoSettings = new VideoSettings.Builder()
        .useVideoParamsFromBackend(true) // This will override other video settings
        .videoFrameRate()
        .videoMaxHeight()
        .videoMaxWidth()
        .videoFrameRateExact() // If set this option to true, it will give as exact preview fps as you want, but the sensor will have less freedom when adapting the exposure to the environment, which may lead to dark preview.
        .build();

faceLivenessView.setVideoSettings(videoSettings);
```

Please note that `videoFrameRate` parameter also set the frame rate for the video preview. Double check the value to ensure good user experience.

### SDK Signature

The SDK now generates a signature for the snapshots it takes. The backend uses the signature to verify picture origin and integrity.
    
The SDK returns signature within `DocumentPictureResult / SelfieResult / FaceLivenessResult` objects. Manual snapshots don't have signatures since they bypass SDK checks. 
    
You can then send the signature to the backend with the /api/sample request.     

### More details on the sdk-api-zenid module

To run optical character recognition (OCR) and investigate documents (please follow the link at http://your.frauds.zenid.cz/Sensitivity/Validators to get more details what investigation is about), you need to connect to our backend systems. To do so, you need to use our `ApiService` and and make appropriate calls to upload documents, for instance:
```
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