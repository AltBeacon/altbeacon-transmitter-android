package org.altbeacon.beacon.transmitter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconTransmitter;


@TargetApi(21)
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		if (checkPrerequisites()) {
			// Transmit an AltBeacon advertisement with Identifiers 2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6 1 2
			Beacon beacon = new Beacon.Builder()
		              .setId1("2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6")
		              .setId2("1")
		              .setId3("2")
		              .setBeaconTypeCode(0xbeac) // 0xbeac is for AltBeacon.  Set to a different value to transmit as a different beacon type
		              .setManufacturer(0x0000) // Choose a number of 0x00ff or less as some devices cannot detect beacons with a manufacturer code > 0x00ff
		              .build();
			
			new BeaconTransmitter(this, beacon).startAdvertising();
		}
    }

	@TargetApi(21)
	private boolean checkPrerequisites() {

        if (android.os.Build.VERSION.SDK_INT < 18) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Bluetooth LE not supported by this device's operating system");			
			builder.setMessage("You will not be able to transmit as a Beacon");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
				}
				
			});
			builder.show();
			return false;
        }
		if (!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Bluetooth LE not supported by this device");			
			builder.setMessage("You will not be able to transmit as a Beacon");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
				}
				
			});
			builder.show();
			return false;
		}		
		if (!((BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled()){
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Bluetooth not enabled");			
			builder.setMessage("Please enable Bluetooth and restart this app.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
				}
				
			});
			builder.show();
			return false;				

		}
		
		try {
			// Check to see if the getBluetoothLeAdvertiser is available.  If not, this will throw an exception indicating we are not running Android L
			((BluetoothManager) this.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().getBluetoothLeAdvertiser();
		}
		catch (Exception e) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Bluetooth LE advertising unavailable");			
			builder.setMessage("Sorry, the operating system on this device does not support Bluetooth LE advertising.  As of July 2014, only the Android L preview OS supports this feature in user-installed apps.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
				}
				
			});
			builder.show();
			return false;
			
		}
		
		return true;
	}
	
}