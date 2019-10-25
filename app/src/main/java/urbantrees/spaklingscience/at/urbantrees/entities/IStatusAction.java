package urbantrees.spaklingscience.at.urbantrees.entities;

import urbantrees.spaklingscience.at.urbantrees.activities.StatusActivity;

public interface IStatusAction {

    int getStringResource();

    void onAction(final StatusActivity statusActivity);

}
