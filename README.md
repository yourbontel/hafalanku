# Hafalan Al-Qur'an & Mutun (dengan Audio Google Drive)

Aplikasi Android (Kotlin + Jetpack Compose + Material 3 + Room + Media3/ExoPlayer) untuk mencatat progres hafalan **Al-Qur'an (114 Surah lengkap)** dan **Mutun Ilmiah**, serta memutar audio murottal / kajian langsung dari **Link Share Google Drive**.

## Fitur Utama

1. **Beranda (Dashboard Progres)**
   - Ringkasan jumlah ayat dihafal, estimasi Juz, jumlah surah Mutqin, dan progres Mutun.
   - Antrean **Prioritas Muraja'ah** otomatis untuk hafalan yang berstatus *Muraja'ah* atau belum diulang ≥ 3 hari.
2. **Hafalan Al-Qur'an (114 Surah Lengkap)**
   - Pencarian Surah (Latin, arti, nomor), filter per **Juz (1–30)**, dan filter status (*Belum, Menghafal, Muraja'ah, Mutqin*).
   - Slider/stepper jumlah ayat dihafal, catat muraja'ah hari ini, catatan mutasyabihat, dan audio Google Drive per Surah.
3. **Hafalan Mutun Ilmiah**
   - Sudah berisi template populer (*Tuhfatul Athfal, Al-Jazariyah, Arba'in Nawawi, Ushul Tsalatsah, Qawa'idul Arba', Al-Ajurrumiyah*) dan bebas menambah kitab baru.
   - Checklist per bab/bagian, slider bait/hadits, catatan, dan audio Google Drive per kitab.
4. **Pemutar Audio Google Drive (Media3)**
   - Cukup tempel **Link Share File** (satu atau banyak link sekaligus) atau **Link Folder Publik** Google Drive — tanpa login OAuth.
   - **Mini-player persisten** di atas navigasi bawah.
   - **Putar Ulang (Loop):** `1x`, `3x`, `5x`, `∞` (terus-menerus).
   - **Kecepatan Audio:** `0.75x`, `1.0x`, `1.25x`, `1.5x`.

---

## Cara Mengambil Link Audio dari Google Drive

1. Upload file audio (`.mp3` / `.m4a` / `.ogg` / `.wav`) ke Google Drive Anda.
2. Klik titik tiga pada file (atau folder) → **Bagikan (Share)**.
3. Pada bagian *Akses umum*, ubah dari *Dibatasi* menjadi **"Siapa saja yang memiliki link" (Anyone with the link)** sebagai *Pengakses lihat saja (Viewer)*.
4. Klik **Salin link**, lalu tempel di menu **Audio → + Link** atau di halaman detail Surah/Mutun.

> Tip untuk input banyak file sekaligus: gunakan format `Judul Audio | Link Google Drive` (satu per baris).

---

## Cara Push ke GitHub & Download APK Otomatis (Tanpa Build Lokal)

Laptop Anda tidak perlu menginstal Android Studio atau Android SDK. GitHub Actions akan mem-build file `.apk` di cloud setiap kali Anda push ke branch `main` atau `master`:

```bash
git init
git add .
git commit -m "Initial commit: Aplikasi Hafalan Quran & Mutun"
git branch -M main
git remote add origin https://github.com/USERNAME_ANDA/NAMA_REPO.git
git push -u origin main
```

Setelah push selesai:
1. Buka repositori Anda di GitHub → klik tab **Actions**.
2. Klik workflow **Build Android APK** yang berjalan.
3. Setelah statusnya hijau (berhasil), gulir ke bagian **Artifacts** lalu unduh **`hafalan-quran-mutun-debug-apk`**.
4. Ekstrak file ZIP tersebut, salin `app-debug.apk` ke HP Android Anda, lalu instal.

---

## Cara Membuka / Mengedit di Google AI Studio

1. Pastikan proyek sudah di-push ke GitHub.
2. Buka **Google AI Studio** → masuk ke **Build mode**.
3. Klik ikon **`+`** di kotak prompt → pilih **Import from GitHub**, lalu pilih repositori ini.
4. Anda dapat meminta AI menambahkan fitur baru lewat chat dan melakukan sinkronisasi balik ke GitHub.
