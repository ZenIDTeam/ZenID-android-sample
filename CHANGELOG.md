# Changelog

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
