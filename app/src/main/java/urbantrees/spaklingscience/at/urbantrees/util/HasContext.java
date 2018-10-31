package urbantrees.spaklingscience.at.urbantrees.util;

import android.app.Activity;
import android.content.Context;

/**
 * Abstract class used to add context activity to non-activity classes.
 * @author Laurenz Fiala
 * @since 2018/05/16
 */
public abstract class HasContext {

    protected Activity context;

    public HasContext(Activity context) {
        this.context = context;
    }

}
