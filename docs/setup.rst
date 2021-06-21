.. _rstsetup:

Setting up isopret
==================

isopret is a desktop Java application that requires several external files to run.


Prerequisites
~~~~~~~~~~~~~

isopret was written with Java version 11. If you want to
build isopret from source, then the build process described below requires
`Git <https://git-scm.com/book/en/v2>`_ and `maven <https://maven.apache.org/install.html>`_.


Installation
~~~~~~~~~~~~

Go the GitHub page of `isopret <https://github.com/TheJacksonLaboratory/isopret>`_, and clone or download the project.
Build the executable from source with maven, and then test the build. ::

    $ git clone https://github.com/TheJacksonLaboratory/isopret.git
    $ cd isopret
    $ mvn package
    $ java -jar target/isopret.jar
    $ Usage: isopret [-hV] [COMMAND]
        Isoform interpretation tool.
        -h, --help      Show this help message and exit.
        -V, --version   Print version information and exit.
      Commands:
        download, D  Download files for prositometry
        hbadeals, H  Analyze HBA-DEALS files
        svg, V       Create SVG/PDF files for a specific gene
        stats, S     Show descriptive statistics about data



isopret requires `maven <https://maven.apache.org/>`_ version 3.5.3.


Prebuilt isopret executable
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Alternatively, go to the `Releases section <https://github.com/TheJacksonLaboratory/isopret/releases>`_ on the
isopret GitHub page and download the latest precompiled version of isopret.



The download command
~~~~~~~~~~~~~~~~~~~~

.. _rstdownload:

isopret requires some additional files to run.

1. ``hp.obo``. The main Human Phenotype Ontology file
2. ``phenotype.hpoa`` The main annotation file with all HPO disease models
3. ``Homo_sapiens_gene_info.gz`` A file from NCBI Entrez Gene with information about human genes
4. ``mim2gene_medgen`` A file from the NCBI medgen project with OMIM-derived links between genes and diseases

isopret offers a convenience function to download all four files
to a local directory. By default, isopret will download all four files into a newly created subdirectory
called ``data`` in the current working directory. You can change this default with the ``-d`` or ``--data`` options
(If you change this, then you will need to pass the location of your directory to all other LIRICAL commands
using the ``-d`` flag). Download the files automatically as follows. ::

    $ java -jar isopret.jar download

isopret will not download the files if they are already present unless the ``--overwrite`` argument is passed. For
instance, the following command would download the four files to a directory called datafiles and would
overwrite any previously downloaded files. ::

    $ java -jar isopret.jar download -d datafiles --overwrite


If desired, you can download these files on your own but you need to place them all in the
same directory to run isopret.

