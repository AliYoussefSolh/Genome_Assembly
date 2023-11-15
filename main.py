import debruijn as db

reads_file = './input/reads1.txt'
# Read reads from file
reads = db.read_reads(reads_file)

# Construct De Bruijn Graph
k = 40
g = db.construct_graph(reads, 31)

# Build the Genome
contig = db.output_contigs(g)

# Print Genome
print (contig)