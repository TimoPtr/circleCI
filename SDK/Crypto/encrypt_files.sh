#!/bin/bash

function validate_argument {
if [[ -z "$2" ]]                           # Is parameter #1 zero length?
  then
    echo "Error: $1"      # Or no parameter passed.
    exit 1
  fi
}

while getopts ":i:o:k:r:" option; do
  case ${option} in
    i) input=${OPTARG};;
    o) output=${OPTARG};;
    k) key=${OPTARG};;
    r) key_output=${OPTARG};;
    :)
        echo "Option -$OPTARG requires an argument." >&2
        exit 1
        ;;
  esac
done

validate_argument "Missing input file" ${input}
validate_argument "Missing output file" ${output}
validate_argument "Missing output dir for the key" ${key_output}

mkdir -p ${output}

# If key is not provided, generate it with random secret.
if [[ -z "$key" ]]
  then
    secret=$(openssl rand -hex 32)

    key_prefix="key="
    key_info=$(openssl aes-256-cbc -e -k ${secret} -P | grep ${key_prefix})

    key=${key_info#"$key_prefix"}
  fi

files=()
while IFS= read -r -d $'\0'; do
    files+=("$REPLY")
done < <(find ${input} -maxdepth 1 -mindepth 1 -print0)

# Encrypt each file along with its iv vector
for file in "${files[@]}"; do
  # Compute the file names
  file_name=$(basename ${file})
  encrypted_file="$output/${file_name}_enc"
  iv_file="$output/${file_name}_iv"

  # Generate IV
  iv=$(openssl rand -hex 16)

  # Encrypt
  openssl aes-256-cbc -e -K ${key} -iv ${iv} -in ${file} -out ${encrypted_file}

  # Write iv to file
  echo ${iv} > ${iv_file}
done

# Write the key to a file
echo ${key} > ${key_output}
