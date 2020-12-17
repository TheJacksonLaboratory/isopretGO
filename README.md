# isopret

Isoform Interpretation (isopret) is (will be) a tool to help interpret the potential biological
functions that are affected by differential alternative splicing.  Isopret is in a very early stage
right now...

## Prerequisites

### variant-api
We will use the Variant-api from Exomiser to work with the transcript data
Please install the library locally (later, it will go into maven central)
```
git clone https://github.com/exomiser/variant-api
cd variant-api
git fetch origin
git checkout -b coordinate-systemed-region origin/coordinate-systemed-region
mvn install
```

### Jannovar
We will require the [Jannovar](https://github.com/charite/jannovar) transcript file for hg38.
The ``download`` command downloads hg38_refseq_curated.ser 
from [Zenodo](https://zenodo.org/record/4311513). If desired, you can create your own 
transcript file as follows (Note: there are some difficulties with Jannovar right now,
so it is probably easier to add a number of transcript files to Jannovar):


```
git clone https://github.com/charite/jannovar
cd jannovar
mvn package
java -jar jannovar-cli/target/jannovar-cli-0.35-SNAPSHOT.jar download -d hg38/ensembl
```

### phenol
We have updated some functions in phenol to perform GO analysis. We will soon release to maven central
but for now we need to install phenol locally
```
git clone https://github.com/monarch-initiative/phenol.git
cd phenol
git fetch
git checkout develop
mvn install
```

This will install phenol-1.6.2-SNAPSHOT to the local .m2 maven repository.


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
includes transcript IDs. Additional options are available (TODO).
```
java -jar target/isopret.jar hbadeals 
-b
<path to HBADEALS output file>
--prositemap
all_prosite_motifs.txt
```

To get these files not the following

1. HBADEALS output -- please use with format that includes the ENSEMBL id in the first column (gene accession number).
2. hg38_ensembl.ser -- this is the [Jannovar](https://github.com/charite/jannovar) transcript file.
