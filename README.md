## ZenID Android SDK - v1.1.5

Android sample app that shows how to use the ZenID Android SDK. The SDK can help you with performing the following operations on documents:

* OCR and data extraction
* verification of authenticity
* real-time face liveness detection
* Hologram verification

Identity cards, driving licenses and passports from Czechia and Slovakia are supported. 
Only landscape orientation is supported for document processing. 
On the other hand, only portrait orientation for selfie or face-liveness.

The SDK supports API level 22 and above.

Apps need to use a single NDK and STL for all native code and dependencies - [one STL per app](https://developer.android.com/ndk/guides/cpp-support#one_stl_per_app).
We use NDK 21.3.6528147 and STL c++_shared by default. If you already rely on an another native library, please do mutual compatibility check as soon as possible.

### Package sizes

|  Package name  |  Size (MB)  |  Note  |
|----------|:-------------:|------:|
|  sdk-core  |  18.4  |  can be reduced using abi-filters or abi-split  |
|  sdk-core-models-at  |  7.5  |  |
|  sdk-core-models-cz  |  2.1  |  |
|  sdk-core-models-de  |  2.6  |  |
|  sdk-core-models-pl  |  2.8  |  |
|  sdk-core-models-sk  |  4.4  |  |
|  sdk-faceliveness  |  34.5  |  |
|  sdk-selfie  |  0.1  |  |
|  sdk-api-zenid  | 0.05  |  |


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
    implementation 'com.otaliastudios:cameraview:2.6.1'
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
        .extraVerifiers(new SelfieVerifier(), new FaceLivenessVerifier())
        .build();

// Make the instance globally accessible.
ZenId.setSingletonInstance(zenId);

// This may take a few seconds. Please do it as soon as possible.
zenId.initialize();
```

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

### Overview of states

DocumentPictureState:
- NO_MATCH_FOUND = No document matching input parameters was found.
- ALIGN_CARD = Align card in front of your back camera.
- HOLD_STEADY = Hold the document steady for a while.
- BLURRY = The picture is too blurry.
- REFLECTION_PRESENT = The reflection is present on text.
- OK = The picture is ok.
- DARK = The picture is dark.

SelfieState:
- OK = The picture is ok.
- NO_FACE_FOUND = No face was found.
- BLURRY = The picture is too blurry.
- DARK = The picture is dark.
- CONFIRMING_FACE = Face was confirmed.
    
HologramState:
- NO_MATCH_FOUND = No document matching input parameters was found.
- TILT_LEFT = Tilt your phone left.
- TILT_RIGHT = Tilt your phone right.
- TILT_UP = Tilt your phone up.
- TILT_DOWN = Tilt your phone down.
- ROTATE_CLOCKWISE = Rotate the phone clockwise.
- ROTATE_COUNTER_CLOCKWISE = Rotate the phone counter clockwise.
- OK = Scanning done, the hologram is ok.

FaceLivenessState:
- LOOK_AT_ME = Look in the camera.
- TURN_HEAD = Slowly turn your head to LEFT and RIGHT.
- SMILE = Smile to the camera or move your mouth.
- DONE = The scanning is done.

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
