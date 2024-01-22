#!/bin/bash
#SBATCH --time=72:00:00
#SBATCH --job-name="pps"
#SBATCH -o ppr-%j.out
#SBATCH -e ppr-%j.err 
#SBATCH --mail-user=guy.karlebach@jax.org
#SBATCH --mail-type=BEGIN,END,FAIL
#SBATCH -N 1
#SBATCH -n 33
#SBATCH --mem-per-cpu=16G
#SBATCH --array=21

mkdir /flashscratch/fastq_$SLURM_ARRAY_TASK_ID

cd /flashscratch/fastq_$SLURM_ARRAY_TASK_ID

module load singularity

cp $SLURM_SUBMIT_DIR/Snakefile .

singularity exec /projects/chesler-lab/jcpg/snakemake/sing.sif bash $SLURM_SUBMIT_DIR/run_snakemake.sh $SLURM_SUBMIT_DIR/case_control_c${SLURM_ARRAY_TASK_ID}.tsv /flashscratch/fastq_$SLURM_ARRAY_TASK_ID
















	







