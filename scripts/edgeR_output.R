library(edgeR)

isopret.input=NULL

args=commandArgs(trailingOnly = TRUE)

# args[1] - path to RSEM control samples
# args[2] - path to RSEM case samples

# Assemble RSEM output files into a matrix of counts in 'countsData'

countsData=NULL

labels=c()

for (next.file.name in list.files(args[1],full.names = TRUE))
{

  if (is.null(countsData))
  {
    countsData=read.table(next.file.name,header=TRUE)[c(2,1)]
  }
    next.file=read.table(next.file.name,header=TRUE)

    countsData=cbind(countsData,next.file$expected_count)

    labels=c(labels,1)
}

for (next.file.name in list.files(args[2],full.names = TRUE))
{
   next.file=read.table(next.file.name,header=TRUE)

   countsData=cbind(countsData,next.file$expected_count)

   labels=c(labels,2)
}

countsData=countsData[rowSums(countsData[,-c(1,2)]>0)==ncol(countsData)-2,]
#run edgeR on isoform counts

isoform.counts=countsData[,-c(1,2)]

rownames(isoform.counts)=paste(countsData[,1],countsData[,2])

y=DGEList(counts=isoform.counts,group=labels)

keep=filterByExpr(y)

y=y[keep,,keep.lib.size=FALSE]

y=calcNormFactors(y)

design=model.matrix(~labels)

y=estimateDisp(y,design)

fit=glmFit(y,design)

lrt=glmLRT(fit,coef=2)

edger.out=(topTags(lrt,n = nrow(y))$table)

isopret.input=edger.out[,c(1,5)]

isopret.input[,1]=2^isopret.input[,1]

isopret.input=cbind(do.call(rbind,lapply(rownames(edger.out),
                              function(x)c(unlist(strsplit(x,split=' '))[1],unlist(strsplit(x,split=' '))[2]))),isopret.input)

rownames(isopret.input)=NULL

num.iso=unlist(lapply(isopret.input[,1],function(x){sum(isopret.input[,1]==x)}))

no.iso.genes=unlist(lapply(split(isopret.input,isopret.input[,1]),function(m)nrow(m)==1))

isopret.input=isopret.input[isopret.input[,1] %in% names(no.iso.genes)[no.iso.genes==FALSE],]

countsData=countsData[countsData[,1] %in% isopret.input[,1],]

#run edgeR on gene counts

gene.counts=do.call(rbind,lapply(split(countsData,countsData[,1]),function(m){

  res=matrix(colSums(m[,-c(1,2)]),nrow=1)

  rownames(res)=m[1,1]

  res

}))

y=DGEList(counts=gene.counts,group=labels)

keep=filterByExpr(y)

y=y[keep,,keep.lib.size=FALSE]

y=calcNormFactors(y)

design=model.matrix(~labels)

y=estimateDisp(y,design)

fit=glmFit(y,design)

lrt=glmLRT(fit,coef=2)

edger.out=(topTags(lrt,n = nrow(y))$table)

gene.results=do.call(rbind,lapply(rownames(edger.out),function(x){

  v=edger.out[rownames(edger.out)==x,]

  matrix(unlist(c(rownames(v),'Expression',v[1],v[5])),nrow=1)

}))

colnames(gene.results)=colnames(isopret.input)

isopret.input=rbind(isopret.input,gene.results)

isopret.input=isopret.input[order(isopret.input[,1]),]

colnames(isopret.input)=c('Gene','Isoform','ExplogFC/FC','BH')

isopret.input$`ExplogFC/FC`=as.numeric(isopret.input$`ExplogFC/FC`)

isopret.input$BH=as.numeric(isopret.input$BH)

isopret.input$BH[abs(isopret.input$`ExplogFC/FC`)<log2(1.5)]=1

isopret.input$`ExplogFC/FC`[isopret.input$Isoform!='Expression']=2^isopret.input$`ExplogFC/FC`[isopret.input$Isoform!='Expression']

write.table(isopret.input,'Isopret_Input.txt',
            sep='\t',quote = F,col.names = T,row.names = F)
