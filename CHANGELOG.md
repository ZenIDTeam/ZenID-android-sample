# Changelog

### 1.1.1

- BREAKING CHANGE Separate sdk-selfie and sdk-faceliveness modules from the sdk-core module. You can save a lot of MBs if you don't use
face-liveness. See the docs. Migration should we very straightforward.
- New states ( DARK, CONFIRMING_FACE ) for the selfie module.
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
