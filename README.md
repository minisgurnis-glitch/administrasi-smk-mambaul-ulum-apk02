# APK Administrasi SMK Mambaul Ulum

Project Android WebView untuk membuka Google Apps Script Web App sebagai aplikasi Android.

## Konfigurasi
- Nama aplikasi: Administrasi SMK Mambaul Ulum
- Package: `com.smkmambaululum.administrasi`
- URL Apps Script: sudah ditanam di `MainActivity.java`
- Ikon: logo SMK Mambaul Ulum yang diberikan
- Portrait, WebView, JavaScript, local storage, cookie, upload file, dan tombol Back Android.

## Cara termudah build APK

### Opsi A — Android Studio
1. Install Android Studio.
2. Pilih **Open** lalu buka folder project ini.
3. Tunggu Gradle sync selesai.
4. Pilih **Build > Build App Bundle(s) / APK(s) > Build APK(s)**.
5. APK debug biasanya berada di:
   `app/build/outputs/apk/debug/app-debug.apk`

### Opsi B — GitHub Actions (gratis, tanpa memasang Android Studio)
Upload seluruh folder project ke repository GitHub, lalu tambahkan workflow `.github/workflows/build-apk.yml` seperti contoh yang disediakan di folder ini. Setelah workflow selesai, APK dapat diambil dari Artifacts.

## Catatan
Project ini adalah wrapper WebView. Data, login, dan logika aplikasi tetap berada pada Google Apps Script/layanan yang dipakai oleh Web App.
