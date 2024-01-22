# Tutorial

This tutorial demonstrates how to generate the output shown in parts of the manuscript.

### Setup the graphical user interface (GUI) version of isopretGO

Follow the [instructions](running-gui.md) to download and start isopretGUI. Use the *Download* button (1) to download
the required input files.

Two HBA-DEALS files are provided for this tutorial in the `tutorial_files` diredtorz of the GitHub repository:

- [Hooks_HBADEALS_output.txt](../tutorial_files/Hooks_HBADEALS_output.txt)
- [Wagner_HBADEALS_output.txt](../tutorial_files/Wagner_HBADEALS_output.txt)


Both files contain the results of analysis of hepatoblastoma datasets. 
[Hooks KB, et al. (2018) New insights into diagnosis and therapeutic options for proliferative hepatoblastoma. Hepatology. 68:89-102](https://pubmed.ncbi.nlm.nih.gov/29152775/)
present RNA sequencing of 25 hepatoblastomas and matched normal liver samples. 
[Wagner AE, et al. (2020) SP8 Promotes an Aggressive Phenotype in Hepatoblastoma via FGF8 Activation. Cancers (Basel) 12:2294](https://pubmed.ncbi.nlm.nih.gov/32824198/)
present RNA-sequencing of four primary hepatoblastomas with metastasis and seven primary hepatoblastomas without metastasis, 11 matching normal liver specimens and four liver tumor cell lines.


### HBA-DEALS input file

Select the file (2) and press the Analyse! button (3).

<figure markdown>
![Overview tab](./img/isopretSetup.png){ width="800" }
<figcaption>IsopretGO overview tab.
</figcaption>
</figure>

Leave the Gene Ontology (GO) settings in their default values. If desired, other GO overrepresentation algorithms or multiple-testing correction (MTC) procedures can be used. See [Bauer et al. (2008)](https://pubmed.ncbi.nlm.nih.gov/18511468/){:target="\_blank"}. for information and
[Introduction to Bio-Ontologies](https://www.amazon.de/-/en/Charite-Universitatsmedizin-Berlin-Germany-Robinson/dp/1439836655){:target="\_blank"}. for detailed explanations.


### Analysis summary
After you press the *Analyse!* button, isopretGO will perform the analysis, which should take between 15-60 seconds on typical laptops. A progress bar is shown. isopretGO will then open the **Analysis* tab, which shows a table with all genes measured in the RNA-seq experiment, ordered by posterior error probability (PEP; See [Käll et al. 2008](https://pubmed.ncbi.nlm.nih.gov/18052118/){:target="\_blank"} for a primer on PEP).

<figure markdown>
![Overview tab](./img/analysis_summary.png){ width="800" }
<figcaption>IsopretGO Analysis tab.
</figcaption>
</figure>

To search for a specific gene, enter the gene symbol in the search bar on the left right underneath the table in this view.
For instance, enter the symbol MICU1, which refers to the [Mitochondrial Calcium Uptake 1](https://www.genenames.org/data/gene-symbol-report/#!/hgnc_id/HGNC:1530){:target="\_blank"} gene.
