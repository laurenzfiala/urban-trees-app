package urbantrees.spaklingscience.at.urbantrees.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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

    private List<BluetoothDevice> displayDevices;

    // ----- UI -----
    private LinearLayout deviceListLayout;
    private LinearLayout searchingDevicesLayout;
    private TextView searchingDevicesText;

    public static DeviceSelectFragment newInstance() {
        return new DeviceSelectFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.displayDevices = new ArrayList<>();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.deviceListLayout = getView().findViewById(R.id.device_list);
        this.searchingDevicesLayout = getView().findViewById(R.id.searching_devices);
        this.searchingDevicesText = getView().findViewById(R.id.searching_devices_text);

        this.mListener.onDeviceSelectOpened();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mListener.onDeviceSelectClosed();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDeviceSelectFragmentInteractionListener) {
            mListener = (OnDeviceSelectFragmentInteractionListener) context;
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

    public synchronized void addDevice(BluetoothDevice device) {
        if (this.getContext() == null) {
            return;
        }

        this.deviceListLayout.addView(new DeviceListItemView(this.getContext(), this, device), this.displayDevices.size());
        this.searchingDevicesText.setText(getResources().getString(R.string.search_devices_more));

        this.displayDevices.add(device);
    }

    @Override
    public void onListItemInteraction(BluetoothDevice device) {
        this.mListener.onDeviceSelected(device);
    }

    public interface OnDeviceSelectFragmentInteractionListener {
        void onDeviceSelectOpened();
        void onDeviceSelectClosed();
        void onDeviceSelected(BluetoothDevice device);
    }
}
