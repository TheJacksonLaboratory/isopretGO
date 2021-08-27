# This script creates a graph in which each node corresponds to an isoform, and an edge between 
# two isoforms represents a possible functional connection between the isoforms.  The two conditions
# for adding an edge between two isoforms (1) the genes containing them share a function/GO term 
# (2) they share at least one domain.   The weight of an edge is the size of the intersection of the
# isoforms' domains divided by the size of the untion of the isoform's domains
# (i.e. a value between 0 and 1).  The isoforms are then clustered into communities using the infomap 
# algorithm.  The community membership is then used by the script predict_funtion.R to predict isoform
# functions.


library(igraph)

library(goseq)

library(parallel)

library(data.table)

num.cores=4  #parallelize the work

#Get the isoform domains:

interpro.tab=read.table('interpro_domains.txt',sep='\t',header=TRUE)

interpro.tab=interpro.tab[interpro.tab[,3]!="",]

interpro.tab=interpro.tab[!duplicated(paste(interpro.tab[,1],interpro.tab[,3])),]

colnames(interpro.tab)[3]='domain'

interpro.tab=data.table(interpro.tab)

interpro.tab=interpro.tab[unlist(lapply(getgo(interpro.tab$ensembl_gene_id,'hg38','ensGene'),length))>0,]

#Get the GO terms for each gene:

gene.functions=getgo(unique(interpro.tab$ensembl_gene_id),'hg38','ensGene',fetch.cats="GO:MF")

iso.logical=!duplicated(interpro.tab$ensembl_transcript_id)

genes=interpro.tab$ensembl_gene_id[iso.logical] #array of gene ensembl ids

remove.go=names(table(unlist(gene.functions))[table(unlist(gene.functions))>=length(unique(genes))/10])

gene.functions=lapply(gene.functions,function(l)l[-which(l %in% remove.go)]) #removing GO terms that are too common

isoforms=interpro.tab$ensembl_transcript_id[iso.logical] #array of isoform ensembl ids

#Create a empty graph:

g=make_empty_graph(n=length(isoforms),directed=FALSE)

# Create a table with all isoform pairs and a domain that they share (used for faster graph construction):
# For efficiency also remove pairs of isoforms whose containing genes do not share any function

edges.tab=merge.data.table(interpro.tab,interpro.tab,by='domain',allow.cartesian = TRUE)

edges.tab=edges.tab[order(edges.tab$ensembl_transcript_id.x,edges.tab$ensembl_transcript_id.y),]

edges.tab=edges.tab[!edges.tab$ensembl_transcript_id.x==edges.tab$ensembl_transcript_id.y,]

edges.tab=edges.tab[apply(edges.tab,1,function(v)length(intersect(gene.functions[[v[3]]],gene.functions[[v[7]]]))>0),]

#Count the number of domains per isoform:

domains.per.isoform=unlist(mclapply(isoforms,function(x)sum(interpro.tab$ensembl_transcript_id==x),mc.cores=num.cores))

#Start adding edges to the graph:

nrow.edges.tab=nrow(edges.tab)

slice.num=500  #break the data structure edges.tab into slices to save memory

for (i in 1:slice.num)
{
  
  # decide where to end the next part of the table that is being processed, so that it doesn't contain only
  # a subset of the domains of a certain isoform:
  
  cut.idx=round(nrow.edges.tab/slice.num)
  
  if (cut.idx>=nrow(edges.tab))
  { 
    cut.idx=nrow(edges.tab)
  }else
  {
    while (
      cut.idx<nrow(edges.tab)
      &&
      (edges.tab$ensembl_transcript_id.x[cut.idx+1]==edges.tab$ensembl_transcript_id.x[cut.idx])
      &&
      (edges.tab$ensembl_transcript_id.y[cut.idx+1]==edges.tab$ensembl_transcript_id.y[cut.idx])
    )
    {
      cut.idx=cut.idx+1
      
      if (cut.idx>=nrow(edges.tab))
        
        break
    }
  }
  
  #split the next part of the table into matrices that contain pairs of isoforms and their shared domains:
  
  sub.edges.tab=split(x=edges.tab[1:cut.idx,],by=c('ensembl_transcript_id.x','ensembl_transcript_id.y'))
  
  #remove this part from the rest of the table to save memory:
  
  edges.tab=edges.tab[-(1:cut.idx),]
  
  gc()
  
  #add the edges to the graph, again in several slices:
  
  while(length(sub.edges.tab)>0)
  {
    end.idx=min(length(sub.edges.tab),10000)
    
    #create a data frame with the edge endpoints (isoforms) and edge weight:
    
    edges.df=do.call(rbind,mclapply(sub.edges.tab[1:end.idx],function(m)
    {
      
      i1=which(isoforms==m$ensembl_transcript_id.x[1])
      
      i2=which(isoforms==m$ensembl_transcript_id.y[1])
        
      return(data.frame(e=i1,i2,
                          w=nrow(m)/(domains.per.isoform[i1]+domains.per.isoform[i2]-nrow(m))))
      
      
    },mc.cores = num.cores))
    
    #remove from the data frame edges that are already in the graph:
    
     edges.df=edges.df[unlist(mclapply(1:nrow(edges.df),function(k)!are_adjacent(g,edges.df[k,1],edges.df[k,2]),mc.cores = num.cores)),]
    
     #add the edges to the graph:
         
     if (nrow(edges.df)>0)
          
        g=add_edges(graph = g,edges = do.call(c,mclapply(1:nrow(edges.df),function(k)c(as.character(edges.df[k,1]),as.character(edges.df[k,2])),mc.cores = num.cores)),weight=edges.df[,3])
        
     #remove the part that was added to the graph from the edges that are being processed:
     
     sub.edges.tab=sub.edges.tab[-(1:end.idx)]
  }
  
}

save.image('function_2.RData')

#Run the Louvain algorithm on the graph:

res=cluster_infomap(g)

save.image('function_2.RData')


