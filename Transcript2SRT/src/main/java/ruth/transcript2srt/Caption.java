/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ruth.transcript2srt;
import java.text.SimpleDateFormat;
import java.sql.Time;
import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * This class represents a SRT style caption, with a maximum of 2 lines
 *  of a maximum of 43 characters each, a before and after timestamp,
 *  and an integer caption index.
 * @author ruthgrace
 */
public class Caption {
    private String firstLine;
    private String secondLine;
    private Time startTime;
    private Time endTime;
    private int captionNumber;
    /**
     * Constructor
     * @param firstLine
     * @param secondLine
     * @param startTime
     * @param endTime
     * @param captionNumber 
     */
    public Caption (String firstLine, String secondLine, Time startTime, Time endTime, int captionNumber) {
        this.firstLine = firstLine;
        this.secondLine = secondLine;
        this.startTime = startTime;
        this.endTime = endTime;
        this.captionNumber = captionNumber;
    }
    /**
     * Constructor without second line
     * @param firstLine
     * @param startTime
     * @param endTime
     * @param captionNumber 
     */
    public Caption (String firstLine, Time startTime, Time endTime, int captionNumber) {
        this.firstLine = firstLine;
        this.secondLine = null;
        this.startTime = startTime;
        this.endTime = endTime;
        this.captionNumber = captionNumber;
    }
    /**
     * Constructor without timestamps
     * @param firstLine
     * @param secondLine
     * @param captionNumber 
     */
    public Caption (String firstLine, String secondLine, int captionNumber) {
        this.firstLine = firstLine;
        this.secondLine = secondLine;
        this.captionNumber = captionNumber;
    }
    /**
     * Constructor without timestamps and second line
     * @param firstLine
     * @param captionNumber 
     */
    public Caption (String firstLine, int captionNumber) {
        this.firstLine = firstLine;
        this.secondLine = null;
        this.captionNumber = captionNumber;
    }
    /**
     * Convert caption to SRT file format string composed of
     * captionindex
     * starttimestamp --> endtimestamp
     * firstline
     * secondline
     * @return 
     */
    @Override
    public String toString() {
        String captionString = "";
        String arrow = " --> ";
        String newLine = "\n";
        String captionTimeFormat = "HH:mm:ss,SSS";
        SimpleDateFormat format = new SimpleDateFormat(captionTimeFormat);
        
        captionString += this.captionNumber+newLine;
        captionString += format.format((Date) this.startTime) + arrow + format.format((Date) this.endTime) + newLine;
        captionString += this.firstLine + newLine;
        if (this.secondLine!=null) {
            captionString += this.secondLine + newLine;
        }
        //some how use time > date > simpledateformat((Date) this.startTime)
        return captionString;
        //toString gives hh:mm:ss format
    }
    /**
     * Given a starting and ending timestamp and number of intermediate
     *  time stamps required, generate a list of timestamps, equally spaced in
     *  time.
     * @param startTimeAnchor starting timestamp
     * @param endTimeAnchor ending timestamp
     * @param numCaptions number of captions time stamps should span
     *  (each caption should have a before timestamp the same as the previous
     *  caption's after timestamp, and the total number of time stamps required
     *  is the number of captions plus one - each caption should go in between
     *  two time stamps)
     * @return array of generated caption times
     */
    private static Time[] generateCaptionTimes(Time startTimeAnchor, Time endTimeAnchor, int numCaptions) {
        //get millisecond measurement of start and end timestamps
        long startMilliseconds = startTimeAnchor.getTime();
        long endMilliseconds = endTimeAnchor.getTime();
        //calculate the average caption duration (timePerCaption)
        long span = endMilliseconds - startMilliseconds;      
        long timePerCaption = span/((long) numCaptions);
        //print out warning if caption duration is less than 2 seconds
        if (timePerCaption < (long) 2) {
            System.out.println("Caption less than 2 seconds long starting at time "+startTimeAnchor+" and ending at "+endTimeAnchor);
        }
        //intialize timestamp array
        Time[] captionTimes = new Time[numCaptions+1];
        //currentCaption time is incremented by the average caption duration
        //  to make the equally spaced timestamps
        long currentCaptionTime = startMilliseconds;
        captionTimes[0] = new Time(currentCaptionTime);
        for (int i = 1; i <=numCaptions; i++) {
            currentCaptionTime+=timePerCaption;
            captionTimes[i] = new Time(currentCaptionTime);
        }
        return captionTimes;
    }
    /**
     * Given captions and time stamps, assign time stamps to captions.
     * @param captions list of captions
     * @param timeAnchors list of time stamps
     */
    public static void assignCaptionTimes (ArrayList<Caption> captions, ArrayList<TimeAnchor> timeAnchors) {
        //check if there are captions before first time stamp
        //  if there are, assume that audio starts at time 00:00:00
        if (timeAnchors.get(0).getCaptionBefore() > 0) {
            assignCaptionTimesBetweenTimeStamps(new Time((long) 0), timeAnchors.get(0).getTime(), captions,0,timeAnchors.get(0).getCaptionBefore()+1);
        }
        //assign caption start and end times to each caption between each pair
        //  of consecutive time stamps
        int numAnchors = timeAnchors.size();
        for (int i = 1; i < numAnchors; i++) {
            captions = assignCaptionTimesBetweenTimeStamps(timeAnchors.get(i-1).getTime(),timeAnchors.get(i).getTime(),captions,timeAnchors.get(i-1).getCaptionAfter(),timeAnchors.get(i).getCaptionBefore());
        }
        //if there are captions after the last time stamp,
        //  assign their start and end times to be equal to the last time stamp
        if(timeAnchors.get(timeAnchors.size()-1).getCaptionAfter() < captions.size()-1) {
            System.out.println("WARNING: captions appear after last timestamp");
            int currentCaption = timeAnchors.get(timeAnchors.size()-1).getCaptionAfter();
            int numCaptions = captions.size();
            while (currentCaption < captions.size()) {
                captions.get(currentCaption).setStartTime(timeAnchors.get(timeAnchors.size()-1).getTime());
                captions.get(currentCaption).setEndTime(timeAnchors.get(timeAnchors.size()-1).getTime());
                currentCaption++;
            }
        }
    }
    /**
     * Given two time stamps with data about the caption indices of the captions
     *  between them, assign time stamps to captions in between.
     * @param startTimeAnchor start time for this block
     * @param endTimeAnchor end time for this block
     * @param captions list of all captions
     * @param captionStartIndex list of first caption for this block
     * @param captionEndIndex list of last caption for this block
     * @return list of captions (with new time stamps)
     */
    private static ArrayList<Caption> assignCaptionTimesBetweenTimeStamps(Time startTimeAnchor, Time endTimeAnchor, ArrayList<Caption> captions, int captionStartIndex, int captionEndIndex) {
        //calculate the number of captions in between the time stamps
        int numCaptions = captionEndIndex-captionStartIndex+1;
        //create intermediate time stamps
        Time[] timeAnchors = generateCaptionTimes(startTimeAnchor, endTimeAnchor, numCaptions);
        //traverse through in between captions and assign intermediate time
        //  stamps
        int captionCounter = captionStartIndex;
        Caption currentCaption;
        while (captionCounter <= captionEndIndex) {
            if (captionCounter >= captions.size()) {
                System.out.println("Array out of bounds about to happen");
            }
            currentCaption = captions.get(captionCounter);
            currentCaption.setStartTime(timeAnchors[captionCounter - captionStartIndex]);
            currentCaption.setEndTime(timeAnchors[captionCounter - captionStartIndex +1]);
            captionCounter++;
        }
        return captions;
    }
    /**
     * Get first line of caption.
     * @return first line of caption
     */
    public String getFirstLine() {
        return this.firstLine;
    }
    /**
     * Get second line of caption.
     * @return second line of caption
     */
    public String getSecondLine() {
        return this.secondLine;
    }
    /**
     * Get start time of caption.
     * @return Time start time of caption
     */
    public Time getStartTime() {
        return this.startTime;
    }
    /**
     * Get end time of caption.
     * @return Time end time of caption
     */
    public Time getEndTime() {
        return this.endTime;
    }
    /**
     * Get index of caption
     * @return integer index of caption
     */
    public int getCaptionNumber() {
        return this.captionNumber;
    }
    /**
     * Set start time of caption.
     * @param startTime Time to be used as start time of caption
     */
    private void setStartTime(Time startTime) {
        this.startTime = startTime;
    }
    /**
     * Set end time of caption.
     * @param startTime Time to be used as end time of caption
     */
    private void setEndTime(Time endTime) {
        this.endTime = endTime;
    }
}
