package com.example.silentmodescheduler;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.silentmodescheduler.data.firebase.auth.FirebaseAuthManager;
import com.example.silentmodescheduler.presentation.screens.main.MainFragment;
import com.example.silentmodescheduler.presentation.screens.userselection.UserSelectionFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FirebaseAuthManager authManager = ((SilentModeSchedulerApp) getApplication())
                    .getAppContainer().getFirebaseAuthManager();

            if (authManager.isUserSignedIn()) {
                navigateToFragment(new MainFragment());
            } else {
                navigateToFragment(new UserSelectionFragment());
            }
        }
    }

    public void navigateToFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void navigateToFragmentWithBackstack(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void clearBackstackAndNavigate(Fragment fragment) {
        getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        navigateToFragment(fragment);
    }
}
