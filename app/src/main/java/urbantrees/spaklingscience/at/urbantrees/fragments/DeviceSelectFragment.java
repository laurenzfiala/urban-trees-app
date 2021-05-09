package urbantrees.spaklingscience.at.urbantrees.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import urbantrees.spaklingscience.at.urbantrees.views.DeviceListItemView;
import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothDevice;

/**
 * Dialog fragment for the user to select a bluetooth
 * device.
 * @author Laurenz Fiala
 * @since 2019/02/21
 */
public class DeviceSelectFragment extends DialogFragment implements DeviceListItemView.OnDeviceListItemInteractionListener {

    public static final String TAG = DeviceSelectFragment.class.getName();

    private OnDeviceSelectFragmentInteractionListener mListener;

    /**
     * Holds all devices to show currently.
     */
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();

    /**
     * State this device select is currently in.
     * Used to display correct info to users.
     */
    private State state;

    // ----- UI -----
    private LinearLayout deviceListLayout;
    private TextView progressText;

    public static DeviceSelectFragment newInstance() {
        return new DeviceSelectFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.deviceListLayout = getView().findViewById(R.id.device_list);
        this.progressText = getView().findViewById(R.id.progress_text);

        this.mListener.onDeviceSelectOpened();

        // re-add devices (if any were restored)
        for (BluetoothDevice d : this.devices) {
            this.addDevice(d);
        }

    }

    @Override
    public void onDestroy() {
        this.mListener.onCloseDeviceSelect();
        super.onDestroy();
        if (this.mListener != null) {
            this.mListener.onDeviceSelectClosed();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDeviceSelectFragmentInteractionListener) {
            this.mListener = (OnDeviceSelectFragmentInteractionListener) context;
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

    public void addDevice(final BluetoothDevice device) {

        this.devices.add(device);

        if (this.getContext() == null) {
            return;
        }
        Handler h = new Handler(this.getContext().getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                DeviceSelectFragment.this.deviceListLayout.addView(
                        new DeviceListItemView(
                                DeviceSelectFragment.this.getContext(),
                                DeviceSelectFragment.this,
                                device
                        ),
                        deviceListLayout.getChildCount() - 1
                );
                DeviceSelectFragment.this.progressText.setText(R.string.search_devices_more);
            }
        });


    }

    public void updateDevice(final BluetoothDevice device) {

        this.devices.set(this.devices.indexOf(device), device);

        if (this.getContext() == null) {
            return;
        }
        Handler h = new Handler(this.getContext().getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                DeviceListItemView itemView = DeviceSelectFragment.this.deviceListLayout.findViewWithTag(device);
                if (itemView == null) {
                    Log.e(DeviceSelectFragment.TAG, "Tried to update a device view which did not yet exist: " + device);
                    return;
                }
                itemView.setDevice(device);
                itemView.update();
            }
        });

    }

    public void removeDevice(final BluetoothDevice device) {

        this.devices.remove(device);

        if (this.getContext() == null) {
            return;
        }
        Handler h = new Handler(this.getContext().getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                DeviceListItemView itemView = DeviceSelectFragment.this.deviceListLayout.findViewWithTag(device);
                if (itemView == null) {
                    Log.e(DeviceSelectFragment.TAG, "Tried to update a device view which did not yet exist: " + device);
                    return;
                }
                DeviceSelectFragment.this.deviceListLayout.removeView(itemView);
            }
        });

    }

    public void onLoadDeviceList() {
        this.progressText.setText(R.string.load_device_list);
    }

    public void onSearchDevices() {
        this.progressText.setText(R.string.search_devices);
    }

    @Override
    public void onListItemInteraction(BluetoothDevice device) {
        this.mListener.onDeviceSelected(device);
    }

    public interface OnDeviceSelectFragmentInteractionListener {
        void onDeviceSelectOpened();
        void onCloseDeviceSelect();
        void onDeviceSelectClosed();
        void onDeviceSelected(BluetoothDevice device);
    }

    /**
     * @see #state
     */
    public enum State {
        LOAD_DEVICE_LIST,
        SEARCHING_DEVICES
    }
}
