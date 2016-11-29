package se.hms.argos.afutest.infra.simulator;

/**
* ModbustcpServer.
* 
* <P>This is the CurrentTime class which set up the time with chosen output.
* 
* @author Hussam Alshammari
* @author Lolita Mageramova
* @version 1.0
*/

import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CurrentTime {
	
	/**
	 * The main method
	 * Used for testing purposes
	 */
	public static void main(String [] a) throws ParseException
	{
		getCurretTime(true);
		getCurretTime(false);
		getOneHourBack();
	}
	
	/**
	 * The getCurretTime method  
	 * This method gets and sets the current time in UTC format.
	 * @param utc - a boolean variable to set the time with UTC or not
	 * @return dateFormat - object which return the current date.
	 */
	
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
	
	/**
	 * The getOneHourBack method.  
	 * This method gets and sets the current time in UTC format, but it set the time to one hour back .
	 * @return dateFormat - object which return the current date.
	 */
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
	
	/**
	 * The default constructor.  
	 * Converts the time from normal form to an angle.
	 * @param h - the current hour
	 * @param m - the minutes
	 * @param angle - this method return the time as an angle 
	 */
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
