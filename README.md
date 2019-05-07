# eeris-lda
Summarization in the field of Smart Metering Data Privacy and Security using LDA.

# About
This repository is created in the context of a research work in the eeRIS project. A corpus of paper publications, downloaded from Elsevier Scopus, is provided as input to an LDA model in order to extract its latent topic structure. The estimated topics and the respective document-topic distributions are used to create a semantic interpretation of the field from a multi-angle perspective.

# Technologies
The utilized technologies are summarized below:
- PDFBox: parsing of the input PDF documents
- Elasticsearch: NL pre-processing and storage of the parsed document collection
- JGibbLDA: LDA implementation

# Input

## Dataset
The input dataset is comprised of paper publications in the form of PDF documents. In addition to the PDF document files, a meta-info file, namely dataset.ris, must be provided to the algorithm in order for the PDF documents to be parsed and indexed in Elasticsearch. The PDF document files along with the dataset.ris file must be placed in *dataset* directory.

## LDA parameters
LDA model's parameters for estiamtion must be provided inside the code by modifying the *App* class. The code is currently implemented so it runs multiple experiments with various parameter values (alpha, beta, K). For each such iteration, the code overwrites a .properties file placed inside *src/main/resources* directory. The parameters' values are then imported in subsequent stages of the implementation.

## Elasticsearch properties
Elasticsearch index properties must be provided in the form of a .properties file which resides in *src/main/resources* directory. This file has already been configured; no change is required.

# Output
The code generates a number of output files inside *output* directory. These files contain information about the topic-word and document-topic distributions, the topic proportions in the dataset, as well as other LDA model's values that JGibbLDA produces. Another output file is generated in the root directory and includes log-likelihood and perplexity values.
