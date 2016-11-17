package se.hms.argos.afutest.infra.simulator;

import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;

public class CurrentTime {
	
	
	public static void main(String [] a) throws ParseException
	{
		getCurretTime(true);
		getCurretTime(false);
		getOneHourBack();
	}
	
	public static String getCurretTime(boolean utc)
	{ 
		//2016-10-14T10:54
		DateFormat dateFormat;
		if (utc)
			dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm':00ZUTC'");
		else
			dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm':00Z'");
		// get current date time with Date()
		Date date = new Date();
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		System.out.println(dateFormat.format(date)); //2016/08/06 16:06:54
		return dateFormat.format(date);
	}
	
	public static String getOneHourBack(){
		DateFormat dateFormat = dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm':00Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		
		Date currentDate = new Date();
		

		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		cal.add(Calendar.HOUR, -1);
		Date oneHourBack = cal.getTime();
		System.out.println(dateFormat.format(oneHourBack));
		
		return  dateFormat.format(oneHourBack);
	}
	
	
	// h = 1..12, m = 0..59
	static double angle(int h, int m) 
	{
	    double hAngle = 0.5D * (h * 60 + m);
	    double mAngle = 6 * m;
	    double angle = Math.abs(hAngle - mAngle);
	    angle = Math.min(angle, 360 - angle);
	    return angle;
	}

}
