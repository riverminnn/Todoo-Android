package msv21a100100107.nguyenquangha;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.todooapp.data.TodoDatabase;

import java.util.List;

public class SinhVienViewModel extends AndroidViewModel {
    private SinhVienDao sinhVienDao;
    private LiveData<List<SinhVien>> allSinhViens;

    public SinhVienViewModel(@NonNull Application application) {
        super(application);
        TodoDatabase db = TodoDatabase.getInstance(application);
        sinhVienDao = db.sinhVienDao();
        allSinhViens = sinhVienDao.getAllSinhViens();
    }

    public LiveData<List<SinhVien>> getAllSinhViens() {
        return allSinhViens;
    }

    public void insert(SinhVien sinhVien) {
        new Thread(() -> sinhVienDao.insert(sinhVien)).start();
    }

    public void update(SinhVien sinhVien) {
        new Thread(() -> sinhVienDao.update(sinhVien)).start();
    }

    public void delete(SinhVien sinhVien) {
        new Thread(() -> sinhVienDao.delete(sinhVien)).start();
    }

    public LiveData<List<SinhVien>> sortByHoTen() {
        return sinhVienDao.sortByHoTen();
    }
}