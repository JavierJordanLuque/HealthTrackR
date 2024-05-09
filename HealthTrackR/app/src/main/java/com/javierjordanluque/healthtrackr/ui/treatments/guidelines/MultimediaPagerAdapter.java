package com.javierjordanluque.healthtrackr.ui.treatments.guidelines;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Multimedia;
import com.javierjordanluque.healthtrackr.models.enumerations.MultimediaType;

import java.util.List;

public class MultimediaPagerAdapter extends RecyclerView.Adapter<MultimediaPagerAdapter.ViewHolder> {
    private final Context context;
    private final List<Multimedia> multimedias;

    public MultimediaPagerAdapter(Context context, List<Multimedia> multimedias) {
        this.context = context;
        this.multimedias = multimedias;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MultimediaType.IMAGE.ordinal()) {
            view = LayoutInflater.from(context).inflate(R.layout.item_image_pager, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_video_pager, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Multimedia multimedia = multimedias.get(position);
        if (multimedia.getType() == MultimediaType.IMAGE) {
            Glide.with(context)
                    .load(multimedia.getUri())
                    .error(R.drawable.ic_error)
                    .into(holder.imageView);
        } else {
            holder.videoView.setVideoURI(Uri.parse(multimedia.getUri().toString()));
            final MediaController mediaController = new MediaController(context);
            mediaController.setAnchorView(holder.videoView);
            holder.videoView.setMediaController(mediaController);
            holder.videoView.setOnPreparedListener(mediaPlayer -> mediaPlayer.setOnCompletionListener(mediaPlayerCompleted -> holder.videoView.seekTo(0)));
        }
    }

    @Override
    public int getItemCount() {
        return multimedias.size();
    }

    @Override
    public int getItemViewType(int position) {
        return multimedias.get(position).getType().ordinal();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        VideoView videoView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            videoView = itemView.findViewById(R.id.videoView);
        }
    }
}
