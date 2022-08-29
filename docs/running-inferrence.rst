.. _rstrunninginferrence:

=====================================
Running the isopret inferrence script
=====================================

Here, we explain how to set up and run the R scripts developed to perform expectation-maximization analysis to infer
GO annotations for isoforms.

The scripts are stored in the directory ``em-scripts`` in the project
`GitHub repository <https://github.com/TheJacksonLaboratory/isopret>`_.

The script ``translate_all_isoforms.R`` can be used for obtaining isoform amino-acid sequences.  It extracts isoform coordinates from a .gtf file and calls the tool gffread to translate the genomic sequence.  The variables containing the paths to the .fasta file containing the genome sequence and the .gtf file with isoform coordinates ('fasta.file' and 'gtf.file', respectively) should match the locations of the files in the system.

The script ``predicts2.R`` processes a subset of the isoforms to compute an assignment of GO terms that agrees with their sequence similarity.  It is currentlty set to process a subset that contains 1/200 (0.5%) of the isoforms (determined by the variable 'number.of.nodes').  The subset number, ranging between 1 and the number of subsets (currently 200), is provided as a command line argument.  The number of cores dedicated to each subset is determined by the parameter 'num.cores' (currently 4).  Each instance of this script can be run on a separate machine, since the output is written to disk and all outputs are combined by the script combine_tables.R.  Paths to files containing the GO annotation, HGNC mappings, interpro domains, interpro2GO mapping and gene and isoform annotations should be modified to match the location of the corrresponding files on the system.  Similarly, the path to the isoform amino-acid sequences should be modified accordingly (currently '/projects/robinson-lab/USERS/karleg/projects/isopret/isoform_seqs/').

The script ``combine_tables.R`` should be run after every instance of predict2.R has completed running and producing output.  It combined all the outputs, perform the M step if needed, and creates new inputs for the next round of predict2.R runs.  The patameter 'number.of.nodes' (currently 200) is the number of subsets the isoforms are split into.  When the local maximum is reached, it outputs -1 into the file 'convergence_log.txt'.  The sparse matrix 'combined_iso_has_func.txt' is an isoform X GO term Boolean matrix that indicates which functions were assigned to each isoforms.  The isoform names that correspond to rows are in the output file named 'rownames.txt' , and the GO terms corresponding to columns are in the output file named 'columns.txt'.

