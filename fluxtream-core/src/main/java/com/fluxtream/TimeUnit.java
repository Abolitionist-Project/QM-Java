package com.fluxtream;

public enum TimeUnit {
	DAY, WEEK, MONTH, YEAR;

	public static TimeUnit fromValue(String s) {
		for (TimeUnit timeUnit : values()) {
			if (timeUnit.toString().equalsIgnoreCase(s))
				return timeUnit;
		}
		return null;
	}

}
