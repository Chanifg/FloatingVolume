# 🎛️ FloatingVolume (AssistiveVolume)

**FloatingVolume** adalah aplikasi Android ringan (dibangun dengan **Kotlin** & **Native UI**) yang menyediakan kontrol volume melayang (Gaya *AssistiveTouch*). Aplikasi ini sangat cocok sebagai solusi alternatif bagi pengguna yang tombol fisik volume di *smartphone*-nya (misalnya Redmi Note 8 Pro atau HP Android lainnya) sedang rusak.

---

## ✨ Fitur Utama
Aplikasi ini berjalan sebagai *Foreground Service* dan memanfaatkan `SYSTEM_ALERT_WINDOW` (*Draw over other apps*) agar bisa selalu diakses kapan saja:

- **Tarik & Geser (Drag-and-Drop)**: Tombol melayang dapat dipindahkan ke sisi mana saja di layar.
- **Auto-Collapse Cerdas**: Membuka panel kontrol penuh saat disentuh, dan **otomatis tertutup / mengecil kembali** jika tidak ada aktivitas selama 3 detik.
- 🔊 **Volume Up** & 🔉 **Volume Down**.
- 🔇 **Mute Toggle**: Menghidupkan atau mematikan suara secara instan.
- 📳 **Ringer Mode Cycle**: Mengganti mode dering secara cepat (Normal → Getar → Senyap).
- **Tema Gelap & Elegan**: Menggunakan warna hitam semi-transparan (*grayscale/glassmorphism*) yang tidak mengganggu pemandangan layar.

---

## 🛠️ Persyaratan Sistem (*Requirements*)
- **Android 8.0 (Oreo / API 26)** atau lebih baru.
- Aplikasi ini dibangun via *Command-Line* menggunakan Gradle Wrapper, sehingga **tidak membutuhkan Android Studio** untuk dikompilasi!

---

## 🚀 Cara Build & Install (Lewat Terminal Linux/Mac)

### 1. Prasyarat
Pastikan kamu sudah menginstal **JDK (Java 17)** dan **Android SDK Command-Line Tools**.
```bash
# Contoh di Ubuntu/Debian untuk Java 17
sudo apt install openjdk-17-jdk
```
Pastikan variabel environment `ANDROID_HOME` sudah diarahkan ke folder SDK kamu.

### 2. Kompilasi APK (Build)
Masuk ke root direktori proyek ini, lalu jalankan Gradle Wrapper:
```bash
chmod +x gradlew
./gradlew assembleDebug
```
*Catatan: Saat pertama kali dijalankan, proses ini akan memakan waktu karena akan mengunduh Gradle beserta dependensinya.*

### 3. Install ke Perangkat
Sambungkan HP Android kamu menggunakan kabel USB, pastikan **USB Debugging** sudah aktif di opsi pengembang, lalu jalankan:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 Cara Penggunaan di Perangkat
1. Buka aplikasi **AssistiveVolume**.
2. Tekan tombol **"Aktifkan Tombol Mengambang"**.
3. Sistem akan meminta dua izin (*permissions*) penting:
   - **Tampilkan di atas aplikasi lain (Overlay)**: Agar tombol bisa mengambang.
   - **Akses Jangan Ganggu (Do Not Disturb)**: Agar fitur pengganti mode dering (Senyap/Getar) dapat bekerja.
4. Kembali ke aplikasi dan tekan aktifkan sekali lagi. Tombol melayang akan muncul!

*(Opsional: Jika menggunakan OS ketat seperti MIUI / HyperOS, disarankan mengaktifkan fitur **Autostart / Mulai Otomatis** pada info aplikasi agar tombol tetap menyala setelah HP di-restart).*

---

## 👨‍💻 Kontribusi & Modifikasi
Project ini bebas digunakan dan dimodifikasi! Seluruh kode *layout* ada di `app/src/main/res/layout` dan logika *Service/Overlay* utamanya berada di `FloatingService.kt`.
