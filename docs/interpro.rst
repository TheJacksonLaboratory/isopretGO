.. _rstinterpro:

Interpro setup
==============

isopret uses the `Interpro <https://www.ebi.ac.uk/interpro/>`_ tool
`InterProScan <https://interproscan-docs.readthedocs.io/en/latest/index.html>`_ tp mark up sequences with the
corresponding protein motifs and domains. By default, isopret uses ENSEMBL transcript definitions. Users can
create interpro-map files for other species or other sets of transcripts as follows. The ENSEMBL transcript definition
map is made available in the isopret GitHub release section.


Download
~~~~~~~~

Download the latest version of InterProScan from the `InterPro download page <http://www.ebi.ac.uk/interpro/download/>`_.
We used ``interproscan-5.50-84.0-64-bit.tar.gz``. Note that InterProScan is only supported on linux systems. Follow
the instructions to setup and test your installation. In brief,

.. code-block:: bash
  :linenos:

  cd interproscan-5.50-84.0/
  ./interproscan.sh
  python3 initial_setup.py # not needed if above line completes
  ./interproscan.sh -i test_all_appl.fasta tsv -dp  # does an analysis

If all has worked, the last command creates a file called ``test_all_appl.fasta.tsv``.

::

    UPI0004FABBC5 92e4b89dd86f8ab828f57121f6d7d460  257 Pfam  PF00243 Nerve growth factor family  145 254 7.7E-52 T 19-02-2021  IPR002072 Nerve growth factor-related
    UPI0004FABBC5 92e4b89dd86f8ab828f57121f6d7d460  257 SUPERFAMILY SSF57501  Cystine-knot cytokines  137 253 3.76E-51  T 19-02-2021  IPR029034 Cystine-knot cytokine
    UPI0004FABBC5 92e4b89dd86f8ab828f57121f6d7d460  257 PANTHER PTHR11589 NERVE GROWTH FACTOR NGF -RELATED  1 257 5.6E-150  T   19-02-2021  IPR020408 Nerve growth factor-like


The TSV format presents the match data in columns as follows:

1.  Protein accession (e.g. P51587)
2.  Sequence MD5 digest (e.g. 14086411a2cdf1c4cba63020e1622579)
3.  Sequence length (e.g. 3418)
4.  Analysis (e.g. Pfam / PRINTS / Gene3D)
5.  Signature accession (e.g. PF09103 / G3DSA:2.40.50.140)
6.  Signature description (e.g. BRCA2 repeat profile)
7.  Start location
8.  Stop location
9.  Score - is the e-value (or score) of the match reported by member
    database method (e.g. 3.1E-52)
10. Status - is the status of the match (T: true)
11. Date - is the date of the run
12. InterPro annotations - accession (e.g. IPR002093)
13. InterPro annotations - description (e.g. BRCA2 repeat)
14. (GO annotations (e.g. GO:0005515) - optional column; only displayed
    if --goterms option is switched on)
15. (Pathways annotations (e.g. REACT\_71) - optional column; only
    displayed if --pathways option is switched on)

If a value is missing in a column, for example, the match has no InterPro annotation, a '-' is displayed.


Nucleic acid sequences
~~~~~~~~~~~~~~~~~~~~~~
Run interpro scan with nucleic acid sequences as follows.

.. code-block:: bash
  :linenos:

  ./interproscan.sh -t n -i /path/to/nucleic_acid_sequences.fasta