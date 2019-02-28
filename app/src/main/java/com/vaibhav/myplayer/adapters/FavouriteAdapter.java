package com.vaibhav.myplayer.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vaibhav.myplayer.R;
import com.vaibhav.myplayer.Songs;
import com.vaibhav.myplayer.fragments.SongPlayingFragment;

import java.util.ArrayList;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.MyViewHolder> {

    ArrayList<Songs> songDetails;
    Context mContext;

    public FavouriteAdapter(ArrayList<Songs> _songDetails, Context _context) {
        this.songDetails = _songDetails;
        this.mContext = _context;
    }

    @NonNull
    @Override
    public FavouriteAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_custom_mainscreen_adapter, viewGroup, false);
        return new FavouriteAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final FavouriteAdapter.MyViewHolder myViewHolder, final int i) {
        final Songs songObject = songDetails.get(i);
        myViewHolder.trackTitle.setText(songObject.songTitle);
        myViewHolder.trackArtist.setText(songObject.artist);
        myViewHolder.contentHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SongPlayingFragment.mediaplayer.isPlaying()) {
                    SongPlayingFragment.mediaplayer.pause();
                }
                SongPlayingFragment songPlayingFragment = new SongPlayingFragment();
                Bundle args = new Bundle();
                args.putString("songArtist", songObject.artist);
                args.putString("path", songObject.songData);
                args.putString("songTitle", songObject.songTitle);
                args.putInt("songId", songObject.songID.intValue());
                args.putInt("songPosition", myViewHolder.getAdapterPosition());
                args.putParcelableArrayList("songData", songDetails);
                songPlayingFragment.setArguments(args);
                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right,R.anim.slide_in_right,R.anim.slide_out_left)
                        .replace(R.id.details_fragment, songPlayingFragment)
                        .addToBackStack("SongPlayingFragmentFavourite")
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (songDetails == null)
            return 0;
        else
            return songDetails.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView trackTitle;
        TextView trackArtist;
        RelativeLayout contentHolder;

        private MyViewHolder(View view) {
            super(view);
            this.trackTitle = view.findViewById(R.id.trackTitle);
            this.trackArtist = view.findViewById(R.id.trackArtist);
            this.contentHolder = view.findViewById(R.id.contentRow);
        }
    }
}
