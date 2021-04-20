# Changelog

### 1.4.0

- BREAKING CHANGE! ZenId.Builder refactoring. We have moved each verifier into its own module. The goal under the hood is to declare what we consider as a library-private API and what can be changed without warning. 
  Everything inside `internal` package is not a published API and you should not be very careful when using it.

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
