library(seqinr)

library(Biostrings)

library(data.table)


#Change the following paths as needed:

path.to.gtf='/Users/karleg/STAR/STAR/bin/MacOSX_x86_64/data/GRCh38/annotation/Homo_sapiens.GRCh38.91.gtf'

fasta.file='/Users/karleg/STAR/STAR/bin/MacOSX_x86_64/data/GRCh38/sequence/GRCh38_r91.all.fa'

path.to.gffread='gffread'

#In order to use the gffread tool for translating isoforms we need to read the GTF file:

gtf.file=fread(path.to.gtf,sep='\t',quote = '',data.table = FALSE)

gtf.file=gtf.file[gtf.file$V3=='exon',]

transcript.ids=gsub(';','',unlist(lapply(strsplit(as.character(gtf.file[,9]),split=' '),'[[',6)))

transcript.ids=gsub("\"",'',transcript.ids)

#The following path to the genomic sequence is passed to the gffread tool in order for it to generate the protein sequence:

input=mclapply(unique(transcript.ids),function(isoform.id)
{
  tr.gtf=gtf.file[transcript.ids == isoform.id,]
  
  write.table(tr.gtf,paste0("transcript",isoform.id,".gtf"),sep='\t',col.names = FALSE,row.names = FALSE,quote = FALSE)
  
  system(paste0(path.to.gffread,' -y isoform_seqs/translated_',isoform.id,'.fa -g ',fasta.file," transcript",isoform.id,".gtf"))
  
  system(paste0("rm transcript",isoform.id,".gtf"))
  
},mc.cores = 32)