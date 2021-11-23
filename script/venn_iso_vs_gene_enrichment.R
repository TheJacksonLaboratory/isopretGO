library(pcaPP)

iso.tab=read.table('isoform_function_enrichment.txt',sep='\t',header=T)

gene.tab=read.table('gene_function_enrichment.txt',sep='\t',header=T)

merged.tab=merge(iso.tab,gene.tab,by=c('GO.Term','dataset'))

a=sum(merged.tab$p.adjust.x<=0.05 & merged.tab$p.adjust.y<=0.05)

library(VennDiagram)
                                                                              # Move to new plotting page
draw.pairwise.venn(area1 =  sum(iso.tab$p.adjust<=0.05),                        # Create pairwise venn diagram
                   area2 = sum(gene.tab$p.adjust<=0.05),
                   cross.area = a)



betacoronas=c("mason_latest","SRP040070_3","SRP040070_7","SRP040070_9","SRP227272_38",'SRP279203_72','SRP294125_74','SRP284977_76','SRP284977_77')

head(sort(table(gene.tab$GO.Term[gene.tab$p.adjust<=0.05 & gene.tab$dataset %in% betacoronas]),decreasing = T))

head(sort(table(iso.tab$GO.Term[iso.tab$p.adjust<=0.05 & iso.tab$dataset %in% betacoronas]),decreasing = T))

head(sort(table(gene.tab$GO.Term[gene.tab$p.adjust<=0.05 & !gene.tab$dataset %in% betacoronas]),decreasing = T))

head(sort(table(iso.tab$GO.Term[iso.tab$p.adjust<=0.05 & !iso.tab$dataset %in% betacoronas]),decreasing = T))


