package com.example.silentmodescheduler.presentation.screens.userselection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.silentmodescheduler.MainActivity;
import com.example.silentmodescheduler.databinding.FragmentUserSelectionBinding;
import com.example.silentmodescheduler.presentation.screens.auth.LoginFragment;
import com.example.silentmodescheduler.presentation.screens.auth.RegisterFragment;

public class UserSelectionFragment extends Fragment {
    private FragmentUserSelectionBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.cardUser.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                mainActivity.navigateToFragmentWithBackstack(new RegisterFragment());
            }
        });

        binding.cardAdmin.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                mainActivity.navigateToFragmentWithBackstack(new LoginFragment());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
