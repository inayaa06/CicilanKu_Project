CICILANKU - PROJECT ANDROID STUDIO (JAVA)

PERSYARATAN
- Android Studio
- JDK yang direkomendasikan oleh Android Studio
- Internet saat Gradle pertama kali melakukan sync
- Minimum Android: API 24 (Android 7.0)

CARA MEMBUKA
1. Ekstrak CicilanKu.zip.
2. Buka Android Studio.
3. Pilih Open.
4. Pilih folder CicilanKu hasil ekstrak (folder yang berisi settings.gradle).
5. Tunggu Gradle Sync selesai. Android Studio mungkin mengunduh Gradle dan Android Gradle Plugin.
6. Pilih perangkat Android atau emulator.
7. Tekan tombol Run (segitiga hijau).

FITUR
- Dashboard ringkasan pinjaman dan cicilan.
- Tambah data pinjaman dan pembuatan jadwal cicilan otomatis.
- Tandai cicilan sebagai sudah dibayar.
- Riwayat pembayaran.
- Pengaturan mode gelap dan preferensi pengingat dengan SharedPreferences.
- Data pinjaman dan cicilan disimpan di SQLite.

CATATAN
- Aplikasi adalah simulasi pencatatan, bukan aplikasi pinjaman atau pembayaran sungguhan.
- Preferensi pengingat hanya menyimpan pilihan aktif/nonaktif. Notifikasi Android terjadwal belum dibuat.
- Pembayaran hanya mengubah status cicilan di database; tidak memproses uang.
- Project menggunakan Android Gradle Plugin 8.6.1 dan Gradle 8.7. Pada sync pertama diperlukan internet.
