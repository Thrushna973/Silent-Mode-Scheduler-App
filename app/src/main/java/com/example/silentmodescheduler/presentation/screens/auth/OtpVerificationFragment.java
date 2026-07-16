package com.example.silentmodescheduler.presentation.screens.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.silentmodescheduler.databinding.FragmentOtpVerificationBinding;
import com.example.silentmodescheduler.di.AppContainer;
import com.example.silentmodescheduler.presentation.screens.main.MainFragment;
import com.example.silentmodescheduler.presentation.viewmodel.OtpVerificationEvent;
import com.example.silentmodescheduler.presentation.viewmodel.OtpVerificationViewModel;

public class OtpVerificationFragment extends Fragment {
    private static final String ARG_PHONE_NUMBER = "phone_number";
    private static final String ARG_VERIFICATION_ID = "verification_id";
    private static final String ARG_REGISTRATION_NAME = "registration_name";

    private FragmentOtpVerificationBinding binding;
    private OtpVerificationViewModel viewModel;

    public static OtpVerificationFragment newInstance(String phoneNumber, String verificationId, String name) {
        OtpVerificationFragment fragment = new OtpVerificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHONE_NUMBER, phoneNumber);
        args.putString(ARG_VERIFICATION_ID, verificationId);
        args.putString(ARG_REGISTRATION_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOtpVerificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) return;

        String phoneNumber = args.getString(ARG_PHONE_NUMBER, "");
        String verificationId = args.getString(ARG_VERIFICATION_ID, "");
        String registrationName = args.getString(ARG_REGISTRATION_NAME, "");

        SilentModeSchedulerApp app = (SilentModeSchedulerApp) requireActivity().getApplication();
        AppContainer appContainer = app.getAppContainer();

        viewModel = new ViewModelProvider(this, new OtpVerificationViewModel.Factory(
                appContainer.getFirebaseAuthManager(),
                appContainer.getFirestoreRepository(),
                registrationName,
                phoneNumber,
                verificationId
        )).get(OtpVerificationViewModel.class);

        setupListeners();
        observeState();
    }

    private void setupListeners() {
        binding.etOtp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.onOtpCodeChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnVerify.setOnClickListener(v -> {
            viewModel.onVerifyOtpClick();
        });
    }

    private void observeState() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            binding.tilOtp.setError(state.getError());
            binding.btnVerify.setEnabled(!state.isLoading() && state.getOtpCode().length() == 6);
            binding.progressBar.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);

            if (state.getCountdownSeconds() > 0) {
                binding.btnVerify.setText("Verify (" + state.getCountdownSeconds() + "s)");
            } else {
                binding.btnVerify.setText("Verify & Sign In");
            }
        });

        viewModel.getEvents().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof OtpVerificationEvent.VerificationSuccess) {
                viewModel.clearEvent();
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    mainActivity.clearBackstackAndNavigate(new MainFragment());
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
