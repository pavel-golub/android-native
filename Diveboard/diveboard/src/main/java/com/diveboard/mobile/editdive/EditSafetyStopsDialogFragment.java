package com.diveboard.mobile.editdive;

import java.util.ArrayList;

import com.diveboard.mobile.ApplicationController;
import com.diveboard.mobile.R;
import com.diveboard.model.DiveboardModel;
import com.diveboard.model.SafetyStop;
import com.diveboard.model.Units;
import com.diveboard.util.DiveboardSpinnerAdapter;

import androidx.fragment.app.DialogFragment;
import android.text.InputType;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class					EditSafetyStopsDialogFragment extends DialogFragment
{
	public interface			EditSafetyStopsDialogListener
	{
        void					onSafetyStopsEditComplete(DialogFragment dialog);
    }
	
	private ArrayList<SafetyStop>		mSafetyStops;
	private DiveboardModel				mModel;
	private Typeface					mFaceR;
	private View						mView;
	EditSafetyStopsDialogListener		mListener;
	private EditText					mDepthField;
	private EditText					mDurationField;
	private Integer						mIndex;
	private Spinner						mDepthLabel;
	private int							mTextSize = 30;
	
	 @Override
	 public void onAttach(Activity activity)
	 {
		 super.onAttach(activity);
		 // Verify that the host activity implements the callback interface
		 try
		 {
			 // Instantiate the NoticeDialogListener so we can send events to the host
			 mListener = (EditSafetyStopsDialogListener) activity;
		 }
		 catch (ClassCastException e)
		 {
			 // The activity doesn't implement the interface, throw exception
			 throw new ClassCastException(activity.toString() + " must implement onSafetyStopsEditComplete");
		 }
	 }
	
	private void				addNoStops(LinearLayout layout)
	{
		final float scale = getResources().getDisplayMetrics().density;
		
		TextView text = new TextView(getActivity().getApplicationContext());
		text.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_body_background));
		text.setTypeface(mFaceR);
		text.setPadding(0, (int)(10 * scale + 0.5f), 0, (int)(10 * scale + 0.5f));
		text.setTextSize(mTextSize);
		text.setText(getResources().getString(R.string.no_safety_stops));
		text.setTextColor(getResources().getColor(R.color.dark_grey));
		text.setGravity(Gravity.CENTER);
		layout.addView(text);
	}
	
	private void				openSafetyStopsEdit(final Integer index)
	{
		if(index == -1)
			mIndex = mSafetyStops.size() - 1;
		else
			mIndex = index;
		LinearLayout safetylist = (LinearLayout) mView.findViewById(R.id.safetyfields);
		safetylist.removeAllViews();
		final float scale = getResources().getDisplayMetrics().density;
		SafetyStop safetyStop = mSafetyStops.get(mIndex);
		
		Button add_button = (Button) mView.findViewById(R.id.add_button);
		add_button.setVisibility(Button.GONE);
		
		LinearLayout depth = new LinearLayout(getActivity().getApplicationContext());
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		depth.setLayoutParams(params);
		depth.setOrientation(LinearLayout.HORIZONTAL);
		depth.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_body_background));
		
		TextView depth_title = new TextView(getActivity().getApplicationContext());
		depth_title.setTypeface(mFaceR);
		depth_title.setTextColor(getResources().getColor(R.color.dark_grey));
		depth_title.setPadding((int)(10 * scale + 0.5f), 0, 0, 0);
		depth_title.setTextSize(mTextSize);
		depth_title.setText(getResources().getString(R.string.depth_label) +" :");
		depth.addView(depth_title);
		
		mDepthField = new EditText(getActivity().getApplicationContext());
		mDepthField.setInputType(InputType.TYPE_CLASS_NUMBER );
		mDepthField.setTypeface(mFaceR);
		mDepthField.setTextColor(getResources().getColor(R.color.dark_grey));
		mDepthField.setTextSize(mTextSize);
		mDepthField.setText(safetyStop.getDepth().toString());
		depth.addView(mDepthField);
		
		mDepthLabel = new Spinner(getActivity().getApplicationContext());
		ArrayAdapter<String> adapter = new DiveboardSpinnerAdapter(getActivity().getApplicationContext(), R.layout.units_spinner);
		String safetystop_unit = mSafetyStops.get(mIndex).getUnit().toString();
		if (safetystop_unit == null)
		{
			if (Units.getDistanceUnit() == Units.Distance.KM)
			{
				adapter.add(getResources().getString(R.string.unit_m));
				adapter.add(getResources().getString(R.string.unit_ft));
			}
			else
			{
				adapter.add(getResources().getString(R.string.unit_ft));
				adapter.add(getResources().getString(R.string.unit_m));
			}
		}
		else
		{
			if (safetystop_unit.compareTo(getResources().getString(R.string.unit_m)) == 0)
			{
				adapter.add(getResources().getString(R.string.unit_m));
				adapter.add(getResources().getString(R.string.unit_ft));
			}
			else
			{
				adapter.add(getResources().getString(R.string.unit_ft));
				adapter.add(getResources().getString(R.string.unit_m));
			}
		}
		adapter.setDropDownViewResource(R.layout.units_spinner_fields);
		mDepthLabel.setAdapter(adapter);
		depth.addView(mDepthLabel);
		
		safetylist.addView(depth);
		
		LinearLayout duration = new LinearLayout(getActivity().getApplicationContext());
		params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		duration.setLayoutParams(params);
		duration.setOrientation(LinearLayout.HORIZONTAL);
		duration.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_body_background));
		
		TextView duration_title = new TextView(getActivity().getApplicationContext());
		duration_title.setTypeface(mFaceR);
		duration_title.setTextColor(getResources().getColor(R.color.dark_grey));
		duration_title.setTextSize(mTextSize);
		duration_title.setText(getResources().getString(R.string.duration_label) + " :");
		duration_title.setPadding((int)(10 * scale + 0.5f), 0, 0, 0);
		duration.addView(duration_title);
		
		mDurationField = new EditText(getActivity().getApplicationContext());
		mDurationField.setInputType(InputType.TYPE_CLASS_NUMBER );
		mDurationField.setTypeface(mFaceR);
		mDurationField.setTextColor(getResources().getColor(R.color.dark_grey));
		mDurationField.setTextSize(mTextSize);
		mDurationField.setText(safetyStop.getDuration().toString());
		duration.addView(mDurationField);
		
		TextView duration_label = new TextView(getActivity().getApplicationContext());
		duration_label.setTypeface(mFaceR);
		duration_label.setTextColor(getResources().getColor(R.color.dark_grey));
		duration_label.setTextSize(mTextSize);
		duration_label.setText(getResources().getString(R.string.unit_min));
		duration.addView(duration_label);
		
		safetylist.addView(duration);
		
        Button cancel = (Button) mView.findViewById(R.id.cancel);
        cancel.setTypeface(mFaceR);
        cancel.setText(getResources().getString(R.string.cancel));
        cancel.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().getApplicationContext().INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mDurationField.getWindowToken(), 0);
				mDepthField = null;
				mDepthField = null;
				mIndex = null;

				//it is a new safetyStop
				if(index == -1){
					mSafetyStops.remove(mSafetyStops.size() - 1);
				}				
				
				openSafetyStopsList();
			}
		});
        
        Button save = (Button) mView.findViewById(R.id.save);
        save.setTypeface(mFaceR);
        save.setText(getResources().getString(R.string.save));
        save.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().getApplicationContext().INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mDurationField.getWindowToken(), 0);
				if (mDepthField.getText().toString().isEmpty())
					mDepthField.setText("0");
				if (mDurationField.getText().toString().isEmpty())
					mDurationField.setText("0");
				mSafetyStops.set(mIndex, new SafetyStop(Integer.parseInt(mDepthField.getText().toString()), Integer.parseInt(mDurationField.getText().toString()), null));
				mIndex = null;
				openSafetyStopsList();
			}
		});
	}
	
	private void				deleteSafetyStop(int index)
	{
		mSafetyStops.remove(index);
		openSafetyStopsList();
	}
	
	private void				openSafetyStopsList()
	{
		LinearLayout safetylist = (LinearLayout) mView.findViewById(R.id.safetyfields);
		safetylist.removeAllViews();
		if (mSafetyStops.size() == 0)
			addNoStops(safetylist);
		else
		{
			for (int i = 0, length = mSafetyStops.size(); i < length; i++)
			{
				final float scale = getResources().getDisplayMetrics().density;
				
				LinearLayout safetyElem = new LinearLayout(getActivity().getApplicationContext());
				LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				safetyElem.setTag(i);
				safetyElem.setLayoutParams(params);
				safetyElem.setOrientation(LinearLayout.HORIZONTAL);
				safetyElem.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_body_background));
				safetyElem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						// Edit Safety Stops
						openSafetyStopsEdit((Integer) v.getTag());
					}
				});

				TextView text = new TextView(getActivity().getApplicationContext());
				text.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
				text.setTypeface(mFaceR);
				text.setPadding(0, (int)(10 * scale + 0.5f), 0, (int)(10 * scale + 0.5f));
				text.setTextSize(mTextSize);
//				text.setText(mSafetyStops.get(i).getDepth() + mSafetyStops.get(i).getUnit() + " - " + mSafetyStops.get(i).getDurationMinutes() + getResources().getString(R.string.unit_min));
				text.setTextColor(getResources().getColor(R.color.dark_grey));
				text.setGravity(Gravity.CENTER);
				
				safetyElem.addView(text);
				
				ImageView delete = new ImageView(getActivity().getApplicationContext());
				delete.setScaleType(ScaleType.FIT_CENTER);
				delete.setLayoutParams(new LinearLayout.LayoutParams((int)(50 * scale + 0.5f), LayoutParams.MATCH_PARENT));
				delete.setPadding(0, 0, (int)(5 * scale + 0.5f), 0);
				delete.setImageDrawable(getResources().getDrawable(R.drawable.ic_recycle_bin));
				delete.setTag(i);
				delete.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						// Delete Safety Stops
						deleteSafetyStop((Integer) v.getTag());
					}
				});
				
				safetyElem.addView(delete);
				
				safetylist.addView(safetyElem);
			}
		}
		
		Button add_button = (Button) mView.findViewById(R.id.add_button);
		add_button.setTypeface(mFaceR);
		add_button.setText(getResources().getString(R.string.add_safetystops_button));
		add_button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
//				openSafetyStopsNew();
				SafetyStop newStop;
				if (Units.getDistanceUnit() == Units.Distance.KM){
//					newStop = new SafetyStop(3, 3, "m");
				}else
//					newStop = new SafetyStop(3, 3, "ft");
//				mSafetyStops.add(newStop);
				openSafetyStopsEdit(-1);
			}
		});
		add_button.setVisibility(Button.VISIBLE);
        
        Button cancel = (Button) mView.findViewById(R.id.cancel);
        cancel.setTypeface(mFaceR);
        cancel.setText(getResources().getString(R.string.cancel));
        cancel.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				dismiss();
			}
		});
        
        Button save = (Button) mView.findViewById(R.id.save);
        save.setTypeface(mFaceR);
        save.setText(getResources().getString(R.string.save));
        save.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				mModel.getDives().get(getArguments().getInt("index")).setSafetyStops(mSafetyStops);
				mListener.onSafetyStopsEditComplete(EditSafetyStopsDialogFragment.this);
				dismiss();
			}
		});
	}
	
	@Override
	public View					onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		mFaceR = Typeface.createFromAsset(getActivity().getApplicationContext().getAssets(), "fonts/Lato-Light.ttf");
		mView = inflater.inflate(R.layout.dialog_edit_safetystops, container);
		mModel = ((ApplicationController) getActivity().getApplicationContext()).getModel();
		if (mModel.getDives().get(getArguments().getInt("index")).getSafetyStops() != null)
			mSafetyStops = (ArrayList<SafetyStop>) mModel.getDives().get(getArguments().getInt("index")).getSafetyStops().clone();
		else
			mSafetyStops = new ArrayList<SafetyStop>();
		
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		TextView title = (TextView) mView.findViewById(R.id.title);
		title.setTypeface(mFaceR);
		title.setText(getResources().getString(R.string.edit_safetystops_title));
		
		openSafetyStopsList();
		return mView;
	}
}
