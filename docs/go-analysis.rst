.. _rstgoanalysis:

===========
GO Analysis
===========

This page explains the isopret approach to performing GO overrepresentation analysis
for both gene expression and isoforms as implemented in the class ``GoOverrepCommand``.

1. Load the Gene Ontology
^^^^^^^^^^^^^^^^^^^^^^^^^

.. code-block:: java

   Ontology geneOntology = loadGeneOntology();

This provides us the standard `phenol <https://github.com/monarch-initiative/phenol>`_ Ontology object.

2. HGNC
^^^^^^^

The following code imports data from the Human Gene Nomenclature Committee (HGNC).

.. code-block:: java

    Map<AccessionNumber, HgncItem> hgncMap = loadHgncMap();

The keys to this map are AccessionNumber objects representing ensembl IDs such as ENSG00000056586, and
the values are HgncItem objects with information such as gene symbol, ucsc, refseq id, and the gene name.

3. Gene ID to transcript map
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

This function uses Jannovar to create a map from the gene id (ensembl) to the corresponding transcript
objects. The gene ids match those of the HGNC map.

.. code-block:: java

   Map<AccessionNumber, List<Transcript>> geneIdToTranscriptMap = loadJannovarGeneIdToTranscriptMap();


4. Gene ID to GO Terms map
^^^^^^^^^^^^^^^^^^^^^^^^^^

This function parses the ``isoform_function_list.txt`` file that results from inference of isoform-specific
functions.

.. code-block:: java

   Map<TermId, Set<TermId>> transcriptIdToGoTermsMap = loadTranscriptIdToGoTermsMap();

We expect this to yielf over 80 thousand entries, each of which is keyed by a TermId with
an Ensembl transcript ID (Note -- it is probably better to adapt phenol to allow StudySets to
take a generic parameter. Now they need to take TermIds, and so this application is representing
Ensembl accessions numbers both as AccessionNumber and TermId objects. This is clearly evil and
needs to be refactored.

5. Transcript To GeneId Map
^^^^^^^^^^^^^^^^^^^^^^^^^^^
This is the inverse of the geneIdToTranscriptMap. Note there is one GeneID for each transcript ID so
that this is a simple map.

.. code-block:: java

   Map<TermId, TermId> transcriptToGeneIdMap = createTranscriptToGeneIdMap(geneIdToTranscriptMap);


6. gene To Go Map
^^^^^^^^^^^^^^^^^

This function creates the counterpart of the ``transcriptIdToGoTermsMap`` map but for genes.

.. code-block:: java

   Map<TermId, Set<TermId>> gene2GoMap = fxnparser.getGeneIdToGoTermsMap(transcriptToGeneIdMap);