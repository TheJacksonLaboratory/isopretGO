# IsopretGO: Isoform interpretation

IsopretGO (Isoform Interpretation for Gene Ontology) leverages predictions of isoform-specific functions (i.e., Gene Ontology[GO] annotations)
made by the [isopret expectation maximization algorithm](https://pubmed.ncbi.nlm.nih.gov/36929917/){:target="\_blank"}.
to perform gene-level  and isoform-level GO overrepresentation analysis.

## Background: Gene Ontology
[Gene Ontology](http://geneontology.org/) traditionally has provided
annotations for genes rather than for specific isoforms. However, in
some cases, the functions of the individual isoforms of a gene are
differ with respect to one or more of the gene's function. In this project,
we have developed an algorithm for prediction of isoform-specific function
across the entire transcriptome. The isopret app offers a number of
ways to visualize and analyze RNA-seq datasets for Gene Ontology
functions that are overrepresented either among the differentially
expressed genes or the differentially spliced isoforms.

## Background: HBA-DEALS
Isopret requires as input a file that has the fold changes and p-values
for genes and isoforms in a case-control cohort that has been studied
by RNA-seq. We have previously published [HBA-DEALS](https://genomebiology.biomedcentral.com/articles/10.1186/s13059-020-02072-6),
a hierarchical Bayesian algorithm that performs such an analysis, and this
is the recommended input file.
Isopret uses the analysis (output) file of
[HBA-DEALS](https://pubmed.ncbi.nlm.nih.gov/32660516/){:target="_blank"}.

TODO -- point to tutorial amd also mention EDGER

HBA-DEALS analyzes RNA-Seq data to determine differentially expression and differential splicing simultaneous. Isopret then performs Gene Ontology analysis using an updated implementation of code from the [Ontologizer](https://pubmed.ncbi.nlm.nih.gov/18511468/){:target="_blank"}.


