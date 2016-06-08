package com.tomato;

import java.util.Calendar;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.widget.DigitalClock;


public class TimeCount extends DigitalClock {
    public TimeCount(Context context) {
		super(context);
		// TODO 自动生成的构造函数存根
		initClock(context);
	}
	Calendar mCalendar;
    private final static String m12 = "h:mm aa";
    private final static String m24 = "k:mm";
    private FormatChangeObserver mFormatChangeObserver;
    
    private Runnable mTicker;
    
    public boolean start;
    
    private Handler mHandler;
    private long endTime;
    public static long distanceTime;
    
    private ClockListener mClockListener;
    
    public long constTime;
 
    private boolean mTickerStopped;
    private boolean mFirstFifteenSecs;
    
    @SuppressWarnings("unused")
    private String mFormat;
    
    public TimeCount(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    initClock(context);
    }
    private void initClock(Context context) {
	    if (mCalendar == null) {
	    	mCalendar = Calendar.getInstance();
	    }
	    mFormatChangeObserver = new FormatChangeObserver();
	    getContext().getContentResolver().registerContentObserver(
	    Settings.System.CONTENT_URI, true, mFormatChangeObserver);
	    setFormat();
    }
    @Override
    protected void onAttachedToWindow() {
	    mTickerStopped = false;

	    constTime /= 1000;
	    setText(dealTime(constTime));
    }
    
    protected void start(){
    	
    	mTickerStopped = false;
    	mFirstFifteenSecs = true;
 	    mHandler = new Handler();	    
    	mTicker = new Runnable() {
		    public void run() {
			    if (mTickerStopped) {
			    	mHandler.removeCallbacks(mTicker);
			    	return;
			    }
			    
			    long currentTime = System.currentTimeMillis();
			    distanceTime = endTime - currentTime;
			    distanceTime /= 1000;
			    
			    if (distanceTime % 15 == 0) {
			    	if (mFirstFifteenSecs) {
			    		mFirstFifteenSecs = false;
			    	} else {
			    		mClockListener.gainGold();
			    	}
			    }
			    
		    	if (distanceTime == 0) {
		    		
		    		mClockListener.gainXP(constTime);
		    		mClockListener.timeEnd();
				    setText(dealTime(0));
				    onDetachedFromWindow();
				    
			    } else if (distanceTime < 0) {
			    	
				    setText(dealTime(0));
				    onDetachedFromWindow();
				    
			    } else {
			    	
			    	setText(dealTime(distanceTime));
			    	
			    }
			    invalidate();
			    long now = SystemClock.uptimeMillis();
			    long next = now + (1000 - now % 1000);
			    mHandler.postAtTime(mTicker, next);
		    }
	    };
	    mTicker.run();
    }
    protected void stop(){
    	onAttachedToWindow();
    	mTickerStopped = true;	
    }
    
    /**
    * deal time string
    *
    * @param time
    * @return
    */
    public static Spanned dealTime(long time) {
	    Spanned str;
	    StringBuffer returnString = new StringBuffer();
	    long minutes = ((time % (24 * 60 * 60)) % (60 * 60)) / 60;
	    //if(time!=0&&minutes==0)minutes =60;  // 这一句什么意思
	    if (time == 3600) minutes = 60;
	    long second = ((time % (24 * 60 * 60)) % (60 * 60)) % 60;
	    String minutesStr = timeStrFormat(String.valueOf(minutes));
	    String secondStr = timeStrFormat(String.valueOf(second));
	  
	    returnString.append(minutesStr).append("分钟").append(secondStr).append("秒");
	    
	    str = Html.fromHtml(returnString.toString());    
	    ((Spannable) str).setSpan(new AbsoluteSizeSpan(16), 2, 4,
	    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    ((Spannable) str).setSpan(new AbsoluteSizeSpan(16), 6, 7,
	    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			    
	    return str;
    }
    /**
    * format time
    *
    * @param timeStr
    * @return
    */
    private static String timeStrFormat(String timeStr) {
	    switch (timeStr.length()) {
		    case 1:
		    	timeStr = "0" + timeStr;
		    	break;
	    }
	    return timeStr;
    }
    @Override
    protected void onDetachedFromWindow() {
	    super.onDetachedFromWindow();
	    mTickerStopped = true;
    }
    /**
    * Clock end time from now on.
    *
    * @param endTime
    */
    public void setEndTime(long endTime) {
    	this.constTime=endTime;
    	this.endTime = endTime+System.currentTimeMillis();
    }
    /**
    * Pulls 12/24 mode from system settings
    */
    private boolean get24HourMode() {
    	return android.text.format.DateFormat.is24HourFormat(getContext());
    }
    private void setFormat() {
	    if (get24HourMode()) {
	    	mFormat = m24;
	    } else {
	    	mFormat = m12;
	    }
    }
    private class FormatChangeObserver extends ContentObserver {
	    public FormatChangeObserver() {
	    	super(new Handler());
	    }
	    @Override
	    public void onChange(boolean selfChange) {
	    	setFormat();
	    }
    }
    public void setClockListener(ClockListener clockListener) {
    	this.mClockListener = clockListener;
    }
    public interface ClockListener {
    	
    	void gainGold();
    	
	    void timeEnd();
	    
	    void gainXP(long time);
    } 
}