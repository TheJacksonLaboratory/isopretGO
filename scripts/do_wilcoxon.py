from collections import defaultdict
import os
import re
import argparse
import math
import numpy as np
from scipy import stats

parser = argparse.ArgumentParser()
parser.add_argument('-i', '--input_dir', help='input directory', type=str)
args = parser.parse_args()
input_dir = args.input_dir
if input_dir is None:
    print("Need to provide path to isorpet file directory with -i argument")
    exit(1)
if not os.path.isdir(input_dir):
    print(f"-i argument ({input_dir}) was not a directory")
    exit(1)
print(f"Searching {input_dir} for isopret files")


def disencombobulate(frac_perc_string):
    """_summary_ parse an isopretGO output string

    Args:
        frac_perc_string (_type_): a string such as 7/29(24.1%)  

    Returns:
        _type_: the elements of the string as int,int,float, e.g., 7, 29, 24.1
    """
    pattern = r"(\d+)\/(\d+)\(([\d.]+)%\)"
    m = re.match(pattern, frac_perc_string)
    if m:
        numerator = int(m.group(1))
        denominator = int(m.group(2))
        perc = float(m.group(3))
        return numerator, denominator, perc
    else:
        return None, None, None

class DgeDasRank:
    def __init__(self, category, go_label, go_id, study, population, pval, adj_pval):
        """_summary_
        category = fields[0]
            go_label = fields[1]
            go_id = fields[2]
            study = fields[3]
            population = fields[4]
            pval = fields[5]
            adj_pval = fields[6]
        Args:
            go_id (_type_): ID of a GO term
            dge_n (int): number of differentially expressed genes
            das_n (int): number of genes with at least one differentially spliced isoform
        """
        self._go_id = go_id
        self._go_label = go_label
        if category == 'DAS':
            n, m, perc = disencombobulate(study)
            self._study_das_n = n
            self._study_das_m = m
            self._study_das_perc = perc
            self._study_dge_n = None
            self._study_dge_m = None
            self._study_dge_perc = None
            if adj_pval is None:
                adj_pval = "1.0"
            self._das_pval = -1* math.log10(float(adj_pval))
            self._dge_pval = None
        elif category == 'DGE':
            n, m, perc = disencombobulate(study)
            self._study_das_n = None
            self._study_das_m = None
            self._study_das_perc = None
            self._study_dge_n = n
            self._study_dge_m = m
            self._study_dge_perc = perc
            if adj_pval is None:
                adj_pval = "1.0"
            self._dge_pval = -1* math.log10(float(adj_pval))
            self._das_pval = None
            
    def add_line(self, category, go_label, go_id, study, population, pval, adj_pval):
        if category == 'DAS':
            n, m, perc = disencombobulate(study)
            if self._study_das_n is not None:
                raise ValueError(f"Attempt to add DAS data twice for {go_label}")
            self._study_das_n = n
            self._study_das_m = m
            self._study_das_perc = perc
            self._das_pval = -1* math.log10(float(adj_pval))
        elif category == 'DGE':
            n, m, perc = disencombobulate(study)
            if self._study_dge_n is not None:
                raise ValueError(f"Attempt to add DGE data twice for {go_label}")
            self._study_dge_n = n
            self._study_dge_m = m
            self._study_dge_perc = perc
            self._dge_pval = -1* math.log10(float(adj_pval))
            
    def get_das_percentage(self):
        return self._study_das_perc
            
    def get_dge_percentage(self):
        return self._study_dge_perc
    
    def get_das_pvalue(self):
        if self._das_pval is None:
            return 0.0
        else:
            return self._das_pval
        
    def get_dge_pvalue(self):
        if self._dge_pval is None:
            return 0.0
        else:
            return self._dge_pval
               
    def get_go_id(self):
        return self._go_id


def get_all_isopret_output_files(indir):
    onlyfiles = [os.path.join(indir, f) for f in os.listdir(indir) if os.path.isfile(os.path.join(indir, f))]
    return [f for f in onlyfiles if f.endswith(".tsv")]




           

go_id_to_label_d = defaultdict()
das_pval_d = defaultdict(list)
dge_pval_d = defaultdict(list)


isopret_files = get_all_isopret_output_files(args.input_dir)
print(f"We got {len(isopret_files)} input files")

for fname in isopret_files:
    das_d = defaultdict(DgeDasRank)
    with open(fname) as f:
        for line in f:
            # print(line)
            fields = line.strip().split('\t')
            category = fields[0]
            go_label = fields[1]
            go_id = fields[2]
            go_id_to_label_d[go_id] = go_label
            study = fields[3]
            population = fields[4]
            pval = fields[5]
            adj_pval = fields[6]
            if go_id in das_d:
                dgedas = das_d.get(go_id)
                dgedas.add_line(category, go_label, go_id, study, population, pval, adj_pval)
            else:
                das_d[go_id] = DgeDasRank(category, go_label, go_id, study, population, pval, adj_pval)
    for dgedas in das_d.values():
        go_id = dgedas.get_go_id()
        das = dgedas.get_das_pvalue()
        dge = dgedas.get_dge_pvalue()
        das_pval_d[go_id].append(das)
        dge_pval_d[go_id].append(dge)
    
N = len(das_pval_d)

for go_id in das_pval_d.keys():
    das_pval_list = das_pval_d.get(go_id)
    dge_pval_list = dge_pval_d.get(go_id)
    go_label = go_id_to_label_d.get(go_id)
    T, p = stats.wilcoxon(das_pval_list, dge_pval_list, zero_method='zsplit', correction=False, alternative='two-sided')
    if p*N < 0.05:
        mean_das = np.mean(das_pval_list)
        mean_dge = np.mean(dge_pval_list)
        #if mean_das > mean_dge:
        print(f"{go_label} ({go_id}): T={T}; p={p} (Bonferroni); mean DAS: {mean_das}; mean DGE: {mean_dge}")

    












