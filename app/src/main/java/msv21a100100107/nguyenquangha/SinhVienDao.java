package msv21a100100107.nguyenquangha;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SinhVienDao {
    @Insert
    long insert(SinhVien sv);

    @Update
    void update(SinhVien sv);

    @Delete
    void delete(SinhVien sv);

    @Query("SELECT * FROM tblSinhVien")
    LiveData<List<SinhVien>> getAllSinhViens();

    @Query("SELECT * FROM tblSinhVien WHERE maSV = :id")
    LiveData<SinhVien> getSinhVienById(long id);

    @Query("SELECT * FROM tblSinhVien ORDER BY HoTen DESC")
    LiveData<List<SinhVien>> sortByHoTen();
}