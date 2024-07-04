# Developers


## Installable version of isopretGO (debian linux)

To create an installable version of isopretGO for debian (e.g. Ubuntu) linux, run the following script (on a debian system).

```
bash package.sh
```
This will create a file called ``isopret-gui_1.0.0_amd64.deb``.

We can install the file using this command (you may need to prepend sudo)

```
dpkg -i isopret-gui_1.0.0_amd64.deb
```

This will install an executable that can be started using this command


```
/opt/isopret-gui/bin/isopret-gui 
```

If desired, the executable file can be moved elsewhere. We have uploaded the  ``isopret-gui_1.0.0_amd64.deb`` file to the isopret-GO releases page.

## Installable version ARM MacIntosh

isopret-GO runs under Mac M1, M2, and M3. Older (intel-based) Mac systems are not supported at this time. To create an installer,
run the same command as above.

```
bash package.sh
```

On a Mac, this command will create a file called  ``isopret-gui-1.0.0.dmg``. Double clicking this file will install a version of isopret-gui in the Applications folder.
We have uploaded this file to the releases page.