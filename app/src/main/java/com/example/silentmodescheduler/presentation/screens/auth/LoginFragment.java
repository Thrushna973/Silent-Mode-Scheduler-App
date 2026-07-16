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
import com.example.silentmodescheduler.databinding.FragmentLoginBinding;
import com.example.silentmodescheduler.di.AppContainer;
import com.example.silentmodescheduler.presentation.viewmodel.LoginEvent;
import com.example.silentmodescheduler.presentation.viewmodel.LoginViewModel;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SilentModeSchedulerApp app = (SilentModeSchedulerApp) requireActivity().getApplication();
        AppContainer appContainer = app.getAppContainer();

        viewModel = new ViewModelProvider(this, new LoginViewModel.Factory(
                appContainer.getFirebaseAuthManager(),
                appContainer.getFirestoreRepository()
        )).get(LoginViewModel.class);

        setupListeners();
        observeState();
    }

    private void setupListeners() {
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

        binding.btnSendOtp.setOnClickListener(v -> {
            viewModel.onSendOtpClick(requireActivity());
        });

        binding.tvRegisterLink.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                mainActivity.navigateToFragment(new RegisterFragment());
            }
        });
    }

    private void observeState() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            binding.tilPhone.setError(state.getPhoneNumberError());
            binding.btnSendOtp.setEnabled(!state.isLoading());
            binding.progressBar.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);

            if (state.getGeneralError() != null) {
                Toast.makeText(getContext(), state.getGeneralError(), Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getEvents().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof LoginEvent.OtpSent) {
                LoginEvent.OtpSent otpSent = (LoginEvent.OtpSent) event;
                viewModel.clearEvent();
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    OtpVerificationFragment fragment = OtpVerificationFragment.newInstance(
                            otpSent.getPhoneNumber(),
                            otpSent.getVerificationId(),
                            "" // empty name indicates login flow
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
