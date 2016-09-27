//MIT License
//
//Copyright (c) 2016 Laurenz Vorderwuelbecke
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in all
//copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//SOFTWARE.

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Laurenz Vorderwuelbecke on 04.09.16.
 * @author Laurenz Vorderwuelbecke
 */
public class DependencyTreeNounPhraseExtractor {

    private String CoNLLFile = "";

    public void extractNounPhrasesFromTreeInCoNLL(String absolutePath) {

        try {

            ArrayList<String> tokens = new ArrayList<String>();
            ArrayList<String> funcs = new ArrayList<String>();
            ArrayList<String> posTags = new ArrayList<String>();
            ArrayList<Integer> references = new ArrayList<Integer>();
            ArrayList<String> types = new ArrayList<String>();

            ArrayList<String[]> results = new ArrayList<String[]>();


            BufferedReader br = new BufferedReader(new FileReader(absolutePath));

            String currentLine;

            while (null != (currentLine = br.readLine())) {


                if (!currentLine.equals("")) {
                    String[] argumentsInLine = currentLine.split("\t");
                    tokens.add(argumentsInLine[1]);
                    funcs.add(argumentsInLine[3]);
                    posTags.add(argumentsInLine[4]);
                    int ref = Integer.parseInt(argumentsInLine[6]) - 1;
                    references.add(Integer.valueOf(ref));
                    types.add(argumentsInLine[7]);
                } else {

                    final String[] r = {""};  //To allow multithreading
                    results.add(r);


                    ArrayList<String> finalTokens = tokens;
                    ArrayList<String> finalPosTags = posTags;
                    ArrayList<String> finalFuncs = funcs;
                    ArrayList<String> finalTypes = types;
                    ArrayList<Integer> finalReferences = references;

                    Thread t = new Thread() {
                        public void run() {

                            int focus = -1;
                            String previousExtractedChunkTag = "";
                            boolean startAgain = false;

                            for (int i = 0; i < finalTokens.size(); i++) {

                                String token = finalTokens.get(i);
                                String POS = finalPosTags.get(i);
                                String func = finalFuncs.get(i);
                                String type = finalTypes.get(i);

                                int localFocus = finalReferences.get(i);

                                String nextType = "NONE";
                                if (i+1 < finalTypes.size()) {
                                    nextType = finalTypes.get(i + 1);
                                }

                                String refType = "NONE";
                                if (localFocus >= 0) {
                                    refType = finalTypes.get(localFocus);
                                }


                                String POSOfNext = "NONE";
                                if (i+1 < finalPosTags.size()) {
                                    POSOfNext = finalPosTags.get(i + 1);
                                }

                                String POSOfReference = "ROOT";
                                if (localFocus >= 0) {
                                    POSOfReference = finalPosTags.get(localFocus);
                                }


                                int nextFocus = -1;
                                if (i+1 < finalReferences.size()) {
                                    nextFocus = finalReferences.get(i + 1);
                                }

                                int referenceFocus = -1;
                                if (localFocus >=0) {
                                    referenceFocus = finalReferences.get(localFocus);
                                }


                                String funcOfReference = "ROOT";
                                if (localFocus >= 0) {
                                    funcOfReference = finalFuncs.get(localFocus);
                                }


                                String funcOfNext = "NONE";
                                if (i+1 < finalFuncs.size()) {
                                    funcOfNext = finalFuncs.get(i+1);
                                }

                                String funcOPrev = "NONE";
                                if (i-1 >= 0) {
                                    funcOPrev = finalFuncs.get(i-1);
                                }

                                String extractedChunkTag = "O";


                                //These types of dependencies occur in NPs. The noun phrase will therefore be extended until the end of the dependency
                                if (localFocus > focus && (type.equals("nn") || type.equals("amod") || type.equals("det") || type.equals("num") || type.equals("number") || type.equals("poss") || type.equals("quantmod") || type.equals("predet"))) {
                                    focus = localFocus;
                                }
                                //dep dependency can be in NPs, but only if the target is a noun or a monetary sign
                                else if (localFocus > focus && type.equals("dep") && (funcOfReference.equals("NOUN") || POSOfReference.equals("$"))) {
                                    focus = localFocus;
                                }
                                //Includes noun phrases with an "and" in them by checking if the second half of the phrases references the first half
                                else if ((previousExtractedChunkTag.equals("B") || previousExtractedChunkTag.equals("I")) && func.equals("CONJ") && ((nextType.equals("conj") && nextFocus == i-1 ) || (nextType.equals("num") && finalTypes.get(nextFocus).equals("conj") && finalReferences.get(nextFocus) == i-1) || (nextType.equals("nn") && finalTypes.get(nextFocus).equals("conj") && finalReferences.get(nextFocus) == i-1) || (POSOfNext.equals("$") && POSOfReference.equals("$")))) {
                                    startAgain = false;
                                    if (nextType.equals("nn")) {
                                        if (nextFocus > focus) {
                                            focus = nextFocus;
                                        }
                                    }
                                    else if (nextType.equals("num")) {
                                        if (nextFocus > focus) {
                                            focus = nextFocus;
                                        }
                                    }
                                    else if (POSOfNext.equals("$")) {
                                        if (i+2 < finalReferences.size()) {
                                            if (finalTypes.get(i+2).equals("number")) {
                                                int newFocus = finalReferences.get(i+2);
                                                if (newFocus > focus) {
                                                    focus = newFocus;
                                                }
                                            }
                                            else if (i+1 > focus) {
                                                focus = i+1;
                                            }
                                        }
                                        else if (i+1 > focus) {
                                            focus = i+1;
                                        }
                                    }
                                    else {
                                        if (i+1 > focus) {
                                            focus = i+1;
                                        }
                                    }
                                }
                                //Adverbs may be part of noun phrases if they modify an amod type token
                                else if (localFocus > focus && type.equals("advmod") && refType.equals("amod") && referenceFocus > i) {
                                    if (referenceFocus > focus) {
                                        focus = referenceFocus;
                                    }
                                }
                                else if (localFocus > focus && type.equals("mwe") && refType.equals("quantmod") && referenceFocus > i) {
                                    if (referenceFocus > focus) {
                                        focus = referenceFocus;
                                    }
                                }
                                //Or if they are infront of monetary signs
                                else if (localFocus > focus && type.equals("advmod") && POSOfReference.equals("$")) {
                                    focus = localFocus;
                                }


                                //Necessary check to include the POS signs in the second part of two noun phrases to conform to CoNLL standard.
                                if (POS.equals("POS")) {
                                    extractedChunkTag = "B";
                                    startAgain = false;
                                    if (localFocus > focus && (type.equals("nn") || type.equals("amod") || type.equals("det") || type.equals("num") || type.equals("poss") || type.equals("quantmod") || type.equals("predet"))) {
                                        focus = localFocus;
                                    }
                                    else if (localFocus > focus && type.equals("dep") && (funcOfReference.equals("NOUN") || POSOfReference.equals("$"))) {
                                        focus = localFocus;
                                    } else {
                                        focus = i + 1;
                                    }
                                }
                                //Includes all tokens up to the last token in a NP
                                else if (i<focus) {
                                    if (startAgain) {
                                        extractedChunkTag = "B";
                                        startAgain = false;
                                    }
                                    else if (previousExtractedChunkTag.equals("B") || previousExtractedChunkTag.equals("I")) {
                                        extractedChunkTag = "I";
                                    } else {
                                        extractedChunkTag = "B";
                                    }
                                }
                                //Last token in noun phrases is marked. The next token should therefore start a new phrase
                                else if (i == focus) {
                                    startAgain = true;
                                    if (previousExtractedChunkTag.equals("B") || previousExtractedChunkTag.equals("I")) {
                                        extractedChunkTag = "I";
                                    } else {
                                        extractedChunkTag = "B";
                                    }
                                }
                                //Detects single nouns without dependencies
                                else if (func.equals("NOUN")) {
                                    extractedChunkTag = "B";
                                    startAgain = true;
                                }
                                //Detects all previously missed determines as beginnings of NPs
                                else if (func.equals("DET")) {
                                    extractedChunkTag = "B";
                                }
                                //Detects pronouns without dependencies
                                else if (func.equals("PRON")) {
                                    extractedChunkTag = "B";
                                    startAgain = true;
                                    if (localFocus > focus && type.equals("poss")) {
                                        focus = localFocus;
                                        startAgain = false;
                                    }
                                }
                                else if (POSOfNext.equals(".") && func.equals("NOUN")) {
                                    extractedChunkTag = "B";
                                }
                                else {
                                    extractedChunkTag = "O";
                                }

                                //Necessary step to get all monetary values, as the dependencies are mostly backwards in these phrases
                                if (POS.equals("$") && funcOfNext.equals("NUM")) {
                                    if (previousExtractedChunkTag.equals("B") || previousExtractedChunkTag.equals("I")) {
                                        extractedChunkTag = "I";
                                    } else {
                                        extractedChunkTag = "B";
                                    }
                                    if (localFocus > i +1 && (type.equals("dep") ||type.equals("nn"))) {
                                        focus = localFocus;
                                    }
                                    else {
                                        if (i+1 > focus) {
                                            focus = i + 1;
                                        }
                                    }
                                    startAgain = false;
                                }
                                //Detects all missed numbers
                                else if ((func.equals("NUM") && !type.equals("num") && !type.equals("number")) ||  (func.equals("NUM") && localFocus <= i)) {
                                    if (previousExtractedChunkTag.equals("B") || previousExtractedChunkTag.equals("I")) {
                                        extractedChunkTag = "I";
                                    } else {
                                        extractedChunkTag = "B";
                                    }
                                }

                                //Special treatment for brackets because of the way CoNLL annotates them
                                if (token.equals("-LRB-") || token.equals("-RRB-") || token.equals("-RCB-")) {
                                    extractedChunkTag = "O";
                                    focus = i - 1;
                                }

                                //There should never be a token tagged I when there is no B in front. This remedies errors, that may have happened above.
                                if (extractedChunkTag.equals("I") && previousExtractedChunkTag.equals("O")) {
                                    extractedChunkTag = "O";
                                }

                                previousExtractedChunkTag = extractedChunkTag;

                                String newLine = token + "\t" + POS + "\t" + extractedChunkTag + "\n";
                                r[0] = r[0] + newLine;

                            }
                        }

                    };
                    t.start();

                    r[0] = r[0] + "\n";

                    tokens = new ArrayList<String>();
                    funcs = new ArrayList<String>();
                    posTags = new ArrayList<String>();
                    references = new ArrayList<Integer>();
                    types = new ArrayList<String>();


                }



            }
            for (String[] result : results) {
                CoNLLFile = CoNLLFile + result[0];
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Saves the previously extracted NPs in the CoNLL text format
    public void saveNounPhrasesAsCoNLLFile(String absolutePath) {

        try {

            PrintWriter writer = new PrintWriter(absolutePath, "UTF-8");
            writer.print(CoNLLFile);
            writer.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


}
