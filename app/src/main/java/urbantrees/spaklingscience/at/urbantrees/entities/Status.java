package urbantrees.spaklingscience.at.urbantrees.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Status implements Serializable {

    private int imageResId;
    private String title;
    private String text;
    private List<StatusAction> actions = new ArrayList<>();

    public Status(int imageResId, String title, String text, StatusAction... actions) {
        this.imageResId = imageResId;
        this.title = title;
        this.text = text;
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

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public List<StatusAction> getActions() {
        return actions;
    }
}
