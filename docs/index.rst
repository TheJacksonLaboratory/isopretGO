=================================
IsopretGO: Isoform interpretation
=================================

IsopretGO (Isoform Interpretation for Gene Ontology) leverages predictions of isoform-specific functions (i.e., Gene Ontology annotations)
made by the `isopret expectation maximization algorithm <https://www.biorxiv.org/content/10.1101/2022.05.13.491897v1>`_ to perform gene-level
and isoform-level `Gene Ontology.




Isopret uses the analysis (output) file of
`HBA-DEALS <https://pubmed.ncbi.nlm.nih.gov/32660516/>`_. HBA-DEALS
analyzes RNA-Seq data to determine differentially expression and differential
splicing simultaneous. Isopret then performs
Gene Ontology analysis using a Java 17 implementation of code from
the `Ontologizer <https://pubmed.ncbi.nlm.nih.gov/18511468/>`_.



.. toctree::
   :maxdepth: 1
   :caption: Contents:

   input
   examples
   running-inferrence
   running-go

