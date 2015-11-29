package test.cl.wlanautoctrlbyble;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class CentralService extends Service {

    private final String TAG ="SrvWlanOnOff";
    //対象BLEデバイス
    private static final String DEVICE_ADDR = "00:A0:50:11:05:19";//CypressSample
    //WLAN OFF判定回数（連続でBLEビーコン見つからない場合WLAN OFFする）
    private static int countWlanOff = 0;
    private final static int MAX_COUNT_WLAN_OFF = 5;
    //BLEスキャン期間

    //BLEスキャン間隔

    private final static int SDKVER_LOLLIPOP = 21;
    private BluetoothManager mBleManager;
    private BluetoothAdapter mBleAdapter;
    private boolean mIsBluetoothEnable = false;
    private BluetoothLeScanner mBleScanner;
    private BluetoothGatt mBleGatt;
    private BluetoothGattCharacteristic mBleCharacteristic;



    private void checkWlanOnOff(String strAddr){
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if(isFindBleDevice(strAddr)){
            if (wifiManager.isWifiEnabled() == false) {
                //WLAN ON
                Log.d(TAG,"WLAN ON");
                wifiManager.setWifiEnabled(true);
            }
            countWlanOff = 0;//カウンタクリア
        }else{
            if (wifiManager.isWifiEnabled() == true) {
                countWlanOff++;
                if (countWlanOff > MAX_COUNT_WLAN_OFF) {
                    //WLAN OFF
                    Log.d(TAG,"WLAN OFF count:"+countWlanOff);
                    wifiManager.setWifiEnabled(false);
                }
            }
        }
    }
    private boolean isFindBleDevice(String strAddr){
        boolean result = false;
        if (DEVICE_ADDR.equals(strAddr)){
            Log.i("TEST","Device Find!!!! addrß"+strAddr);
            result = true;
        }
        return result;
    }

    private final LeScanCallback mScanCallback = new LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            //WLAN OnOff Check
            checkWlanOnOff(device.getAddress().toString().toUpperCase());

        }
    };
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            // 接続状況が変化したら実行.
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // 接続に成功したらサービスを検索する.
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // 接続が切れたらGATTを空にする.
                if (mBleGatt != null)
                {
                    mBleGatt.close();
                    mBleGatt = null;
                }
                mIsBluetoothEnable = false;
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            // Serviceが見つかったら実行.
            if (status == BluetoothGatt.GATT_SUCCESS) {

            }
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {

        }
    };


    @Override
    public void onCreate() {

        super.onCreate();
       mIsBluetoothEnable = false;


        // Bluetoothの使用準備.
        mBleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBleAdapter = mBleManager.getAdapter();

        // BluetoothがOffならインテントを表示する.
        if ((mBleAdapter == null)
                || (! mBleAdapter.isEnabled())) {
            Log.e("TEST","not get ble adapter!");
        }
        else
        {
            // BLEが使用可能ならスキャン開始.
            this.scanNewDevice();
        }
    }

    private void scanNewDevice()
    {
        // OS ver.5.0以上ならBluetoothLeScannerを使用する.
        if (Build.VERSION.SDK_INT >= SDKVER_LOLLIPOP)
        {
            this.startScanByBleScanner();
        }
        else
        {
            // デバイスの検出.
            mBleAdapter.startLeScan(mScanCallback);
        }
    }
    @TargetApi(SDKVER_LOLLIPOP)
    private void startScanByBleScanner()
    {
        mBleScanner = mBleAdapter.getBluetoothLeScanner();
        // デバイスの検出.
        mBleScanner.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                //WLAN OnOff判定
                checkWlanOnOff(result.getDevice().getAddress().toString().toUpperCase());
            }
            @Override
            public void onScanFailed(int intErrorCode)
            {
                super.onScanFailed(intErrorCode);
            }
        });
    }

    @Override
    public void onDestroy()
    {
        // 画面遷移時は通信を切断する.
        mIsBluetoothEnable = false;
        if(mBleGatt != null) {
            mBleGatt.close();
            mBleGatt = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}