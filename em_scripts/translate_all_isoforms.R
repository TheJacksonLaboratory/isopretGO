library(seqinr)

library(Biostrings)

library(data.table)

gtf.file=fread('/Users/karleg/STAR/STAR/bin/MacOSX_x86_64/data/GRCh38/annotation/Homo_sapiens.GRCh38.91.gtf',sep='\t',quote = '',data.table = FALSE)

gtf.file=gtf.file[gtf.file$V3=='exon',]

transcript.ids=gsub(';','',unlist(lapply(strsplit(as.character(gtf.file[,9]),split=' '),'[[',6)))

transcript.ids=gsub("\"",'',transcript.ids)

fasta.file='/Users/karleg/STAR/STAR/bin/MacOSX_x86_64/data/GRCh38/sequence/GRCh38_r91.all.fa'

input=mclapply(unique(transcript.ids),function(isoform.id)
{
  tr.gtf=gtf.file[transcript.ids == isoform.id,]
  
  write.table(tr.gtf,paste0("transcript",isoform.id,".gtf"),sep='\t',col.names = FALSE,row.names = FALSE,quote = FALSE)
  
  system(paste0('gffread -y isoform_seqs/translated_',isoform.id,'.fa -g ',fasta.file," transcript",isoform.id,".gtf"))
  
  system(paste0("rm transcript",isoform.id,".gtf"))
  
},mc.cores = 32)