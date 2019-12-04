package com.diveboard.mobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import com.diveboard.dataaccess.datamodel.DeleteResponse;
import com.diveboard.dataaccess.datamodel.Dive;
import com.diveboard.dataaccess.datamodel.DivesResponse;
import com.diveboard.model.DivesService;
import com.diveboard.util.ResourceHolder;
import com.diveboard.util.ResponseCallback;
import com.diveboard.util.TabAdapter;
import com.diveboard.viewModel.DiveDetailsViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.util.UUID;

import br.com.ilhasoft.support.validation.Validator;

public class DiveDetailsPage extends Fragment {
    private DiveDetailsViewModel viewModel;
    private DiveDetailsGeneralFragment general;
    private TabLayout tabLayout;
    private ApplicationController ac;
    private DiveDetailsPeopleFragment people;
    private DiveDetailsNotesFragment notes;
    private AlertDialog deleteConfirmationDialog;
    private DivesService divesService;
    private boolean isNewDive;
    private View view;
    private AlertDialog backConfirmationDialog;
    private ResourceHolder resourceHolder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                goBack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            return view;
        }
        view = inflater.inflate(R.layout.activity_dive_details2, container, false);
        ac = (ApplicationController) getActivity().getApplicationContext();
        resourceHolder = new ResourceHolder(ac);
        divesService = ac.getDivesService();
        //cannot use dive.id as it is not always exist e.g. for offline dives
        String shakenId = DiveDetailsPageArgs.fromBundle(getArguments()).getShakenId();
        isNewDive = shakenId == null;
        setupViewModel(shakenId);
        setupTabs(view);
        setupToolbar(view);
        return view;
    }

    private void setupViewModel(String shakenId) {
        ac.getDivesService().getDivesAsync(new ResponseCallback<DivesResponse, String>() {
            @Override
            public void success(DivesResponse data) {
                if (ac.currentDive != null) {
                    viewModel = ac.currentDive;
                } else {
                    if (isNewDive) {
                        viewModel = DiveDetailsViewModel.createNewDive(
                                data.getMaxDiveNumber() + 1,
                                data.getLastTripName(),
                                ac.getUserPreferenceService().getUnits(),
                                resourceHolder.getVisibilityValues(),
                                resourceHolder.getCurrentValues(),
                                ac.getCurrentUser().id,
                                resourceHolder.getMaterialsValues(),
                                resourceHolder.getGasMixValues(),
                                resourceHolder.getCylindersCountValues(),
                                data.getTripNames());
                    } else {
                        viewModel = DiveDetailsViewModel.createFromModel(data.getDive(shakenId),
                                resourceHolder.getVisibilityValues(),
                                resourceHolder.getCurrentValues(),
                                ac.getUserPreferenceService().getUnits(),
                                resourceHolder.getMaterialsValues(),
                                resourceHolder.getGasMixValues(),
                                resourceHolder.getCylindersCountValues(),
                                data.getTripNames());
                    }
                }
                setViewModel(viewModel);
            }

            @Override
            public void error(String s) {
                Toast.makeText(ac, s, Toast.LENGTH_SHORT).show();
            }
        }, false);
    }

    private void setupTabs(View view) {
        ViewPager viewPager = view.findViewById(R.id.pager);
        tabLayout = view.findViewById(R.id.tabLayout);
        TabAdapter adapter = new TabAdapter(getChildFragmentManager(), getActivity().getApplicationContext());
        general = new DiveDetailsGeneralFragment();
        adapter.addFragment(general, R.string.tab_details_label);
        people = new DiveDetailsPeopleFragment();
        adapter.addFragment(people, R.string.tab_people_label);
        notes = new DiveDetailsNotesFragment();
        adapter.addFragment(notes, R.string.tab_notes_label);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setViewModel(DiveDetailsViewModel viewModel) {
        ac.currentDive = viewModel;
        general.setViewModel(viewModel);
        people.setViewModel(viewModel);
        notes.setViewModel(viewModel);
    }

    private void setupToolbar(View view) {
        Toolbar actionBar = view.findViewById(R.id.toolbar);
        actionBar.inflateMenu(R.menu.dive_details);
        actionBar.setNavigationOnClickListener(v -> goBack());
        actionBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.save:
                    save();
                    break;
                case R.id.clone:
                    cloneDive();
                    break;
                case R.id.delete:
                    getDeleteConfirmationDialog().show();
                    break;
            }
            return true;
        });
    }

    private void cloneDive() {
        if (viewModel.isModified()) {
            getBackNavigationConfirmationDialog(false).show();
            return;
        }
        doClone();
    }

    public void rotateAnimation(View view) {
        final ObjectAnimator oa1 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f);
        final ObjectAnimator oa2 = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f);
        oa1.setInterpolator(new DecelerateInterpolator());
        oa2.setInterpolator(new AccelerateDecelerateInterpolator());
        oa1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                oa2.start();
            }
        });
        oa1.start();
    }

    private void doClone() {
        ac.getDivesService().getDivesAsync(new ResponseCallback<DivesResponse, String>() {
            @Override
            public void success(DivesResponse data) {
                general.scrollToTop();
                TabLayout.Tab tab = tabLayout.getTabAt(0);
                tab.select();
                rotateAnimation(view);
                isNewDive = true;
                Gson gson = new Gson();
                Dive clonedDive = gson.fromJson(gson.toJson(viewModel.getModel()), Dive.class);
                clonedDive.diveNumber = data.getMaxDiveNumber() + 1;
                clonedDive.shakenId = UUID.randomUUID().toString();

                viewModel = DiveDetailsViewModel.createFromModel(clonedDive,
                        resourceHolder.getVisibilityValues(),
                        resourceHolder.getCurrentValues(),
                        ac.getUserPreferenceService().getUnits(),
                        resourceHolder.getMaterialsValues(),
                        resourceHolder.getGasMixValues(),
                        resourceHolder.getCylindersCountValues(),
                        data.getTripNames());

                setViewModel(viewModel);
            }

            @Override
            public void error(String s) {
                Toast.makeText(ac, s, Toast.LENGTH_SHORT).show();
            }
        }, false);
    }

    private void goBack() {
        hideKeyBoard(view);
        if (viewModel.isModified()) {
            getBackNavigationConfirmationDialog(true).show();
            return;
        }
        ac.currentDive = null;
        Navigation.findNavController(view).popBackStack();
    }

    private void delete() {
        divesService.deleteDiveAsync(viewModel.getModel(), new ResponseCallback<DeleteResponse, Exception>() {
            @Override
            public void success(DeleteResponse data) {
                Navigation.findNavController(tabLayout).popBackStack();
            }

            @Override
            public void error(Exception e) {
                Toast.makeText(ac, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private AlertDialog getDeleteConfirmationDialog() {
        if (deleteConfirmationDialog != null) {
            return deleteConfirmationDialog;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.deleteDive);
        builder.setPositiveButton(R.string.delete, (dialog, id) -> {
            delete();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, id) -> {

        });
        deleteConfirmationDialog = builder.create();
        return deleteConfirmationDialog;
    }

    private AlertDialog getBackNavigationConfirmationDialog(boolean goBack) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.unsavedChanges);
        builder.setPositiveButton(R.string.yes, (dialog, id) -> {
            if (goBack) {
                ac.currentDive = null;
                Navigation.findNavController(view).popBackStack();
            } else {
                doClone();
            }
        });
        builder.setNegativeButton(R.string.no, (dialog, id) -> {

        });
        return builder.create();
    }

    public void save() {
        //TODO: there should be a better solution for this
        this.getView().requestFocus();
        hideKeyBoard(getView());

        Validator generalValidator = general.getValidator();
        if (!generalValidator.validate()) {
            TabLayout.Tab tab = tabLayout.getTabAt(0);
            tab.select();
            return;
        }
        Toast.makeText(ac, R.string.saving, Toast.LENGTH_LONG).show();
        ac.getDivesService().saveDiveAsync(viewModel.getModel(), new ResponseCallback<Dive, Exception>() {
            @Override
            public void success(Dive data) {
                Navigation.findNavController(tabLayout).popBackStack();
            }

            @Override
            public void error(Exception e) {
                Toast toast = Toast.makeText(ac, e.getMessage(), Toast.LENGTH_SHORT);
                toast.show();
            }
        }, isNewDive);
    }

    public void hideKeyBoard(View v) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = getActivity().getCurrentFocus();
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }
}