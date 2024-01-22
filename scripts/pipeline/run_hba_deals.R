library(hbadeals)
library(edgeR)

#args[1] - name and path of samples table
#args[2] - cohort number (which cases should be compared against which controls)
#args[3] - input directory where the RSEM files are

args=commandArgs(trailingOnly = TRUE)

sample.table=read.table(args[1],header=TRUE,sep='\t')

#Keep paired end samples:
#sample.table=sample.table[sample.table$isPaired==args[4],]

#Keep only the control and samples in this SRP that belong to the given cohort
sample.table=sample.table[sample.table$status=='control' | sample.table$cohort==as.integer(args[2]),]


countsData=NULL
labels=c()

for (srr in unique(sample.table$BioSample[sample.table$status=='control']))
{
  
  if (is.null(countsData))
  {
    countsData=read.table(paste0(args[3],'/',srr,'/rsem.isoforms.results'),header=TRUE)[c(2,1)]
  }
   next.file=read.table(paste0(args[3],'/',srr,'/rsem.isoforms.results'),header=TRUE)
   countsData=cbind(countsData,next.file$expected_count)
  
   labels=c(labels,1)  
}

for (srr in unique(sample.table$BioSample[sample.table$status!='control']))
{
   next.file=read.table(paste0(args[3],'/',srr,'/rsem.isoforms.results'),header=TRUE)
   countsData=cbind(countsData,next.file$expected_count)
   
   labels=c(labels,2)
}


#labels=labels[colSums(countsData[,-c(1,2)])>=10^6]

#countsData=countsData[,c(1,2,2+which(colSums(countsData[,-c(1,2)])>=10^6))]

if (sum(colSums(countsData[,-c(1,2)])<10^6)>=1)
	print("Warning: Sum of sample counts less than 10^6")

tmp.countsData=countsData

if (ncol(countsData)-2<=9){
  countsData=countsData[rowSums(countsData[,-c(1,2)]>0)>=ncol(countsData)-2,]
}else{
  countsData=countsData[rowSums(countsData[,2+which(labels==1)]>0)>=0.9*sum(labels==1),]
  countsData=countsData[rowSums(countsData[,2+which(labels==2)]>0)>=0.9*sum(labels==2),]
}


num.iso=unlist(lapply(countsData$gene_id,function(x){sum(countsData$gene_id %in% x)}))

countsData=countsData[num.iso>1,]


if (length(unique(countsData[,1]))<100)
{
  countsData=tmp.countsData
  thresh=quantile(colMeans(countsData[,-c(1,2)]>0),0.5)
  labels=labels[colMeans(countsData[,-c(1,2)]>0)>thresh]
  if (sum(labels==1)<3 || sum(labels==2)<3)  {print('Not enough expressed isoforms');quit('no')}
  countsData=countsData[,c(1,2,2+which(colMeans(countsData[,-c(1,2)]>0)>thresh))]
  if (ncol(countsData)-2<=9){
 	 countsData=countsData[rowSums(countsData[,-c(1,2)]>0)>=ncol(countsData)-2,]
  }else{
  	 countsData=countsData[rowSums(countsData[,2+which(labels==1)]>0)>=0.9*sum(labels==1),]
  	 countsData=countsData[rowSums(countsData[,2+which(labels==2)]>0)>=0.9*sum(labels==2),]
  }
  num.iso=unlist(lapply(countsData$gene_id,function(x){sum(countsData$gene_id %in% x)}))
  countsData=countsData[num.iso>1,]
  if (length(unique(countsData[,1]))<100) {print('Not enough expressed isoforms');quit('no')}  
}

rm(tmp.countsData)

res=hbadeals(countsData = countsData,labels = labels,n.cores = 45,isoform.level=TRUE,mcmc.iter=400000,mcmc.warmup=10000,mtc=TRUE,lib.size=colSums(countsData[,-c(1,2)])*calcNormFactors(as.matrix(countsData[,-c(1,2)]),method='TMM'),hierarchy='auto')


write.table(res,paste0('/projects/chesler-lab/jcpg/snakemake/',as.character(sample.table$srp)[sample.table$cohort==as.integer(args[2])][1]
  ,'_',args[2],'.txt'),
            sep='\t',quote = F,col.names = T,row.names = F)
  
  
  
  
  
