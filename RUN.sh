#!/bin/bash

print_menu() {
  clear
  echo "Select an option:"
  echo "1. Run indexing program."
  echo "2. Run prompt program."
  echo "3. Exit"
}

compile_index() {
  echo "Enter parameters for indexing:"
  echo "params: "
  echo "-compressed: Enable compressed reading of the collection in tar.gz format. Default: uncompressed reading."
  echo "-stemmer: Enable Porter Stemming in document preprocessing. Default: disabled."
  read -p "Parameters: " params
#  java -jar out/index-1.0-SNAPSHOT.jar $params
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
