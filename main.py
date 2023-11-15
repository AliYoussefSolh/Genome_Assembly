import debruijn as db

reads_file = './input/reads1.txt'
# Read reads from file
reads = db.read_reads(reads_file)

# Construct De Bruijn Graph
k = 31
g = db.construct_graph(reads, k)

# Build the Genome
contig = db.output_contigs(g)

# Print Genome
print (contig)