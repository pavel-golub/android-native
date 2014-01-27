package com.diveboard.mobile.newdive;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.diveboard.config.AppConfig;
import com.diveboard.mobile.ApplicationController;
import com.diveboard.mobile.DiveDetailsActivity;
import com.diveboard.mobile.GalleryCarouselActivity;
import com.diveboard.mobile.R;
import com.diveboard.mobile.WaitDialogFragment;
import com.diveboard.mobile.editdive.EditConfirmDialogFragment;
import com.diveboard.mobile.editdive.TabEditSpotsActivity;
import com.diveboard.mobile.editdive.EditConfirmDialogFragment.EditConfirmDialogListener;
import com.diveboard.mobile.editdive.TabEditSpotsActivity.SpotAdapter;
import com.diveboard.model.Dive;
import com.diveboard.model.DiveCreateListener;
import com.diveboard.model.DiveboardModel;
import com.diveboard.model.Picture;
import com.diveboard.model.Spot;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Pair;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.TextView.OnEditorActionListener;

public class					TabNewSpotsActivity extends FragmentActivity implements EditConfirmDialogListener
{
	private Typeface					mFaceR;
	private Typeface					mFaceB;
	private Dive						mDive;
	private int							mIndex;
	private SpotsTask 					mSpotsTask;
	private Spot						mSpot;
	private JSONObject					mSelectedObject = null;
	private JSONArray					mArray;
	private Boolean						mHasChanged = false;
	private boolean						mError = false;
	private GoogleMap					mMap;
	private List<Marker>				mListMarkers = new ArrayList<Marker>();
	private Marker						mMyMarker;
	LocationManager 					mLocationManager;
	myLocationListener 					mLocationListener;
	Double 								mLatitude = 0.0;
	Double 								mLongitude = 0.0;
	public final int					mZoom = 12;
	
	private class myLocationListener implements LocationListener
	{
		public void onLocationChanged(Location location)
		{
			ApplicationController AC = (ApplicationController)getApplicationContext();
			mLongitude = location.getLongitude();
			mLatitude = location.getLatitude();
		}

		@Override
		public void onProviderDisabled(String provider) {			
		}

		@Override
		public void onProviderEnabled(String provider) {			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {			
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		ApplicationController AC = (ApplicationController)getApplicationContext();
		AC.handleLowMemory();
	}
	
	@Override
	public void onBackPressed()
	{
		if (mDive.getEditList().size() > 0)
		{
			EditConfirmDialogFragment dialog = new EditConfirmDialogFragment();
	    	Bundle args = new Bundle();
	    	args.putInt("index", mIndex);
	    	dialog.setArguments(args);
	    	dialog.show(getSupportFragmentManager(), "EditConfirmDialogFragment");
		}
		else
		{
			clearEditList();
		}
	};
	
	public void clearEditList()
	{
		Bundle bundle = new Bundle();
		
		// put
		Intent intent = new Intent();
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		super.onBackPressed();
		mDive = null;
		((ApplicationController)getApplicationContext()).setTempDive(null);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ApplicationController AC = (ApplicationController)getApplicationContext();
        if (AC.handleLowMemory() == true)
			return ;
	    setContentView(R.layout.tab_edit_spots);
	    mFaceR = Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Regular.otf");
	    mFaceB = Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.otf");
	    mDive = ((ApplicationController)getApplicationContext()).getTempDive();
		mIndex = getIntent().getIntExtra("index", 0);
		
		TextView title = (TextView) findViewById(R.id.title);
	    title.setTypeface(mFaceB);
	    title.setText(getResources().getString(R.string.tab_spots_title));
//	    ((TextView)findViewById(R.id.current_spot_title)).setTypeface(mFaceB);
//	    ((TextView)findViewById(R.id.current_spot)).setTypeface(mFaceR);
	    ((TextView)findViewById(R.id.no_spot)).setTypeface(mFaceR);
	    if (mDive.getSpot().getId() != 1)
	     {
	    	 //((TextView)findViewById(R.id.current_spot)).setText(mModel.getDives().get(mIndex).getSpot().getName());
	    	 ((EditText)findViewById(R.id.search_bar)).setText(mDive.getSpot().getName());
	     }
//	     if (mDive.getSpot().getId() != 1)
//	    	 ((TextView)findViewById(R.id.current_spot)).setText(mDive.getSpot().getName());
	    ((TextView)findViewById(R.id.search_bar)).setTypeface(mFaceR);
	    //((Button)findViewById(R.id.ok_search)).setTypeface(mFaceR);
	    Button save = (Button) findViewById(R.id.save_button);
	    save.setTypeface(mFaceB);
	    save.setText(getResources().getString(R.string.save_button));
	    save.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
//				mModel.getDives().get(mIndex).setNotes(mNotes.getText().toString());
//				mModel.getDataManager().save(mModel.getDives().get(mIndex));
				//System.out.println("current spot = " + ((TextView)findViewById(R.id.current_spot)).getText().toString());
				
				ArrayList<Dive> dives = ((ApplicationController)getApplicationContext()).getModel().getDives();
				ArrayList<Pair<String, String>> editList = mDive.getEditList();
				if (mDive.getMaxdepth() == null || mDive.getDuration() == null)
					mError = true;
				else
					mError = false;
				if (mError == false)
				{
					WaitDialogFragment dialog = new WaitDialogFragment();
					dialog.show(getSupportFragmentManager(), "WaitDialogFragment");
					if (editList != null && editList.size() > 0)
					{
						JSONObject edit = new JSONObject(); 
						for (int i = 0, size = editList.size(); i < size; i++)
							try {
								if (editList.get(i).first.equals("spot"))
									edit.put(editList.get(i).first, new JSONObject(editList.get(i).second));
								else
									edit.put(editList.get(i).first, editList.get(i).second);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						try {
							mDive.applyEdit(edit);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						mDive.clearEditList();
					}
					dives.add(0, mDive);
					((ApplicationController)getApplicationContext()).getModel().getDataManager().setOnDiveCreateComplete(new DiveCreateListener() {
						@Override
						public void onDiveCreateComplete() {
							finish();
						}
					});
					((ApplicationController)getApplicationContext()).getModel().getDataManager().save(mDive);
					((ApplicationController)getApplicationContext()).setRefresh(1);
					((ApplicationController)getApplicationContext()).setTempDive(null);
				}
				else
				{
					Toast toast = Toast.makeText(getApplicationContext(), "Max Depth or Duration fields are missing", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			}
		});
//	    ImageView remove = (ImageView) findViewById(R.id.remove_button);
//	    remove.setOnClickListener(new OnClickListener()
//        {
//
//			@Override
//			public void onClick(View v) {
//				((TextView)findViewById(R.id.current_spot)).setText("");
//				mSelectedObject = null;
//				mHasChanged = true;
//				v.setVisibility(View.GONE);
//				JSONObject jobject = new JSONObject();
//				try {
//					jobject.put("id", 1);
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				mDive.setSpot(jobject);
//			}
//		});
//	    if (mDive.getSpot().getId() == 1)
//	    {
//	    	remove.setVisibility(View.GONE);
//	    }
	    ((EditText)findViewById(R.id.search_bar)).setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.requestFocusFromTouch();
				return false;
			}
		});
	    ((EditText)findViewById(R.id.search_bar)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ListView lv = ((ListView)findViewById(R.id.list_view));
				SpotAdapter adapter = new SpotAdapter(TabNewSpotsActivity.this, new ArrayList<Spot>());
				lv.setAdapter(adapter);
				//mMyMarker.remove();
				ApplicationController AC = (ApplicationController)getApplicationContext();
				Integer zoom = mDive.getSpot().getZoom();
				if (zoom == null)
					zoom = mZoom;
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), zoom));
				mMyMarker = mMap.addMarker(new MarkerOptions()
				.position(new LatLng(0, 0))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
				//((TextView)findViewById(R.id.current_spot)).setText("");
				((EditText)findViewById(R.id.search_bar)).setText("");
				mSelectedObject = null;
				mHasChanged = true;
				//v.setVisibility(View.GONE);
				JSONObject jobject = new JSONObject();
				try {
					jobject.put("id", 1);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mDive.setSpot(jobject);
			}
		});
	    
	    EditText editText = (EditText) findViewById(R.id.search_bar);
	    editText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				boolean handled = false;
	            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
	                doMySearch();
	                handled = true;
	            }
	            return handled;
			}
	    });
	    
	    
	    if (mMap == null) {
			FragmentManager fm = getSupportFragmentManager();
	        Fragment fragment = fm.findFragmentById(R.id.mapfragment);
	        SupportMapFragment support = (SupportMapFragment)fragment;
	        mMap = support.getMap();
	        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
	        // Check if we were successful in obtaining the map.
	        if (mMap != null) {
	        	mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	            // The Map is verified. It is now safe to manipulate the map
	        }
	        else
	        	System.out.println("Map non safe");
	        mMap.getUiSettings().setAllGesturesEnabled(true);
			mMap.getUiSettings().setMyLocationButtonEnabled(true);
			mMap.getUiSettings().setZoomControlsEnabled(true);
			mMap.getUiSettings().setZoomGesturesEnabled(true);
			
			mMap.getUiSettings().setRotateGesturesEnabled(true);
			mMap.getUiSettings().setScrollGesturesEnabled(true);
			mMap.getUiSettings().setCompassEnabled(true);
			if (mDive.getSpot().getId() != 1)
			{
				((LinearLayout)findViewById(R.id.view_details)).setVisibility(View.VISIBLE);
				((TextView)findViewById(R.id.details_name)).setTypeface(mFaceB);
				((TextView)findViewById(R.id.details_name_content)).setTypeface(mFaceR);
				((TextView)findViewById(R.id.details_gps)).setTypeface(mFaceB);
				((TextView)findViewById(R.id.details_gps_content)).setTypeface(mFaceR);
				((TextView)findViewById(R.id.details_name_content)).setText(mDive.getSpot().getName());
				((TextView)findViewById(R.id.details_gps_content)).setText(getPosition());
				((Button)findViewById(R.id.goToSearch)).setTypeface(mFaceB);
				
				System.out.println(mDive.getSpot().getId());
				mMyMarker = mMap.addMarker(new MarkerOptions()
				.position(new LatLng(mDive.getSpot().getLat(), mDive.getSpot().getLng()))
				.title(mDive.getSpot().getName())
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
				System.out.println(mDive.getSpot().getId());
				Integer zoom = mDive.getSpot().getZoom();
				if (zoom == null || zoom > mZoom)
					zoom = mZoom;
				if (mDive.getSpot().getLat() != null && mDive.getSpot().getLng() != null)
					mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mDive.getSpot().getLat(), mDive.getSpot().getLng()), zoom));
			}
			else
			{
				mMyMarker = mMap.addMarker(new MarkerOptions()
				.position(new LatLng(0, 0))
				.title(mDive.getSpot().getName())
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
				((LinearLayout)findViewById(R.id.view_search)).setVisibility(View.VISIBLE);
			}
			
		}
    }
    
    public String getPosition()
	{
    	ApplicationController AC = (ApplicationController)getApplicationContext();
		String pos = "";
		if (mDive.getSpot().getLat() == null)
		{
			pos += "0° ";
			pos += "N";
		}
		else if (mDive.getSpot().getLat() >= 0)
		{
			pos += String.valueOf(mDive.getSpot().getLat()) + "° ";
			pos += "N";
		}
		else if (mDive.getSpot().getLat() < 0)
		{
			pos += String.valueOf(mDive.getSpot().getLat() * (-1)) + "° ";
			pos += "S";
		}
		pos += ", ";
		if (mDive.getSpot().getLng() == null)
		{
			pos += "0° ";
			pos += "E";
		}
		else if (mDive.getSpot().getLng() >= 0)
		{
			pos += String.valueOf(mDive.getSpot().getLng()) + "° ";
			pos += "E";
		}
		else if (mDive.getSpot().getLng() < 0)
		{
			pos += String.valueOf(mDive.getSpot().getLng() * (-1)) + "° ";
			pos += "W";
		}
		if ((mDive.getSpot().getLat() == null ||mDive.getSpot().getLat() == 0) && 
				(mDive.getSpot().getLng() == null || mDive.getSpot().getLng() == 0))
			pos = "";
		return (pos);
	}
    
    public void goToSearch(View view)
    {
    	((LinearLayout)findViewById(R.id.view_details)).setVisibility(View.GONE);
    	((LinearLayout)findViewById(R.id.view_search)).setVisibility(View.VISIBLE);
    }
    
    
    public void activeGPS(View view)
    {
    	//removeMarkers();
    	mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		// Define a listener that responds to location updates
		mLocationListener = new myLocationListener();
		// Register the listener with the Location Manager to receive location updates
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
		//mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
		Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastKnownLocation == null)
			lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (lastKnownLocation != null)
		{
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), mZoom));
			mLongitude = lastKnownLocation.getLongitude();
			mLatitude = lastKnownLocation.getLatitude();
//			mMyMarker = mMap.addMarker(new MarkerOptions()
//			.position(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
//			.title("My location")
//			.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
		}
    }
//    public void setCurrentSpot(View view)
//    {  	
//    	((TextView)findViewById(R.id.current_spot)).setText(((TextView)view.findViewById(R.id.name)).getText().toString());
//    	ListView lv = ((ListView)findViewById(R.id.list_view));
//    	List<Spot> listSpots = new ArrayList<Spot>();
//    	SpotAdapter adapter = new SpotAdapter(TabEditSpotsActivity.this, listSpots);
//    	lv.setAdapter(adapter);
////    	SpotAdapter adapter = ((SpotAdapter)lv.getAdapter());
////    	for (int i = 0; i < adapter.getCount(); i++)
////    	{
////    		adapter.
////    	}
//    }
    
    public void doMySearch()
    {  	
    	ListView lv = ((ListView)findViewById(R.id.list_view));
    	List<Spot> listSpots = new ArrayList<Spot>();
    	SpotAdapter adapter = new SpotAdapter(TabNewSpotsActivity.this, listSpots);
    	lv.setAdapter(adapter);
    	((TextView)findViewById(R.id.no_spot)).setVisibility(View.GONE);
    	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, 0);
    	SpotsTask spots_task = new SpotsTask();
	    spots_task.execute(((TextView)findViewById(R.id.search_bar)).getText().toString());
	    removeMarkers();
	    //((TextView)findViewById(R.id.search_bar)).setText("");
    }
    
    public void removeMarkers()
    {
    	for (int i = 0; i < mListMarkers.size(); i++)
    	{
    		mListMarkers.get(i).remove();
    	}
    	mListMarkers.clear();
    }
    
    private class SpotsTask extends AsyncTask<String, Void, JSONObject>
	{
    	private JSONObject	result;
    	private boolean		searchDone = false;
    	
    	private class SearchTimer extends Thread
    	{
    		private String query;
    		
    		public SearchTimer(String query)
    		{
    			this.query = query;
    		}
    		
    		@Override
    		public void run()
    		{
    			ApplicationController AC = (ApplicationController)getApplicationContext();
    			result = AC.getModel().searchSpotText(query, null, null, null, null, null, null);
    			searchDone = true;
    		}
    	}
    	
		protected JSONObject doInBackground(String... query)
		{
			ApplicationController AC = (ApplicationController)getApplicationContext();
			SearchTimer task = new SearchTimer(query[0]);
			task.start();
			try {
				task.join(DiveboardModel._searchTimeout);
				if (searchDone == false)
				{
					DiveboardModel._searchtimedout = true;
					return AC.getModel().offlineSearchSpotText(query[0], null, null, null, null, null, null);
				}
				return result;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
//			ApplicationController AC = (ApplicationController)getApplicationContext();
//			
//			return AC.getModel().searchSpotText(query[0], null, null, null, null, null, null);
		}
		
		protected void onPostExecute(JSONObject result)
		{
			if (DiveboardModel._searchtimedout == true)
			{
				if (AppConfig.DEBUG_MODE == 1)
				{
					Toast toast = Toast.makeText(getApplicationContext(), "Spot Search Timeout", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				DiveboardModel._searchtimedout = false;
			}
			if (DiveboardModel._cotimedout == true)
			{
				if (AppConfig.DEBUG_MODE == 1)
				{
					Toast toast = Toast.makeText(getApplicationContext(), "Connection Timeout", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				DiveboardModel._cotimedout = false;
			}
			else if (DiveboardModel._sotimedout == true)
			{
				if (AppConfig.DEBUG_MODE == 1)
				{
					Toast toast = Toast.makeText(getApplicationContext(), "Socket Timeout", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				DiveboardModel._sotimedout = false;
			}
			
			try {
				if (result != null && result.getBoolean("success") == true)
				{
					try {
						mArray = result.getJSONArray("spots");
						final ListView lv = ((ListView)findViewById(R.id.list_view));
						List<Spot> listSpots = new ArrayList<Spot>();
						for (int i = 0; i < mArray.length(); i++)
						{
							Spot spot = new Spot(mArray.getJSONObject(i));
							listSpots.add(spot);
						}
						if (listSpots.size() == 0)
						{
							((TextView)findViewById(R.id.no_spot)).setVisibility(View.VISIBLE);
						}
						else
						{
							for (int i = 0; i < listSpots.size(); i++)
							{
								Marker marker = mMap.addMarker(new MarkerOptions()
								.position(new LatLng(listSpots.get(i).getLat(), listSpots.get(i).getLng()))
								.title(i + ": " + listSpots.get(i).getName())
								.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
								mListMarkers.add(marker);
							}
							mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
								
								@Override
								public boolean onMarkerClick(Marker marker) {
									System.out.println(Integer.valueOf(marker.getId().replace("m", "")) - 1);
									lv.smoothScrollToPosition(Integer.valueOf(marker.getId().replace("m", "")) - 1);
									return false;
								}
							});
							System.out.println(result.toString());
							SpotAdapter adapter = new SpotAdapter(TabNewSpotsActivity.this, listSpots);
							//Zoom out to show markers
							LatLngBounds.Builder builder = new LatLngBounds.Builder();
							for (Marker marker : mListMarkers) {
							    builder.include(marker.getPosition());
							}
							LatLngBounds bounds = builder.build();
							int padding = 100; // offset from edges of the map in pixels
							CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
							mMap.animateCamera(cu, new CancelableCallback(){

								@Override
								public void onCancel() {
									// TODO Auto-generated method stub
									
								}

								@Override
								public void onFinish() {
									if (mMap.getCameraPosition().zoom > mZoom)
										mMap.animateCamera(CameraUpdateFactory.zoomTo(mZoom));
								}
								
							});
							lv.setAdapter(adapter);
							lv.setOnItemClickListener(new OnItemClickListener()
							{
								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {
									removeMarkers();
									
									((Button)findViewById(R.id.goToSearch)).setTypeface(mFaceB);
									((LinearLayout)findViewById(R.id.view_details)).setVisibility(View.VISIBLE);
							    	((LinearLayout)findViewById(R.id.view_search)).setVisibility(View.GONE);
									((EditText)findViewById(R.id.search_bar)).setText(((TextView)view.findViewById(R.id.name)).getText().toString());
									//((TextView)findViewById(R.id.current_spot)).setText(((TextView)view.findViewById(R.id.name)).getText().toString());
							    	ListView lv = ((ListView)findViewById(R.id.list_view));
							    	List<Spot> listSpots = new ArrayList<Spot>();
							    	SpotAdapter adapter = new SpotAdapter(TabNewSpotsActivity.this, listSpots);
							    	lv.setAdapter(adapter);
//							    	ImageView remove = (ImageView) findViewById(R.id.remove_button);
//							    	remove.setVisibility(View.VISIBLE);
							    	mHasChanged = true;
							    	try {
										mSelectedObject = mArray.getJSONObject(position);
										mDive.setSpot(mSelectedObject);
										System.out.println(mDive.getSpot().toString());
										mMyMarker.remove();
										Integer zoom = mDive.getSpot().getZoom();
										if (zoom == null || zoom > mZoom)
											zoom = mZoom;
										Spot spot = mDive.getSpot();
										mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(spot.getLat(), spot.getLng()), zoom));
										mMyMarker = mMap.addMarker(new MarkerOptions()
										.position(new LatLng(spot.getLat(), spot.getLng()))
										.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
										
										((TextView)findViewById(R.id.details_name)).setTypeface(mFaceB);
										((TextView)findViewById(R.id.details_name_content)).setTypeface(mFaceR);
										((TextView)findViewById(R.id.details_gps)).setTypeface(mFaceB);
										((TextView)findViewById(R.id.details_gps_content)).setTypeface(mFaceR);
										((TextView)findViewById(R.id.details_name_content)).setText(mDive.getSpot().getName());
										((TextView)findViewById(R.id.details_gps_content)).setText(getPosition());
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							});
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					((TextView)findViewById(R.id.no_spot)).setVisibility(View.VISIBLE);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
		protected void onCancelled() {
			mSpotsTask = null;
		}
	}
    
    public class SpotAdapter extends BaseAdapter
    {
    	LayoutInflater	mLayoutInflater;
    	List<Spot>		mSpotsList;
    	
    	public SpotAdapter(Context context, List<Spot> spotsList)
    	{
    		mLayoutInflater = LayoutInflater.from(context);
    		this.mSpotsList = spotsList;
    	}
    	
		@Override
		public int getCount() {
			return mSpotsList.size();
		}

		@Override
		public Object getItem(int position) {
			return mSpotsList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			
			if(convertView == null) {
				holder = new ViewHolder();
				convertView = mLayoutInflater.inflate(R.layout.item_spots, null);

				//holder.id = (TextView)convertView.findViewById(R.id.id);
				holder.name = (TextView)convertView.findViewById(R.id.name);
				holder.location_country = (TextView)convertView.findViewById(R.id.location_country);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			//holder.id.setText(Integer.toString(mSpotsList.get(position).getId()));
			holder.name.setText(position + ": " + mSpotsList.get(position).getName());
			holder.name.setTypeface(mFaceR);
			holder.location_country.setText(mSpotsList.get(position).getLocationName() + ", " + mSpotsList.get(position).getCountryName());
			holder.location_country.setTypeface(mFaceR);
			return convertView;
		}
		
		private class ViewHolder {
			TextView id;
			TextView name;
			TextView location_country;
		}
    	
    }
    
    @Override
	public void onConfirmEditComplete(DialogFragment dialog) {
		clearEditList();
	}
}
