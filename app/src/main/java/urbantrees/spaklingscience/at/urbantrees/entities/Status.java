package urbantrees.spaklingscience.at.urbantrees.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Status implements Serializable {

    private int imageResId;
    private int titleResId;
    private int textResId;
    private List<StatusAction> actions = new ArrayList<>();

    public Status(int imageResId, int titleResId, int textResId, StatusAction... actions) {
        this.imageResId = imageResId;
        this.titleResId = titleResId;
        this.textResId = textResId;
        if (actions != null && actions.length > 0) {
            Collections.addAll(this.actions, actions);
        }
    }

    public void addAction(StatusAction action) {
        this.actions.add(action);
    }

    public int getImageResId() {
        return imageResId;
    }

    public int getTitleResId() {
        return titleResId;
    }

    public int getTextResId() {
        return textResId;
    }

    public List<StatusAction> getActions() {
        return actions;
    }
}
