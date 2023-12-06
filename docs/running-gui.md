# GUI version


Isopret-gui is a Java application for investigating and visualizing overrepresentation of
[Gene Ontology (GO)](http://geneontology.org/) annotations in differentially spliced or
differentially expressed genes. Only the graphical user interface (GUI) version of isopretGO offers the full
functionality.

### Starting isopretGO for the first time: Downloading input files


IsopretGO can be started with a double click (assuming Java 17 is installed on your computer) or from
the command line as

``` shell title="Starting isopretGO from the command line"
java -jar Isopret.jar
```


<figure markdown>
![First run](./img/isopret1.png){ width="1000" }
<figcaption>Appearance of the app when started for the first time.</figcaption>
</figure>



You should now click on the ``Download`` button and choose a directory to which to download the data files required
by isopret to run (these files include the inferred isoformGO function files of the project as well as other files such
as the Gene Ontology json file). This step only needs to be performed once.

### Choosing the HBA-DEALS file


It is assumed you will have run HBA-DEALS on RNA-seq files of interest prior to running Isopret. Choose the output
file of HBA-DEALS.


### Gene Ontology Settings


isopretGO offers three Gene Ontology (GO) overrepresentation algorithms. The ``Term-for-Term`` method is the
standard procedure for assessing whether genes annotated to a specific GO term are more common in the set of
differentially expressed genes than one would expect given the proportion of all genes that are annotated to the
term. Formally, it is the upper tail of a hypergeometric distribution, which is also known as the one-tailed Fisher's exact test
([Bauer et al., 2008](https://academic.oup.com/bioinformatics/article/24/14/1650/182451?login=false)).

The drawback of the term-for-term approach is that it does not respect dependencies between the GO terms that
are caused by overlapping annotations. As a result of the true-path rule, each term in GO shares all the
annotations of all of its descendants. Isopret also offers two algorithms for GO analysis that
assess GO term overrepresentation that examines each term in the context of its parent terms,
which we call the parent–child approach
([Grossmann et al., 2007](https://academic.oup.com/bioinformatics/article/23/22/3024/208216?login=false)).
In our experiments, the ``parent–child-intersection`` approach is generally more conservative than
the ``parent–child-union`` approach.

### Multiple testing correction


Isopret offers the following multiple-testing correction options for the GO analysis: Bonferroni, Bonferroni-Holm,
Sidak, Benjamini-Hochberg, Benjamini-Yukutieli, None. The book
[Introduction to Bio-Ontologies](https://www.routledge.com/Introduction-to-Bio-Ontologies/Robinson-Bauer/p/book/9780367659271)
provides detailed explanations of the GO Overrepresentation analysis procedures and multiple testing correction approaches.

<figure markdown>
![First run](/img/isopret2.png){ width="1000" }
<figcaption>Appearance of the app after data download with an HBA-DEAL file chosen and the analysis set to
Parent-Child Intersection with Benjamini-Hochberg MTC.</figcaption>
</figure>



## Running isopretGO


Finally, click the ``Analyse`` button to start the analysis. The tool will typically require less than 5 minutes to complete
on a typical laptop or consumer desktop. Users can follow progress with a progress bar. If analysis is slow, consider
starting Isopret with additional memory.

```bash title="starting isopret with additional memory"
java -Xmx 8g -jar Isopret.jar
```


When the analysis finished, two new tabs will appear, DGE (differental gene expression)
and DAS (differential alternative splicing). See [output](output.md) for instructions on how to interpret the results.