//参考 
// http://blog.fenrir-inc.com/jp/2013/10/bluetooth-le-android.html
// http://yegang.hatenablog.com/entry/2014/08/09/195246

package test.cl.wlanautoctrlbyble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.util.UUID;

public class BleTestActivity extends Activity implements BluetoothAdapter.LeScanCallback {
    /** BLE 機器スキャンタイムアウト (ミリ秒) */
    private static final long SCAN_PERIOD = 10000;
    /** 検索機器の機器名 */
    //private static final String DEVICE_NAME = "SensorTag";
    //private static final String DEVICE_NAME = "Blank";
    private static final String DEVICE_NAME = "BSBT4PT02BK";
    //private static final String DEVICE_ADDR = "59:9A:41:AA:4A:7A";
    //private static final String DEVICE_ADDR = "00:1B:DC:44:01:39";
    private static final String DEVICE_ADDR = "00:02:5B:00:B9:21";//CSR評価基板
    /** 対象のサービスUUID */
    //private static final String DEVICE_BUTTON_SENSOR_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    //private static final String DEVICE_BUTTON_SENSOR_SERVICE_UUID = "00000532-0000-1000-8000-00805f9b34fb";
    //private static final String DEVICE_BUTTON_SENSOR_SERVICE_UUID = "0000180F-0000-1000-8000-00805f9b34fb";
    private static final String DEVICE_BUTTON_SENSOR_SERVICE_UUID = "00005500-d102-11e1-9b23-00025b00a5a5";//CSR SPP用サービス

    /** 対象のキャラクタリスティックUUID */
    //private static final String DEVICE_BUTTON_SENSOR_CHARACTERISTIC_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final String DEVICE_BUTTON_SENSOR_CHARACTERISTIC_UUID = "00005501-d102-11e1-9b23-00025b00a5a5";//CSR SPP用デスクリプタ-

    /** キャラクタリスティック設定UUID */
    //private static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    //private static final String CLIENT_CHARACTERISTIC_CONFIG = "000052c1-0000-1000-8000-00805f9b34fb";
    private static final String CLIENT_CHARACTERISTIC_CONFIG = "00005501-d102-11e1-9b23-00025b00a5a5";//CSR SPP Notification 受信用

    private final static int SDKVER_LOLLIPOP = 21;

    private static final String TAG = "BLESample";
    private BleStatus mStatus = BleStatus.DISCONNECTED;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mBluetoothGatt;
    private TextView mStatusText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    //	setContentView(R.layout.main);

        mBluetoothManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        //clickConnect();



        /*
        findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickConnect();
            }
        });
        findViewById(R.id.btn_disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickDisconnect();
            }
        });

        mStatusText = (TextView)findViewById(R.id.text_status);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mStatusText.setText(((BleStatus) msg.obj).name());
            }
        };
        */
    }

    private void scanNewDevice()
    {
        // OS ver.5.0以上ならBluetoothLeScannerを使用する.
        if (Build.VERSION.SDK_INT >= SDKVER_LOLLIPOP)
        {
  //          this.startScanByBleScanner();
        }
        else
        {
            // デバイスの検出.
    //        mBluetoothAdapter.startLeScan(mScanCallback);
        }
    }
    /**
    @TargetApi(SDKVER_LOLLIPOP)
    private void startScanByBleScanner()
    {
        LeScanCallback mBleScanner = mBluetoothAdapter.getBluetoothLeScanner();
        // デバイスの検出.
        LeScanCallback mBleScanner.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                // スキャン中に見つかったデバイスに接続を試みる.第三引数には接続後に呼ばれるBluetoothGattCallbackを指定する.
                result.getDevice().connectGatt(getApplicationContext(), false, mBluetoothGattCallback);
            }
            @Override
            public void onScanFailed(int intErrorCode)
            {
                super.onScanFailed(intErrorCode);
            }
        });
    }
    */
    /** BLE機器を検索する */
    private void clickConnect() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(BleTestActivity.this);
                if (BleStatus.SCANNING.equals(mStatus)) {
                    setStatus(BleStatus.SCAN_FAILED);
                }
            }
        }, SCAN_PERIOD);

        mBluetoothAdapter.stopLeScan(this);
        mBluetoothAdapter.startLeScan(this);
        setStatus(BleStatus.SCANNING);
    }

    /** BLE 機器との接続を解除する */
    private void clickDisconnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            setStatus(BleStatus.CLOSED);
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.d(TAG, "device found: " + device.getAddress());
    //	if (DEVICE_NAME.equals(device.getName())) {
        if (DEVICE_ADDR.equals(device.getAddress())){
            setStatus(BleStatus.DEVICE_FOUND);
            // 省電力のためスキャンを停止する
            mBluetoothAdapter.stopLeScan(this);
            // GATT接続を試みる
            mBluetoothGatt = device.connectGatt(this, false, mBluetoothGattCallback);
        }
    }

    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange: " + status + " -> " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                // GATTへ接続成功
                // サービスを検索する
                gatt.discoverServices();


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // GATT通信から切断された
                setStatus(BleStatus.DISCONNECTED);
                mBluetoothGatt = null;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered received: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(UUID.fromString(DEVICE_BUTTON_SENSOR_SERVICE_UUID));
                if (service == null) {
                    // サービスが見つからなかった
                    setStatus(BleStatus.SERVICE_NOT_FOUND);
                } else {
                    // サービスを見つけた
                    setStatus(BleStatus.SERVICE_FOUND);
                    //Bondする
                    BluetoothDevice device = gatt.getDevice();
                    device.setPairingConfirmation(true);
                    device.createBond();


                    BluetoothGattCharacteristic characteristic =
                    service.getCharacteristic(UUID.fromString(DEVICE_BUTTON_SENSOR_CHARACTERISTIC_UUID));

                    if (characteristic == null) {
                        // キャラクタリスティックが見つからなかった
                        setStatus(BleStatus.CHARACTERISTIC_NOT_FOUND);
                    } else {
                        // キャラクタリスティックを見つけた
                        //■実験Write ここでは書込できない？？早すぎる？？？
    //					characteristic.setValue(new byte[] { 'a','b','c'});
    //					gatt.writeCharacteristic(characteristic);
                        //■実験Read 確認OK
    //					Log.e(TAG, "readCharacteristic: " + gatt.readCharacteristic(characteristic));
    //					Log.e(TAG, "readCharacteristic: " + gatt.readCharacteristic(characteristic));
    //					Log.e(TAG, "readCharacteristic: " + gatt.readCharacteristic(characteristic));



                        // Notification を要求する
                        boolean registered = gatt.setCharacteristicNotification(characteristic, true);
                        // Characteristic の Notification 有効化
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                        UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);

                        if (registered) {
                            // Characteristics通知設定完了
                            setStatus(BleStatus.NOTIFICATION_REGISTERED);
                        } else {
                            setStatus(BleStatus.NOTIFICATION_REGISTER_FAILED);
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
        BluetoothGattCharacteristic characteristic,
        int status) {
            Log.d(TAG, "onCharacteristicRead: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // READ成功
                Log.e(TAG, "getValue: " + characteristic.getValue()[0]);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
        BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged");
            // Characteristicの値更新通知

            if (DEVICE_BUTTON_SENSOR_CHARACTERISTIC_UUID.equals(characteristic.getUuid().toString())) {
                Byte value = characteristic.getValue()[0];
                Log.e(TAG, "str:"+characteristic.getStringValue(0));

                //■Write 実験 WriteとNotificationが同じcharacteristicのため確認できた
    //			characteristic.setValue(new byte[] { 'a','b','c'});
    //			gatt.writeCharacteristic(characteristic);


                boolean left = (0 < (value & 0x02));
                boolean right = (0 < (value & 0x01));
                updateButtonState(left, right);
            }
        }
    };//Callback

    private void updateButtonState(final boolean left, final boolean right) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
    /*			View leftView = findViewById(R.id.left);
                View rightView = findViewById(R.id.right);
                leftView.setBackgroundColor( (left ? Color.BLUE : Color.TRANSPARENT) );
                rightView.setBackgroundColor( (right ? Color.BLUE : Color.TRANSPARENT) );
    */
            }
        });
    }
    private void setStatus(BleStatus status) {
        mStatus = status;
        mHandler.sendMessage(status.message());
    }
    private enum BleStatus {
        DISCONNECTED, SCANNING, SCAN_FAILED, DEVICE_FOUND, SERVICE_NOT_FOUND, SERVICE_FOUND,
        CHARACTERISTIC_NOT_FOUND, NOTIFICATION_REGISTERED, NOTIFICATION_REGISTER_FAILED, CLOSED ;
        public Message message() {
            Message message = new Message();
            message.obj = this;
            return message;
        }
    }


}
//BleTestActivity　Class
