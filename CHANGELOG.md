# Changelog

### 1.3.0

- Support auto-rotate setting for document picture processing. DocumentPictureView doesn't require landscape mode anymore.

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
