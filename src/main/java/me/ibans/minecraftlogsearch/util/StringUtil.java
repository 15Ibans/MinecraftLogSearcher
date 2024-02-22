package me.ibans.minecraftlogsearch.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StringUtil {

    public static boolean contains(CharSequence s, CharSequence match, boolean ignoreCase) {
        if (ignoreCase) {
            return StringUtils.containsIgnoreCase(s, match);
        } else {
            return StringUtils.contains(s, match);
        }
    }

//    public static int countMatches(String str, String sub) {
//        Pattern pattern = Pattern.compile(sub, Pattern.CASE_INSENSITIVE);
//        Matcher matcher = pattern.matcher(str);
//        return (int) matcher.results().count();
//    }

    static int indexOf(CharSequence cs, CharSequence searchChar, int start) {
        if (cs instanceof String) {
            return ((String)cs).indexOf(searchChar.toString(), start);
        } else if (cs instanceof StringBuilder) {
            return ((StringBuilder)cs).indexOf(searchChar.toString(), start);
        } else {
            return cs instanceof StringBuffer ? ((StringBuffer)cs).indexOf(searchChar.toString(), start) : cs.toString().indexOf(searchChar.toString(), start);
        }
    }

    public static boolean isRegexValid(String input) {
        try {
            Pattern.compile(input);
        } catch (PatternSyntaxException e) {
            return false;
        }
        return true;
    }

}
