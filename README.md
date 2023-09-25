# TEXTUAL SEARCH ENGINE
 
## Descrizione

Inserisci una descrizione più dettagliata del tuo progetto qui. Spiega di cosa si tratta, perché è importante e come funziona. Fornisci contesto agli utenti che visitano il tuo repository GitHub per la prima volta.

## Modules

The project is divided into 2 modules:

- INDEX
- PROMPT

### INDEX
The Index module implements the main program for building an inverted index from a collection of documents. This module takes care of building the lexicon and index of documents.
AGGIUNGERE DECSRIZIONE OPZIONI 
### PROMPT
The Prompt module implements the main program for querying the index built by the Index module. This module takes care of querying the index and returning the results to the user.
AGGIUNGERE DESCRIZIONE OPZIONI

## Requirements
minimum java version is 17 

## Installation

### Clone the repository
```shell
git clone https://github.com/tuonome/textualSearchEngine.git
cd textualSearchEngine
```
### Download the dat
You must use the document collection available on this page:
https://microsoft.github.io/msmarco/TREC-Deep-Learning-2020. Scroll down
the page until you come to a section titled “Passage ranking dataset”, and download the first link in the table, collection.tar.gz. Note that this is a collection of
8.8M documents, also called passages, about 2.2GB in size. Put the collection in the folder textualSearchEngine/data/collection/ and unzip it.
### Download the queries
You must use the queries available on this page: https://msmarco.blob.core.windows.net/msmarcoranking/queries.tar.gz. Put the queries in the folder textualSearchEngine/data/collection/ and unzip them.
### Download the qrels
You must use the qrels available on this page: https://msmarco.blob.core.windows.net/msmarcoranking/qrels.dev.tsv. Put the qrels in the folder textualSearchEngine/data/collection/ and unzip them.
## Run the program
```shell
cd textualSearchEngine
bash run.sh
```

Once run.sh has started you will be able to choose from the following menu:
```shell
Select an option:
1. Run indexing program.
2. Run prompt program.
3. Exit
```

Select option 1 to start the indexing program.
### Index
```shell
DA COMPLETARE
```

Select option 2 to start the prompt program. You will have to enter the parameters choosing from the following options
### Prompt
```shell
Enter parameters for the prompt:
List of params:
-scoring <value>: Specify the scoring function [BM25, TFIDF]. Default: TFIDF-topK: Specify the number of documents to return. Default: 10
-dynamic: Enable dynamic pruning using MAXSCORE. Default: disabled
-conjunctive: Enable conjunctive mode. Default: disjunctive
-stemmer: Enable Porter Stemming in query preprocessing NOTE: MUST MATCH THE OPTION USED IN index.java. Default: disabled
```
Once the parameters have been entered, you will be able to choose from the following menu:
```shell
Welcome to the search engine!
MENU:
- insert 1 to search 
- insert 2 to evaluate searchEngine 
- insert 3 calculate TUBs for dynamic pruning 
- insert 10 to exit
```