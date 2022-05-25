# Package Isopret as a self-contained Java application

This file explains how we have packaged Isopret as a standalone application.
This is an alternative to the traditional method for creating executable jar files for
Java applications (this is also possible using the standard ``mvn package`` command).

The packaging must be done on the target platform. For instance, to create the native app for Linux,
the packaging must be done on Linux, using the Linux JDK.


## Linux

Run the following to create a deb installer (for Debian, Ubuntu, and related systems).

```aidl
bash package.sh
```