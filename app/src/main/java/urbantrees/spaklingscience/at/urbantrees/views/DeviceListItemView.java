package urbantrees.spaklingscience.at.urbantrees.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothDevice;


/**
 * TODO
 * @author Laurenz Fiala
 * @since 2019/02/20
 */
public class DeviceListItemView extends FrameLayout {

    private OnDeviceListItemInteractionListener listener;

    private BluetoothDevice device;

    private static String namePrefix;

    public DeviceListItemView(Context context, OnDeviceListItemInteractionListener listener, BluetoothDevice device) {
        super(context);
        this.listener = listener;
        this.device = device;
        if (namePrefix == null) {
            namePrefix = getResources().getString(R.string.search_devices_prefix);
        }

        View v = LayoutInflater.from(context).inflate(R.layout.view_device_select_list_item, this);
        ((TextView) v.findViewById(R.id.device_name)).setText(namePrefix + " " + device.getBeacon().getDeviceId());

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                DeviceListItemView.this.listener.onListItemInteraction(DeviceListItemView.this.device);
                return false;
            }
        });
    }

    public interface OnDeviceListItemInteractionListener {
        void onListItemInteraction(BluetoothDevice device);
    }

}
