package urbantrees.spaklingscience.at.urbantrees.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import androidx.fragment.app.FragmentActivity;
import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.entities.Status;
import urbantrees.spaklingscience.at.urbantrees.entities.StatusAction;

public class StatusActivity extends FragmentActivity {

    private static Queue<Status> statuses = new LinkedBlockingQueue<>();

    private ImageView image;
    private TextView title;
    private TextView text;
    private LinearLayout actionLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        // set layout elements
        this.image = this.findViewById(R.id.img_statusimg);
        this.title = this.findViewById(R.id.text_statustitle);
        this.text = this.findViewById(R.id.text_statustext);
        this.actionLayout = this.findViewById(R.id.layout_actions);

        if (statuses.size() > 0) {
            this.update(Objects.requireNonNull(statuses.peek()));
        }

    }

    @Override
    public void onBackPressed() {}

    private void update(Status status) {

        this.image.setImageResource(status.getImageResId());
        this.title.setText(status.getTitle());
        this.text.setText(status.getText());

        this.actionLayout.removeAllViews();

        Button actionBtn;
        for (final StatusAction a : status.getActions()) {
            actionBtn = new Button(this);
            actionBtn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            actionBtn.setText(this.getString(a.getStringResource()));
            actionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    a.onAction(StatusActivity.this);
                    StatusActivity.statuses.poll();
                }
            });
            this.actionLayout.addView(actionBtn);
        }

    }

    public static void putStatus(Status newStatus) {
        StatusActivity.statuses.add(newStatus);
    }

}
