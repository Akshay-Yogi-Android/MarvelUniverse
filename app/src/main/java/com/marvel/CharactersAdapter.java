package com.marvel;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.marvel.Model.Characters;


import java.util.List;

public class CharactersAdapter extends RecyclerView.Adapter<CharactersAdapter.ViewHolder>{

    Context context;
    List<Characters> mCharacters;

    public CharactersAdapter(Context context, List<Characters> mCharacters) {
        this.context = context;
        this.mCharacters = mCharacters;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_characters,parent,false);

        return new CharactersAdapter.ViewHolder(v);


    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Characters characters = mCharacters.get(position);

        holder.txt_name.setText(characters.getName());

        Glide.with(context)
                .load(characters.getImage())
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .placeholder(R.mipmap.ic_launcher)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(holder.img_path);

        Log.d("imagePath",characters.getImage());

    }

    @Override
    public int getItemCount() {
        return mCharacters.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView img_path;
        TextView txt_name;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            img_path = itemView.findViewById(R.id.img_Path);
            txt_name = itemView.findViewById(R.id.txt_name);
        }
    }
}
