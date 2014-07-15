package com.sensortest;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.io.File;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;






import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.vishnu.sensortest.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements BluetoothAdapter.LeScanCallback {
    private static final String TAG = "BluetoothGattActivity";

    private static final String DEVICE_NAME = "SensorTag";
    TestGesture t = new TestGesture();
    /* Humidity Service */
    private static final UUID HUMIDITY_SERVICE = UUID.fromString("f000aa20-0451-4000-b000-000000000000");
    private static final UUID HUMIDITY_DATA_CHAR = UUID.fromString("f000aa21-0451-4000-b000-000000000000");
    private static final UUID HUMIDITY_CONFIG_CHAR = UUID.fromString("f000aa22-0451-4000-b000-000000000000");
    /* Barometric Pressure Service */
    private static final UUID PRESSURE_SERVICE = UUID.fromString("f000aa40-0451-4000-b000-000000000000");
    private static final UUID PRESSURE_DATA_CHAR = UUID.fromString("f000aa41-0451-4000-b000-000000000000");
    private static final UUID PRESSURE_CONFIG_CHAR = UUID.fromString("f000aa42-0451-4000-b000-000000000000");
    private static final UUID PRESSURE_CAL_CHAR = UUID.fromString("f000aa43-0451-4000-b000-000000000000");
    /* Acceleromter configuration servcie */
    private static final UUID ACCELEROMETER_SERVICE = UUID.fromString("f000aa10-0451-4000-b000-000000000000");
    private static final UUID ACCELEROMETER_DATA_CHAR = UUID.fromString("f000aa11-0451-4000-b000-000000000000");
    private static final UUID ACCELEROMETER_CONFIG_CHAR = UUID.fromString("f000aa12-0451-4000-b000-000000000000");
    private static final UUID ACCELEROMETER_PERIOD_CHAR = UUID.fromString("f000aa13-0451-4000-b000-000000000000");

    /* Gyroscope Configuration service */
    private static final UUID GYROSCOPE_SERVICE = UUID.fromString("f000aa50-0451-4000-b000-000000000000");
    private static final UUID GYROSCOPE_DATA_CHAR = UUID.fromString("f000aa51-0451-4000-b000-000000000000");
    private static final UUID GYROSCOPE_CONFIG_CHAR = UUID.fromString("f000aa52-0451-4000-b000-000000000000");
    /* Client Configuration Descriptor */
    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    private BluetoothAdapter mBluetoothAdapter;
    private SparseArray<BluetoothDevice> mDevices;
    

    private BluetoothGatt mConnectedGatt;

    private TextView mResult,/*mTemperature //mHumidity //mPressure*/mAccelerometer,trainingCounter,segmentValues,helpText;
    private Button testGesture,saveButton;
    private EditText gestureName;
    private ProgressDialog mProgress;
    private Boolean testStart=false;
    private static int testCounter =0;
    private String fileName="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        setProgressBarIndeterminate(true);

        /*
         * We are going to display the results in some text fields
         */
        ////mTemperature = (TextView) findViewById(R.id.text_temperature);
        ////mHumidity = (TextView) findViewById(R.id.text_humidity);
        ////mPressure = (TextView) findViewById(R.id.text_pressure);
      mAccelerometer=(TextView)findViewById(R.id.text_accelerometer);
        testGesture=(Button)findViewById(R.id.testGesture);
       // saveButton=(Button)findViewById(R.id.saveButton);
        gestureName=(EditText)findViewById(R.id.gestureName);
        trainingCounter=(TextView)findViewById(R.id.textView1);
        segmentValues=(TextView)findViewById(R.id.startAndEnd);
        helpText=(TextView)findViewById(R.id.textView2); 
        /*
         * Bluetooth in Android 4.3 is accessed via the BluetoothManager, rather than
         * the old static BluetoothAdapter.getInstance()
         */
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

        mDevices = new SparseArray<BluetoothDevice>();

        /*
         * A progress dialog will be needed while the connection process is
         * taking place
         */
        mProgress = new ProgressDialog(this);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);
        
       Button b3 = (Button)findViewById(R.id.button1);
		b3.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			/*	WebView webview = new WebView(MainActivity.this);
				 setContentView(webview);
				 webview.loadUrl("http://192.168.199.128:8080/HMMWS/jaxrs/generic/HMMTrainingTest/-home-cloudera-Stomp.seq:-home-cloudera-Facepalm.seq/stomp:facepalm/-home-cloudera-Stomp.seq");
*/
				HttpClient dClientRequest = new DefaultHttpClient();
				
				InputStream resultStream = null;
				String sWeatherResult = "";
				String s="";
				//String URL=arg0[0];
				//String URL="http://api.openweathermap.org/data/2.5/weather?q="+rEditText1.getText();
				
				try {
					
					HttpGet hpURL = new HttpGet("http://192.168.199.128:8080/HMMWS/jaxrs/generic/HMMTrainingTest/-home-cloudera-Stomp.seq:-home-cloudera-Facepalm.seq/stomp:facepalm/-home-cloudera-Stomp.seq");
				   HttpResponse hrWebResponse = dClientRequest.execute(hpURL);
				    StatusLine statusLine = hrWebResponse.getStatusLine();
				    if(statusLine.getStatusCode() == HttpStatus.SC_OK)
				    {
				    HttpEntity heWebEntity = hrWebResponse.getEntity();
				    resultStream = heWebEntity.getContent();
				    BufferedReader bReader = new BufferedReader(new InputStreamReader(resultStream, "UTF-8"), 8);
				    StringBuilder sb = new StringBuilder();
				    
				    while((s = bReader.readLine()) != null)
				    {
				        sb.append(s + "\n");
				        //rEditText.setText(s);
				        
				        
				        
				    }
				    sWeatherResult = sb.toString();	
				    
				    if(sWeatherResult.contains("stomp"))
				    {
				    	  mResult=(TextView)findViewById(R.id.textView2);
				    	  mResult.setText("you are angry");
				    }
				    else if(sWeatherResult.contains("facepalm"))
				    		{
				    	  mResult=(TextView)findViewById(R.id.textView2);
				    	  mResult.setText("you are irritated");
				    		}
				    
				    
				    else
				    {
				    	
				    	  mResult=(TextView)findViewById(R.id.textView2);
				    	  mResult.setText("you are happy");
				    	  ImageView i;
				    	  i=(ImageView)findViewById(R.id.imageView1);
				    	 // i.setImageDrawable(R.drawable.ic_launcher1);
				    }
				    }
				    
				    
				    
				} catch (Exception e) { 
					e.printStackTrace();
				
				}
				
				

			}
		});
        
        
        
        
    }
		public void testGesture(View view){
		
			testStart=true;
			
			fileName=gestureName.getText().toString();
			helpText.setTextSize(50);
			helpText.setText("After stop you can see the file in /sdCard/Data"+fileName);
		}
    public void stopApp(View view){
    	this.finish();
    }
		
    @Override
    protected void onResume() {
        super.onResume();
        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        /*
         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
         * from installing on these devices, but this will allow test devices or other
         * sideloads to report whether or not the feature exists.
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        clearDisplayValues();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Make sure dialog is hidden
        mProgress.dismiss();
        //Cancel any scans in progress
        mHandler.removeCallbacks(mStopRunnable);
        mHandler.removeCallbacks(mStartRunnable);
        mBluetoothAdapter.stopLeScan(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Disconnect from any active tag connection
        if (mConnectedGatt != null) {
            mConnectedGatt.disconnect();
            mConnectedGatt = null;
        }
        
       //new SendFile().execute("");        
    }
   /*
class SendFile extends AsyncTask<String, Void, String> {
		

	    private Exception exception;

	    protected String doInBackground(String... urls) {
	        try {        	
	        	
	        	JSch ssh = new JSch();
			        JSch.setConfig("StrictHostKeyChecking", "no");
			        Session session;
					try {
						session = ssh.getSession("cloudera", "134.193.136.147", 22);
					
			        session.setPassword("password");
			        session.connect();
			        Channel channel = session.openChannel("sftp");
			        channel.connect();
			        ChannelSftp sftp = (ChannelSftp) channel;
			        
			        File sdCard = Environment.getExternalStorageDirectory(); 
					File directory = new File (sdCard.getAbsolutePath() + "/Data");
				    
					Log.i(null,directory+"/sensor711.txt");
					
			        
			        sftp.put(directory+"/sensor711.txt", "/home/cloudera/");
			    	
					} catch (JSchException e) {
						// TODO Auto-generated catch block
						Log.i(null,e.toString());
						e.printStackTrace();
						 Toast.makeText(getApplicationContext(), 
				                   e.toString(), Toast.LENGTH_LONG).show();
					} catch (SftpException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						 Toast.makeText(getApplicationContext(), 
				                   e.toString(), Toast.LENGTH_LONG).show();
					}

	        	
	        } catch (Exception e) {
	            this.exception = e;
	            return null;
	        }
			return null;
	    }
    
}
    */
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add the "scan" option to the menu
        getMenuInflater().inflate(R.menu.main, menu);
        //Add any device elements we've discovered to the overflow menu
        for (int i=0; i < mDevices.size(); i++) {
            BluetoothDevice device = mDevices.valueAt(i);
            menu.add(0, mDevices.keyAt(i), 0, device.getName());
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                mDevices.clear();
                startScan();
                return true;
            default:
                //Obtain the discovered device to connect with
                BluetoothDevice device = mDevices.get(item.getItemId());
                Log.i(TAG, "Connecting to "+device.getName());
                /*
                 * Make a connection with the device using the special LE-specific
                 * connectGatt() method, passing in a callback for GATT events
                 */
                mConnectedGatt = device.connectGatt(this, false, mGattCallback);
                //Display progress UI
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Connecting to "+device.getName()+"..."));
                //t.train();
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearDisplayValues() {
       // //mTemperature.setText("---");
        //mHumidity.setText("---");
        //mPressure.setText("---");
        mAccelerometer.setText("---");
    }


    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    private void startScan() {
        mBluetoothAdapter.startLeScan(this);
        setProgressBarIndeterminateVisibility(true);

        mHandler.postDelayed(mStopRunnable, 2500);
    }

    private void stopScan() {
        mBluetoothAdapter.stopLeScan(this);
        setProgressBarIndeterminateVisibility(false);
    }

    /* BluetoothAdapter.LeScanCallback */

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);
        /*
         * We are looking for SensorTag devices only, so validate the name
         * that each device reports before adding it to our collection
         */
        if (DEVICE_NAME.equals(device.getName())) {
            mDevices.put(device.hashCode(), device);
            //Update the overflow menu
            invalidateOptionsMenu();
        }
    }

    /*
     * In this callback, we've created a bit of a state machine to enforce that only
     * one characteristic be read or written at a time until all of our sensors
     * are enabled and we are registered to get notifications.
     */
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        /* State Machine Tracking */
        private int mState = 0;

        private void reset() { mState = 0; }

        private void advance() { mState++; }

        /*
         * Send an enable command to each sensor by writing a configuration
         * characteristic.  This is specific to the SensorTag to keep power
         * low by disabling sensors you aren't using.
         */
        private void enableNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 0:
                    Log.d(TAG, "Enabling pressure cal");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_CONFIG_CHAR);
                    characteristic.setValue(new byte[] {0x02});
                    break;
                case 1:
                    Log.d(TAG, "Enabling pressure");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_CONFIG_CHAR);
                    characteristic.setValue(new byte[] {0x01});
                    break;
                case 2:
                    Log.d(TAG, "Enabling humidity");
                    characteristic = gatt.getService(HUMIDITY_SERVICE)
                            .getCharacteristic(HUMIDITY_CONFIG_CHAR);
                    characteristic.setValue(new byte[] {0x01});
                    break;
                case 3:
                    Log.d(TAG, "Enabling Accelerometer");
                    characteristic = gatt.getService(ACCELEROMETER_SERVICE)
                            .getCharacteristic(ACCELEROMETER_CONFIG_CHAR);
                    characteristic.setValue(new byte[] {0x01});
                    break;
                case 4:
                    Log.d(TAG,"Enabling accelerometer");
                    characteristic= gatt.getService(ACCELEROMETER_SERVICE)
                            .getCharacteristic(ACCELEROMETER_PERIOD_CHAR);
                    characteristic.setValue(new byte[]{(byte)10});
                    break;
                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }

            gatt.writeCharacteristic(characteristic);
        }

        /*
         * Read the data characteristic's value for each sensor explicitly
         */
        private void readNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 0:
                    Log.d(TAG, "Reading pressure cal");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_CAL_CHAR);
                    break;
                case 1:
                    Log.d(TAG, "Reading pressure");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_DATA_CHAR);
                    break;
                case 2:
                    Log.d(TAG, "Reading humidity");
                    characteristic = gatt.getService(HUMIDITY_SERVICE)
                            .getCharacteristic(HUMIDITY_DATA_CHAR);
                    break;
                case 3:
                    Log.d(TAG, "Reading Accelerometer");
                    characteristic = gatt.getService(ACCELEROMETER_SERVICE)
                            .getCharacteristic(ACCELEROMETER_DATA_CHAR);
                    break;
                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }

            gatt.readCharacteristic(characteristic);
        }

        /*
         * Enable notification of changes on the data characteristic for each sensor
         * by writing the ENABLE_NOTIFICATION_VALUE flag to that characteristic's
         * configuration descriptor.
         */
        private void setNotifyNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 0:
                    Log.d(TAG, "Set notify pressure cal");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_CAL_CHAR);
                    break;
                case 1:
                    Log.d(TAG, "Set notify pressure");
                    characteristic = gatt.getService(PRESSURE_SERVICE)
                            .getCharacteristic(PRESSURE_DATA_CHAR);
                    break;
                case 2:
                    Log.d(TAG, "Set notify humidity");
                    characteristic = gatt.getService(HUMIDITY_SERVICE)
                            .getCharacteristic(HUMIDITY_DATA_CHAR);
                    break;
                case 3:
                    Log.d(TAG, "Set notify accelerometer");
                    characteristic = gatt.getService(ACCELEROMETER_SERVICE)
                            .getCharacteristic(ACCELEROMETER_DATA_CHAR);
                    break;
                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }

            //Enable local notifications
            gatt.setCharacteristicNotification(characteristic, true);
            //Enabled remote notifications
            BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(desc);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "Connection State Change: "+status+" -> "+connectionState(newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                /*
                 * Once successfully connected, we must next discover all the services on the
                 * device before we can read and write their characteristics.
                 */
                gatt.discoverServices();
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Discovering Services..."));
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                /*
                 * If at any point we disconnect, send a message to clear the weather values
                 * out of the UI
                 */
                mHandler.sendEmptyMessage(MSG_CLEAR);
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                /*
                 * If there is a failure at any stage, simply disconnect
                 */
                gatt.disconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "Services Discovered: "+status);
            mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Enabling Sensors..."));
            /*
             * With services discovered, we are going to reset our state machine and start
             * working through the sensors we need to enable
             */
            reset();
            enableNextSensor(gatt);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //For each read, pass the data up to the UI thread to update the display
            if (HUMIDITY_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_HUMIDITY, characteristic));
            }
            if (PRESSURE_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE, characteristic));
            }
            if (PRESSURE_CAL_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE_CAL, characteristic));
            }
            if (ACCELEROMETER_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_ACCELEROMETER, characteristic));
            }

            //After reading the initial value, next we enable notifications
            setNotifyNextSensor(gatt);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //After writing the enable flag, next we read the initial value
            readNextSensor(gatt);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            /*
             * After notifications are enabled, all updates from the device on characteristic
             * value changes will be posted here.  Similar to read, we hand these up to the
             * UI thread to update the display.
             */
            if (HUMIDITY_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_HUMIDITY, characteristic));
            }
            if (PRESSURE_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE, characteristic));
            }
            if (PRESSURE_CAL_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_PRESSURE_CAL, characteristic));
            }
            if (ACCELEROMETER_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_ACCELEROMETER, characteristic));
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //Once notifications are enabled, we move to the next sensor and start over with enable
            advance();
            enableNextSensor(gatt);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "Remote RSSI: "+rssi);
        }

        private String connectionState(int status) {
            switch (status) {
                case BluetoothProfile.STATE_CONNECTED:
                    return "Connected";
                case BluetoothProfile.STATE_DISCONNECTED:
                    return "Disconnected";
                case BluetoothProfile.STATE_CONNECTING:
                    return "Connecting";
                case BluetoothProfile.STATE_DISCONNECTING:
                    return "Disconnecting";
                default:
                    return String.valueOf(status);
            }
        }
    };

    /*
     * We have a Handler to process event results on the main thread
     */
    private static final int MSG_HUMIDITY = 101;
    private static final int MSG_PRESSURE = 102;
    private static final int MSG_PRESSURE_CAL = 103;
    private static final int MSG_ACCELEROMETER = 104;
    private static final int MSG_PROGRESS = 201;
    private static final int MSG_DISMISS = 202;
    private static final int MSG_CLEAR = 301;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            BluetoothGattCharacteristic characteristic;
            switch (msg.what) {
                case MSG_HUMIDITY:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining humidity value");
                        return;
                    }
                    updateHumidityValues(characteristic);
                    break;
                case MSG_PRESSURE:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining pressure value");
                        return;
                    }
                    updatePressureValue(characteristic);
                    break;
                case MSG_PRESSURE_CAL:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining cal value");
                        return;
                    }
                    updatePressureCals(characteristic);
                    break;
                case MSG_ACCELEROMETER:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining Accelerometer value");
                        return;
                    }
                    updateAccelerometerValue(characteristic);
                    break;
                case MSG_PROGRESS:
                    mProgress.setMessage((String) msg.obj);
                    if (!mProgress.isShowing()) {
                        mProgress.show();
                    }
                    break;
                case MSG_DISMISS:
                    mProgress.hide();
                    break;
                case MSG_CLEAR:
                    clearDisplayValues();
                    break;
            }
        }
    };

    /* Methods to extract sensor data and update the UI */

    private void updateHumidityValues(BluetoothGattCharacteristic characteristic) {
        double humidity = SensorTagData.extractHumidity(characteristic);

        //mHumidity.setText(String.format("%.0f%%", humidity));
    }
    double x1,y1,z1,d=0.0f,norm;
    Boolean trigger=false;
    ArrayList<String> dataPoints = new ArrayList<String>();
    
    private void updateAccelerometerValue(BluetoothGattCharacteristic characteristic) {
        //Float[] data = SensorTagData.extractAccelerometerReading(characteristic,0);
        Float[] values = SensorTagData.extractAccelerometerReading(characteristic, 0);
        double x,y,z;
        x=values[0];
        y=values[1];
        z=values[2];
        mAccelerometer.setText(x+"\t"+y+"\t"+z);
        //String formatdata=data[0].toString()+","+data[1].toString()+","+data[2].toString();
        if(testStart){
        	d= Math.sqrt( Math.pow((x-x1),2 )  + Math.pow((y-y1),2 ) + Math.pow((z-z1),2 ));
            if(d>=0.3 && !trigger){
         	   Log.i("start","start");
         	  segmentValues.setText("start");
         	   trigger=true;
            }
            else if(d<=0.1 && trigger){
          	   Log.i("end", "end");
          	   trigger=false;
          	   segmentValues.setText("end");
          	   try{
          		   if(dataPoints.size()>6){
          			   
          		   whichGesture(dataPoints);
          		   testCounter++;
          		 Log.i("train counter", String.valueOf(testCounter));
             	   trainingCounter.setText(String.valueOf(testCounter));
          		   }
          	   dataPoints.clear(); 
          	 
          	   }catch(Exception e){
          		   e.printStackTrace();
          	   }
            }
            if(trigger){
           	  dataPoints.add("[ "+x + " " + y + " " + z+" ] ;");
              }
            
            x1=x;y1=y;z1=z;
        }
             
        
    }

   // private int[] //mPressureCals;
    private void updatePressureCals(BluetoothGattCharacteristic characteristic) {
        //mPressureCals = SensorTagData.extractCalibrationCoefficients(characteristic);
    }

    private void updatePressureValue(BluetoothGattCharacteristic characteristic) {
       // if (//mPressureCals == null) return;
        //double pressure = SensorTagData.extractBarometer(characteristic, //mPressureCals);
       // double temp = SensorTagData.extractBarTemperature(characteristic, //mPressureCals);

        //mTemperature.setText(String.format("%.1f\u00B0C", temp));
        //mPressure.setText(String.format("%.2f", pressure));
    }
    Boolean gesture = false;
	StringBuffer buffer = new StringBuffer();
    private void whichGesture(ArrayList<String> datapointsList) throws Exception {
    	
    	for (int i = 0; i < datapointsList.size(); i++) {
			
    		buffer.append(datapointsList.get(i));
    		
		}
    	buffer.append(System.getProperty("line.separator"));
    	SaveData(buffer.toString());
        buffer.delete(0, buffer.length());
         
    }
    private void SaveData(String string) {
    	// Log.i("string", string); 
    	File sdCard = Environment.getExternalStorageDirectory(); 
    	File directory = new File (sdCard.getAbsolutePath() + "/Data"); 
    	if(!directory.exists()) 
    		directory.mkdirs(); 
    	String fname = fileName; 
    	File file = new File (directory, fname); 
    	try { 
    		if(!file.exists()) file.createNewFile(); 
    		FileOutputStream out = new FileOutputStream(file,true); 
    		out.write(string.getBytes()); 
    		out.flush(); 
    		out.close(); } 
    	catch (Exception e) 
    	{ e.printStackTrace();
     }
    	
    }
}
