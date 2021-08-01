package com.example.musicplayer;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder> {
    Context context;
    private ArrayList<AudioModel> musics;
    OnItemClick listener;

    public RecyclerAdapter(Context context, ArrayList<AudioModel> musics) {
        this.context = context;
        this.musics = musics;
    }
    interface OnItemClick {
        void onItemClickListener(int position);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        AudioModel model=musics.get(position);

        holder.name.setText(model.getMusicName());
        holder.artist.setText(model.getMusicArtist());
        Utils.getCover(context,holder.cover,model);


    }

    @Override
    public int getItemCount() {
        return musics.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView name, artist, currentTime, fullTime;

        public Holder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.item_image);
            name = itemView.findViewById(R.id.item_music);
            artist = itemView.findViewById(R.id.item_artist);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getAdapterPosition()!=RecyclerView.NO_POSITION){
                        listener.onItemClickListener(getAdapterPosition());
                    }
                }
            });

        }
    }
    public void setOnItemClickListener(OnItemClick listener){
        this.listener=listener;
    }
}
