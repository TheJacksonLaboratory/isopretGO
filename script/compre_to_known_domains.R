library(data.table)

library(goseq)

load('function.RData')

gene.functions=getgo(unique(interpro.tab$ensembl_gene_id),'hg38','ensGene',fetch.cats="GO:MF")

iso.logical=!duplicated(interpro.tab$ensembl_transcript_id)

genes=interpro.tab$ensembl_gene_id[iso.logical]

remove.go=names(table(unlist(gene.functions))[table(unlist(gene.functions))>=length(unique(genes))/10])

gene.functions=lapply(gene.functions,function(l)l[-which(l %in% remove.go)])

interpro2go=fread('function prediction/interpro2go',sep=';',header=FALSE,data.table = FALSE,skip = 3)

interpro2go$V1=unlist(lapply(lapply(lapply(interpro2go$V1,strsplit,split=' '),unlist),'[[',1))

interpro2go$V1=gsub('InterPro:','',interpro2go$V1)

interpro2go=interpro2go[interpro2go$V2 %in% unlist(gene.functions),]

isoform.functions=read.table('predicted_isoform_functions_MF.txt',header=T,sep='\t')

isoform.functions=isoform.functions[p.adjust(isoform.functions$P.Val,method='BY')<=0.05,]

res=matrix(ncol=3,nrow=0)

colnames(res)=c('domain','go.term','p.value')

for (i in 1:nrow(interpro2go))
{
  
  #For each domain, white balls drawn are the number of isoforms with the domain that were assigned the function
  #, white balls present are the number of isoforms with the domain, black balls are the number of isoforms
  # without the domain, and number of draws is the total number of times this functions has been assigned.
  #Since the function can only be assigned to isoforms whose genes contain the function, we restrict the universe to those 
  #isoforms for each GO term.

  domain=interpro2go[i,1]
  
  go.term=interpro2go[i,2]
  
  interpro.tab.res=interpro.tab[interpro.tab$ensembl_gene_id %in% names(gene.functions)[unlist(lapply(gene.functions,function(l)go.term %in% l))]]
  
  isoform.functions.res=isoform.functions[isoform.functions$Ensembl.ID %in% isoforms[genes %in% names(gene.functions)[unlist(lapply(gene.functions,function(l)go.term %in% l))]],]
  
  isoforms.with.domain=unique(interpro.tab.res$ensembl_transcript_id[interpro.tab.res$domain==domain])
  
  white.balls.present=length(isoforms.with.domain)
  
  if (white.balls.present<20)
    
    next
  
  isoform.with.domain.assigned=white.balls.drawn=sum(unique(isoform.functions.res$Ensembl.ID[isoform.functions.res$Go.Terms==go.term]) %in% isoforms.with.domain)
    
  isoforms.without.domain=black.balls.present=length(unique(interpro.tab.res$ensembl_transcript_id[!interpro.tab.res$ensembl_transcript_id %in% isoforms.with.domain]))
    
  total.function.assigned=number.draws=sum(isoform.functions.res$Go.Terms==go.term)
  
  p=phyper(q = white.balls.drawn-1,m = white.balls.present,n=black.balls.present,k = number.draws,lower.tail = F )
  
  res=rbind(res,c(domain,go.term,p))
}

res=cbind(res,p.adjust(as.numeric(res[,3]),method='BH'))

colnames(res)[4]='p.adjusted'

res=res[order(as.numeric(res[,4])),]

write.table(res,'domain_comparison.txt',sep='\t',quote = F,row.names=F,col.names=T)





