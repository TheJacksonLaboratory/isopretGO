#use after faster_function.R,  this scripts uses data structures from the previous script
library(parallel)

library(igraph)

load('function_2.RData')

res=cluster_infomap(g)

gene.functions=getgo(unique(interpro.tab$ensembl_gene_id),'hg38','ensGene',fetch.cats="GO:BP")

n.cores=4

function.counts=table(unlist(gene.functions))

num.genes.with.functions=sum(unlist(lapply(gene.functions,function(l)length(unlist(l))>0)))

isoform.functions=do.call(rbind,mclapply(isoforms,function(isoform)
{
  cur.funcs=gene.functions[genes[which(isoforms==isoform)]]
  
  gene=genes[isoforms==isoform]
 
  if (length(unlist(cur.funcs))==0)
    
    return(matrix(nrow=0,ncol=3))
   
  adj.vtx=ends(g, E(g)[from(which(isoforms==isoform))])[,1]
  
  if (length(adj.vtx)==0)
    
    return(matrix(nrow=0,ncol=3))
  
  if (adj.vtx[1]==which(isoforms==isoform))
    
    adj.vtx=ends(g, E(g)[from(which(isoforms==isoform))])[,2]
    
  e.weights=get.edge.attribute(g,'weight',E(g)[from(which(isoforms==isoform))])
    
  e.weights=e.weights[which(adj.vtx %in% which((res$membership==res$membership[which(isoforms==isoform)]) & isoforms!=isoform))]
    
  adj.vtx=adj.vtx[which(adj.vtx %in% which((res$membership==res$membership[which(isoforms==isoform)]) & isoforms!=isoform))]
    
  if (length(unlist(adj.vtx))==0)
    
    return(matrix(nrow=0,ncol=3))
  
  cur.counts=unlist(lapply(unlist(cur.funcs),function(x)sum(unlist(lapply(1:length(adj.vtx),function(i)sum(e.weights[i]*gene.functions[[genes[adj.vtx[i]]]] %in% unlist(x)))))))
    
  names(cur.counts)=unlist(cur.funcs)
  
  cur.counts=cur.counts[cur.counts>0]
  
  if (length(unlist(cur.funcs))==0)
    
    return(matrix(nrow=0,ncol=3))
  
  p.vals=c()
  
  for (func in names(cur.counts))
  {
    
    p.head=function.counts[func]/num.genes.with.functions
    
    p.vals=c(p.vals,pbinom(q=round(cur.counts[func])-1,size=round(sum(e.weights)),prob=p.head,lower.tail=FALSE))
    
  }
  
  matrix(c(rep(isoform,length(cur.counts)),names(cur.counts),p.vals),ncol=3)
  
},mc.cores=n.cores))

isoform.functions=cbind(isoform.functions,p.adjust(isoform.functions[,3]))

colnames(isoform.functions)=c('Ensembl ID','Go Terms','P.Val','P.adjusted')

write.table(isoform.functions,'predicted_isoform_functions.txt',sep='\t',quote=F,row.names=F,col.names=T)
