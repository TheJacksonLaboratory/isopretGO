IsopretGO: Isoform interpretation

IsopretGO (Isoform Interpretation for Gene Ontology) leverages predictions of isoform-specific functions (i.e., Gene Ontology annotations)
made by the [isopret expectation maximization algorithm](https://pubmed.ncbi.nlm.nih.gov/36929917/){:target="_blank"}
to perform gene-level  and isoform-level `Gene Ontology.




Isopret uses the analysis (output) file of
[HBA-DEALS](https://pubmed.ncbi.nlm.nih.gov/32660516/){:target="_blank"}. 

TODO -- point to tutorial amd also mention EDGER

HBA-DEALS
analyzes RNA-Seq data to determine differentially expression and differential
splicing simultaneous. Isopret then performs
Gene Ontology analysis using a Java 17 implementation of code from
the [Ontologizer](https://pubmed.ncbi.nlm.nih.gov/18511468/){:target="_blank"}.


