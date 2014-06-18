/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ruth.transcript2srt;

import java.sql.Time;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
/**
 * This class represents a time stamp for a caption, with the index value of the
 *  preceeding caption, the index value of the caption after, and a time.
 * @author ruthgrace
 */
public class TimeAnchor {
    private int captionBefore;
    private int captionAfter;
    private Time time;
    /**
     * Constructor
     * @param timeStamp Time time stamp
     * @param captionBefore integer index of caption just before time stamp
     * @param captionAfter integer index of caption right after time stamp
     */
    public TimeAnchor(String timeStamp, int captionBefore, int captionAfter) {
        this.captionBefore = captionBefore;
        this.captionAfter = captionAfter;
        this.time = this.string2time(timeStamp);
    }
    /**
     * Convert "[[hh:mm:ss]]" time anchor string into Time object.
     * @param timeAnchor time stamp string formatted as [[hh:mm:ss]]
     * @return Time time stamp object
     */
    private Time string2time(String timeAnchor) {
        timeAnchor = timeAnchor.trim();
        //extract hh:mm:ss string out of double square brackets
        String timeRegEx = "\\[\\[([0-9:]*)\\]\\]";
        Pattern pattern = Pattern.compile(timeRegEx);
        Matcher matcher = pattern.matcher(timeAnchor);
        if (matcher.find()) {
            //convert hh:mm:ss string into Time object
            Time time = Time.valueOf(matcher.group(1));
            return time;
        }
        else {
            return null;
        }
    }
    /**
     * Retrieve caption index of caption immediately preceeding this time stamp.
     * @return 
     */
    public int getCaptionBefore() {
        return this.captionBefore;
    }
    /**
     * Retrieve caption index of caption immediately after this time stamp.
     * @return 
     */
    public int getCaptionAfter() {
        return this.captionAfter;
    }
    /**
     * Retrieve Time object representing the time of this time stamp.
     * @return 
     */
    public Time getTime() {
        return this.time;
    }
}
