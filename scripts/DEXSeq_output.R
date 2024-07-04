library(DEXSeq)
library(dplyr)
library(Seurat)
library(stringr)

testDifferentialExpression = function(seurat_obj, cellType, transcript2gene, sampleData, outdir){
    
    ###################### Filter & process (single-cell) expression data ######################
    ##subset by cell type
    cellTypeSubset <- seurat_obj %>% subset(celltype==cellType)

    ##aggregate counts per sample on gene- and transcript-level 
    counts_geneExp <- AggregateExpression(cellTypeSubset, group.by = 'orig.ident', assay = 'RNA')$RNA #gene-level
    counts_transExp <- AggregateExpression(cellTypeSubset, group.by = 'orig.ident', assay = 'Transcript')$Transcript #transcript-level
    counts_geneExp <- counts_geneExp[,rownames(sampleData)]
    counts_transExp <- counts_transExp[,rownames(sampleData)]

    ##get transcript-gene assignment and convert to Ensembl IDs
    rownames(counts_geneExp) <- transcript2gene[match(rownames(counts_geneExp), transcript2gene$gene_name),"gene_id"]

    ##filter transcripts based on expression and adapt transcript2gene
    keep <- rowSums(counts_transExp >= 5) >= 2
    counts_transExp <- counts_transExp[keep,]
    transcript2gene <- transcript2gene[match(rownames(counts_transExp),transcript2gene$transcript_id),]

    ##include only genes with more than 1 transcript for DTU
    transcript2gene <- transcript2gene %>% group_by(gene_id) %>% mutate(NrTranscripts = n_distinct(transcript_id)) %>% subset(NrTranscripts>1)
    counts_transExp <- counts_transExp[transcript2gene$transcript_id,]

    ############################# run DESeq on gene-level expression ###########################
    dds <- DESeqDataSetFromMatrix(countData = counts_geneExp,
                                colData = sampleData,
                                design = ~ condition)
                                                        
    dds <- DESeq(dds)

    ########################### run DEXSeq on transcript-level expression ######################         

    ##create DEXSeq object
    dxd <- DEXSeqDataSet(countData = as.matrix(counts_transExp), sampleData = sampleData,
                                    design = ~sample + exon + condition:exon, featureID = transcript2gene$transcript_id,
                                            groupID = transcript2gene$gene_id)

    ## run DEXSeq analysis, parallelization possible if subsets are large enough
    BPPARAM = MulticoreParam(32)
    message('estimating size factors..')
    dxd = estimateSizeFactors(dxd)
    message('estimating dispersion..')
    dxd = estimateDispersions(dxd)#, BPPARAM=BPPARAM)
    message('testing for differential exon usage..')
    dxd = testForDEU(dxd)#, BPPARAM=BPPARAM)
    message('estimating exon fold changes..')
    dxd = estimateExonFoldChanges(dxd, fitExpToVar="condition")#,BPPARAM=BPPARAM,  )                          

    ################################ convert DGE and DTU results ##############################

    results_gene <- data.frame(results(dds))
    results_gene$Gene <- rownames(results_gene)

    results_gene <- results_gene %>% mutate(Isoform = 'Expression') %>% 
                                    select(Gene, Isoform, log2FoldChange, padj) %>%
                                    rename('ExplogFC/FC' = log2FoldChange, BH = padj)

    results_transcript <- DEXSeqResults(dxd) %>% as.data.frame() %>% select(!genomicData)

    results_trans <- results_transcript %>% 
                        mutate(fold_COVID_Control = 2^log2fold_COVID_Control) %>%
                        select(groupID, featureID, fold_COVID_Control, padj) %>%
                        rename(Gene = groupID, Isoform = featureID, 'ExplogFC/FC' = fold_COVID_Control, BH = padj)

    results_combined <- rbind(results_gene, results_trans) %>% arrange(Gene)

    results_combined$`ExplogFC/FC` <- as.numeric(results_combined$`ExplogFC/FC`)
    results_combined$BH <- as.numeric(results_combined$BH)

    write.table(results_combined, paste0(outdir, "/DE_", cellType ,'.tsv'), row.names = FALSE, quote = FALSE, sep = "\t")
    write.table(na.omit(results_combined), paste0(outdir, "/DE_", cellType ,'_noNA.tsv'), row.names = FALSE, quote = FALSE, sep = "\t")
}



############################### Test for differential gene expression and transcript usage ##############################

##integrated seurat object (.rds) containing "RNA" and "Transcript" assays and MetaData columns "orig.ident" and "celltype"
seurat_NS <- readRDS("/path/to/seurat_obj.rds" )
##data.frame with columns gene_id, gene_name and transcript_id
transcript2gene_df <- read.table("/path/to/transcript2gene.tsv", header = T, sep = " ") 
##cell type used for subsetting
celltype <- "Monocytes"
##test design
sampleDat <- data.frame(name = unique(seurat_NS$orig.ident),
                        condition = ifelse(startsWith(unique(seurat_NS$orig.ident), 'COVID'),'COVID', 'Control'),
                        row.names = unique(seurat_NS$orig.ident))
##output directory
out_dir = "output_directory/"

testDifferentialExpression(seurat_obj = seurat_NS, cellType = celltype, transcript2gene = transcript2gene_df, sampleData = sampleDat, outdir = out_dir)


