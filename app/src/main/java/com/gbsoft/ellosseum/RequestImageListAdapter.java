package com.gbsoft.ellosseum;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

public class RequestImageListAdapter extends RecyclerView.Adapter<RequestImageListAdapter.ViewHolder> {

    private final int limit = 3;
    private String mServerPath;

    public interface OnItemClickListener {
        void onItemClick(int pos);
    }

    private OnItemClickListener onItemClickListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnLongItemClickListener {
        void onLongItemClick(int pos);
    }

    private OnLongItemClickListener onLongItemClickListener = null;

    public void setOnLongItemClickListener(OnLongItemClickListener listener) {
        this.onLongItemClickListener = listener;
    }

    private OnItemClickListener onDeleteItemClickListener = null;

    public void setOnDeleteItemClickListener(OnItemClickListener listener){
        this.onDeleteItemClickListener = listener;
    }

    private ArrayList<String> mBitmaps = null;

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView mDelete_Img;
        LinearLayout linearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            mDelete_Img = itemView.findViewById(R.id.img_delete);
            linearLayout = itemView.findViewById(R.id.layout_img_delete);

            linearLayout.setVisibility(View.GONE);

//            imageView.setAdjustViewBounds(true);
//            Glide.get(itemView.getContext()).onLowMemory();

            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAbsoluteAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        if (onDeleteItemClickListener != null) {
                            onDeleteItemClickListener.onItemClick(pos);
                        }
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAbsoluteAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(pos);
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = getAbsoluteAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        if (onLongItemClickListener != null) {
                            onLongItemClickListener.onLongItemClick(pos);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    public RequestImageListAdapter(ArrayList<String> bitmaps, String serverPath) {
        this.mBitmaps = bitmaps;
        this.mServerPath = serverPath;
    }

    public void recycleBitmap(){
//        for(int i = 0; i < this.mBitmaps.size(); i++){
//            this.mBitmaps.get(i).recycle();
//        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.activity_request_image_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(holder.itemView)
                .load(mServerPath + "/uploads_android/" + mBitmaps.get(position) + ".jpg")
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerInside()
                .into(holder.imageView);

        Glide.with(holder.itemView)
                .load(R.drawable.cross_button)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerInside()
                .into(holder.mDelete_Img);

//        holder.imageView.setImageBitmap(mBitmaps.get(position));
    }


    @Override
    public int getItemCount() {
        return mBitmaps.size();
    }

    public void removeAt(int position) {
        mBitmaps.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,mBitmaps.size());
    }
}

