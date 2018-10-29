package urbantrees.spaklingscience.at.urbantrees.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanResult;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * High level abstraction layer of the standard {@link android.bluetooth.BluetoothGatt}
 * handling and associated logic.
 * @author Laurenz Fiala
 * @since 2018/05/14
 */
public class BluetoothDiscoverer implements Callable<ScanResult> {

    @Override
    public ScanResult call() throws Exception {
        return null;
    }

}
