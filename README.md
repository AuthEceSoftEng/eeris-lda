# eeris-lda
Summarization in the field of Smart Metering Security and Privacy.

# About
This repository is created in the context of a research work in the eeRIS project that aims at identifying categories of techniques in the field of Security and Pivacy in Smart Metering Systems.

The research work conducted in this context is comprised of three parts:
- A field research survey
- A presentation of EU initiatives
- A data-driven analysis of a large corpus of publications aiming at identifying topics (or themes) of the collection

The details and results of this work are presented in this [paper](https://drive.google.com/open?id=1yR6OMbR7BRmrwn_844rWJGFbb349CQZl).

On top of the above, a table of security and privacy techniques for smart metering systems, clustered according to the type/domain of application, is provided [here](https://github.com/AuthEceSoftEng/eeris-lda/blob/master/BAT_EU_TABLE.md).

This table is a product of a two-year assessment of the Best Available Techniques (BATs) for security and privacy in smart metering systems (2014-2016), that was carried out by the Smart Grids Task Force (a consortium set up by the European Commission to advise on issues with regards to smart grids development and deployment) with the assistance of external stakeholders.

# Java Implementation
The code that resides in this repository is part of the data-driven analysis that was conducted in the context of the aforementioned research work. A corpus of paper publications, downloaded from Elsevier Scopus, is provided as input to an LDA model in order to extract its latent topic structure. The estimated topics and the respective document-topic distributions are used to create a semantic interpretation of the field from a multi-angle perspective.

## Technologies
The utilized technologies are summarized below:
- PDFBox: parsing of the input PDF documents
- Elasticsearch: NL pre-processing and storage of the parsed document collection
- JGibbLDA: LDA implementation

## Input

### Dataset
The input dataset is comprised of paper publications in the form of PDF documents. In addition to the PDF document files, a meta-info file, namely dataset.ris, must be provided to the algorithm in order for the PDF documents to be parsed and indexed in Elasticsearch. The PDF document files along with the dataset.ris file must be placed in *dataset* directory.

### LDA parameters
LDA model's parameters for estiamtion must be provided inside the code by modifying the *App* class. The code is currently implemented so it runs multiple experiments with various parameter values (alpha, beta, K). For each such iteration, the code overwrites a .properties file placed inside *src/main/resources* directory. The parameters' values are then imported in subsequent stages of the implementation.

### Elasticsearch properties
Elasticsearch index properties must be provided in the form of a .properties file which resides in *src/main/resources* directory. This file has already been configured; no change is required.

## Output
The code generates a number of output files inside *output* directory. These files contain information about the topic-word and document-topic distributions, the topic proportions in the dataset, as well as other LDA model's values that JGibbLDA produces. Another output file is generated in the root directory and includes log-likelihood and perplexity values.
