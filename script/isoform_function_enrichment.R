source('/Users/karleg/get_fdr_prob.R')

isoform.functions=read.table('predicted_isoform_functions_BP.txt',header=T,sep='\t')

isoform.functions=isoform.functions[p.adjust(isoform.functions$P.Val,method='BY')<=0.05,]

datasets=c("SRP040070_3","SRP040070_7","SRP040070_9",
"SRP227272_38",'SRP279203_72','SRP294125_74',"SRP078309_53","SRP178454_50","SRP186406_51","SRP216763_55","SRP222569_54","SRP251704_52",
"SRP273785_56",'SRP284977_76','SRP284977_77')

enrich.mat=matrix(ncol=4,nrow=0)

colnames(enrich.mat)=c('GO Term','dataset','FC','p-value')


for (dataset in datasets)
{
  res=read.table(paste0('/Users/karleg/HBA-DEALS-covid19_output/',dataset,'.txt'),header=TRUE)
  
  res=res[res$Isoform %in% isoform.functions$Ensembl.ID,]
  
  thresh=get.fdr.prob(res)

  ds.isoforms=res$Isoform[res$Isoform!='Expression' & res$P<=thresh[[2]]]
  
  go.tab=table(isoform.functions$Go.Terms[isoform.functions$Ensembl.ID %in% gsub('\\.[0-9]*','',ds.isoforms)])
  
  for (term in names(go.tab)[go.tab>=10])
  {
    
    white.balls.drawn=sum(ds.isoforms %in% isoform.functions$Ensembl.ID[isoform.functions$Go.Terms==term])
    
    if (white.balls.drawn<10)
      
      next
    
    white.balls.present=sum(res$Isoform %in% isoform.functions$Ensembl.ID[isoform.functions$Go.Terms==term])
    
    black.balls.present=sum(res$Isoform!='Expression')-white.balls.present
    
    number.draws=length(ds.isoforms)
    
    fc=1
    
    if (number.draws>0 && white.balls.present>0)
      
      fc=white.balls.drawn/number.draws*(white.balls.present+black.balls.present)/white.balls.present
    
    if (fc>1)
    {
      p=phyper(q = white.balls.drawn-1,m = white.balls.present,n=black.balls.present,k = number.draws,lower.tail = F )
    }else{
      
      p=phyper(q = white.balls.drawn,m = white.balls.present,n=black.balls.present,k = number.draws)
    }
    
    enrich.mat=rbind(enrich.mat,c(term,dataset,fc,min(p*2,1)))
    
  }
  
}  

enrich.mat=cbind(enrich.mat,p.adjust(enrich.mat[,4],method='BH'))

colnames(enrich.mat)[5]='p.adjust'

enrich.mat=enrich.mat[order(enrich.mat[,'p.adjust']),]

write.table(enrich.mat,paste0('isoform_function_enrichment.txt'),sep='\t',col.names = T,row.names = F,quote = F)
