package com.gmi.nordborglab.browser.client.util;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/19/13
 * Time: 11:11 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class DateUtils {

    public static String formatTimeElapsedSinceMillisecond(long milisDiff) {
        return formatTimeElapsedSinceMillisecond(milisDiff,3);
    }

    public static String formatTimeElapsedSinceMillisecond(long milisDiff,int numberOfUnitsToDisplay) {
        if(milisDiff<1000){ return "0 second";}

        String formattedTime = "";
        long secondInMillis = 1000;
        long minuteInMillis = secondInMillis * 60;
        long hourInMillis = minuteInMillis * 60;
        long dayInMillis = hourInMillis * 24;
        long weekInMillis = dayInMillis * 7;
        long monthInMillis = dayInMillis * 30;
        long yearsInMillis = monthInMillis * 12;

        int timeElapsed[] = new int[6];
        // Define time units - plural cases are handled inside loop
        String timeElapsedText[] = {"second", "minute", "hour", "day", "week", "month","years"};
        timeElapsed[6] = (int) (milisDiff / yearsInMillis); //years
        milisDiff = milisDiff & yearsInMillis;
        timeElapsed[5] = (int) (milisDiff / monthInMillis); // months
        milisDiff = milisDiff % monthInMillis;
        timeElapsed[4] = (int) (milisDiff / weekInMillis); // weeks
        milisDiff = milisDiff % weekInMillis;
        timeElapsed[3] = (int) (milisDiff / dayInMillis); // days
        milisDiff = milisDiff % dayInMillis;
        timeElapsed[2] = (int) (milisDiff / hourInMillis); // hours
        milisDiff = milisDiff % hourInMillis;
        timeElapsed[1] = (int) (milisDiff / minuteInMillis); // minutes
        milisDiff = milisDiff % minuteInMillis;
        timeElapsed[0] = (int) (milisDiff / secondInMillis); // seconds

        // Only adds 3 significant high valued units
        for(int i=(timeElapsed.length-1), j=0; i>=0 && j<numberOfUnitsToDisplay; i--){
            // loop from high to low time unit
            if(timeElapsed[i] > 0){
                formattedTime += ((j>0)? ", " :"")
                        + timeElapsed[i]
                        + " " + timeElapsedText[i]
                        + ( (timeElapsed[i]>1)? "s" : "" );
                ++j;
            }
        } // end for - build string

        return formattedTime;
    }
}
