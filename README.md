# isopret

Isoform Interpretation (isopret) is (will be) a tool to help interpret the potential biological
functions that are affected by differential alternative splicing.  Isopret is in a very early stage
right now...



## Building isopret
isopret was developed under Java 11. To build the app, clone this repository and
build the executable with maven.
```
git clone https://github.com/TheJacksonLaboratory/isopret
cd isopret
mvn package
```
This will create an executbale in the ``target`` subdirectory. Run the following command to make sure
the executable was created.
```
$ java -jar target/isopret.jar 
Usage: isopret [-hV] [COMMAND]
Isoform interpretation tool.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  biomart, B   Fetch data from biomart
  download, D  Download files for prositometry
  hbadeals, H  Analyze HBA-DEALS files
  stats, S     Show descriptive statistics about data
```


## Webservice

We are trying to get the tool to query the Ensembl biomart web service.
To run the code, enter this

```
$ java -jar target/isopret.jar biomart
```


## Download

isopret requires several files to run (NOTE--everything is being refactored, but this command works now). It will download these files and copy them
to a subdirectory called data with the ``download`` command. If desired, the paths
of the files can be adjusted, but by default prositometry will assume that all
required files are in the ``data`` directory.
```
java -jar target/isopret.jar download
```

## HBA-DEALS

After setting up the app and downloading the files, prositometry can be run with an HBA-DEALS output file that
includes transcript IDs.
```
java -jar target/isopret.jar hbadeals --hbadeals <path-to-hbadeals-file>
```
