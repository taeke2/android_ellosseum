package com.gbsoft.ellosseum;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gbsoft.ellosseum.databinding.ItemIssueBinding;
import com.gbsoft.ellosseum.databinding.ItemNoticeBinding;
import com.gbsoft.ellosseum.dto.IssueDTO;
import com.gbsoft.ellosseum.dto.NoticeDTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.MyViewHolder> {
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

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemIssueBinding mBinding;

        public MyViewHolder(ItemIssueBinding binding) {
            super(binding.getRoot());
            mBinding = binding;

            mBinding.layoutItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        if (onItemClickListener != null)
                            onItemClickListener.onItemClick(position);
                }
            });
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new IssueAdapter.MyViewHolder(ItemIssueBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        IssueDTO dto = Common.sIssueDTOS.get(position);

        holder.mBinding.txtTitle.setText(dto.getTitle());
//        if (dto.getImportantYn() == 0)
            holder.mBinding.imgViewImportant.setVisibility(View.GONE);
        if (!isNew(dto.getUpdateAt(), 3))
            holder.mBinding.txtNew.setVisibility(View.GONE);
        holder.mBinding.txtWriter.setText(dto.getName());
        holder.mBinding.txtDate.setText(dto.getUpdateAt());
    }

    private boolean isNew(String date, int day) {
        Date nowDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null;
        try {
            startDate = format.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.add(Calendar.DAY_OF_MONTH, day);

            Date endDate = cal.getTime();
            return nowDate.after(startDate) && nowDate.before(endDate);
        } catch (ParseException e) {
            Log.e(Common.TAG_ERR, "ERROR: Parse exception - isNew");
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return Common.sIssueDTOS.size();
    }

    // 리사이클러 뷰 스크롤하고 다시 올리면 뷰가 초기화 되는데, 이 메소드로 뷰를 유지시킨다.
    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
