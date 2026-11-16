# Muraja'ah Audio — Hafalan Al-Qur'an & Mutun (Google Drive)

Aplikasi Android (Kotlin + Jetpack Compose + Media3 `MediaSessionService`) yang berfokus pada **pemutaran audio muraja'ah** dari **Google Drive**, dirancang agar nyaman dan aman digunakan saat berkendara di motor (mendukung pemutaran latar belakang saat layar mati, kontrol notifikasi/lock-screen, dan tombol headset Bluetooth).

## Fitur Utama

1. **Dengarkan (Mode Pemutar & Mode Motor)**
   - Langsung memutar daftar audio hafalan secara berurutan (default target: **Surah 1 Al-Fatihah s.d. Surah 9 At-Taubah**, dapat diubah kapan saja).
   - **Auto-Lanjut Playlist:** otomatis lanjut dari satu surah ke surah berikutnya tanpa menyentuh HP.
   - **Pemutaran Latar Belakang (`MediaSessionService`):** tetap berputar saat layar HP dimatikan/dikantongi, serta mendukung kontrol lock-screen dan tombol Next/Prev/Pause headset Bluetooth.
   - **Mode Berkendara (Mode Motor):** layar kontras tinggi dengan tombol raksasa (*Play/Pause, Next, Prev, ±10 detik, Loop*) dan opsi layar tetap menyala jika dipasang di holder.
   - **Putar Ulang (Loop):** `1x`, `3x`, `5x`, `∞` + **Kecepatan Audio:** `0.75x`, `1.0x`, `1.25x`, `1.5x`.
2. **Hafalan Saya (Target Surah & Mutun)**
   - Atur rentang hafalan Anda (misalnya Al-Fatihah s.d. At-Taubah), lihat surah mana yang sudah punya audio Drive, dan putar atau tautkan link Drive langsung per Surah/Mutun.
3. **Pustaka Google Drive**
   - Tempel **Link Share File** (1 atau banyak link sekaligus dengan format `Judul | Link`) atau **1 Link Folder Publik Google Drive**.
   - **Deteksi Otomatis Surah:** file seperti `001 Al-Fatihah.mp3` hingga `009 At-Taubah.mp3` otomatis terdeteksi nomor surahnya agar urut saat diputar.

## Cara Build APK Manual di GitHub Actions

Workflow `.github/workflows/build-apk.yml` diset ke mode manual (`workflow_dispatch`) agar tidak otomatis mem-build APK pada setiap push. Saat Anda siap mengambil APK:
1. Buka tab **Actions** di repositori GitHub Anda.
2. Pilih workflow **Build Android APK** → klik **Run workflow**.
3. Setelah selesai hijau, unduh artifact **`hafalan-quran-mutun-debug-apk`**.
