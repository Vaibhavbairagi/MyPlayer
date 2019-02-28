package com.vaibhav.myplayer.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vaibhav.myplayer.R;
import com.vaibhav.myplayer.activities.MainActivity;
import com.vaibhav.myplayer.fragments.AboutUsFragment;
import com.vaibhav.myplayer.fragments.FavouriteFragment;
import com.vaibhav.myplayer.fragments.MainScreenFragment;
import com.vaibhav.myplayer.fragments.SettingsFragment;

import java.util.ArrayList;

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder> {
    ArrayList<String> contentList;
    int[] getImages;
    Context mContext;

    public NavigationDrawerAdapter(ArrayList<String> _contentlist, int[] _getImages, Context _context) {
        this.contentList = _contentlist;
        this.getImages = _getImages;
        this.mContext = _context;
    }

    @NonNull
    @Override
    public NavViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemview = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_custom_navigation_drawer, viewGroup, false);
        return new NavViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull final NavViewHolder navViewHolder, int i) {
        navViewHolder.icon_GET.setBackgroundResource(getImages[i]);
        navViewHolder.text_GET.setText(contentList.get(i));
        navViewHolder.contentHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int x = navViewHolder.getAdapterPosition();
                if (x == 0) {
                    MainScreenFragment mainScreenFragment = new MainScreenFragment();
                    ((MainActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.details_fragment, mainScreenFragment)
                            .commit();
                } else if (x == 1) {
                    FavouriteFragment favouriteFragment = new FavouriteFragment();
                    ((MainActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.details_fragment, favouriteFragment)
                            .commit();
                } else if (x == 2) {
                    SettingsFragment settingsFragment = new SettingsFragment();
                    ((MainActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.details_fragment, settingsFragment)
                            .commit();
                } else {
                    AboutUsFragment aboutUsFragment = new AboutUsFragment();
                    ((MainActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.details_fragment, aboutUsFragment)
                            .commit();
                }
                MainActivity.drawerLayout.closeDrawers();
            }
        });
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public static class NavViewHolder extends RecyclerView.ViewHolder {
        ImageView icon_GET;
        TextView text_GET;
        RelativeLayout contentHolder;

        private NavViewHolder(@NonNull View itemView) {
            super(itemView);
            this.icon_GET = itemView.findViewById(R.id.icon_navdrawer);
            this.text_GET = itemView.findViewById(R.id.text_navdrawer);
            this.contentHolder = itemView.findViewById(R.id.navdrawer_item_content_holder);
        }
    }

}
