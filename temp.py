# Combine the lines from the reads 

def combine_lines_and_save(input_file_path, output_file_path):
    try:
        # Read the input file
        with open(input_file_path, 'r') as file:
            lines = file.readlines()

        # Combine lines into one line
        single_line = ''.join(line.strip() for line in lines)

        # Write the single line to the output file
        with open(output_file_path, 'w') as file:
            file.write(single_line)

        print("File processed successfully.")

    except Exception as e:
        print(f"An error occurred: {e}")

# Example usage
input_file = 'genome.txt'  # Replace with your input file path
output_file = 'new_genome.txt'  # Replace with your desired output file path

combine_lines_and_save(input_file, output_file)
