package com.gbsoft.ellosseum;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gbsoft.ellosseum.databinding.ItemAttendanceBinding;
import com.gbsoft.ellosseum.dto.EmployeeAttendanceDTO;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.MyViewHolder> {
    public class MyViewHolder extends RecyclerView.ViewHolder {
        ItemAttendanceBinding binding;

        public MyViewHolder(ItemAttendanceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(ItemAttendanceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        EmployeeAttendanceDTO dto = Common.sEmployeeAttendanceDTOS.get(position);

        holder.binding.txtDate.setText(dto.getDate());
        holder.binding.txtAttendanceDateTime.setText(dto.getAttendanceDateTime());
        holder.binding.txtLeaveWorkDateTime.setText(dto.getLeaveWorkDateTime());
    }

    @Override
    public int getItemCount() {
        return Common.sEmployeeAttendanceDTOS.size();
    }

    // 리사이클러 뷰 스크롤하고 다시 올리면 뷰가 초기화 되는데, 이 메소드로 뷰를 유지시킨다.
    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
