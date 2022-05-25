# isopret

Isoform Interpretation (isopret) is Java tool to help interpret the potential biological
functions that are affected by differential alternative splicing. Isopret is
available as a Java desktop application. Most users should download the latest version from the
Releases page.



## Background
[Gene Ontology](http://geneontology.org/) traditionally has provided
annotations for genes rather than for specific isoforms. However, in 
some cases, the functions of the individual isoforms of a gene are 
differ with respect to one or more of the gene's function. In this project,
we have developed an algorithm for prediction of isoform-specific function
across the entire transcriptome. The isopret app offers a number of
ways to visualize and analyze RNA-seq datasets for Gene Ontology
functions that are overrepresented either among the differentially
expressed genes or the differentially spliced isoforms.

## HBA-DEALS
Isopret requires as input a file that has the fold changes and p-values
for genes and isoforms in a case-control cohort that has been studied
by RNA-seq. We have previously publised [HBA-DEALS](https://genomebiology.biomedcentral.com/articles/10.1186/s13059-020-02072-6),
a hierarchical Bayesian algorithm that performs such an analysis, and this
is the recommended input file.

## Note to Macintosh users

Isopret requires Java 17 and makes use of [JavaFX](https://openjfx.io/) to implement the
graphical user interface (GUI). We have noticed that JavaFX GUI apps including isopret have crashed on
Macintosh laptops with the new ARM M1 chip when using Oracle's SDK (version 17.0.2). We
have used [Azul Zulu](https://www.azul.com/downloads/?package=jdk) JDKs on M1 Macintoshes and
could run Isopret and other JavaFX apps without problems.

We offer pre-built installation files for MacIntosh (M1 and Intel) in the Release section. 

## Note to Linux users

The easiest way to run isopret-gui on a linux system is to run the downloadable JAR file from the releases page.

```aidl
java -jar isopret-gui.jar
```

Additionally, an installation file is provided as explained in [PACKAGE](PACKAGE.md).


### Further information

Please see the [ReadTheDocs](https://isopret.readthedocs.io/en/latest/).
A manuscript is in preparation.




