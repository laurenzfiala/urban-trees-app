package urbantrees.spaklingscience.at.urbantrees.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.activities.IntroActivity;

public class IntroMainFragment extends IntroGenericFragment {

    private boolean isTreeDataCollectEnabledPreset;

    public IntroMainFragment() {
        super();
    }

    public static IntroMainFragment newInstance(boolean isTreeDataCollectEnabledPreset) {
        IntroMainFragment instance = new IntroMainFragment();
        instance.isTreeDataCollectEnabledPreset = isTreeDataCollectEnabledPreset;
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_app_intro,
                container, false);

        Switch collectToggle = (Switch) view.findViewById(R.id.intro_main_collect_toggle);
        collectToggle.setChecked(this.isTreeDataCollectEnabledPreset);
        collectToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((IntroActivity) IntroMainFragment.this.getActivity()).setTreeDataCollectEnabled(isChecked);
            }
        });
        return view;

    }

}
