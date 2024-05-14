package com.javierjordanluque.healthtrackr.ui.treatments.guidelines;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.enumerations.MultimediaType;
import com.javierjordanluque.healthtrackr.util.media.Media;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SelectableMultimediaAdapter extends RecyclerView.Adapter<SelectableMultimediaAdapter.ImageViewHolder> {
    private final Context context;
    private final List<Media> mediaList;
    private OnItemClickListener listener;
    private final int cellSize;

    public SelectableMultimediaAdapter(Context context, List<Media> mediaList, int cellSize) {
        this.context = context;
        this.mediaList = mediaList;
        this.cellSize = cellSize;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MultimediaType.IMAGE.ordinal()) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_selector_item_image, parent, false);
        } else if (viewType == MultimediaType.VIDEO.ordinal()) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_selector_item_video, parent, false);
        } else {
            throw new IllegalArgumentException("Failed to create a view holder with viewType + (" + viewType + ")");
        }

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = cellSize;
        layoutParams.height = cellSize;
        view.setLayoutParams(layoutParams);

        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Media media = mediaList.get(position);

        Glide.with(context)
                .load(media.getUri())
                .apply(new RequestOptions().error(R.drawable.ic_error))
                .into(holder.imageView);

        if (media.getMimeType().contains("image")) {
            holder.imageView.setContentDescription(context.getString(R.string.content_description_selectable_image) + " " + position + ": " + media.getName());
        } else if (media.getMimeType().contains("video")) {
            holder.imageView.setContentDescription(context.getString(R.string.content_description_selectable_video) + " " + position + ": " + media.getName());
            holder.textViewDuration.setText(formatDuration((media.getDuration())));
        }
    }

    private String formatDuration(long durationInMillis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % 60;

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    @Override
    public int getItemViewType(int position) {
        String mimeType = mediaList.get(position).getMimeType();

        if (mimeType.contains("image")) {
            return MultimediaType.IMAGE.ordinal();
        } else if (mimeType.contains("video")) {
            return MultimediaType.VIDEO.ordinal();
        }

        return -1;
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewDuration;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            textViewDuration = itemView.findViewById(R.id.textViewDuration);
            itemView.setOnClickListener(view -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getAdapterPosition(), view);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
