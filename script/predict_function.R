# This script is runafter fast_function.R and uses data structures created there, therefore it is loading
# an .RData file.   The prediction proceeds as follows:  For each isoform, look at the distribution 
# of GO terms of other isoforms in its community (communities are assigned by the infomap algorithm in
# the script faster_function.R). Go terms that appear more than expected by chance(with respect to their
# frequency in the population, i.e all the genes in the data), and also belong to the gene that contains the
# isoform, are selected as the isoform's functions.

library(parallel)

load('function.RData')

n.cores=32

function.counts=table(unlist(gene.functions)) #frequency of GO terms in the population

#Assign function to each isoform:

isoform.functions=do.call(rbind,mclapply(isoforms,function(isoform)
{
  
  #Get the functions/GO terms of the gene that contains the isoform
  
  cur.funcs=gene.functions[genes[which(isoforms==isoform)]]
  
  gene=genes[isoforms==isoform]
 
  #If the gene does not have functions associated with it the isoform does not either:
  
  if (length(unlist(cur.funcs))==0)
    
    return(matrix(nrow=0,ncol=4))
  
  #Count the number of times each GO term appears in other community members:
   
  cur.counts=unlist(lapply(unlist(cur.funcs),function(x)sum(unlist(gene.functions[genes[(res$membership==res$membership[which(isoforms==isoform)]) & isoforms!=isoform]]) %in% unlist(x))))
    
  names(cur.counts)=unlist(cur.funcs)
  
  cur.counts=cur.counts[cur.counts>0] #keep terms that appear at least once
  
  if (length(unlist(cur.funcs))==0)
    
    return(matrix(nrow=0,ncol=3))
  
  #Use the binomial test to get a p-value for each term to assess its over-representation:
  
  p.vals=c()
  
  for (func in names(cur.counts))
  {
    
    p.head=function.counts[func]/length(unique(genes))
    
    p.vals=c(p.vals,pbinom(q=cur.counts[func]-1,size=sum((res$membership==res$membership[which(isoforms==isoform)]) & isoforms!=isoform),prob=p.head,lower.tail=FALSE))
    
  }
  
  #return the p-values found for this isoforms and the GO terms
  
  matrix(c(rep(isoform,length(cur.counts)),names(cur.counts),p.vals),ncol=3)
  
},mc.cores=n.cores))

isoform.functions=cbind(isoform.functions,p.adjust(isoform.functions[,3]))

colnames(isoform.functions)=c('Ensembl ID','Go Terms','P.Val','P.adjusted')

write.table(isoform.functions,'predicted_isoform_functions.txt',sep='\t',quote=F,row.names=F,col.names=T)
