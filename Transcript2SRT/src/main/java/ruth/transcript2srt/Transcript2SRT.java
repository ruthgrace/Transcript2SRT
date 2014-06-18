/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ruth.transcript2srt;

/**
 * Converts a text file formatted to the SpeechPad SDH captioning style guide
 * (https://sites.google.com/a/speechpad.com/transcribers/style-guides/sdh-captions---42706/sdh-captions-42706---REVIEW)
 * to a .SRT file.
 * Caption lengths will not exceed 43 characters, and lines will not end with if
 *  they are longer than 30 characters.
 * Future features include correcting inappropriate single dashes
 *  (single dashes should only be used to denote a new speaker)
 *  and an GUI (currently the file to be converted is hard coded).
 * @author ruthgrace
 */
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.io.PrintWriter;
import java.util.Iterator;
import java.io.File;

public class Transcript2SRT {
    private ArrayList<Caption> captions;
    private ArrayList<TimeAnchor> timeAnchors;
    private HashSet<String> PREPOSITIONS;
    private final int CAPTIONLENGTH = 32;
    
    public static void main (String[] args) {
        Transcript2SRT t = new Transcript2SRT();
        //hard-coded text file to be converted, and filepath of output
        t.convert2SRT("/home/ruthgrace/Desktop/A1415780.txt","/home/ruthgrace/Desktop/A1415780.srt");
    }
    /**
     * Constructor.
     * Initializes captions, time anchors, and preposition lists.
     */
    public Transcript2SRT() {
        captions = new ArrayList<Caption>();
        timeAnchors = new ArrayList<TimeAnchor>();
        PREPOSITIONS = new HashSet<String>();
        PREPOSITIONS.add("aboard");
        PREPOSITIONS.add("about");
        PREPOSITIONS.add("above");
        PREPOSITIONS.add("across");
        PREPOSITIONS.add("after");
        PREPOSITIONS.add("against");
        PREPOSITIONS.add("along");
        PREPOSITIONS.add("amid");
        PREPOSITIONS.add("among");
        PREPOSITIONS.add("anti");
        PREPOSITIONS.add("around");
        PREPOSITIONS.add("as");
        PREPOSITIONS.add("at");
        PREPOSITIONS.add("before");
        PREPOSITIONS.add("behind");
        PREPOSITIONS.add("below");
        PREPOSITIONS.add("beneath");
        PREPOSITIONS.add("beside");
        PREPOSITIONS.add("besides");
        PREPOSITIONS.add("between");
        PREPOSITIONS.add("beyond");
        PREPOSITIONS.add("but");
        PREPOSITIONS.add("by");
        PREPOSITIONS.add("concerning");
        PREPOSITIONS.add("considering");
        PREPOSITIONS.add("despite");
        PREPOSITIONS.add("down");
        PREPOSITIONS.add("during");
        PREPOSITIONS.add("except");
        PREPOSITIONS.add("excepting");
        PREPOSITIONS.add("excluding");
        PREPOSITIONS.add("following");
        PREPOSITIONS.add("for");
        PREPOSITIONS.add("from");
        PREPOSITIONS.add("in");
        PREPOSITIONS.add("inside");
        PREPOSITIONS.add("into");
        PREPOSITIONS.add("like");
        PREPOSITIONS.add("minus");
        PREPOSITIONS.add("near");
        PREPOSITIONS.add("of");
        PREPOSITIONS.add("off");
        PREPOSITIONS.add("on");
        PREPOSITIONS.add("onto");
        PREPOSITIONS.add("opposite");
        PREPOSITIONS.add("outside");
        PREPOSITIONS.add("over");
        PREPOSITIONS.add("past");
        PREPOSITIONS.add("per");
        PREPOSITIONS.add("plus");
        PREPOSITIONS.add("regarding");
        PREPOSITIONS.add("round");
        PREPOSITIONS.add("save");
        PREPOSITIONS.add("since");
        PREPOSITIONS.add("than");
        PREPOSITIONS.add("through");
        PREPOSITIONS.add("to");
        PREPOSITIONS.add("toward");
        PREPOSITIONS.add("towards");
        PREPOSITIONS.add("under");
        PREPOSITIONS.add("underneath");
        PREPOSITIONS.add("unlike");
        PREPOSITIONS.add("until");
        PREPOSITIONS.add("up");
        PREPOSITIONS.add("upon");
        PREPOSITIONS.add("versus");
        PREPOSITIONS.add("via");
        PREPOSITIONS.add("with");
        PREPOSITIONS.add("within");
        PREPOSITIONS.add("without");
    }
    /**
     * Converts a transcription plaintext file formatted to SpeechPad SDH
     * captioning style into an SRT file.
     * @param inFile file path of input text file.
     * @param outFile file path of output SRT file.
     */
    public void convert2SRT (String inFile, String outFile) {
        try {
            //initialize reader for input text file
            BufferedReader br = new BufferedReader(new FileReader(inFile));
            String line = br.readLine();
            //time anchors are in the format [[hh:mm:ss]]
            String timeAnchorRegEx = "\\[\\[[0-9:]*\\]\\]";
            //intialize caption counter index
            int captionCounter = -1;
            //process input text file
            while (line!=null) {
                line = line.trim();
                //if line is time anchor, add the time to the time anchor list
                if (line.matches(timeAnchorRegEx)) {
                    this.timeAnchors.add(new TimeAnchor(line,captionCounter,captionCounter+1));
                }
                //otherwise, make a list of captions out of the line
                else {
                    captionCounter++;
                    captionCounter = makeCaptions(line, captionCounter);
                }
                line = br.readLine();
            }
            System.out.println("got caption text");
            //assign startTime and endTime to each caption
            Caption.assignCaptionTimes(captions,timeAnchors);
            System.out.println("got caption times");
            //output resulting SRT file
            this.printSRT(outFile);
            System.out.println("SRT output to "+outFile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
   }
   /**
    * Turn a line of text into a list of SRT style captions.
    * @param line line of text transcript, representing the voice of one speaker
    * @param captionCounter integer representing index of new caption
    * @return New caption counter value, equal to the index of the last caption
    * to be added.
    */
   private int makeCaptions(String line, int captionCounter) {
       //if line is empty, return current caption size (do not process)
       if (line.equals("")) { return this.captions.size()-1; }
       //initialize index of characters in line
       int lineIndex = 0;
       //initialize size of current caption
       int captionSize = 0;
       //initialize counter for number of words processed into caption
       int wordCounter = 0;
       //split line into array of words
       String[] words = line.split(" ");
       //make next caption (43 characters or less) and increment wordCounter
       wordCounter = makeNextCaption(words,wordCounter,captionCounter);
       //make captions until there are no more words for captions.
       while (wordCounter < words.length) {
            captionCounter++;
            wordCounter = makeNextCaption(words,wordCounter,captionCounter);           
       }
       //return index of next caption to be added
       return this.captions.size()-1;
   }
   /**
    * Makes a caption with up to 2 lines of less than or equal to 43 characters
    * from a list of words.
    * @param words list of words in the line the caption is to be made from
    * @param wordCounter number of words already captionized
    * @param captionCounter index of new caption to be added
    * @return 
    */
   private int makeNextCaption(String[] words, int wordCounter, int captionCounter) {
        //caption length records the number of characters in the caption so far
        int captionLength = words[wordCounter].length();
        //each caption can have a maximum of 2 lines of 43 characters
        String firstLine = ""; 
        //if the first word is already longer than 43 characters, then
        //  put the first 43 characters into the first line of the caption
        if (captionLength > CAPTIONLENGTH) {
            firstLine += words[wordCounter].substring(0,CAPTIONLENGTH);
            words[wordCounter] = words[wordCounter].substring(CAPTIONLENGTH);
        }
        //add words to the first line of the caption until adding another word
        //  would cause the line to exceed 43 characters (CAPTIONLENGTH).
        else {
            captionLength = 0;
            while (wordCounter < words.length && captionLength + 1 + words[wordCounter].length() < CAPTIONLENGTH) {
                captionLength += 1 + words[wordCounter].length();
                firstLine += words[wordCounter++] + " ";
            }
        }
        //return if all the words were used up in the first caption
        if (wordCounter >= words.length) {
            this.captions.add(new Caption(firstLine, captionCounter));
            return wordCounter;
        }
        //if the line is empty, something has gone wrong
        if (firstLine.equals("")) { 
            System.out.println("empty first caption");
            return wordCounter;
        }
        //remove trailing space of first line
        firstLine = firstLine.substring(0,firstLine.length()-1);
        //remove a single trailing preposition
        //  if the line is greater than 10 characters below max caption length
        if (captionLength - 1 - words[wordCounter].length() > CAPTIONLENGTH-10 && this.PREPOSITIONS.contains(words[wordCounter-1])) {
            wordCounter--;
            firstLine = firstLine.substring(0,firstLine.length()-words[wordCounter].length());
        }
        //create second line of caption, same as first line
        String secondLine = "";
        captionLength = words[wordCounter].length();
        if (captionLength > CAPTIONLENGTH) {
            secondLine += words[wordCounter].substring(0,CAPTIONLENGTH);
            words[wordCounter] = words[wordCounter].substring(CAPTIONLENGTH);
        }
        else {
            captionLength = 0;
            while (wordCounter < words.length && captionLength + 1 + words[wordCounter].length() < CAPTIONLENGTH) {
                captionLength += 1 + words[wordCounter].length();
                secondLine += words[wordCounter++] + " ";
            }
        }
        if (wordCounter >= words.length) {
            this.captions.add(new Caption(firstLine, secondLine, captionCounter));
            return wordCounter;
        }
        if (secondLine.equals("")) {
            this.captions.add(new Caption(firstLine, secondLine, captionCounter));
            return wordCounter;
        }
        secondLine = secondLine.substring(0,secondLine.length()-1);
        if (captionLength - 1 - words[wordCounter].length() > 30 && this.PREPOSITIONS.contains(words[wordCounter-1])) {
            wordCounter--;
            secondLine = secondLine.substring(0,secondLine.length()-words[wordCounter].length());
        }
        //add newnly created caption to list
        this.captions.add(new Caption(firstLine, secondLine, captionCounter));
        //return index of next unused word
        return wordCounter;
   }
   /**
    * Write .srt file with correctly formatted captions and timestamps.
    * @param fileName 
    */
   public void printSRT(String fileName) {
       try {
           PrintWriter writer = new PrintWriter(new File(fileName));
           int numCaptions = this.captions.size();
           //write each caption to the file one by one, using its toString()
           for (int i = 0; i < numCaptions; i++) {
               writer.println(this.captions.get(i).toString());
           }
           writer.close();
       }
       catch (Exception e) {
           e.printStackTrace();
       }
   }
}
