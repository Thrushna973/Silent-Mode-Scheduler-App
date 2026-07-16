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
import com.example.silentmodescheduler.databinding.FragmentRegisterBinding;
import com.example.silentmodescheduler.di.AppContainer;
import com.example.silentmodescheduler.presentation.viewmodel.RegisterEvent;
import com.example.silentmodescheduler.presentation.viewmodel.RegisterViewModel;

public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding binding;
    private RegisterViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SilentModeSchedulerApp app = (SilentModeSchedulerApp) requireActivity().getApplication();
        AppContainer appContainer = app.getAppContainer();

        viewModel = new ViewModelProvider(this, new RegisterViewModel.Factory(
                appContainer.getFirebaseAuthManager(),
                appContainer.getFirestoreRepository()
        )).get(RegisterViewModel.class);

        setupListeners();
        observeState();
    }

    private void setupListeners() {
        binding.etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.onNameChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.onPhoneNumberChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnRegister.setOnClickListener(v -> {
            viewModel.onSendOtpClick(requireActivity());
        });

        binding.tvLoginLink.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                mainActivity.navigateToFragment(new LoginFragment());
            }
        });
    }

    private void observeState() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            binding.tilName.setError(state.getNameError());
            binding.tilPhone.setError(state.getPhoneNumberError());
            binding.btnRegister.setEnabled(!state.isLoading());
            binding.progressBar.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);

            if (state.getGeneralError() != null) {
                Toast.makeText(getContext(), state.getGeneralError(), Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getEvents().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof RegisterEvent.OtpSent) {
                RegisterEvent.OtpSent otpSent = (RegisterEvent.OtpSent) event;
                viewModel.clearEvent();
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    OtpVerificationFragment fragment = OtpVerificationFragment.newInstance(
                            otpSent.getPhoneNumber(),
                            otpSent.getVerificationId(),
                            otpSent.getName() // Pass name to complete registration on OTP success
                    );
                    mainActivity.navigateToFragmentWithBackstack(fragment);
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
