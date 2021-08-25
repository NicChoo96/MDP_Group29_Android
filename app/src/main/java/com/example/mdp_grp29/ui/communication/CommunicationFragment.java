package com.example.mdp_grp29.ui.communication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.mdp_grp29.R;
import com.example.mdp_grp29.databinding.FragmentCommunicationBinding;

public class CommunicationFragment extends Fragment {

    private CommunicationViewModel communicationViewModel;
    private FragmentCommunicationBinding binding;
    private TextView textView;

    private final String TAG = "CommunicationFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        communicationViewModel =
                new ViewModelProvider(this).get(CommunicationViewModel.class);

        binding = FragmentCommunicationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);


        textView = view.findViewById(R.id.textView2);
        communicationViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

//        getParentFragmentManager().setFragmentResultListener("myKey", this, new FragmentResultListener() {
//            @Override
//            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
//                // We use a String here, but any type that can be put in a Bundle is supported
//                String result = bundle.getString("thisNutz");
//                // Do something with the result
//                textView.setText(result);
//                Log.e(TAG, result);
//            }
//        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG, "Communication Destroyed");
        binding = null;
    }
}