package com.example.sosbicicletta2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ContattiFragment extends Fragment {

    View v;
   private RecyclerView myrecyclerview;
   private FirebaseDatabase db = FirebaseDatabase.getInstance();
   private DatabaseReference root = db.getReference().child("Driver");
   private List<Contatti> lstContatti;
   private RecyclerViewAdapter adapter;
   public ContattiFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_contatti,container,false);
        myrecyclerview = (RecyclerView) v.findViewById(R.id.cont_recycler);
        //RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(getContext(),lstContatti);
        myrecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new RecyclerViewAdapter(getContext(),lstContatti);
        myrecyclerview.setAdapter(adapter);

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lstContatti = new ArrayList<>();
        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot Snapshot) {
                for (DataSnapshot dataSnapshot : Snapshot.getChildren()){
                    Contatti model = dataSnapshot.getValue(Contatti.class);
                    lstContatti.add(model);

                }
                adapter.notifyDataSetChanged();

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }
}