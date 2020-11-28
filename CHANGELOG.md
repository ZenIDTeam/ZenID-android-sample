# Changelog

### 1.1.1

- BREAKING CHANGE Make selfie and faceliveness optional modules. You can save some MBs if you don't include unused modules. See the docs. 
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
