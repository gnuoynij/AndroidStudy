package com.example.adate.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adate.R;
import com.example.adate.chat.MessageActivity;
import com.example.adate.model.UserModel;
import com.squareup.picasso.Picasso;

import java.util.List;


public class PeopleRecyclerViewAdapter extends RecyclerView.Adapter<PeopleRecyclerViewAdapter.PeopleViewHolder> {

    List<UserModel> userModels; // 상대방 정보 담김
    Context context;
    public PeopleRecyclerViewAdapter(List<UserModel> userModels, Context context) {
        this.userModels = userModels;
        this.context = context;
    }

    @NonNull
    @Override
    public PeopleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);

        return new PeopleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PeopleViewHolder holder, int position) {
        Picasso.get().load(userModels.get(position).profileImageUrl).into(holder.imageView);
        holder.textView.setText(userModels.get(position).userName);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MessageActivity.class);
                intent.putExtra("destinationUid", userModels.get(position).uid);
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return userModels.size();
    }

    class PeopleViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;

        public PeopleViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.frienditem_imageview);
            textView = view.findViewById(R.id.frienditem_textview);
        }
    }
}
