.. _rstrunning:

===============
Running isopret
===============

Isopret offers two main commands (in addition to the download command).


stats
^^^^^

This command outputs simple descriptive statistics about the input data.



.. code-block:: java
  :linenos:

  $ java -jar target/isopret.jar stats
    ### Isopret: Input data  ###
    ### Gene Ontology:
	    Version: http://purl.obolibrary.org/obo/go/releases/2021-10-26/go.owl
	    Terms n=43833
    ### GO associations:
	    Annotations n=609748
	    Number of GO terms used for annotations n=18527
	    Number of annotated genes n=19788
    ### Human Gene Nomenclature Committee:
	    HGNC Map n=39711
    ### Jannovar Transcript Map:
	Genes n=34898
	Isoforms n=164776
	Mean isoforms per gene 4.7
    ### Interpro Annotations:
    ----- Domains ------------
	PTM: 17.
	FAMILY: 22618.
	HOMOLOGOUS_SUPERFAMILY: 3326.
	REPEAT: 322.
	CONSERVED_SITE: 692.
	ACTIVE_SITE: 132.
	BINDING_SITE: 76.
	DOMAIN: 11162.
    ----- Annotations ------------
	Total annotated transcripts: 19328
	Total annotations: 694207

hbadeals
^^^^^^^^

isopret is designed to use the output files of the RNA-seq analysis program
`HBA-DEALS <https://pubmed.ncbi.nlm.nih.gov/32660516/>`_
(See also the  `HBA-DEALS GitHib repository <https://github.com/TheJacksonLaboratory/HBA-DEALS>`_).
HBA-DEALS identifies differentially spliced and/or expressed genes from RNA-seq data. isopret starts from the
HBA-DEALS output file representing the analysis of a cohort or experiment of interest and identified
`Gene Ontology (GO) <http://geneontology.org/>`_ terms that are overrepresented in the differentially spliced
and in the differentially expressed genes. isopret additionally visualizes the transcript structure of
differential genes, the corresponding protein domains, and summarizes GO annotations.


.. code-block:: java
  :linenos:

  $ java -jar target/isopret.jar hbadeals --hbadeals <inputfile>


This will produce one or multiple HTML files.

TODO -- finish this after we have finalized the algorithms!