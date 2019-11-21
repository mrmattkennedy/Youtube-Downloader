package android.youtube_dl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder>  {
    private String[] mDataset;

    private List<String> titles;
    private List<String> channels;
    private List<String> thumbnailURLs;
    private List<String> videoIDs;

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    private Context context;

    // data is passed into the constructor
    VideoAdapter(Context context,
                 List<String> titles,
                 List<String> channels,
                 List<String> thumbnailURLs,
                 List<String> videoIDs) {
        this.mInflater = LayoutInflater.from(context);
        this.titles = titles;
        this.channels = channels;
        this.thumbnailURLs = thumbnailURLs;
        this.videoIDs = videoIDs;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.videoviewrow, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String title = titles.get(position);
        String channel = channels.get(position);
        String thumbnailURL = thumbnailURLs.get(position);

        holder.title.setText(title);
        holder.channel.setText(channel);
        Picasso.with(context).load(thumbnailURL).into(holder.thumbnail);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return titles.size();
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView channel;
        ImageView thumbnail;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.videoTitle);
            channel = itemView.findViewById(R.id.channelTitle);
            thumbnail = itemView.findViewById(R.id.videoThumbnail);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public String getID(int id)
    {
        return videoIDs.get(id);
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
