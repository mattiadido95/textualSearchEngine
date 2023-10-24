#!/bin/bash

# Definisci il percorso della cartella
cartella="data/collection"

# Controlla se la cartella esiste
if [ ! -d "$cartella" ]; then
    echo "La cartella $cartella non esiste. Creazione della cartella."
    echo "Scarico i seguenti file: queries.tar.gz, qrels.dev.tsv, collection.tar.gz"
    mkdir -p "$cartella"

    wget https://msmarco.blob.core.windows.net/msmarcoranking/queries.tar.gz -P "$cartella"
    wget https://msmarco.blob.core.windows.net/msmarcoranking/qrels.dev.tsv -P "$cartella"
    wget https://msmarco.blob.core.windows.net/msmarcoranking/collection.tar.gz -P "$cartella"

    # Decomprimi il file scaricato
    echo "Decomprimo il queries.tar.gz."
    tar -xzvf "$cartella/queries.tar.gz" -C "$cartella"

    # Rimuovi i file decompressi se esistono
    if [ -f "$cartella/queries.eval.tsv" ]; then
        echo "Rimuovo queries.eval.tsv"
        rm "$cartella/queries.eval.tsv"
    fi

    if [ -f "$cartella/queries.train.tsv" ]; then
        echo "Rimuovo queries.train.tsv"
        rm "$cartella/queries.train.tsv"
    fi

    if [ -f "$cartella/queries.tar.gz" ]; then
        echo "Rimuovo queries.tar.gz"
        rm "$cartella/queries.tar.gz"
    fi

    echo "Operazione completata. Premi INVIO per continuare..."
    read -n 1 -s
else
    echo "La cartella $cartella esiste già. Premi INVIO per continuare..."
    read -n 1 -s
fi

print_menu() {
  clear
  echo "Select an option:"
  echo "1. Run indexing program."
  echo "2. Run prompt program."
  echo "3. Exit"
}

compile_index() {
  echo "Enter parameters for indexing:"
  echo "List of params: "
  echo ""
  echo "-compressed: Enable compressed reading of the collection in tar.gz format. Default: uncompressed reading."
  echo ""
  echo "-stemmer: Enable Porter Stemming in document preprocessing. Default: disabled."
  echo ""
  read -p "Parameters: " params
  java -jar out/artifacts/index_jar/index.jar $params
  read -p "Press ENTER to continue..."
}

start_prompt() {
  echo "Enter parameters for the prompt:"
  echo "List of params:"
  echo ""
  echo "-scoring <value>: Specify the scoring function [BM25, TFIDF]. Default: TFIDF."
  echo ""
  echo "-topK <value>: Specify the number of documents to return. Default: 10."
  echo ""
  echo "-dynamic: Enable dynamic pruning using MAXSCORE. Default: disabled."
  echo ""
  echo "-conjunctive: Enable conjunctive mode. Default: disjunctive."
  echo ""
  read -p "Parameters: " params
  java -jar out/artifacts/prompt_jar/prompt.jar $params
  read -p "Press ENTER to continue..."
}

while true; do
  print_menu
  read -p "Select one option: " choice

  case $choice in
    1)
      compile_index
      ;;
    2)
      start_prompt
      ;;
    3)
      echo "Bye!"
      exit 0
      ;;
    *)
      echo "Wrong option! Try again."
      read -p "Press ENTER to continue..."
      ;;
  esac
done
