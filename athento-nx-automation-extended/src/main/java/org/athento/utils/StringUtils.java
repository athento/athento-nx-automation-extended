package org.athento.utils;
/**
 * 
 */


import java.io.Serializable;

/**
 * @author athento
 *
 */
public class StringUtils {

	public static final String AND_WITH_SPACES = " AND ";
	public static final String BLANK = " ";
	public static final String EMPTY = "";
	public static final Object EQUALS_WITH_SPACES = " = ";
	public static final String OR_WITH_SPACES = " OR ";
	public static final String SINGLE_QUOTE = "'";
	public static final Object NULL = "null";
	public static final Object IS_WITH_SPACES = " is ";
	public static final Object PARENTHESIS_LEFT = "(";
	public static final Object PARENTHESIS_RIGHT = ")";
	
	public static String getLastField (String path, String delimiter){
		String[] pieces = path.split(delimiter);
		if (pieces.length > 0) {
			return pieces[pieces.length-1];
		}
		return path;
	}
	
	public static boolean isNullOrEmpty (String s) {
		if (s == null) return true;
		return s.isEmpty();
	}

	public static String getValue(Serializable value) {
		if (value != null) return value.toString();
		return null;
	}

	public static String getPath(String path) {
		String rtn = "";
		String[] pieces = path.split("/");
		int i = 0;
		while (i < pieces.length-1) {
			rtn += pieces[i];
		}
		return rtn;
	}
}
