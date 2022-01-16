# isopret

Isoform Interpretation (isopret) is (will be) a tool to help interpret the potential biological
functions that are affected by differential alternative splicing.  Isopret is in a very early stage
right now...

## Building isopret
isopret was developed under Java 17, which is required to build the app from source. To do so, clone this repository and
build the executable with maven.



Isopret can then be built using standard maven:
```
git clone https://github.com/TheJacksonLaboratory/isopret
cd isopret
mvn package
```
This will create an executable app
```
java -jar isopret-gui/target/Isopret.jar
```

Note that for now the Download button does not import all of the files we require for analysis. To
run the isopret GUI, Use the download button, choose a directory to odownload files, and copy in (or ln -s)
the three additional files, interpro_domain_desc.txt, interpro_domains.txt, and isoform_function_list.txt.
After this, you can use the HBA-DEALS button to choose the output file of HBA-DEALS for this analysis.
Note that the genes must be in ENSEMBL (ENSG) notation.

# Running isopret
Note that for now we need to run the download command once (from the GUI or the CLI) and then
add the following files to the data directory (with the other downlaoded files):

- isoform_function_list.txt
- interpro_domain_desc.txt
- interpro_domains.txt

## Note
Some of the commands in the CLI section will not work currently because I have
refactored a ton of stuff for the GUI. We should decide exactly what we want
the GUI and the CLI to do.

Note that probably we will rework the command-line version of isopret to just output tab-separated files and not HTML.
The following documentation is not guaranteed to be up-to-date.

## Download

The command-line version of isopret requires several files to run (NOTE--everything is being refactored, but this command works now). It will download these files and copy them
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
Usage: isopret hbadeals [-hV] [-a=<goGafFile>] -b=<hbadealsFile>
                        [-f=<fastaFile>] [-g=<goOboFile>] [-j=<jannovarPath>]
                        [-p=<prositeDataFile>] [--prefix=<outprefix>]
                        --prositemap=<prositeMapFile>
Analyze HBA-DEALS files
  -a, --gaf=<goGafFile>      goa_human.gaf.gz file
  -b, --hbadeals=<hbadealsFile>
                             HBA-DEALS output file
  -g, --go=<goOboFile>       go.obo file
  -h, --help                 Show this help message and exit.
  -j, --jannovar=<jannovarPath>
                             Path to Jannovar transcript file
  -p, --prosite=<prositeDataFile>
                             prosite.dat file
      --prefix=<outprefix>   Name of output file (without .html ending)
      --prositemap=<prositeMapFile>
                             prosite mape file
  -V, --version              Print version information and exit.
```

To get these files note the following

1. HBADEALS output -- please use with format that includes the ENSEMBL id in the first column (gene accession number).


### Documentation

To generate the HTML (ReadTheDocs documentation), enter the following

```bazaar
cd docs
virtualenv p3
source p3/bin/activate
pip install sphinx
pip install sphinx_rtd_theme
make html
```
This will generate a readthedocs site in the subdirectory docs/_build/html.
