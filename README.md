## ZenID Android SDK

A simple sample app that shows how to use the ZenID Android SDK. The SDK can help you with:

* verification of your documents (Identity card, Driving license, Passport)
* real-time face liveness detection

In order to start integration, you will need the **API key** and **URL** of the backend system.

### Instalation

* Put **zenid-sdk-release.aar** in the libs folder of the app
* Edit the build.gradle file of your app and add
```groovy
ext {
    okHttpVersion = '3.13.0'
    retrofitVersion = '2.5.0'
}

dependencies {
    implementation fileTree(include: ['*.aar'], dir: 'libs')
    implementation 'androidx.appcompat:appcompat:1.0.2'
    implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
    implementation 'androidx.fragment:fragment:1.0.0'
    implementation "com.squareup.okhttp3:okhttp:$okHttpVersion"
    implementation "com.squareup.okhttp3:logging-interceptor:$okHttpVersion"
    implementation "com.squareup.retrofit2:retrofit:$retrofitVersion"
    implementation "com.squareup.retrofit2:converter-gson:$retrofitVersion"
    implementation 'com.jakewharton.timber:timber:4.7.1'
    implementation 'pub.devrel:easypermissions:3.0.0'
    implementation 'com.otaliastudios:cameraview:2.1.0'
}
```
We plan to setup our own maven repository but so far only this option is available
* Rebuild the project

### Multi-APK split

C++ code needs to be compiled for each of the CPU architectures (known as "ABIs") present on the Android environment. Currently, the SDK supports the following ABIs:

* `armeabi-v7a`: Version 7 or higher of the ARM processor. Most recent Android phones use this
* `arm64-v8a`: 64-bit ARM processors. Found on new generation devices

The SDK binary contains a copy of the native `.so` file for each of these four platforms.
You can considerably reduce the size of your `.apk` by applying APK split by ABI, editing your `build.gradle` as the following:

```groovy
android {

  splits {
    abi {
        enable true
        reset()
        include 'arm64-v8a', 'armeabi-v7a'
        universalApk false
    }
  }
}
```

More information on the [Android documentation](http://tools.android.com/tech-docs/new-build-system/user-guide/apk-splits)

### Initialization

To use the SDK, you need to obtain an instance of the RemoteConfig and pass your **URL** and **API key**:

```
RemoteConfig remoteConfig = new RemoteConfig.Builder()
        .baseUrl("http://your.frauds.zenid.cz/api/") 
        .apiKey("your_api_key")
        .build();
```
also you need an instance of the okHttpClient:

```
OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
        // .addInterceptor() if you want to add a logging interceptor or such cases
        .build();
```
and then initialize the client:
```
ZenId zenId = new ZenId.Builder()
        .applicationContext(getApplicationContext())
        .remoteConfig(remoteConfig)
        .okHttpClient(okHttpClient)
        .build();

// Make the client globally accessible.
ZenId.setSingletonInstance(zenId);

// This may take a few seconds. Please do it as soon as possible.
zenId.initialize();
```
 
### Document verification flow

To begin with the document verifier just start the MatcherActivity with expected role, page and country. 
```
ZenId.get().startMatcherActivity(MyActivity.this, DocumentRole.ID, DocumentPage.FRONT_SIDE, DocumentCountry.CZ);
```

### Face liveness flow

To begin with the real-time face liveness check just start the FaceLivenessDetectorActivity. 
```
ZenId.get().startFaceLivenessDetectorActivity(MyActivity.this);
```

### Results

Set the `ZenId.Callback` to receive results from both flows.
```
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
```

### Sample project

You can find a simple example how to use the ZenID Android SDK attached. Please have a look and launch it.