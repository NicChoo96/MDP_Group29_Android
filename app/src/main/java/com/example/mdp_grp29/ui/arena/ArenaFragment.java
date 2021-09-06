package com.example.mdp_grp29.ui.arena;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mdp_grp29.R;
import com.example.mdp_grp29.arena_objects.Obstacles;
import com.example.mdp_grp29.databinding.FragmentArenaBinding;

public class ArenaFragment extends Fragment {

    private ArenaViewModel arenaViewModel;
    private FragmentArenaBinding binding;
    private ArenaView arenaView;
    private ImageView focusButton;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        arenaViewModel =
                new ViewModelProvider(this).get(ArenaViewModel.class);

        binding = FragmentArenaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



        //getActivity().setContentView(R.layout.fragment_home);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        arenaView = view.findViewById(R.id.arenaView);
        focusButton = view.findViewById(R.id.focus_button);
        focusButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                arenaView.ResetArenaView();
            }
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}