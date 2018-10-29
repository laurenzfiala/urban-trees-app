package urbantrees.spaklingscience.at.urbantrees.util;

import java.util.regex.Pattern;

/**
 * Regex helper class with static utility
 * methods.
 *
 * @author Laurenz Fiala
 * @since 2018/05/19
 */
public class Regex {

    public static final Pattern INTEGER = Pattern.compile("[+-]?\\d+");

    public static final Pattern FLOATING_POINT = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");

}
