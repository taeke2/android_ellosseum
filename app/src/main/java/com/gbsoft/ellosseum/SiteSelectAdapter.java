package com.gbsoft.ellosseum;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gbsoft.ellosseum.databinding.ItemSiteBinding;

public class SiteSelectAdapter extends RecyclerView.Adapter<SiteSelectAdapter.MyViewHolder> {

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ItemSiteBinding mBinding;

        public MyViewHolder(ItemSiteBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;

            mBinding.txtSiteName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int pos);
    }

    private SiteSelectAdapter.OnItemClickListener onItemClickListener = null;

    public void setOnItemClickListener(SiteSelectAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SiteSelectAdapter.MyViewHolder(ItemSiteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.mBinding.txtSiteName.setText(Common.sSiteDTOS.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return Common.sSiteDTOS.size();
    }


}
