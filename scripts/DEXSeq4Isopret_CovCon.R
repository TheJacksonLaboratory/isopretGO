library(DEXSeq)
library(dplyr)
library(Seurat)
library(stringr)

## Adjust the following as needed, usually a file such as NS_IntegratedONT_Reseq_Filtered.rds
seurat_obj_path <- "/SOME_PATH/NS_IntegratedONT_Reseq_Filtered.rds"
## Adjust the following, usually a file such as transcript2gene_ONT.tsv
transcript2gene_file <_ "/SOME_PATH/transcript2gene_ONT.tsv"

###################### Filter & process (single-cell) expression data ######################

##read integrated seurat object containing transcript- and gene-level expression
seurat_NS <- readRDS(seurat_obj_path)

##subset by cell type
cellType <- commandArgs(trailingOnly=TRUE)[1]
cellTypeSubset <- seurat_NS %>% subset(celltype==cellType)

##aggregate counts per sample on gene- and transcript-level 
counts_geneExp <- AggregateExpression(cellTypeSubset, group.by = 'orig.ident', assay = 'RNA')$RNA #gene-level
counts_transExp <- AggregateExpression(cellTypeSubset, group.by = 'orig.ident', assay = 'Transcript')$Transcript #transcript-level

##get transcript-gene assignment and convert to Ensembl IDs
TranscriptGeneDf <- read.table(transcript2gene_file, header = T, sep = " ")
rownames(counts_geneExp) <- TranscriptGeneDf[match(rownames(counts_geneExp), TranscriptGeneDf$gene_name),"gene_id"]

##filter transcripts based on expression and adapt TranscriptGeneDf
keep <- rowSums(counts_transExp >= 5) >= 2
counts_transExp <- counts_transExp[keep,]
TranscriptGeneDf <- TranscriptGeneDf[match(rownames(counts_transExp),TranscriptGeneDf$transcript_id),]

##include only genes with more than 1 transcript for DTU
TranscriptGeneDf <- TranscriptGeneDf %>% group_by(gene_id) %>% mutate(NrTranscripts = n_distinct(transcript_id)) %>% subset(NrTranscripts>1)
counts_transExp <- counts_transExp[TranscriptGeneDf$transcript_id,]

rm(seurat_NS, cellTypeSubset, keep)

############################### Create test design dataframe ##############################

sampleDat <- data.frame(name = colnames(counts_transExp),
                        condition = ifelse(startsWith(colnames(counts_transExp), 'COVID'),
                                          'COVID', 'Control'))
rownames(sampleDat) <- sampleDat$name

########################### run DESeq on gene-level expression ###########################

dds <- DESeqDataSetFromMatrix(countData = counts_geneExp,
                              colData = sampleDat,
                              design = ~ condition)
                                                     
dds <- DESeq(dds)


######################### run DEXSeq on transcript-level expression ####################         

##create DEXSeq object
dxd <- DEXSeqDataSet(countData = as.matrix(counts_transExp), sampleData = sampleDat,
		                          design = ~sample + exon + condition:exon, featureID = TranscriptGeneDf$transcript_id,
					                       groupID = TranscriptGeneDf$gene_id)

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


############################## convert DGE and DTU results ############################

results_gene <- data.frame(results(dds))
results_gene$Gene <- rownames(results_gene)

#write.table(results_gene, paste0('DE_output/DESeq_CovCon', cellType ,'.tsv'), row.names = FALSE, quote = FALSE, sep = "\t")

results_gene <- results_gene %>% mutate(Isoform = 'Expression') %>% 
                                 select(Gene, Isoform, log2FoldChange, padj) %>%
                                 rename('ExplogFC/FC' = log2FoldChange, BH = padj)

results_transcript <- DEXSeqResults(dxd) %>% as.data.frame() %>% select(!genomicData)
#write.table(results_transcript, paste0('DE_output/DEXSeq_CovCon', cellType ,'.tsv'), row.names = FALSE, quote = FALSE, sep = "\t")

results_trans <- results_transcript %>% 
                    mutate(fold_COVID_Control = 2^log2fold_COVID_Control) %>%
                    select(groupID, featureID, fold_COVID_Control, padj) %>%
                    rename(Gene = groupID, Isoform = featureID, 'ExplogFC/FC' = fold_COVID_Control, BH = padj)

results_combined <- rbind(results_gene, results_trans) %>% arrange(Gene)

results_combined$`ExplogFC/FC` <- as.numeric(results_combined$`ExplogFC/FC`)
results_combined$BH <- as.numeric(results_combined$BH)

write.table(results_combined, paste0('isopretGO_input/ConVsCov/DE_', cellType ,'.tsv'), row.names = FALSE, quote = FALSE, sep = "\t")
write.table(na.omit(results_combined), paste0('isopretGO_input/ConVsCov/DE_', cellType ,'_noNA.tsv'), row.names = FALSE, quote = FALSE, sep = "\t")