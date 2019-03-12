package com.diveboard.mobile;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.diveboard.mobile.databinding.DiveDetailsGeneralBinding;
import com.diveboard.model.SafetyStop;
import com.diveboard.model.Units;
import com.diveboard.util.BindingConvertions;
import com.diveboard.util.Callback;
import com.diveboard.util.DateConverter;
import com.diveboard.util.binding.recyclerViewBinder.adapter.ClickHandler;
import com.diveboard.util.binding.recyclerViewBinder.adapter.binder.ItemBinder;
import com.diveboard.util.binding.recyclerViewBinder.adapter.binder.ItemBinderBase;
import com.diveboard.viewModel.AddSafetyStopViewModel;
import com.diveboard.viewModel.DiveDetailsViewModel;

import java.text.SimpleDateFormat;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import br.com.ilhasoft.support.validation.Validator;

public class DiveDetailsGeneralFragment extends Fragment {

    DiveDetailsViewModel viewModel;
    private Validator validator;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dive_details_general, container, false);

        //TODO: get model from singleton tempDive so it is restored after app switch
        viewModel = DiveDetailsViewModel.createNewDive(55, "Mexico", Units.UnitsType.Metric);
        setupSafetyStops(view);
        setupTripName(view);
        return view;
    }

    private void setupSafetyStops(View view) {
        DiveDetailsGeneralBinding binding = DataBindingUtil.bind(view);
        binding.setModel(viewModel);
        binding.setView(this);
        binding.setConverter(new BindingConvertions());
        binding.setDateConverter(new DateConverter(getContext()));

        validator = new Validator(binding);
        validator.enableFormValidationMode();
    }

    private void setupTripName(View view) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line,
                ApplicationController.getInstance().getModel().getUser().getTripNames());
        AutoCompleteTextView textView = view.findViewById(R.id.trip_name);
        textView.setAdapter(adapter);
    }

    public String getTimeFormat() {
        return ((SimpleDateFormat) DateFormat.getTimeFormat(getContext())).toPattern();
    }

    public void showSafetyStopDialog() {
        AddSafetyStopDialog dialog = new AddSafetyStopDialog();
        dialog.setPositiveCallback(new Callback<AddSafetyStopViewModel>() {
            @Override
            public void execute(AddSafetyStopViewModel data) {
                viewModel.addSafetyStop(data.toModel());
            }
        });
        dialog.show(getFragmentManager(), "add_safety_stop");
    }

    public ItemBinder<SafetyStop> safetyStopsItemViewBinder() {
        return new ItemBinderBase<>(BR.model, R.layout.safety_stop_item);
    }

    public ClickHandler<SafetyStop> removeStopHandler() {
        return new ClickHandler<SafetyStop>() {
            @Override
            public void onClick(SafetyStop safetyStop) {
                viewModel.removeSafetyStop(safetyStop);
            }
        };
    }

    public void save() {
        if (validator.validate()) {

        }
    }
}
