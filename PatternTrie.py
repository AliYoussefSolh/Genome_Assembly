from collections import defaultdict


class TrieNode:
    def __init__(self):
        self.children = {}
        self.is_leaf = False


def build_trie(patterns):
    root = TrieNode()
    for pattern in patterns:
        node = root
        for symbol in pattern:
            if symbol in node.children:
                node = node.children[symbol]
            else:
                new_node = TrieNode()
                node.children[symbol] = new_node
                node = new_node
        node.is_leaf = True
    return root


def prefix_trie_matching(text, trie):

    symbol = text[0]
    v = trie
    i = 0

    pattern_matchings = []

    while True:
        if v.is_leaf:
            pattern_matchings.append(text[:i])

        if symbol in v.children:
            v = v.children[symbol]
            i += 1
            if i < len(text):
                symbol = text[i]
        else:
            return pattern_matchings


def trie_matching(text, patterns):
    trie = build_trie(patterns)
    positions = defaultdict(list)

    for i in range(len(text)):
        pattern_matchings = prefix_trie_matching(text[i:], trie)
        for pattern in pattern_matchings:

            positions[pattern].append(i)

    return positions


def main():
    # Read text from genome.txt
    genome_file_path = r"C:\Users\user\OneDrive\Desktop\Rana\AUB\Fall 2023\Bio Informatics\Project\genome.txt"
    with open(genome_file_path, "r") as genome_file:
        text = ''.join(genome_file.read().splitlines())

    # Read patterns from reads.txt
    reads_file_path = r"C:\Users\user\OneDrive\Desktop\Rana\AUB\Fall 2023\Bio Informatics\Project\reads.txt"
    with open(reads_file_path, "r") as reads_file:
        patterns = [line.strip() for line in reads_file]



    positions = trie_matching(text, patterns)

    with open("output.txt", "w") as output_file:
        for pattern, position in positions.items():

            output_file.write(f"{pattern}: {' '.join(map(str, position))}\n")

main()
