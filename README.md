# prositometry
prositometry -- a tool for searching for PROSITE Motifs in proteins encoded by sets of transcripts

## Building prositometry
prositometry was developed under Java 11. To build the app, clone this repository and
build the executable with maven.
```
git clone https://github.com/pnrobinson/prositometry.git
cd prositometry
mvn package
```
This will create an executbale in the ``target`` subdirectory. Run the following command to make sure
the executable was created.
```
java -jar target/prositometry.jar -h
```

## Setup

Prositometry requires several files to run. It will download these files and copy them
to a subdirectory called data with the ``download`` command. If desired, the paths
of the files can be adjusted, but by default prositometry will assume that all
required files are in the ``data`` directory.
```
java -jar target/prositometry.jar download
```

## Running

After setting up the app and downloading the files, prositometry can be run with an HBA-DEALS output file that
includes transcript IDs.
```
java -jar target/prositometry.jar hbadeals --hbadeals <path-to-hbadeals-file>
```