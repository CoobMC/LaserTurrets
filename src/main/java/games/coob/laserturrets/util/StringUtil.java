package games.coob.laserturrets.util;

public class StringUtil {

	public static String capitalize(final String string) {
		if (string == null || string.length() <= 1) return string;
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	public static String getStringBeforeSymbol(final String string, final String symbol) {
		return string.substring(string.lastIndexOf("/") + 1, string.indexOf(symbol));
	}
}
