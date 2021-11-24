
isoform.functions=read.table('predicted_isoform_functions_BP.txt',header=T,sep='\t')

isoform.functions=isoform.functions[p.adjust(isoform.functions$P.Val,method='BY')<=0.05,]

iso.diff=function(iso1,iso2){
res=setdiff(isoform.functions$Go.Terms[isoform.functions$Ensembl.ID==iso1],isoform.functions$Go.Terms[isoform.functions$Ensembl.ID==iso2])
if (length(res)==0)
  return('None')
return(paste(res,collapse = ','))
}
iso.inter=function(iso1,iso2){
res=intersect(isoform.functions$Go.Terms[isoform.functions$Ensembl.ID==iso1],isoform.functions$Go.Terms[isoform.functions$Ensembl.ID==iso2])
  if (length(res)==0)
    return('None')
return(paste(res,collapse = ','))
}

curation.tab=read.table('/Users/karleg/Desktop/Curation/curation.txt',sep='\t',header = T)

curation.tab=cbind(curation.tab,apply(curation.tab,1,function(v)iso.diff(v[2],v[3])))

colnames(curation.tab)[ncol(curation.tab)]='PredictedForIso1'

curation.tab=cbind(curation.tab,apply(curation.tab,1,function(v)iso.diff(v[3],v[2])))

colnames(curation.tab)[ncol(curation.tab)]='PredictedForIso2'

curation.tab=cbind(curation.tab,apply(curation.tab,1,function(v)iso.inter(v[2],v[3])))

colnames(curation.tab)[ncol(curation.tab)]='PredictedForBoth'

write.table(curation.tab,'curation_table.txt',sep='\t',row.names = FALSE,col.names = TRUE,quote = FALSE)

all.go.terms=unique(c(unlist(strsplit(curation.tab$PredictedForIso1,split = ',')),unlist(strsplit(curation.tab$PredictedForIso2,split = ',')),unlist(strsplit(curation.tab$PredictedForBoth,split = ','))))

all.go.terms=setdiff(all.go.terms,'None')

library(biomaRt)

mart <- useMart(biomart = "ensembl", dataset = "hsapiens_gene_ensembl",host="www.ensembl.org")

go.id.to.name=matrix(nrow=0,ncol=2)

for (i in seq(51,length(all.go.terms),50))
{
  go.id.to.name=rbind(go.id.to.name,getBM(attributes = c("name_1006","go_id"),
      filters    = "go",
      values     = all.go.terms[(i-50):i], 
     mart       = mart))
  
  go.id.to.name=go.id.to.name[go.id.to.name$go_id %in% all.go.terms,]
}

write.table(go.id.to.name,'GO_ID_to_name',sep='\t',row.names = FALSE,col.names = TRUE,quote = FALSE)
