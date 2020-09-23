package urbantrees.spaklingscience.at.urbantrees.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
    private TextView nameTextView;
    private TextView annotationTextView;
    private ImageView iconImageView;

    private static String namePrefix;

    public DeviceListItemView(Context context, OnDeviceListItemInteractionListener listener, BluetoothDevice device) {
        super(context);
        this.setTag(device);

        this.listener = listener;
        this.device = device;
        if (namePrefix == null) {
            namePrefix = getResources().getString(R.string.search_devices_prefix);
        }

        View v = LayoutInflater.from(context).inflate(R.layout.view_device_select_list_item, this);
        this.nameTextView = v.findViewById(R.id.device_name);
        this.annotationTextView = v.findViewById(R.id.device_annotation);
        this.iconImageView = v.findViewById(R.id.device_icon);

        this.update();
    }

    /**
     * Update this item according to its members.
     */
    public void update() {

        if (this.device.getBeacon() == null) {
            this.nameTextView.setText(getResources().getString(R.string.search_devices_unknown_device));
            this.annotationTextView.setText(device.getAddress());
            this.annotationTextView.setVisibility(VISIBLE);
            this.iconImageView.setImageAlpha(127);
        } else {
            this.nameTextView.setText(namePrefix + " " + this.device.getBeacon().getDeviceId());
            this.annotationTextView.setVisibility(GONE);
            this.iconImageView.setImageAlpha(255);
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    DeviceListItemView.this.listener.onListItemInteraction(DeviceListItemView.this.device);
                }
            });
        }

    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public interface OnDeviceListItemInteractionListener {
        void onListItemInteraction(BluetoothDevice device);
    }

}
