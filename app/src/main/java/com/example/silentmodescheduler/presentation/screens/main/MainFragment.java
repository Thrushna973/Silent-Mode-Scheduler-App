package com.example.silentmodescheduler.presentation.screens.main;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.silentmodescheduler.MainActivity;
import com.example.silentmodescheduler.SilentModeSchedulerApp;
import com.example.silentmodescheduler.databinding.FragmentMainBinding;
import com.example.silentmodescheduler.di.AppContainer;
import com.example.silentmodescheduler.presentation.screens.timeperiod.TimePeriodDialogFragment;
import com.example.silentmodescheduler.presentation.screens.userselection.UserSelectionFragment;
import com.example.silentmodescheduler.presentation.viewmodel.MainEvent;
import com.example.silentmodescheduler.presentation.viewmodel.MainViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainFragment extends Fragment {
    private FragmentMainBinding binding;
    private MainViewModel viewModel;
    private TimePeriodAdapter adapter;
    private boolean hasCheckedDndOnLoad = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SilentModeSchedulerApp app = (SilentModeSchedulerApp) requireActivity().getApplication();
        AppContainer appContainer = app.getAppContainer();

        viewModel = new ViewModelProvider(this, new MainViewModel.Factory(
                appContainer.getFirebaseAuthManager(),
                appContainer.getFirestoreRepository(),
                appContainer.getSilentModeScheduler()
        )).get(MainViewModel.class);

        setupRecyclerView();
        setupListeners();
        observeState();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkDndPermissionState();
    }

    private void setupRecyclerView() {
        adapter = new TimePeriodAdapter(new TimePeriodAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(com.example.silentmodescheduler.data.model.TimePeriod item) {
                showTimePeriodDialog(item.getId());
            }

            @Override
            public void onDeleteClick(com.example.silentmodescheduler.data.model.TimePeriod item) {
                showDeleteConfirmationDialog(item.getId());
            }
        });
        binding.rvSchedules.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        binding.btnGrantDnd.setOnClickListener(v -> showDndExplanationDialog());

        binding.fabAddSchedule.setOnClickListener(v -> showTimePeriodDialog(null));
    }

    private void checkDndPermissionState() {
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasPermission = notificationManager.isNotificationPolicyAccessGranted();
            binding.cardDndWarning.setVisibility(hasPermission ? View.GONE : View.VISIBLE);
            
            if (!hasPermission && !hasCheckedDndOnLoad) {
                hasCheckedDndOnLoad = true;
                showDndExplanationDialog();
            }
        } else {
            binding.cardDndWarning.setVisibility(View.GONE);
        }
    }

    private void showDndExplanationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("DND Access Required")
                .setMessage("This application requires Do Not Disturb (Notification Policy Access) permission to automatically toggle your phone's Silent Mode. We will redirect you to system settings to grant it.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout? This will cancel all your active silent mode schedules.")
                .setPositiveButton("Logout", (dialog, which) -> viewModel.logout())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showDeleteConfirmationDialog(String periodId) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Schedule")
                .setMessage("Are you sure you want to delete this schedule? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteTimePeriod(periodId))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showTimePeriodDialog(@Nullable String periodId) {
        TimePeriodDialogFragment dialogFragment = TimePeriodDialogFragment.newInstance(periodId);
        dialogFragment.setOnDismissListener(() -> viewModel.loadTimePeriods());
        dialogFragment.show(getChildFragmentManager(), "time_period_dialog");
    }

    private void observeState() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            binding.tvGreeting.setText("Hello, " + state.getUserName() + "!");
            binding.progressBar.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
            binding.tvEmpty.setVisibility(!state.isLoading() && state.getTimePeriods().isEmpty() ? View.VISIBLE : View.GONE);
            
            adapter.updateItems(state.getTimePeriods());

            if (state.getError() != null) {
                Toast.makeText(getContext(), state.getError(), Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getEvents().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof MainEvent.LoggedOut) {
                viewModel.clearEvent();
                com.example.silentmodescheduler.core.scheduler.SilentModeReceiver.disableSilentMode(requireContext(), "Logout");
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    mainActivity.clearBackstackAndNavigate(new UserSelectionFragment());
                }
            } else if (event instanceof MainEvent.ShowSnackbar) {
                viewModel.clearEvent();
                Toast.makeText(getContext(), ((MainEvent.ShowSnackbar) event).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
