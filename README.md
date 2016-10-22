## Synopsis

The DTBNPE (Dependency Tree Base Noun Phrase Extraction) system extracts base noun phrases from a dependency tree in the CoNLL format.
It is written in JAVA and can be easily included in Natural Language Processing (NLP) projects.

Evaluation on section 20 of the WSJ data set, taken from the CoNLL-2000 shared task, shows an F1-score of 95.8%

## Code Example

Example of extraction from dependency trees and output in the CoNLL format:

```java
    DependencyTreeNounPhraseExtractor depTreeNPE = new DependencyTreeNounPhraseExtractor();
    depTreeNPE.extractNounPhrasesFromTreeInCoNLL("input/dependencyTree.txt");
    depTreeNPE.saveNounPhrasesAsCoNLLFile("output/extractedNPsCoNLL.txt");
```

## Motivation
I created this project as part of my Bachelor's thesis about base noun phrase extraction using hand-crafted rules. I wanted to compare it to other performant systems. Syntaxnet was recently made available with excellent performance in the task of dependency tree parsing. I therefore decided to try to extract the base noun phrase chunk information out of these dependency trees. The result was a system better than the previous state of the art system (Shen and Shakar (2005)).
It shows the practicality and excellent performance of a very simple system using the dependency tree and some simple lingusitic rules. It is provided as a very simple JAVA interface.


## Installation
No special installation needed

- Import Classes

## API Reference

The API is very simple for now and only allows input using CoNLL format (http://universaldependencies.org/format.html) and output using CoNLL three column format.



**Input:**

The method expects a path to a dependency tree as a string
```java
    void extractNounPhrasesFromTreeInCoNLL(String absolutePath)
```

**Output:**

Writes the previously extracted base noun phrases to the given absolute path in the CoNLL Format
- 1. Column entails the tokens
- 2. Column entails the POS Tags taken from the dependency tree
- 3. Column entails the chunk tags in the IOB2 format, only with baseNP information (Only B, I and O)

```java
    void saveNounPhrasesAsCoNLLFile(String absolutePath)
```
## Contributors

Contributions to the system are greatly appreciated.

## License

The RBBNPE code is written in Java and licensed under the MIT license
