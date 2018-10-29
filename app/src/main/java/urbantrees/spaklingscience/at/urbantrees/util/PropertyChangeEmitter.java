package urbantrees.spaklingscience.at.urbantrees.util;

import android.app.Activity;
import android.content.Context;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * TODO
 * @author Laurenz Fiala
 * @since 2018/05/16
 */
public /*abstract*/ class PropertyChangeEmitter extends HasContext {

    private PropertyChangeSupport observable = new PropertyChangeSupport(this);

    public PropertyChangeEmitter(Activity context) {
        super(context);
    }

    public void listen(Class propertyType, PropertyChangeListener propertyChangeListener) {
        this.observable.addPropertyChangeListener(propertyType.getName(), propertyChangeListener);
    }

    public void notify(Class<?> type, Object newProperty) {
        this.observable.firePropertyChange(type.getName(), null, newProperty);
    }

}
