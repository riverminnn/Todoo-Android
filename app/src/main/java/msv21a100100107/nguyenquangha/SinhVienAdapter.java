package msv21a100100107.nguyenquangha;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todooapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SinhVienAdapter extends RecyclerView.Adapter<SinhVienAdapter.SinhVienViewHolder> {
    private List<SinhVien> sinhViens;
    private OnSinhVienClickListener listener;

    public interface OnSinhVienClickListener {
        void onSinhVienClick(SinhVien sinhVien);
        void onSinhVienLongClick(SinhVien sinhVien);
    }

    public SinhVienAdapter(List<SinhVien> sinhViens, OnSinhVienClickListener listener) {
        this.sinhViens = sinhViens;
        this.listener = listener;
    }

    public void setSinhViens(List<SinhVien> sinhViens) {
        this.sinhViens = sinhViens;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SinhVienViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sv, parent, false);
        return new SinhVienViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SinhVienViewHolder holder, int position) {
        SinhVien sinhVien = sinhViens.get(position);
        holder.tvMaSV.setText("Mã SV: " + sinhVien.getMaSV());
        holder.tvHoTen.setText(sinhVien.getHoTen());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(sinhVien.getNgaySinh()));
        holder.tvNgaySinh.setText("Ngày sinh: " + formattedDate);

        holder.tvGender.setText("Giới tính: " + (sinhVien.isGender() ? "Nam" : "Nữ"));
        holder.tvChucVu.setText("Chức vụ: " + sinhVien.getChucVu());
        holder.tvHsl.setText(String.format(Locale.getDefault(), "HSL: %.2f", sinhVien.getHsl()));
        holder.tvLuongCB.setText(String.format(Locale.getDefault(), "Lương CB: %.0f", sinhVien.getLuongCB()));

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSinhVienLongClick(sinhVien);
            }
        });

        holder.itemView.setOnClickListener(v -> listener.onSinhVienClick(sinhVien));
    }

    @Override
    public int getItemCount() {
        return sinhViens != null ? sinhViens.size() : 0;
    }

    static class SinhVienViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaSV, tvHoTen, tvNgaySinh, tvGender, tvChucVu, tvHsl, tvLuongCB;
        ImageButton btnDelete;

        SinhVienViewHolder(View itemView) {
            super(itemView);
            tvMaSV = itemView.findViewById(R.id.tvMaSV);
            tvHoTen = itemView.findViewById(R.id.tvHoTen);
            tvNgaySinh = itemView.findViewById(R.id.tvNgaySinh);
            tvGender = itemView.findViewById(R.id.tvGender);
            tvChucVu = itemView.findViewById(R.id.tvChucVu);
            tvHsl = itemView.findViewById(R.id.tvHsl);
            tvLuongCB = itemView.findViewById(R.id.tvLuongCB);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}