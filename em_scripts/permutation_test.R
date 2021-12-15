library(parallel)

library(Matrix)

library(ggpubr)

library(data.table)

num.cores=4

#read assigned functions

iso.has.func=readMM('/Users/karleg/function prediction/backup/combined_iso_has_func.txt')

rownames(iso.has.func)=read.table('/Users/karleg/function prediction/backup/rownames.txt')$V1

colnames(iso.has.func)=read.table('/Users/karleg/function prediction/backup/colnames.txt')$V1

#read interpro domains

interpro.tab=read.table('/Users/karleg/function prediction/interpro_domains.txt',sep='\t',header=TRUE)

interpro.tab=interpro.tab[interpro.tab[,2]!="",]

interpro.tab=interpro.tab[!duplicated(paste(interpro.tab[,1],interpro.tab[,2])),]

colnames(interpro.tab)[2]='domain'

#read all GO terms per gene/isoform

gtf.file=fread('/Users/karleg/STAR/STAR/bin/MacOSX_x86_64/data/GRCh38/annotation/Homo_sapiens.GRCh38.91.gtf',sep='\t',quote = '',data.table = FALSE)

gtf.file=gtf.file[gtf.file$V3=='transcript',]

transcript.ids=gsub(';','',unlist(lapply(strsplit(as.character(gtf.file[,9]),split=' '),'[[',6)))

transcript.ids=gsub("\"",'',transcript.ids)

gene.ids=gsub(';','',unlist(lapply(strsplit(as.character(gtf.file[,9]),split=' '),'[[',2)))

gene.ids=gsub("\"",'',gene.ids)

sequence.exists=transcript.ids %in% gsub('translated_','',gsub('.fa','',list.files('/Users/karleg/function prediction/isoform_seqs/')))

gene.ids=gene.ids[sequence.exists]

transcript.ids=transcript.ids[sequence.exists]


interpro2go=fread('/Users/karleg/function prediction/interpro2go',sep=';',header=FALSE,data.table = FALSE,skip = 3)

interpro2go$V1=unlist(lapply(lapply(lapply(interpro2go$V1,strsplit,split=' '),unlist),'[[',1))

interpro.ids=interpro2go$V1

interpro2go=interpro2go[,2]

names(interpro2go)=gsub('InterPro:','',interpro.ids)

rm(interpro.ids)



#GO terms per gene

hgnc.tab=fread('/Users/karleg/function prediction/hgnc_complete_set.txt',sep='\t',quote = '',data.table = FALSE)

gaf.file=fread('/Users/karleg/function prediction/goa_human.gaf',sep='\t',quote = '',data.table = FALSE)

gaf.file=gaf.file[gaf.file$V9=='F',]

gene.functions=mclapply(unique(gene.ids),function(x){
  
  if (sum(hgnc.tab$ensembl_gene_id==x)==0)
    
    return(NULL)
  
  next.uniprot=hgnc.tab$uniprot_ids[hgnc.tab$ensembl_gene_id==x]
  
  if (nchar(next.uniprot)<=1)
    
    return(NULL)
  
  all.trs=unique(transcript.ids[gene.ids==x])
  
  domains=interpro.tab$domain[interpro.tab$ensembl_transcript_id %in% all.trs]
  
  interpro2go.terms=unique(interpro2go[names(interpro2go) %in% domains])
  
  unique(c(gaf.file$V5[gaf.file$V2 %in% next.uniprot],interpro2go.terms))
  
},mc.cores = num.cores)

names(gene.functions)=unique(gene.ids)

gene.functions=gene.functions[!unlist(lapply(gene.functions,function(l)sum(is.na(l))>0))]

remove.go=names(table(unlist(gene.functions))[table(unlist(gene.functions))>=length(unique(gene.ids))/10])

gene.functions=lapply(gene.functions,function(l)l[-which(l %in% remove.go)]) #removing GO terms that are too common

interpro2go=interpro2go[!interpro2go %in% remove.go]

gene.functions=gene.functions[unlist(lapply(gene.functions,length))>0]

transcript.ids=transcript.ids[gene.ids %in% names(gene.functions)]

gene.ids=gene.ids[gene.ids %in% names(gene.functions)]

isoform.functions=gene.functions[gene.ids]

names(isoform.functions)=transcript.ids

isoform.functions=isoform.functions[names(isoform.functions) %in% rownames(iso.has.func)]

length(isoform.functions)==nrow(iso.has.func)  #sanity check

total.length=length(unlist(isoform.functions))  

start.funcs=unlist(lapply(isoform.functions,function(l)length(l))) 



assigned.isoform.functions=cbind(rownames(iso.has.func)[iso.has.func@i+1],colnames(iso.has.func)[iso.has.func@j+1])

colnames(assigned.isoform.functions)=c('Ensembl.ID','Go.Terms')

assigned.isoform.functions=as.data.frame(assigned.isoform.functions)

assigned.isoform.functions=split(assigned.isoform.functions$Go.Terms,assigned.isoform.functions$Ensembl.ID)


num.chosen=length(unlist(assigned.isoform.functions))

set.seed(123)

sensitivity.by.random=c()

iso.has.func=iso.has.func[match(names(isoform.functions),rownames(iso.has.func)),]

for (i in 1:5)
{
  random.sol=c(rep(1,num.chosen),rep(0,total.length-num.chosen))
  
  random.sol=sample(random.sol)
  
  random.isoform.functions=cbind(rep(1:nrow(iso.has.func),start.funcs)[random.sol==1],unlist(isoform.functions)[random.sol==1])
  
  colnames(random.isoform.functions)=c('Ensembl.ID','Go.Terms')
  
  random.isoform.functions=as.data.frame(random.isoform.functions)
  
  random.isoform.functions$Ensembl.ID=rownames(iso.has.func)[as.integer(random.isoform.functions$Ensembl.ID)]
  
  random.isoform.functions=split(random.isoform.functions$Go.Terms,random.isoform.functions$Ensembl.ID)
  
  
  random.sensitivity=unlist(mclapply(rownames(iso.has.func),function(tr){  #for each transcript 
    
    domains=interpro.tab$domain[interpro.tab$ensembl_transcript_id==tr]
    
    go.terms=random.isoform.functions[[tr]]
    
    interpro2go.terms=unique(interpro2go[names(interpro2go) %in% domains])
    
    
    if(length(go.terms)==0 || length(domains)==0 || length(interpro2go.terms)==0)
      
      return(NA)
    
    
    length(intersect(go.terms,interpro2go.terms))/length(unique(c(go.terms,interpro2go.terms)))
    
  },mc.cores = num.cores))
  
  sensitivity.by.random=c(sensitivity.by.random,mean(random.sensitivity,na.rm=T))
}


sensitivity.by.isoform=unlist(mclapply(rownames(iso.has.func),function(tr){  #for each transcript 
  
  domains=interpro.tab$domain[interpro.tab$ensembl_transcript_id==tr]
  
  go.terms=assigned.isoform.functions[[tr]]
  
  interpro2go.terms=unique(interpro2go[names(interpro2go) %in% domains])
  
  
  if(length(go.terms)==0 || length(domains)==0 || length(interpro2go.terms)==0)
    
    return(NA)
  
  
  length(intersect(go.terms,interpro2go.terms))/length(unique(c(go.terms,interpro2go.terms)))
  
},mc.cores = num.cores))


mean(sensitivity.by.isoform,na.rm=T)/mean(sensitivity.by.random,na.rm=T)

sum(mean(sensitivity.by.isoform,na.rm=T)<=sensitivity.by.random)

ggboxplot(sensitivity.by.random,ylab='Mean Interpro2GO Proportion',xlab='', add = 'jitter')+
  geom_point(aes(x=1, y=mean(sensitivity.by.isoform,na.rm=T)), colour="blue")+
  theme(axis.title.x=element_blank(),
        axis.text.x=element_blank())


