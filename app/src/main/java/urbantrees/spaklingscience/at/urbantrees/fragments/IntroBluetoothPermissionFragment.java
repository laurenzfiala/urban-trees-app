package urbantrees.spaklingscience.at.urbantrees.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.activities.IntroActivity;
import urbantrees.spaklingscience.at.urbantrees.util.Utils;

public class IntroBluetoothPermissionFragment extends IntroGenericFragment implements IntroPermissionRequestor {

    /**
     * Request code for requesting new permissions.
     */
    public static final int        REQUEST_PERMISSIONS = 2;

    /**
     * Permissions needed to be granted for the app to work.
     */
    public static final String[]    NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private Switch permissionsGrantedToggle;

    public IntroBluetoothPermissionFragment() {
        super();
    }

    public static IntroBluetoothPermissionFragment newInstance() {
        return new IntroBluetoothPermissionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bluetooth_permission,
                container, false);

        this.permissionsGrantedToggle = (Switch) view.findViewById(R.id.intro_bluetooth_permission_toggle);
        if (this.isPermissionsGranted()) {
            this.permissionsGrantedToggle.setChecked(true);
            this.permissionsGrantedToggle.setEnabled(false);
        } else {
            this.permissionsGrantedToggle.setChecked(false);
        }

        this.permissionsGrantedToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                Utils.requestPermissions(getActivity(), IntroBluetoothPermissionFragment.this, NEEDED_PERMISSIONS, REQUEST_PERMISSIONS);
            }
            }
        });

        return view;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            for(int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    this.onRequestFailed();
                    return;
                }
            }
            this.onRequestSuccessful();
        }
    }

    public boolean isPermissionsGranted() {
        return Utils.isPermissionsGranted(this.getActivity(), NEEDED_PERMISSIONS);
    }

    @Override
    public void onRequestFailed() {
        this.permissionsGrantedToggle.setChecked(false);

        ((IntroActivity) this.getActivity()).setConnectivityPermissionsGranted(false);
    }

    @Override
    public void onRequestSuccessful() {
        this.permissionsGrantedToggle.setChecked(true);
        this.permissionsGrantedToggle.setEnabled(false);

        ((IntroActivity) this.getActivity()).setConnectivityPermissionsGranted(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        this.permissionsGrantedToggle.setChecked(this.isPermissionsGranted());
    }

    @Override
    public boolean canContinue() {
        if (this.permissionsGrantedToggle == null) {
            return false;
        }
        return this.permissionsGrantedToggle.isChecked();
    }

}