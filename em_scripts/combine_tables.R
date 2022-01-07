library(data.table)

library(parallel)

library(Matrix)



number.of.nodes=200

if (file.exists('convergence_log.txt'))
{
  if (as.integer(read.table('convergence_log.txt',header=F))==-1)
    
    quit('no')
  
}

combined.table=NULL

combined.x.seq=NULL

combined.isoform.functions=NULL

combined.transcript.ids=NULL

share.vec=c()

fit.sample=c()

new.coefs=NULL

total.lik=0

#read

for (node.number in 1:number.of.nodes)
{
  
  load(paste0('interpro_state_',node.number,'.RData'))
  
  total.lik=total.lik+res.ga@fitnessValue/sum(lower.tri(seq.sim.mat) & compare.pairs)
  
  if (is.null(combined.table))
  {
    combined.table=iso.has.func
    
  }else{
    
    if (sum(colnames(iso.has.func) %in% colnames(combined.table))<ncol(iso.has.func))
    {
      
      col.dif=colnames(iso.has.func)[!colnames(iso.has.func) %in% colnames(combined.table)]
      
      combined.table=cbind(combined.table,Matrix(rep(0,nrow(combined.table)*length(col.dif)),nrow=nrow(combined.table)))
      
      colnames(combined.table)[(ncol(combined.table)-length(col.dif)+1):ncol(combined.table)]=col.dif    
    }
    
    combined.table=combined.table[,order(colnames(combined.table))]
    
    if (sum(colnames(combined.table) %in% colnames(iso.has.func))<ncol(combined.table))
    {
      
      col.dif=colnames(combined.table)[!colnames(combined.table) %in% colnames(iso.has.func)]
      
      iso.has.func=cbind(iso.has.func,Matrix(rep(0,nrow(iso.has.func)*length(col.dif)),nrow=nrow(iso.has.func)))
      
      colnames(iso.has.func)[(ncol(iso.has.func)-length(col.dif)+1):ncol(iso.has.func)]=col.dif    
    }
    
    iso.has.func=iso.has.func[,order(colnames(iso.has.func))]
    
    combined.table=rbind(combined.table,iso.has.func)
  }
  
  prev.length=length(combined.x.seq)
  
  combined.x.seq=c(combined.x.seq,x.seq)
  
  names(combined.x.seq)[(prev.length+1):length(combined.x.seq)]=transcript.ids
  
  combined.isoform.functions=c(combined.isoform.functions,isoform.functions)
  
  combined.transcript.ids=c(combined.transcript.ids,transcript.ids)
  
  #re-fit coefficients
  
  next.share.vec=(iso.has.func%*%t(iso.has.func))[lower.tri(seq.sim.mat) & compare.pairs]
  
  fit.sample=c(fit.sample,seq.sim.mat[lower.tri(seq.sim.mat) & compare.pairs])
  
  share.vec=c(share.vec,next.share.vec)
  
}


rm(next.share.vec)

#check convergence

args=commandArgs(trailingOnly = TRUE)

if (file.exists('last_lik.txt'))
{
  
  writeMM(combined.table,'combined_iso_has_func.txt')
  
  write.table(colnames(combined.table),'colnames.txt',sep='\t',col.names = F,row.names = F,quote = F)
  
  write.table(rownames(combined.table),'rownames.txt',sep='\t',col.names = F,row.names = F,quote = F)
  
  prev.lik=as.numeric(read.table('last_lik.txt',header=F) )
  
  lik.diff=total.lik-prev.lik
  
  write.table(total.lik,'last_lik.txt',sep='\t',col.names = F,row.names = F,quote = F)
  
  system(paste0('echo ',lik.diff,' >> lik_diff.txt'))
  
  dif.log=read.table('lik_diff.txt')
  
  if (nrow(dif.log)>25)
    
    if(cumsum(dif.log$V1)[nrow(dif.log)]-cumsum(dif.log$V1)[nrow(dif.log)-25]<1/3)
    {
      print('Algorithm Converged')
      
      share.vec2=share.vec^2
      
      new.coefs=lm(fit.sample~share.vec+share.vec2)$coefficients
      
      if (!file.exists('convergence_log.txt'))
      {
        write.table(args[1],'convergence_log.txt',col.names = F,row.names = F,quote = F)
      }else{
        prev.conv=read.table('convergence_log.txt',header=F)
        
        if ((as.integer(prev.conv)+1==as.integer(args[1])) || as.integer(prev.conv)==-1)
        {
          print('Local Maximum Reached')
          
          write.table(-1,'convergence_log.txt',col.names = F,row.names = F,quote = F)
          
          quit('no')
        }else
        {
          write.table(args[1],'convergence_log.txt',col.names = F,row.names = F,quote = F)
        }
        
        rm(prev.conv)
      }
      
    }
  

}else{
  
  writeMM(combined.table,'combined_iso_has_func.txt')
  
  write.table(colnames(combined.table),'colnames.txt',sep='\t',col.names = F,row.names = F,quote = F)
  
  write.table(rownames(combined.table),'rownames.txt',sep='\t',col.names = F,row.names = F,quote = F)
  
  write.table(total.lik,'last_lik.txt',sep='\t',col.names = F,row.names = F,quote = F)
  
  
}


#re-allocate

rm(fit.sample)

rm(share.vec)

split.ids.comb=sample(1:number.of.nodes,length(combined.transcript.ids),replace=TRUE)

for (node.number in 1:number.of.nodes)
{
  
  transcript.ids=combined.transcript.ids[which(split.ids.comb==node.number)]
  
  x.seq=combined.x.seq[which(names(combined.x.seq) %in% transcript.ids)]
  
  isoform.functions=combined.isoform.functions[which(names(combined.isoform.functions) %in% transcript.ids)]
  
  iso.has.func=combined.table[rownames(combined.table) %in% transcript.ids,]
  
  if (!is.null(new.coefs))
    
    coefs=new.coefs
  
  save(transcript.ids,x.seq,isoform.functions,iso.has.func,coefs,file=paste0('interpro_state_',node.number,'.RData'))
  
}

