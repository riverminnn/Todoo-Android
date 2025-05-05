package msv21a100100107.nguyenquangha;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tblSinhVien")
public class SinhVien {
    @PrimaryKey(autoGenerate = true)
    private long maSV;
    private String hoTen;
    private long ngaySinh;
    private boolean gender;
    private String chucVu;
    private double hsl;
    private double luongCB;

    public SinhVien(String hoTen, long ngaySinh, boolean gender, String chucVu, double hsl, double luongCB) {
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gender = gender;
        this.chucVu = chucVu;
        this.hsl = hsl;
        this.luongCB = luongCB;
    }

    public long getMaSV() {
        return maSV;
    }

    public void setMaSV(long maSV) {
        this.maSV = maSV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public long getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(long ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public double getHsl() {
        return hsl;
    }

    public void setHsl(double hsl) {
        this.hsl = hsl;
    }

    public double getLuongCB() {
        return luongCB;
    }

    public void setLuongCB(double luongCB) {
        this.luongCB = luongCB;
    }
}