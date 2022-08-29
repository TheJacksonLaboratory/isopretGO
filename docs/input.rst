.. _rstinput:

===================
Isopret input files
===================

isopret can be run with `HBA-DEALS`_ or `edgeR`_ results files as input.

HBA-DEALS
=========
.. _HBA-DEALS:


Hierarchical Bayesian Analysis of Differential Expression and ALternative Splicing (HBA-DEALS)
simultaneously characterizes differential expression and splicing in cohorts.


.. figure:: /img/hbadeals.png
   :width: 80%
   :align: center

   HBA-DEALS. The log-transformed expression of a gene with three isoforms (green, orange, and blue) is shown. The gene expression is the sum of the expression of the isoforms. Differential gene expression is modeled as two Normal distributions whose means differ by the parameter β. The proportions of the corresponding isoforms have a Dirichlet prior, and the difference in proportions between controls and cases is modeled by α (symbolized by the two triangles). An MCMC procedure is used to solve for the posterior distribution of the parameters of the model for all genes and isoforms at once.

The HBA-DEALS algorithm is explained in `Karlebach et al, 2020, Genome Biology 21:171 <https://genomebiology.biomedcentral.com/articles/10.1186/s13059-020-02072-6>`_.
Source code is available at the `HBA-DEALS GitHub repository <https://github.com/TheJacksonLaboratory/HBA-DEALS>`_.


Important
=========

For use with isopret, HBA-DEALS *must* be run with the ``isoform.level`` option set to true.
See the `HBADEALS documentation <https://hba-deals.readthedocs.io/en/latest/>`_
for details.




HBA-DEALS output format
^^^^^^^^^^^^^^^^^^^^^^^

HBA-DEALS produces an output file with the following format. A gene is shown
using its `Ensembl <http://ensembl.org/>`_ identifier. If the word ``Expression``
appears in the second column, then the row refers to the gene, otherwise it
refers to the indicated transcript (isoform). The third column refers to the
expression log fold change (if the row refers the a gene) or the fold change
(if the row refers to an isoform). The fourth column is the posterior error probability (PEP; see
`Käll et al. <https://pubs.acs.org/doi/10.1021/pr700739d>`_ for an explanation).


+-----------------+-----------------+-----------------+-----------------+
| Gene            |Isoform          | ExplogFC/FC     | P               |
|                 |                 |                 |                 |
+=================+=================+=================+=================+
| ENSG00000001629 | Expression      | 1.17010167106799| 0.19007         |
+-----------------+-----------------+-----------------+-----------------+
| ENSG00000001629 | ENST00000265742 | 1.17010167106799|     0.22928     |
+-----------------+-----------------+-----------------+-----------------+
| ENSG00000001629 | ENST00000422095 | 0.51809521525105|  0.04285        |
+-----------------+-----------------+-----------------+-----------------+
| ENSG00000001629 | ENST00000442183 | 1.04618237020161|  0.23606        |
+-----------------+-----------------+-----------------+-----------------+
| ENSG00000002586 | Expression      | 0.53692491414042|  0.05712        |
+-----------------+-----------------+-----------------+-----------------+
| ENSG00000002586 | ENST00000381177 | 1.63300567810385|  0.10156        |
+-----------------+-----------------+-----------------+-----------------+


Isopret supports
only `Ensembl <http://ensembl.org/>`_ gene/transcript models.




Running HBA-DEALS
^^^^^^^^^^^^^^^^^

A Snakemake pipeline for running HBA-DEALS is described in the `GitHub repository <https://github.com/TheJacksonLaboratory/covid19splicing>`_
for the paper  `Betacoronavirus-specific alternate splicing <https://pubmed.ncbi.nlm.nih.gov/35074468/>`_. The GitHub repository
also contains 15 HBA-DEALS output files related to that publicaton that can be used as input for Isopret-Gui.


edgeR
=====
.. _edgeR:
todo