package com.lonsec.interview;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {

	public static Date convertStringToDate(String dateStr) {
		Date newDate = null;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(DefaultValues.DEFAULT_DATE_FROMAT);
			newDate = formatter.parse(dateStr);
		} catch (ParseException e) {
			System.err.println("Error Parsing date @" + dateStr);
		}
		return newDate;
	}

	public static String convertDateToString(Date recDate) {
		SimpleDateFormat formatter = new SimpleDateFormat(DefaultValues.DEFAULT_DATE_FROMAT);
		return formatter.format(recDate);
	}

	public static double roundToDown(double fundReturn) {
		double fundPer = Math.round((fundReturn) * 100.0) / 100.0;
		DecimalFormat df = new DecimalFormat(DefaultValues.DEFAULT_DECIMAL_FORMAT);
		return Double.parseDouble(df.format(fundPer));
	}
}
