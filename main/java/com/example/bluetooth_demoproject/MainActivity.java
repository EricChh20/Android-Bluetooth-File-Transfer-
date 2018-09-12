package com.example.bluetooth_demoproject;

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



import java.util.Set;
import java.io.File;
import java.util.List;
import java.util.ArrayList;



public class MainActivity extends Activity {

    // Creating objects -----------------------------
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_BLU = 1;
  //  private static final int REQUEST_DISCOVER_BT_ = 1;
    private static int CUSTOM_DIALOG_ID = 0;
    ListView dialog_ListView;
    TextView mBluetoothStatus, mPairedDevicesList, mTextFolder;
    ImageView mBluetoothIcon;
    Button mOnButton, mDiscoverableButton, mPairedDevices, mbuttonOpenDialog, msendBluetooth, mbuttonUp;
    File root, fileroot, curFolder;
    EditText dataPath;
    private static final int DISCOVER_DURATION = 300;
    private List<String> fileList = new ArrayList<String>();
// -------------------------------------------------------
    BluetoothAdapter mBlueAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataPath =(EditText)findViewById(R.id.FilePath);


        mTextFolder = findViewById(R.id.folder);
        mBluetoothStatus = findViewById(R.id.BluetoothStatus);
        mBluetoothIcon = findViewById(R.id.bluetoothIcon);
        mOnButton = findViewById(R.id.onButton);                        //assigns variables to buttons in xml layouts
        mDiscoverableButton = findViewById(R.id.discoverableButton);
        mPairedDevices = findViewById(R.id.pairedDevices);
        mPairedDevicesList = findViewById(R.id.pairedDeviceList);
        mbuttonOpenDialog = findViewById(R.id.opendailog);
        msendBluetooth = findViewById(R.id.sendBluetooth);
        mbuttonUp = findViewById(R.id.up);

        mbuttonOpenDialog.setOnClickListener(new View.OnClickListener() {                             // responds to button click
            @Override
            public void onClick(View v) {
                dataPath.setText("");
                showDialog(CUSTOM_DIALOG_ID);
            }
        });

        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        curFolder = root;
        msendBluetooth.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendViaBluetooth();
            }
        });


        // default adapter--------------------------------------------------------------------
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();                // retrives the devices's own bluetooth adapter

        if(mBlueAdapter == null){
            mBluetoothStatus.setText("Bluetooth is not available");
            return;                                                           // checks the device if bluetooth is available
        }
        else {
            mBluetoothStatus.setText("Bluetooth is available");
        }
        //if Bluetooth isnt enabled, enable it----------------------------------------------------------
        if (!mBlueAdapter.isEnabled()) {
            Intent enableBtIntent = new                                             //enables bluetooth if it is avail
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
            //set image according to bluetooth Status----------------------------------------------------
        if (mBlueAdapter.isEnabled()) {
            mBluetoothIcon.setImageResource(R.drawable.action_on);
        }                                                                               //sets image in in the main page of app, depending on status of bluetooth
        else {
            mBluetoothIcon.setImageResource(R.drawable.action_off);
        }


        //on button Click-----------------------------------------------------------------------------
        mOnButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!mBlueAdapter.isEnabled()) {
                    showToast("Turning Bluetooth on...");
                    // intent to on bluetooth
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);

                }
                else {
                    showToast("Bluetooth is already on");
                }

            }
        });

        //discover Bluetooth button------------------------------------------------------------------
        mDiscoverableButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){                                                                // will make the device discoverable for about 120 secs
                if (!mBlueAdapter.isDiscovering()) {
                    showToast("Making device discoverable");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent, REQUEST_BLU);
                }

            }

        });



        //get paired device button click--------------------------------------------------------------
        mPairedDevices.setOnClickListener(new View.OnClickListener() {                          // listening that reponds to clicks
            @Override
            public void onClick(View v) {
                if (mBlueAdapter.isEnabled()) {
                    mPairedDevices.setText("Paired Devices");
                    Set<BluetoothDevice> devices = mBlueAdapter.getBondedDevices();
                    for (BluetoothDevice device : devices){
                        mPairedDevices.append("\nDevice: " + device.getName() + "," + device );         // prints the name/ id of the device

                    }
                }
                else {
                    //bluetooth is off and cant get paired devices
                    showToast("Turn on bluetooth to get paired devices");
                }

            }
        });




    }
            //sets up dialog boxes --------------------------------------------------------------------
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        if (id == CUSTOM_DIALOG_ID) {
            dialog = new Dialog(MainActivity.this);                         // dialogs are pop up texts
            dialog.setContentView(R.layout.dailoglayout);
            dialog.setTitle("Select Files");
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);                                  //used in this app such as allowing bluetooth or making device discoverable
            mTextFolder = (TextView) dialog.findViewById(R.id.folder);
            mbuttonUp = (Button) dialog.findViewById(R.id.up);
            mbuttonUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {                                           // functionality for the get parent folder button
                    ListDir(curFolder.getParentFile());
                }
            });

            dialog_ListView = (ListView) dialog.findViewById(R.id.dialoglist);                  // the 2nd page, where you select files/ folders
            dialog_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    File selected = new File(fileList.get(position));
                    if (selected.isDirectory()) {
                        ListDir(selected);
                    }                                                                                  // all the functions of going through file paths and directories
                    else if (selected.isFile()) {
                        getSelectedFile(selected);
                    }
                    else {
                        dismissDialog(CUSTOM_DIALOG_ID);
                    }
                }
            });

        }
        return dialog;
    }


    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        if (id == CUSTOM_DIALOG_ID) {
            ListDir(curFolder);
        }
    }




    public void getSelectedFile(File f) {
        dataPath.setText(f.getAbsolutePath());
        fileList.clear();
        dismissDialog(CUSTOM_DIALOG_ID);
    }

    public void ListDir(File f) {
        if (f.equals(root)) {
            mbuttonUp.setEnabled(false);
        }
        else {
            mbuttonUp.setEnabled(true);
        }
        curFolder = f;
        mTextFolder.setText(f.getAbsolutePath());
        dataPath.setText(f.getAbsolutePath());
        File[] files = f.listFiles();
        fileList.clear();

        for (File file : files) {
            fileList.add(file.getPath());
        }
        ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList);
        dialog_ListView.setAdapter(directoryList);

    }

    // exits to app --------------------------------
    public void exit(View V) {
        mBlueAdapter.disable();
        Toast.makeText(this, "*** Now bluetooth is off...", Toast.LENGTH_LONG).show();
        finish();
    }

    // send file via bluetooth ------------------------
    public void sendViaBluetooth() {
        if(!dataPath.equals(null)) {
            if(mBlueAdapter == null) {
                Toast.makeText(this, "Device doesnt support bluetooth", Toast.LENGTH_LONG).show();
            }
            else {
               enableBluetooth();
            }

        }
        else {
            Toast.makeText(this, "please select a file", Toast.LENGTH_LONG).show();
        }
    }


    public void enableBluetooth() {
        showToast("Making device discoverable");
        Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
        startActivityForResult(discoveryIntent, REQUEST_BLU);
    }


    
// Obtain the data from the selected files
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == DISCOVER_DURATION && requestCode == REQUEST_BLU) {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_SEND);//
            i.setType("*/*");
            File file = new File(dataPath.getText().toString());
            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

            PackageManager pm = getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(i, 0);
            if (list.size() > 0) {
                String packageName = null;
                String className = null;
                boolean found = false;

                for (ResolveInfo info : list) {
                    packageName = info.activityInfo.packageName;
                    if (packageName.equals("com.android.bluetooth")) {
                        className = info.activityInfo.name;
                        found = true;
                        break;
                    }
                }
                //CHECK BLUETOOTH available or not------------------------------------------------
               // if (!found) {
               //     Toast.makeText(this, "Bluetooth not been found", Toast.LENGTH_LONG).show();
               // } else {
               //     i.setClassName(packageName, className);
               //     startActivity(i);
              //  }
            }
        } else {
            Toast.makeText(this, "Bluetooth is cancelled", Toast.LENGTH_LONG).show();
        }


    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
     //   if (id == R.id.action_settings) {
           // Toast.makeText(this, "**********************************\nDeveloper: www.santoshkumarsingh.com\n**********************************", Toast.LENGTH_LONG).show();
       //     return true;
       // }
        return super.onOptionsItemSelected(item);
    }

    //toast message function
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT) .show();
    }

}



