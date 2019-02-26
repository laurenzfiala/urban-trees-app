package urbantrees.spaklingscience.at.urbantrees.util;

import android.app.Activity;

import urbantrees.spaklingscience.at.urbantrees.activities.ApplicationProperties;

/**
 * Abstract class used to add context activity to non-activity classes.
 * @author Laurenz Fiala
 * @since 2018/05/16
 */
public abstract class HasContext {

    protected Activity context;

    protected ApplicationProperties props;

    public HasContext(Activity context, ApplicationProperties props) {
        this.context = context;
        this.props = props;
    }

}
