package com.example.silentmodescheduler.presentation.screens.timeperiod;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.silentmodescheduler.SilentModeSchedulerApp;
import com.example.silentmodescheduler.databinding.DialogTimePeriodBinding;
import com.example.silentmodescheduler.di.AppContainer;
import com.example.silentmodescheduler.presentation.viewmodel.TimePeriodViewModel;
import java.util.Calendar;
import java.util.Locale;

public class TimePeriodDialogFragment extends DialogFragment {
    private static final String ARG_TIME_PERIOD_ID = "time_period_id";
    private DialogTimePeriodBinding binding;
    private TimePeriodViewModel viewModel;
    private OnDismissListener onDismissListener;

    public interface OnDismissListener {
        void onDismissed();
    }

    public static TimePeriodDialogFragment newInstance(@Nullable String timePeriodId) {
        TimePeriodDialogFragment fragment = new TimePeriodDialogFragment();
        Bundle args = new Bundle();
        if (timePeriodId != null) {
            args.putString(ARG_TIME_PERIOD_ID, timePeriodId);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.onDismissListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogTimePeriodBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String timePeriodId = null;
        if (getArguments() != null) {
            timePeriodId = getArguments().getString(ARG_TIME_PERIOD_ID);
        }

        SilentModeSchedulerApp app = (SilentModeSchedulerApp) requireActivity().getApplication();
        AppContainer appContainer = app.getAppContainer();

        viewModel = new ViewModelProvider(this, new TimePeriodViewModel.Factory(
                appContainer.getFirebaseAuthManager(),
                appContainer.getFirestoreRepository(),
                appContainer.getSilentModeScheduler(),
                timePeriodId
        )).get(TimePeriodViewModel.class);

        setupListeners();
        observeState();
    }

    private void setupListeners() {
        binding.etPeriodName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.onNameChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnPickStart.setOnClickListener(v -> showTimePicker(true));
        binding.btnPickEnd.setOnClickListener(v -> showTimePicker(false));

        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnSave.setOnClickListener(v -> viewModel.onSaveClick());
    }

    private void showTimePicker(boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minuteOfHour) -> {
                    String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    if (isStart) {
                        viewModel.onStartTimeChanged(timeStr);
                    } else {
                        viewModel.onEndTimeChanged(timeStr);
                    }
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void observeState() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            binding.tvDialogTitle.setText(state.isEditMode() ? "Edit Time Period" : "Add Time Period");
            binding.tilPeriodName.setError(state.getNameError());

            if (!state.getStartTime().isEmpty()) {
                binding.btnPickStart.setText(state.getStartTime());
            }
            if (!state.getEndTime().isEmpty()) {
                binding.btnPickEnd.setText(state.getEndTime());
            }

            if (state.getStartTimeError() != null) {
                Toast.makeText(getContext(), state.getStartTimeError(), Toast.LENGTH_SHORT).show();
            }
            if (state.getEndTimeError() != null) {
                Toast.makeText(getContext(), state.getEndTimeError(), Toast.LENGTH_SHORT).show();
            }

            binding.btnSave.setEnabled(!state.isLoading());

            if (state.getGeneralError() != null) {
                Toast.makeText(getContext(), state.getGeneralError(), Toast.LENGTH_LONG).show();
            }

            if (state.isSaveSuccess()) {
                if (onDismissListener != null) {
                    onDismissListener.onDismissed();
                }
                dismiss();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
