#!/bin/bash

print_menu() {
  clear
  echo "Select an option:"
  echo "1. Run indexing program."
  echo "2. Run prompt program."
  echo "3. Exit"
}

compile_index() {

  # echo "Avvio la compilazione di index.jar..."
  # Sostituisci con il comando di compilazione effettivo
  # Esempio: javac -jar index.jar <parametri>
  # Esempio: java -jar index.jar <parametri>
  # Esempio: ./index.jar <parametri>
  # Assicurati di aggiungere i parametri necessari
  # Esempio: java -jar index.jar parametro1 parametro2
  # Esempio: ./index.jar parametro1 parametro2

  echo "Enter parameters for indexing:"
  echo "params: -compressed -stemmer"
  echo "<compressed> Enable compressed reading of the collection in tar.gz format. Default: uncompressed reading."
  echo "<stemmer> Enable Porter Stemming in document preprocessing. Default: disabled."
  read -p "Parameters: " params
  java -jar out/artifacts/index_jar/index.jar $params
  read -p "Press ENTER to continue..."
}

start_prompt() {

  # echo "Avvio prompt.jar..."
  # Sostituisci con il comando di avvio effettivo
  # Esempio: java -jar prompt.jar
  # Esempio: ./prompt.jar
  # Assicurati di aggiungere i parametri necessari

  echo "Enter parameters for the prompt:"
  echo "params: -TFIDF or BM25 -K -mode -dynamic -stemmer"
  echo "<TFIDF/BM25> Specify the scoring function [BM25, TFIDF]. Default: TFIDF.."
  echo "<K> Specify the number of documents to return. Default: 10."
  echo "<conjunctive> Enable conjunctive mode. Default: disjunctive."
  echo "<dynamic> Enable dynamic pruning using MAXSCORE. Default: disabled."
  echo "<stemmer> Enable Porter Stemming in query preprocessing NOTE: MUST MATCH THE OPTION USED IN index.java. Default: disabled."
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
