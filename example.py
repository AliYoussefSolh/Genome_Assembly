import debruijn as db

# Main script
fname = 'test.fa'
reads = db.read_reads(fname)
# print reads

test = ['bcdefg', 'defghi', 'abcd']
# g = construct_graph(test, 3)
g = db.construct_graph(reads, 2)
# print_graph(g)
# for k in g.keys():
#   print k, g[k]
# g = construct_graph(reads)
contig = db.output_contigs(g)
print (contig)
