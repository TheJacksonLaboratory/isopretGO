library(data.table)

library(parallel)

library(Matrix)

library(L1pack)


#This script is executed after all 200 instances of predict2.R have completed.  It combined their outputs, checks for convergence, and prepares inputs
#for the next iteration by dividing the isoforms randomly into 200 distinct sets



number.of.nodes=200  #Number of groups into which the isoforms were splitted in order to speed up the computation

if (file.exists('convergence_log.txt'))  #If a previous iteration created the file convergence_log.txt and printed -1 into it, don't do anything
{
  if (as.integer(read.table('convergence_log.txt',header=F))==-1)
    
    quit('no')
  
}

#define data structures that will be used for combining the results that were computed for the 200 different sets of isoforms
#their role is explained in the body of the loop, at the part of the code where data is added to them

combined.table=NULL

combined.x.seq=NULL

combined.isoform.functions=NULL

combined.transcript.ids=NULL

share.vec=c()

fit.sample=c()

new.coefs=NULL  #If the E step converged, this will be used to store the new coefficients of the quadratic model

total.lik=0  #sums the log-likelihood from the different sets of isoforms

#read the outputs of the 200 instances of predict2.R, and combine them into unified data structures that contain all the isoforms

for (node.number in 1:number.of.nodes)  #For every set of isoforms
{
  
  load(paste0('interpro_state_',node.number,'.RData'))  #read the state after the last run for isoform set number 'node.number'
  
  total.lik=total.lik+res.ga@fitnessValue/sum(lower.tri(seq.sim.mat) & compare.pairs)  #add to the total likelihood the value of the solution that the
                                                                                      #genetic algorithm found, but also divide by the number of isoform pairs in that 
                                                                                      #group for comparability, since group sizes vary 
  
  if (is.null(combined.table))  #If this is the first iteration of the for loop above (i.e. node.number==1) assign the 'iso.has.func' matrix 
                                #from the output of the script that processed the first isoform set to the combined matrix
  {
    combined.table=iso.has.func
    
  }else{
    #merge the next 'iso.has.func' matrix with the combined table
    
    if (sum(colnames(iso.has.func) %in% colnames(combined.table))<ncol(iso.has.func))  #add zero columns to 'combined.table' if there are columns(GO terms)
                                                                                      #that are in the next 'iso.has.func' but not in it.  This is neccessary for merging.
    {
      
      col.dif=colnames(iso.has.func)[!colnames(iso.has.func) %in% colnames(combined.table)]
      
      combined.table=cbind(combined.table,Matrix(rep(0,nrow(combined.table)*length(col.dif)),nrow=nrow(combined.table)))
      
      colnames(combined.table)[(ncol(combined.table)-length(col.dif)+1):ncol(combined.table)]=col.dif    
    }
    
    combined.table=combined.table[,order(colnames(combined.table))]  #sort the columns of the combined table by name
    
    if (sum(colnames(combined.table) %in% colnames(iso.has.func))<ncol(combined.table))  #similarly, if there are columns in 'combined.table' that are not in
                                                                                          #'isoform.has.func', add zero columns to 'isoform.has.func'
    {
      
      col.dif=colnames(combined.table)[!colnames(combined.table) %in% colnames(iso.has.func)]
      
      iso.has.func=cbind(iso.has.func,Matrix(rep(0,nrow(iso.has.func)*length(col.dif)),nrow=nrow(iso.has.func)))
      
      colnames(iso.has.func)[(ncol(iso.has.func)-length(col.dif)+1):ncol(iso.has.func)]=col.dif    
    }
    
    iso.has.func=iso.has.func[,order(colnames(iso.has.func))]  #order the columns of 'iso.has.func' by name.  Now its has the same columns of 'combined.table'
    
    combined.table=rbind(combined.table,iso.has.func)  #add the rows of the next 'iso.has.func' to the end of 'combined.table'
  }
  
  #next, add the next array of isoform protein sequences to the unified array
  
  prev.length=length(combined.x.seq)
  
  combined.x.seq=c(combined.x.seq,x.seq)
  
  names(combined.x.seq)[(prev.length+1):length(combined.x.seq)]=transcript.ids
  
  #Concatenate the lists of functions that each isoform can choose from (the functions of the gene of each isoform)
  
  combined.isoform.functions=c(combined.isoform.functions,isoform.functions)
  
  #add the next array of transcript IDs to the unified array
  
  combined.transcript.ids=c(combined.transcript.ids,transcript.ids)
  
  #Collect the number of shared functions and local alignment scores, in case we need to re-fit coefficients
  
  next.share.vec=(iso.has.func%*%t(iso.has.func))[lower.tri(seq.sim.mat) & compare.pairs]  #number of shared GO terms between isoform pairs
  
  fit.sample=c(fit.sample,seq.sim.mat[lower.tri(seq.sim.mat) & compare.pairs])  #normalized local alignment scores between pairs of isoforms
  
  share.vec=c(share.vec,next.share.vec)
  
}

#free memory

rm(next.share.vec)

#check convergence

args=commandArgs(trailingOnly = TRUE)  #The argument to this script is the iteration number

if (file.exists('last_lik.txt'))  #If the value of the likelihood was recorded before, i.e. the current script was run before
{
  #write the function assignment for all isoforms into a sparse matrix.  The row names and column names must be written separately.
  
  writeMM(combined.table,'combined_iso_has_func.txt')
  
  write.table(colnames(combined.table),'colnames.txt',sep='\t',col.names = F,row.names = F,quote = F)
  
  write.table(rownames(combined.table),'rownames.txt',sep='\t',col.names = F,row.names = F,quote = F)
  
  #Fetch the likelihood value from the previous iteration
  
  prev.lik=as.numeric(read.table('last_lik.txt',header=F) )
  
  lik.diff=total.lik-prev.lik  #compute the difference of the objective between this iteration and the previous one and print to a file
  
  write.table(total.lik,'last_lik.txt',sep='\t',col.names = F,row.names = F,quote = F)  #print the last likelihood
  
  system(paste0('echo ',lik.diff,' >> lik_diff.txt'))  #print the current likelihood difference at the end of the file lik_diff.txt
  
  dif.log=read.table('lik_diff.txt')   #read all the differences calculated in iterations so far
  
  if (nrow(dif.log)>25)  #If at least 25 iterations completed, we can start checking the convergencew criterion, that is based on the last 25
                          #differences
    
    if(cumsum(dif.log$V1)[nrow(dif.log)]-cumsum(dif.log$V1)[nrow(dif.log)-25]<1/3) #if the cumulative sum of likelihood differences in the last 25 iterations is
    {                                                                              #less than 1/3, we have converged
      print('Algorithm Converged')
      
      share.vec2=share.vec^2  #the square of the number of shared go terms between isoforms
      
      new.coefs=lm(fit.sample~share.vec+share.vec2,method='EM')$coefficients  #fit new quadratic model parameters
      
      if (!file.exists('convergence_log.txt'))  #record the iteration number at which convergence was detected
      {
        write.table(args[1],'convergence_log.txt',col.names = F,row.names = F,quote = F)
      }else{
        prev.conv=read.table('convergence_log.txt',header=F)
        
        if ((as.integer(prev.conv)+1==as.integer(args[1])) || as.integer(prev.conv)==-1)  #if converged two iterations consecutively, optimization completed
        {
          print('Local Maximum Reached')
          
          write.table(-1,'convergence_log.txt',col.names = F,row.names = F,quote = F)  #record that no further optimization is needed
          
          quit('no')
        }else
        {
          write.table(args[1],'convergence_log.txt',col.names = F,row.names = F,quote = F)  #if just the E step converged, record the iteration number
        }
        
        rm(prev.conv)  #free memory
      }
      
    }
  

}else{  #This means it is the first iteration of this script, so just save the unified isoform function assignments and the likelihood value
  
  writeMM(combined.table,'combined_iso_has_func.txt')
  
  write.table(colnames(combined.table),'colnames.txt',sep='\t',col.names = F,row.names = F,quote = F)
  
  write.table(rownames(combined.table),'rownames.txt',sep='\t',col.names = F,row.names = F,quote = F)
  
  write.table(total.lik,'last_lik.txt',sep='\t',col.names = F,row.names = F,quote = F)
  
  
}


#free memory

rm(fit.sample)

rm(share.vec)


#split the isoforms into new subsets

set.seed(as.integer(args[1]))

split.ids.comb=sample(1:number.of.nodes,length(combined.transcript.ids),replace=TRUE)  #randomly assign set labels (1-200 to isoforms)

for (node.number in 1:number.of.nodes)  #for each new set
{
  
  transcript.ids=combined.transcript.ids[which(split.ids.comb==node.number)]  #transcript IDs for this set of isoforms
  
  x.seq=combined.x.seq[which(names(combined.x.seq) %in% transcript.ids)]  #protein sequences forthis set of isoforms
  
  isoform.functions=combined.isoform.functions[which(names(combined.isoform.functions) %in% transcript.ids)]  #candidate functions for this set of isoforms
  
  iso.has.func=combined.table[rownames(combined.table) %in% transcript.ids,]  # assignment of functions to this set of isoforms in the last iteration
  
  if (!is.null(new.coefs))  #if new coefficients were computed, update the variable 'coefs'
    
    coefs=new.coefs
  
  #create an .RData file for the script predict2.R, which will be used when that script is run with argument equal to 'node.number'
  
  save(transcript.ids,x.seq,isoform.functions,iso.has.func,coefs,file=paste0('interpro_state_',node.number,'.RData'))
  
}

