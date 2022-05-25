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

This will create a deb file called ``isopret-gui_1.0.0-1_amd64.deb``. On some
systems, the file can be installed with a right-click. From the command line,
the following installs the package

```aidl
sudo dpkg -i isopret-gui_1.0.0-1_amd64.deb
```

This command will have the effect of installing the application in ``/opt/isopret-gui``.
The application will not be on the system path unless you add it manually, but you should be able to start the app via
the ``Show Applications`` or ``Main Menu`` apps. Alternatively, the app can be started
with
```aidl
 /opt/isopret-gui/bin/isopret-gui 
```
to remove the app, enter the following command
```aidl
sudo dpkg remove isopret-gui_1.0.0-1_amd64.deb
```

## Mac

The command
```aidl
bash package.sh
```
will generate a file called ``isopret-gui-1.0.0.dmg``. This file can
be used to install the package as usual (start with a double-click)

Note that the JAR files and the dmg installation files for M1 and intel macs are not
compatible with each other. We have uploaded one version of the
app for M1 Macs (``isopret-gui-1.0.0-M1.dmg``) and one for older (intel)
Macs (````isopret-gui-1.0.0-intel.dmg``). We produced the files on an M1 and an Intel mac and
changed the file names by hand.

## Windows

At present, we do not offer a Windows installation file, please use the JAR file as described in the README.