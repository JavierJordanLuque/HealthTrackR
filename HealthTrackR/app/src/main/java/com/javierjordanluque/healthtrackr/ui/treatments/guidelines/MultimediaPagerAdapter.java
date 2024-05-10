package com.javierjordanluque.healthtrackr.ui.treatments.guidelines;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Multimedia;
import com.javierjordanluque.healthtrackr.models.enumerations.MultimediaType;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        } else if (viewType == MultimediaType.VIDEO.ordinal()) {
            view = LayoutInflater.from(context).inflate(R.layout.item_video_pager, parent, false);
        } else {
            throw new IllegalArgumentException("Failed to create a view holder with viewType + (" + viewType + ")");
        }

        return new ViewHolder(view);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Multimedia multimedia = multimedias.get(position);
        View multimediaNotFoundView = LayoutInflater.from(context).inflate(R.layout.layout_multimedia_not_found, null);

        if (multimedia.getType() == MultimediaType.IMAGE) {
            Glide.with(context)
                    .load(multimedia.getUri())
                    .error(new BitmapDrawable(context.getResources(), createBitmapFromView(multimediaNotFoundView)))
                    .into(holder.imageView);
        } else {
            if (!isYouTubeUrl(multimedia.getUri().toString())) {
                holder.videoView.setVideoURI(Uri.parse(multimedia.getUri().toString()));
                final MediaController mediaController = new MediaController(context);
                mediaController.setAnchorView(holder.videoView);
                holder.videoView.setMediaController(mediaController);

                holder.videoView.setOnPreparedListener(mediaPlayer -> {
                    holder.videoView.setBackgroundResource(R.drawable.layout_play_video);
                    mediaPlayer.setOnCompletionListener(mediaPlayerCompleted -> {
                        holder.videoView.seekTo(0);
                        holder.videoView.setBackgroundResource(R.drawable.layout_play_video);
                    });
                    mediaPlayer.setOnInfoListener((mp, what, extra) -> {
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            holder.videoView.setBackground(null);
                            return true;
                        }
                        return false;
                    });
                });

                holder.videoView.setOnErrorListener((mediaPlayer, what, extra) -> {
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    ImageView imageView = new ImageView(context);
                    imageView.setImageBitmap(createBitmapFromView(multimediaNotFoundView));
                    ((ViewGroup) holder.videoView.getParent()).addView(imageView, layoutParams);
                    holder.videoView.setVisibility(View.GONE);
                    return true;
                });
            } else {
                holder.videoView.setVisibility(View.GONE);
                holder.webView.setVisibility(View.VISIBLE);

                String video = "<html><head><style type=\"text/css\">body {margin:0;padding:0;}</style></head><body><iframe width=\"100%\" height=\"100%\" src=\"" +
                        getYouTubeEmbedUrl(multimedia.getUri()) + "\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; " +
                        "encrypted-media; gyroscope; picture-in-picture; web-share\"></iframe></body></html>";
                holder.webView.loadData(video, "text/html", "utf-8");
                holder.webView.getSettings().setJavaScriptEnabled(true);
                holder.webView.setWebChromeClient(new WebChromeClient());
            }
        }
    }

    private Bitmap createBitmapFromView(View view) {
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(canvas);

        return bitmap;
    }

    private String getYouTubeEmbedUrl(Uri uri) {
        String videoId = extractYouTubeVideoId(uri.toString());
        return "https://www.youtube.com/embed/" + videoId;
    }

    private String extractYouTubeVideoId(String url) {
        String videoId = "";

        if (url != null && !url.trim().isEmpty()) {
            String pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*";
            Pattern compiledPattern = Pattern.compile(pattern);
            Matcher matcher = compiledPattern.matcher(url);
            if (matcher.find())
                videoId = matcher.group();
        }

        return videoId;
    }

    private boolean isYouTubeUrl(String url) {
        return url.contains("youtube.com") || url.contains("youtu.be");
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
        WebView webView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            videoView = itemView.findViewById(R.id.videoView);
            webView = itemView.findViewById(R.id.webView);
        }
    }
}
