package urbantrees.spaklingscience.at.urbantrees.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import urbantrees.spaklingscience.at.urbantrees.R;


/**
 * Used to show status and progress to the user.
 * @author Laurenz Fiala
 * @since 2019/02/21
 */
public class StatusBottomSheetFragment extends Fragment {

    public static final String TAG = StatusBottomSheetFragment.class.getName();

    private int statusResId;
    private String status;
    private int progress;

    private TextView statusText;
    private ProgressBar progressBar;
    private Button cancelBtn;

    private OnStatusBottomSheetInteractionListener mListener;

    public StatusBottomSheetFragment() {}

    public static StatusBottomSheetFragment newInstance() {
        return new StatusBottomSheetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.statusText = getView().findViewById(R.id.status_text);
        this.progressBar = getView().findViewById(R.id.progress_bar);
        this.cancelBtn = getView().findViewById(R.id.cancel_btn);
        this.update();

        this.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StatusBottomSheetFragment.this.cancelBtn.setVisibility(View.INVISIBLE);
                StatusBottomSheetFragment.this.mListener.onStatusCancelled();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStatusBottomSheetInteractionListener) {
            mListener = (OnStatusBottomSheetInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void update() {

        if (getContext() == null) {
            return;
        }

        Handler mainHandler = new Handler(getContext().getMainLooper());

        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {

                if (getContext() == null) {
                    return;
                }

                if (statusText != null) {
                    if (statusResId < 0) {
                        statusText.setText(status);
                    } else {
                        statusText.setText(getResources().getString(statusResId));
                    }
                }
                if (progressBar != null) {
                    progressBar.setIndeterminate(progress < 0);
                    progressBar.setProgress(progress);
                }

            }
        };
        mainHandler.post(updateRunnable);


    }

    public void setStatus(int statusResId) {
        this.statusResId = statusResId;
        this.update();
    }

    public void setStatus(String status) {
        this.statusResId = -1;
        this.status = status;
        this.update();
    }

    public int getProgress() {
        return this.progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        this.update();
    }

    public void setIndeterminate() {
        this.progress = -1;
        this.update();
    }

    public interface OnStatusBottomSheetInteractionListener {
        void onStatusCancelled();
    }
}
