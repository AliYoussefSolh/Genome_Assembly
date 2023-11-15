from collections import defaultdict
import pickle
class TrieNode:
    def __init__(self):
        self.children = {}
        self.is_leaf = False

class Trie:
    def __init__(self):
        self.root = TrieNode()

    def add_word(self, word):
        node = self.root
        for symbol in word:
            if symbol in node.children:
                node = node.children[symbol]
            else:
                new_node = TrieNode()
                node.children[symbol] = new_node
                node = new_node
        node.is_leaf = True

def prefix_trie_matching(text, trie):
    symbol = text[0]
    v = trie.root
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

def trie_matching(text, trie):
    positions = defaultdict(list)

    for i in range(len(text)):
        pattern_matchings = prefix_trie_matching(text[i:], trie)
        for pattern in pattern_matchings:
            positions[pattern].append(i)

    return positions

def build_trie(patterns):
    trie = Trie()
    for pattern in patterns:
        trie.add_word(pattern)
    return trie

def save_trie_to_file(trie, filename):
    with open(filename, 'wb') as file:
        pickle.dump(trie, file)

def load_trie_from_file(filename):
    with open(filename, 'rb') as file:
        trie = pickle.load(file)
    return trie
def print_trie(node, level=0):
    if not node:
        return

    indent = '  ' * level
    print(indent + f"Node (Level {level}): {'Leaf' if node.is_leaf else 'Non-leaf'}")

    for symbol, child_node in node.children.items():
        print(indent + f"  Symbol: {symbol}")
        print_trie(child_node, level + 1)




def trie_matching(text, trie):
    positions = defaultdict(list)

    for i in range(len(text)):
        pattern_matchings = prefix_trie_matching(text[i:], trie)
        for pattern in pattern_matchings:
            positions[pattern].append(i)

    return positions

def main():
    genome_file_path = r"C:\Users\user\OneDrive\Desktop\Rana\AUB\Fall 2023\Bio Informatics\Project\test\genome.txt"
    with open(genome_file_path, "r") as genome_file:
        text = ''.join(genome_file.read().splitlines())

    # Read patterns from reads.txt
    reads_file_path = r"C:\Users\user\OneDrive\Desktop\Rana\AUB\Fall 2023\Bio Informatics\Project\test\reads.txt"
    with open(reads_file_path, "r") as reads_file:
        patterns = [line.strip() for line in reads_file]

    trie = build_trie(patterns)

    # Save the trie to a file
    save_trie_to_file(trie, "trie.pkl")

    # Uncomment the following line to load the trie from the file
    # trie = load_trie_from_file("trie.pkl")

    positions = trie_matching(text, trie)


    result_sequence = ""
    previous_end = 0

    for pattern, start_positions in positions.items():
        start_position = start_positions[0]
        end_position = start_position + len(pattern)
        overlap_length = 0
        # Check for overlap
        if start_position < previous_end:
            overlap_length = previous_end - start_position
            if start_position + overlap_length < end_position:
                result_sequence += text[start_position + overlap_length:end_position]
        else:
            result_sequence += text[start_position:end_position]

        previous_end = max(previous_end, end_position - overlap_length)

    with open("output.txt", "w") as output_file:
        output_file.write(result_sequence)


main()