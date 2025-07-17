> [!WARNING]
> **The following versions contain a bug where a `libnative-lib.so` library in the builds was not compiled with 16KB page support.** <br/>
> **This means those versions are not ready to be used with Android OSes running in 16KB page size mode.**
>  - 4.8.6 to 4.8.13 for 4.8.x branch
>  - 4.9.6 to 4.9.14 for 4.9.x branch
> 
> The affected versions are tagged with `[WARNING NOT 16KB-ready]` in the release list.<br/>
> *(Versions prior to 4.8.6 were never advertised as having 16KB page size support and so aren't tagged)*

<hr />

> [!IMPORTANT]
> **For up-to-date documentation, visit the online manual directly in the ZenID web application.**

## ZenID Android SDK

Android sample app that shows how to use the ZenID Android SDK. The SDK can help you with performing the following operations on documents:

* OCR and data extraction
* verification of authenticity
* real-time face liveness detection
* Hologram verification

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
