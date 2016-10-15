package se.hms.argos.afutest.infra.simulator;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CurrentTime {
	
	
	public static void main(String [] a)
	{
		getCurretTime();
	}
	
	public static String getCurretTime()
	{ 
		//2016-10-14T10:54
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		// get current date time with Date()
		Date date = new Date();
		System.out.println(dateFormat.format(date)); //2016/08/06 16:06:54
		return dateFormat.format(date);
	}

}
