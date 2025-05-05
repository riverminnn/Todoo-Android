package msv21a100100107.nguyenquangha;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todooapp.R;
import com.example.todooapp.utils.shared.TodooDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SinhVienFragment extends Fragment implements SinhVienAdapter.OnSinhVienClickListener {
    private SinhVienViewModel viewModel;
    private SinhVienAdapter adapter;
    private RecyclerView recyclerView;
    private long selectedDate;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sv_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SinhVienViewModel.class);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SinhVienAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fabAddTest);
        fab.setOnClickListener(v -> showAddDialog());

        FloatingActionButton sort = view.findViewById(R.id.fabSort);
        sort.setOnClickListener(v -> sortByName());

        viewModel.getAllSinhViens().observe(getViewLifecycleOwner(),
                sinhViens -> adapter.setSinhViens(sinhViens));
    }

    private void sortByName() {
        viewModel.sortByHoTen().observe(getViewLifecycleOwner(),
                sinhViens -> adapter.setSinhViens(sinhViens));
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sv_input, null);
        EditText etHoTen = dialogView.findViewById(R.id.etHoTen);
        EditText etNgaySinh = dialogView.findViewById(R.id.etNgaySinh);
        RadioGroup rgGender = dialogView.findViewById(R.id.rgGender);
        EditText etChucVu = dialogView.findViewById(R.id.etChucVu);
        EditText etHsl = dialogView.findViewById(R.id.etHsl);
        EditText etLuongCB = dialogView.findViewById(R.id.etLuongCB);

        // Setup date picker
        etNgaySinh.setOnClickListener(v -> showDatePicker(etNgaySinh));

        AlertDialog dialog = new TodooDialogBuilder(requireContext())
                .setTitle("Thêm Sinh Viên")
                .setView(dialogView)
                .setPositiveButton("Thêm", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (validateInput(etHoTen, etNgaySinh, etChucVu, etHsl, etLuongCB)) {
                    String hoTen = etHoTen.getText().toString().trim();
                    boolean gender = rgGender.getCheckedRadioButtonId() == R.id.rbNam;
                    String chucVu = etChucVu.getText().toString().trim();
                    double hsl = Double.parseDouble(etHsl.getText().toString().trim());
                    double luongCB = Double.parseDouble(etLuongCB.getText().toString().trim());

                    SinhVien sinhVien = new SinhVien(hoTen, selectedDate, gender, chucVu, hsl, luongCB);
                    viewModel.insert(sinhVien);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void showDatePicker(EditText etNgaySinh) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    selectedDate = calendar.getTimeInMillis();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etNgaySinh.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private boolean validateInput(EditText etHoTen, EditText etNgaySinh,
                                  EditText etChucVu, EditText etHsl, EditText etLuongCB) {

        if (etHoTen.getText().toString().trim().isEmpty() ||
                etNgaySinh.getText().toString().trim().isEmpty() ||
                etChucVu.getText().toString().trim().isEmpty() ||
                etHsl.getText().toString().trim().isEmpty() ||
                etLuongCB.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return false;
        }

        Calendar calendarSelected = Calendar.getInstance();
        calendarSelected.setTimeInMillis(selectedDate);
        Calendar now = Calendar.getInstance();
        int age = now.get(Calendar.YEAR) - calendarSelected.get(Calendar.YEAR);

        // Check Sinh Nhat
        if (now.get(Calendar.DAY_OF_YEAR) < calendarSelected.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        if (age < 18) {
            Toast.makeText(requireContext(), "Sinh viên phải từ 18 tuổi trở lên", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void showEditDialog(SinhVien sinhVien) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sv_input, null);
        EditText etHoTen = dialogView.findViewById(R.id.etHoTen);
        EditText etNgaySinh = dialogView.findViewById(R.id.etNgaySinh);
        RadioGroup rgGender = dialogView.findViewById(R.id.rgGender);
        EditText etChucVu = dialogView.findViewById(R.id.etChucVu);
        EditText etHsl = dialogView.findViewById(R.id.etHsl);
        EditText etLuongCB = dialogView.findViewById(R.id.etLuongCB);

        etHoTen.setText(sinhVien.getHoTen());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etNgaySinh.setText(dateFormat.format(new Date(sinhVien.getNgaySinh())));
        selectedDate = sinhVien.getNgaySinh();

        if (sinhVien.isGender()) {
            rgGender.check(R.id.rbNam);
        } else {
            rgGender.check(R.id.rbNu);
        }

        etChucVu.setText(sinhVien.getChucVu());
        etHsl.setText(String.format(Locale.getDefault(), "%.2f", sinhVien.getHsl()));
        etLuongCB.setText(String.format(Locale.getDefault(), "%.0f", sinhVien.getLuongCB()));

        etNgaySinh.setOnClickListener(v -> showDatePicker(etNgaySinh));

        AlertDialog dialog = new TodooDialogBuilder(requireContext())
                .setTitle("Sửa Sinh Viên")
                .setView(dialogView)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (validateInput(etHoTen, etNgaySinh, etChucVu, etHsl, etLuongCB)) {
                    sinhVien.setHoTen(etHoTen.getText().toString().trim());
                    sinhVien.setNgaySinh(selectedDate);
                    sinhVien.setGender(rgGender.getCheckedRadioButtonId() == R.id.rbNam);
                    sinhVien.setChucVu(etChucVu.getText().toString().trim());
                    sinhVien.setHsl(Double.parseDouble(etHsl.getText().toString().trim()));
                    sinhVien.setLuongCB(Double.parseDouble(etLuongCB.getText().toString().trim()));

                    viewModel.update(sinhVien);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    @Override
    public void onSinhVienClick(SinhVien sinhVien) {
        showEditDialog(sinhVien);
    }

    @Override
    public void onSinhVienLongClick(SinhVien sinhVien) {
        new TodooDialogBuilder(requireContext())
                .setTitle("Xóa Sinh Viên")
                .setMessage("Bạn có chắc chắn muốn xóa sinh viên này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.delete(sinhVien);
                    Toast.makeText(requireContext(), "Đã xóa sinh viên", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}