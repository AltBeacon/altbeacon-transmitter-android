## AltBeacon Transmitter

This app demonstrates how to use Android L APIs to transmit as a beacon.  By default it will transmit an AltBeacon advertisement, but can easily be modified to transmit any other
beacon format as well.

If you cannot see the transmission, make sure you have a tool that can detect AltBeacons, like the Android Locate app.  Otherwise, change the code to transmit as a different beacon type.

The BeaconTransmitter class will be merged into the Android Beacon Library when Android L is released.

### Building the App

You must have an Android L development environment set up with Eclipse, and you must install the Android Beacon Library in Eclipse as a dependent Android Library project.

A more full-featured version of this app called QuickBeacon is available in the Google Play store [here](https://play.google.com/store/apps/details?id=com.radiusnetworks.quickbeacon)

### Getting Google L on your device

Google L is [downloadable from Google here.](https://developer.android.com/preview/setup-sdk.html)

### Key Android L Bluetooth LE Features:

* A bug in Android 4.4.3’s Bluetooth Advertising code is no longer an issue -- advertisements in Android L may now be the full 25-26 bytes necessary to act as a beacon.  This allows this OS version to include a transmitter power calibration value, allowing for distance estimates between the Android device and beacon.

* The APIs needed to transmit as a beacon are public in the Android L preview.  Third parties can develop such apps and install them to the phone just like any other app.  Of course, until this operating system is released, this doesn’t much matter -- phone users need root access to install Android L anyway.  But this confirms that non-rooted Android devices will be able to run third-party apps transmitting as a beacon by the end of the year.

* The APIs allow you to select between four different levels of transmitter power.   This feature is important because it allows the beacon to trigger an action at a variable distance (when it is first detected), especially on iOS devices which generally cannot range for beacons in the background.  Testing these different settings showed they do indeed make a difference on a Nexus 5.  An iPhone 4S picked up the Android transmission at the following power levels when positioned one meter away.  Note that the ultra low setting is indeed ultra low -- the iPhone could not even pick up the transmission when sitting immediately next to the Nexus 5, although a Mac running ScanBeacon could.  (Presumably the mac has a better antenna than the iPhone 4S for picking up Bluetooth signals.)  Also, note that the high power setting is roughly equivalent to the non-configurable transmitter power of an iPhone, which is -59 dBm at one meter away.

<table class="rsum">
<tr><th>Setting</th><th>RSSI @1m (iOS)</th><th>RSSI @1m (Mac)</th></tr>
<tr><td>ADVERTISE_TX_POWER_HIGH
</td><td>-56 dBm
</td><td>-55 dBm
</td></tr>
<tr><td>ADVERTISE_TX_POWER_MEDIUM
</td><td>-66 dBm
</td><td>-66 dBm
</td></tr>
<tr><td>ADVERTISE_TX_POWER_LOW
</td><td>-75 dBm
</td><td>-70 dBm
</td></tr>
<tr><td>ADVERTISE_TX_POWER_ULTRA_LOW
</td><td>(not detected)
</td><td>-79 dBm
</td></tr>
</table>

* Another setting allows you to configure the transmitter frequency by choosing one of three settings.  This is important because the more frequent the transmissions, the more accurate the distance estimates can be for an Android or iOS device detecting the beacon.  The settings below correspond to the following frequencies.  The frequencies were measured by counting packets detected by a Mac Bluetooth scanner, so they may not be exact.  Note that the `ADVERTISE_MODE_LOW_POWER` setting actually transmits the most frequently -- something that should use more power (possibly signifying a bug in the naming of the settings).

<table class="rsum">
<tr><th>Setting
</th><th>Transmit Frequency
</th></tr><tr><td>ADVERTISE_MODE_LOW_LATENCY
</td><td>approx 1 Hz
</td></tr><tr><td>ADVERTISE_MODE_BALANCED
</td><td>approx 3 Hz
</td></tr><tr><td>ADVERTISE_MODE_LOW_POWER
</td><td>approx 10 Hz
</td></tr></table>

##Android L API Changes

All these changes are possible because Android now has brand new APIs for  interacting with Bluetooth LE under the android.bluetooth.le package documented [here](https://developer.android.com/preview/api-overview.html).
To some extent, these appear to wrap the same Bluedroid drivers under the hood, but the changes are significant.  Source code for Nexus device configurations was released a few days ago, but the more important source code for the APIs is still not available, nor is full documentation.   

But based on what we can derive from interacting with the the binary preview SDK, a new Java API layer for interacting with bluetooth LE has little resemblance to what was in Android 4.4.3.  Some of the hidden APIs needed to transmit as a bluetooth LE peripheral have been moved to the android.bluetooth.le package, and lots of new APIs have been added.  The old public APIs are still available, but the new APIs offer much more functionality.

The code needed to transmit as a beacon with Android L’s android.bluetooth.le APIs is a bit different from the way we did it with the android.bluetooth APIs in Android 4.4.3.   The first difference to note is that you no longer need the special android.permission.BLUETOOTH_PRIVILEGED permission to transmit an advertisement -- just  the standard android.permission.BLUETOOTH_ADMIN permission in your AndroidManifest.xml.

## How to set up transmitting 

In order to tell Android to transmit a beacon byte sequence, you have to first get an instance of the BluetoothAdapter, and then use the brand new BluetoothLEAdvertiser class which gives you control of advertising.

```java
BluetoothManager bluetoothManager = (BluetoothManager) this.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();		
BluetoothLEAdvertiser bluetoothAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser()
```

We next need to set the advertisingBytes on the bluetoothAdvertiser.  The new APIs still allow you to set both ManufacturerData (which will be sent out with Bluetooth AD type 0xFF) and ServiceData (which will be sent out with Bluetooth AD type 0x16)  For the purposes of transmitting as a beacon, we must use the ManufacturerData, because devices looking for beacons expect to see the 0xFF Bluetooth AD type.

```java
AdvertisementData.Builder dataBuilder = new AdvertisementData.Builder();
dataBuilder.setManufacturerData((int) 0, advertisingBytes);
```
Next we can exercise the additional control Android L gives you over advertising.  Both the transmitter power level and the frequency (“advertiseMode”) can be set:

```java
AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);			
settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH); 		
settingsBuilder.setType(AdvertiseSettings.ADVERTISE_TYPE_NON_CONNECTABLE);
```

The last step is to start advertising:

```java
bluetoothAdvertiser.startAdvertising(settingsBuilder.build(), dataBuilder.build(), advertiseCallback);
```

You’ll note that this requires an advertiseCallback definition, which we will define the same way as with Android 4.4.3 like this:

```java
private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {

	@Override
	public void onAdvertiseStart(int result) {
		if (result == BluetoothAdapter.ADVERTISE_CALLBACK_SUCCESS) {
			Log.d(TAG, "started advertising successfully.");					
		}
		else {
			Log.d(TAG, "did not start advertising successfully");
		}
		
	}

	@Override
	public void onAdvertiseStop(int result) {
		if (result == BluetoothAdapter.ADVERTISE_CALLBACK_SUCCESS) {
			Log.d(TAG, "stopped advertising successfully");
		}
		else {
			Log.d(TAG, "did not stop advertising successfully");
		}
		
	}
    
};
```