library(data.table)

library(goseq)

library(Biostrings)

library(Rcpi)

library(Matrix)

library(GA)

library(bestNormalize)

#This script optimizes isoform function assignment to predict local alignment between isoforms optimally.  There are 200 instances of this script
#that are executed each iteration, each handles a different subset of isoforms.


num.cores=4  #The number of cores that will be used by this script


#The following function calculates the fitness, i.e. minus the sum of absolute residuals, of the regression model
#for a given assignment of GO terms to isoforms(the input parameter sol). The prefix 'ga' is used because the optimization of function
#assignment is performed by a Genetic Algorithm (appears later in this script)

ga.fitness=function(sol)
{
  #This is a product of a the isoformsXGOP terms Boolean matrix and its transpose.  The resulting isoformsXisoforms matrix at entry [i,j]
  #contains the number of GO terms shared by isoforms i and j.  These values correspond to the independent variable in the model.
  
  number.shared.functions=Matrix::tcrossprod(Matrix::sparseMatrix(i=rep(1:length(transcript.ids),start.funcs)[sol==1],
                                                                  j = unlist(isoform.functions)[sol==1],dims=c(nrow(iso.has.func),ncol(iso.has.func))),boolArith=F)
  
  #Restrict the data only to isoforms whose genes share at least one GO function.  Since the isoformsXisoforms matrix is symmetrix, we only need
  #it lower triangle for calculating the residuals
  
  b=number.shared.functions[lower.tri(number.shared.functions) & compare.pairs]
  
  
  #The normalized local alignment scores between the isoforms, used as the dependent variabe in the model.
  
  a=seq.sim.mat[lower.tri(seq.sim.mat) & compare.pairs]
  
  #The values predicted by the model for all the data points in b
  
  v1=(b^2)*coefs[3]+b*coefs[2]+coefs[1]
  
  #Return the negative sum of absolute residuals
  
  -sum(abs(v1-a))
  
}


#The following function accepts a list of protein sequences (the products of isoforms), and returns their local alignment matrrix.
#This function and the one it calls(appears after it in the script) are mostly copied from another package, I just removed their normalization
#to get the textbook local alignment scores and not a number between 0 and 1 as in the original code.

calcParProtSeqSim=function (protlist, cores = 2, type = "local", submat = "BLOSUM62")
{
  doParallel::registerDoParallel(cores)
  idx = combn(1:length(protlist), 2)
  seqsimlist = vector("list", ncol(idx))
  seqsimlist <- foreach(i = 1:length(seqsimlist), .errorhandling = "pass") %dopar%
  {
    tmp <- IPcalcSeqPairSim(rev(idx[, i]), protlist = protlist,
                            type = type, submat = submat)
  }
  seqsimmat = matrix(0, length(protlist), length(protlist))
  for (i in 1:length(seqsimlist)) seqsimmat[idx[2, i], idx[1,
                                                           i]] = seqsimlist[[i]]
  seqsimmat[upper.tri(seqsimmat)] = t(seqsimmat)[upper.tri(t(seqsimmat))]
  diag(seqsimmat) = 1
  return(seqsimmat)
}

IPcalcSeqPairSim=function (twoid,protlist = protlist, type = type, submat = submat)
{
  id1 = twoid[1]
  id2 = twoid[2]
  if (protlist[[id1]] == "" | protlist[[id2]] == "") {
    sim = 0L
  }
  else {
    s1 = try(Biostrings::AAString(protlist[[id1]]), silent = TRUE)
    s2 = try(Biostrings::AAString(protlist[[id2]]), silent = TRUE)
    s12 = try(Biostrings::pairwiseAlignment(s1, s2, type = type,
                                            substitutionMatrix = submat, scoreOnly = TRUE),
              silent = TRUE)
    
    if (is.numeric(s12) == FALSE ) {
      sim = 0L
    }else {
      sim = s12
    }
  }
  return(sim)
}


pop.size=50  #Population side for the Genetic Algorithm that modifies the isoform function assignment to optimize the fitness funtion

number.of.nodes=200  #This is the number of groups that the isoforms will be split into in order to speed up computation

args=commandArgs(trailingOnly = TRUE)  #The argument to this script is an integer between 1 and 200, and it gives the group of isoforms that
                                        #the machine that called the script will process

node.number=as.integer(args[1])  #convert the argument from string to integer

if (file.exists('convergence_log.txt'))  #if the algorithm converged (determined by a master script, combine_tables.R, that runs after all 200 instances of this script finished) don't do anything
{
  if (as.integer(read.table('convergence_log.txt',header=F))==-1)
    
    quit('no')
  
}

if (!file.exists(paste0('interpro_state_',node.number,'.RData')))  #if this is the first iteration of the algorithm
{
  
  set.seed(123)  #set a randon seed
  
  init.coefs=c(-0.1,0.05,0)  #start from an initial guess for the quadratic model coeffcients
  
  #init
  
  coefs=init.coefs
  
  #The following lines read the GTF files with all genes and isoforms, and extract their Ensembl IDs
  
  gtf.file=fread('/projects/robinson-lab/USERS/karleg/projects/lps/sra/star_files/Homo_sapiens.GRCh38.91.gtf',sep='\t',quote = '',data.table = FALSE)
  
  gtf.file=gtf.file[gtf.file$V3=='transcript',]
  
  transcript.ids=gsub(';','',unlist(lapply(strsplit(as.character(gtf.file[,9]),split=' '),'[[',6)))
  
  transcript.ids=gsub("\"",'',transcript.ids)
  
  gene.ids=gsub(';','',unlist(lapply(strsplit(as.character(gtf.file[,9]),split=' '),'[[',2)))
  
  gene.ids=gsub("\"",'',gene.ids)
  
  #Using the gene ensembl IDs from above, we obtain each gene's GO terms
  
  gene.functions=getgo(unique(gene.ids),'hg38','ensGene',fetch.cats="GO:BP")
  
  #remove GO terms that are common to 10% of the genes or more
  
  remove.go=names(table(unlist(gene.functions))[table(unlist(gene.functions))>=length(unique(gene.ids))/10])
  
  gene.functions=lapply(gene.functions,function(l)l[-which(l %in% remove.go)]) #removing GO terms that are too common
  
  #check that we have a protein sequence for each isoform, if not (non-coding) remove its ID
  
  sequence.exists=transcript.ids %in% gsub('translated_','',gsub('.fa','',list.files('/projects/robinson-lab/USERS/karleg/projects/isopret/isoform_seqs/')))
  
  gene.ids=gene.ids[sequence.exists]
  
  transcript.ids=transcript.ids[sequence.exists]
  
  # For each isoform ID make sure that it is not duplicated, that it belongs to the group processed by this instance of the script (node.number) 
  # and that the protein code does not contain characters other than amino acids
  
  unique.iso=!duplicated(transcript.ids) & ((1:length(transcript.ids))%%number.of.nodes+1==node.number)  & unlist(mclapply(transcript.ids,function(x){
    
    seq=(readAAStringSet(paste0('/projects/robinson-lab/USERS/karleg/projects/isopret/isoform_seqs/translated_',x,'.fa')))
    
    if (length(seq)==0)
      
      return(FALSE)
    
    if (sum(!names(table(strsplit(as.character(seq),split=''))) %in% c("A", "C" ,"D" ,"E" ,"F", "G", "H" ,"I" ,"K" ,"L" ,"M", "N" ,"P", "Q", "R","S" ,"T", "V" ,"W" ,"Y"))>0)
      
      return(FALSE)
    
    TRUE
    
  },mc.cores = num.cores))
  
  #remove gene and isoformn IDs that do not satisfy the check above
  
  gene.ids=gene.ids[unique.iso]
  
  transcript.ids=transcript.ids[unique.iso]
  
  #free memory
  
  rm(gtf.file)
  
  #for each isoform, list all the GO terms that it may be assigned, i.e. the GO terms that belong to its gene
  
  isoform.functions=gene.functions[gene.ids]
  
  names(isoform.functions)=transcript.ids
  
  #remove isoforms that did not have a GO term
  
  isoform.functions=isoform.functions[!(unlist(lapply(isoform.functions,is.null)) | unlist(lapply(isoform.functions,length))==0)]
  
  #keep only IDs of isoforms that have at least one function
  
  transcript.ids=names(isoform.functions)
  
  #free memory
  
  rm(gene.functions)
  
  #Create a binary matrix of isoform IDS X GO terms, so that entry i,j is 1 if isoform i has GO term j, otherwise 0
  
  iso.has.func=do.call(cbind,mclapply(unique(unlist(isoform.functions)),function(x){
    
    Matrix(as.integer(unlist(lapply(isoform.functions,function(l)x %in% l))),ncol=1)
    
  },mc.cores = num.cores))
  
  colnames(iso.has.func)=unique(unlist(isoform.functions))
  
  rownames(iso.has.func)=names(isoform.functions)
  
  print(paste0('Num isoforms: ',length(transcript.ids)))
  
  #free memory
  
  rm(sequence.exists)
  
  rm(unique.iso)
  
  #read the protein sequences for all the isoforms
  
  x.seq=mclapply(paste0('/projects/robinson-lab/USERS/karleg/projects/isopret/isoform_seqs/translated_',transcript.ids,'.fa'),function(x)as.character(readFASTA(x)),mc.cores = num.cores)
  
  print('Read sequences')
  
  #calculate local alignment between all pairs of isoforms
  
  seq.sim.mat=calcParProtSeqSim(x.seq, cores = num.cores, type = "local", submat = "BLOSUM62")
  
  #Use integers instead of strings for identifying GO terms
  
  isoform.functions=mclapply(isoform.functions,function(l)which(colnames(iso.has.func) %in% l),mc.cores = num.cores) 
  #loop

  #sugg is the initial assignment of functions to isoforms, since this is the first iteration it is set to NULL
  
  sugg=NULL
  
}else{
  
  #If this is not the first iteration, load the information from the last iteration.  This will include the isoform function assignments to
  #all the isoforms that were randomly chosen by combine_tables.R for the subset processed by this instance of the script
  
  load(paste0('interpro_state_',node.number,'.RData'))
  
  #Use integers instead of strings for identifying GO terms
  
  isoform.functions=mclapply(isoform.functions,function(l)which(colnames(iso.has.func) %in% l),mc.cores = num.cores) 
  
  #Set the initial values of a solution, i.e. the isform GO term assignment from end of the previous iteration
  
  sugg=c()
  
  for (iso.itr in 1:length(transcript.ids))
    
    sugg=c(sugg,iso.has.func[iso.itr,isoform.functions[[iso.itr]]])  #this concatenates for each isoform the values of the 
                                                                      #Boolean matrix 'iso.has.func' that indicate whether it has each of the GO terms
                                                                      #of the gene that contains it
  
  #Calculate the local alignment scores between all pairs of isoforms.  This is needed because in each iterations a new set of isoform is being processed
  
  seq.sim.mat=calcParProtSeqSim(x.seq, cores = num.cores, type = "local", submat = "BLOSUM62")
  
}

print('Starting optimization')

total.length=length(unlist(isoform.functions))  #The sum of the number of GO terms that each isoform can have.  This is the length of a solution,
                                                #because for each isoform we have to specify which of the candidate GO terms are assigned to it

start.funcs=unlist(lapply(isoform.functions,function(l)length(l)))  #The maximal number of functions each isoform can have

#Next we compute the pairs of isoforms whose genes share at least one GO term.  These are stored in a logical isoforms X isoforms matrix

compare.pairs=do.call(cbind,mclapply(unique(unlist(isoform.functions)),function(x){
  
  matrix(as.integer(unlist(lapply(isoform.functions,function(l)x %in% l))),ncol=1)
  
},mc.cores = num.cores))

compare.pairs=(compare.pairs%*%t(compare.pairs))>0

#Apply yeo-johnson normalization to the local alignment values and update them in the matrix 'seq.sim.mat'

t.data=yeojohnson(seq.sim.mat[lower.tri(seq.sim.mat) & compare.pairs])$x.t

seq.sim.mat[lower.tri(seq.sim.mat) & compare.pairs]=t.data

#Run a genetic algorithm that will optimize the assignment of functions to isoforms such that the model fit, returned by the functions gs.fitness,
#is optimal.

res.ga=ga(type = 'binary',fitness = ga.fitness,nBits = total.length,maxiter = 200,popSize = pop.size,suggestions = sugg,parallel=num.cores)
    
#Next we extract the solution into a new matrix 'iso.has'func', so that the GO term assignment that it chose will be passed to the next iteration

col.names.ihf=colnames(iso.has.func)

row.names.ihf=rownames(iso.has.func)

iso.has.func=Matrix(0,nrow=nrow(iso.has.func),ncol=ncol(iso.has.func))  #Start with a matrix of zeroes

if (sum(res.ga@solution[1,])>0)

  #Wherever the value of the solution is 1 (a function is assigned to an isoform) , set the value of the matrix 'iso.has.func'
  #to 1 in the row that corresponds to the isoform and the column that corresponds to the GO function
   
  iso.has.func[cbind(rep(1:length(transcript.ids),start.funcs)[res.ga@solution[1,]==1],unlist(isoform.functions)[res.ga@solution[1,]==1])]=1

colnames(iso.has.func)=col.names.ihf

rownames(iso.has.func)=row.names.ihf

#Transform the GO terms from integers back to strings, for the master script's use

isoform.functions=mclapply(isoform.functions,function(l)colnames(iso.has.func)[l],mc.cores = num.cores) 

save.image(paste0('interpro_state_',node.number,'.RData'))   #save the state for the master script's use
