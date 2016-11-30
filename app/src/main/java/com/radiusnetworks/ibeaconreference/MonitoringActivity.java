package com.radiusnetworks.ibeaconreference;

import java.util.ArrayList;
import java.util.Collection;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

import com.radiusnetworks.lib.SQLite;
import com.radiusnetworks.lib.NTPClient;

public class MonitoringActivity extends Activity implements IBeaconConsumer {
	protected static final String TAG = "MonitoringActivity";

	private ListView list = null;
	private BeaconAdapter adapter = null;
	private ArrayList<IBeacon> arrayL = new ArrayList<IBeacon>();
	private LayoutInflater inflater;

	private BeaconServiceUtility beaconUtill = null;
	private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

	private SQLite dbSQLite;
	private NTPClient cNTP;
	private boolean showView = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitor);
		beaconUtill = new BeaconServiceUtility(this);
		list = (ListView) findViewById(R.id.list);
		adapter = new BeaconAdapter();
		list.setAdapter(adapter);
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		dbSQLite = new SQLite(this, MonitoringActivity.this);
		cNTP = new NTPClient();
		showView = true;
		
						Thread thread = new Thread(){
							public void run(){
								String _TIME = cNTP.getTime(4);
								dbSQLite.add(dbSQLite.tableName_Beacon, _UUID, _INFO, _INFO2, _TIME);
							}
						};
						thread.start();
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		Log.d(TAG, "onBackPressed");
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		showView = false;

		if (dbSQLite != null)
			dbSQLite.close();

		finish();
	}

	@Override
	protected void onStart() {
		super.onStart();
		beaconUtill.onStart(iBeaconManager, this);
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
		Log.d(TAG, "onRestart");
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		Log.d(TAG, "onResume");
	}

	@Override
	protected void onStop() {
		beaconUtill.onStop(iBeaconManager, this);
		super.onStop();
	}

	@Override
	public void onIBeaconServiceConnect() {

		iBeaconManager.setRangeNotifier(new RangeNotifier() {
			@Override
			public void didRangeBeaconsInRegion(final Collection<IBeacon> iBeacons, Region region) {

				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						if (showView)
						{
							arrayL.clear();
							arrayL.addAll((ArrayList<IBeacon>) iBeacons);
							adapter.notifyDataSetChanged();
						}
					}
				});
			}

		});

		iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
			@Override
			public void didEnterRegion(Region region) {
				Log.e("BeaconDetactorService", "didEnterRegion");
				// logStatus("I just saw an iBeacon for the first time!");
			}

			@Override
			public void didExitRegion(Region region) {
				Log.e("BeaconDetactorService", "didExitRegion");
				// logStatus("I no longer see an iBeacon");
			}

			@Override
			public void didDetermineStateForRegion(int state, Region region) {
				Log.e("BeaconDetactorService", "didDetermineStateForRegion");
				// logStatus("I have just switched from seeing/not seeing iBeacons: " + state);
			}

		});

		try {
			iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		try {
			iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private class BeaconAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (arrayL != null && arrayL.size() > 0)
				return arrayL.size();
			else
				return 0;
		}

		@Override
		public IBeacon getItem(int arg0) {
			return arrayL.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@TargetApi(Build.VERSION_CODES.N)
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			try {
				ViewHolder holder;

				if (convertView != null) {
					holder = (ViewHolder) convertView.getTag();
				} else {
					holder = new ViewHolder(convertView = inflater.inflate(R.layout.tupple_monitoring, null));
				}
				if (arrayL.get(position).getProximityUuid() != null)
				{
					if (showView)
					{
						final String _UUID = arrayL.get(position).getProximityUuid();
						final int[] _INFO = new int[5];
						_INFO[0] = arrayL.get(position).getMajor();
						_INFO[1] = arrayL.get(position).getMinor();
						_INFO[2] = arrayL.get(position).getProximity();
						_INFO[3] = arrayL.get(position).getRssi();
						_INFO[4] = arrayL.get(position).getTxPower();
						final double _INFO2 = arrayL.get(position).getAccuracy();
						final String _TIME = null;

						Thread thread = new Thread(){
							public void run(){
								//String _TIME = cNTP.getTime(4);
								
								Calendar calendar = Calendar.getInstance();

								String _TIME = calendar.get(Calendar.YEAR) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + " - " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + ":" + calendar.get(Calendar.MILLISECOND);
								Log.d("TIME", _TIME);
								dbSQLite.add(dbSQLite.tableName_Beacon, _UUID, _INFO, _INFO2, _TIME);
							}
						};
						thread.start();

						holder.beacon_uuid.setText("UUID: " + arrayL.get(position).getProximityUuid());
						holder.beacon_major.setText("Major: " + arrayL.get(position).getMajor());
						holder.beacon_minor.setText(", Minor: " + arrayL.get(position).getMinor());
						holder.beacon_proximity.setText("Proximity: " + arrayL.get(position).getProximity());
						holder.beacon_rssi.setText(", Rssi: " + arrayL.get(position).getRssi());
						holder.beacon_txpower.setText(", TxPower: " + arrayL.get(position).getTxPower());
						holder.beacon_range.setText("" + arrayL.get(position).getAccuracy());

					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return convertView;
		}

		private class ViewHolder {
			private TextView beacon_uuid;
			private TextView beacon_major;
			private TextView beacon_minor;
			private TextView beacon_proximity;
			private TextView beacon_rssi;
			private TextView beacon_txpower;
			private TextView beacon_range;

			public ViewHolder(View view) {
				beacon_uuid = (TextView) view.findViewById(R.id.BEACON_uuid);
				beacon_major = (TextView) view.findViewById(R.id.BEACON_major);
				beacon_minor = (TextView) view.findViewById(R.id.BEACON_minor);
				beacon_proximity = (TextView) view.findViewById(R.id.BEACON_proximity);
				beacon_rssi = (TextView) view.findViewById(R.id.BEACON_rssi);
				beacon_txpower = (TextView) view.findViewById(R.id.BEACON_txpower);
				beacon_range = (TextView) view.findViewById(R.id.BEACON_range);

				view.setTag(this);
			}
		}

	}

}
