package com.example.cicilanku;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.database.Cursor;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends Activity {
    private LinearLayout root, content, nav;
    private DatabaseHelper db;
    private PrefsManager prefs;
    private boolean dark;
    private String currentPage = "Beranda";

    private int bg, card, fg, muted, accent;
    private final NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(this);
        prefs = new PrefsManager(this);
        applyPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefs != null) {
            applyPreferences();
            render();
        }
    }

    private void applyPreferences() {
        dark = prefs.isDarkMode();
        bg = Color.parseColor(dark ? "#101827" : "#F3F6FB");
        card = Color.parseColor(dark ? "#1D2A3D" : "#FFFFFF");
        fg = Color.parseColor(dark ? "#F8FAFC" : "#172033");
        muted = Color.parseColor(dark ? "#B8C4D6" : "#64748B");
        accent = Color.parseColor("#2563EB");
        render();
    }

    private void render() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);
        setContentView(root);

        LinearLayout header = new LinearLayout(this);
        header.setPadding(20, 22, 20, 14);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.VERTICAL);
        TextView brand = text("CicilanKu", 25, true, fg);
        header.addView(brand);
        TextView subtitle = text("Catat cicilan, atur pembayaran", 13, false, muted);
        header.addView(subtitle);
        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(16, 6, 16, 20);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        nav = new LinearLayout(this);
        nav.setBackgroundColor(card);
        String[] pages = {"Beranda", "Pinjaman", "Jadwal", "Riwayat", "Pengaturan"};
        for (String page : pages) {
            TextView b = text(page, 11, currentPage.equals(page), currentPage.equals(page) ? accent : muted);
            b.setGravity(Gravity.CENTER);
            b.setPadding(3, 15, 3, 15);
            b.setOnClickListener(v -> { currentPage = page; render(); });
            nav.addView(b, new LinearLayout.LayoutParams(0, -2, 1));
        }
        root.addView(nav);
        showPage();
    }

    private void showPage() {
        if (currentPage.equals("Beranda")) showDashboard();
        else if (currentPage.equals("Pinjaman")) showLoans();
        else if (currentPage.equals("Jadwal")) showInstallments(false);
        else if (currentPage.equals("Riwayat")) showInstallments(true);
        else showSettings();
    }

    private void showDashboard() {
        content.addView(sectionTitle("Ringkasan"));
        content.addView(statCard("Total pinjaman", money(db.getTotalPrincipal())));
        content.addView(statCard("Cicilan belum dibayar", String.valueOf(db.getUnpaidCount()) + " tagihan"));
        content.addView(statCard("Cicilan jatuh tempo bulan ini", money(db.getCurrentMonthAmount())));
        content.addView(sectionTitle("Mulai kelola cicilan"));
        content.addView(actionButton("Tambah pinjaman", v -> { currentPage = "Pinjaman"; render(); showAddLoanDialog(); }));
        content.addView(note("Aplikasi ini merupakan simulasi pencatatan pinjaman dan pembayaran, bukan layanan pinjaman atau transaksi uang sungguhan."));
    }

    private void showLoans() {
        content.addView(sectionTitle("Data Pinjaman"));
        content.addView(actionButton("+ Tambah pinjaman", v -> showAddLoanDialog()));
        Cursor c = db.getLoans();
        if (c.getCount() == 0) content.addView(note("Belum ada data pinjaman. Tekan tombol Tambah pinjaman untuk memulai."));
        while (c.moveToNext()) {
            String details = "Pokok: " + money(c.getDouble(2)) + "\nBunga: " + c.getDouble(3) + "%  •  Tenor: " + c.getInt(4) + " bulan";
            content.addView(infoCard(c.getString(1), details));
        }
        c.close();
    }

    private void showInstallments(boolean history) {
        content.addView(sectionTitle(history ? "Riwayat Pembayaran" : "Jadwal Cicilan"));
        Cursor c = db.getInstallments(history, !history);
        if (c.getCount() == 0) content.addView(note(history ? "Belum ada pembayaran yang selesai." : "Belum ada cicilan yang menunggu pembayaran."));
        while (c.moveToNext()) {
            long id = c.getLong(0);
            String name = c.getString(1);
            String detail = money(c.getDouble(2)) + "\nJatuh tempo: " + c.getString(3);
            LinearLayout item = cardLayout();
            item.addView(text(name, 16, true, fg));
            item.addView(text(detail, 13, false, muted));
            Button action = new Button(this);
            action.setText(history ? "Batalkan status lunas" : "Tandai sudah dibayar");
            action.setOnClickListener(v -> {
                db.setPaid(id, !history);
                render();
            });
            item.addView(action);
            content.addView(item);
        }
        c.close();
    }

    private void showSettings() {
        content.addView(sectionTitle("Pengaturan"));
        content.addView(note("Pengaturan sederhana disimpan menggunakan SharedPreferences."));
        Switch darkSwitch = new Switch(this);
        darkSwitch.setText("Mode gelap");
        darkSwitch.setTextColor(fg);
        darkSwitch.setChecked(prefs.isDarkMode());
        darkSwitch.setPadding(8, 16, 8, 16);
        darkSwitch.setOnCheckedChangeListener((button, checked) -> {
            prefs.setDarkMode(checked);
            applyPreferences();
        });
        content.addView(darkSwitch);
        Switch reminderSwitch = new Switch(this);
        reminderSwitch.setText("Preferensi pengingat pembayaran");
        reminderSwitch.setTextColor(fg);
        reminderSwitch.setChecked(prefs.isReminderEnabled());
        reminderSwitch.setPadding(8, 16, 8, 16);
        reminderSwitch.setOnCheckedChangeListener((button, checked) -> prefs.setReminderEnabled(checked));
        content.addView(reminderSwitch);
        content.addView(note("Pengaturan disimpan dengan apply() dan dibaca kembali saat onResume(). Preferensi pengingat di sini adalah pilihan pengguna; notifikasi terjadwal belum diaktifkan."));
    }

    private void showAddLoanDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setPadding(20, 8, 20, 0);
        form.setOrientation(LinearLayout.VERTICAL);
        EditText name = field("Nama pinjaman");
        EditText principal = field("Jumlah pinjaman (contoh: 1000000)");
        principal.setInputType(2);
        EditText interest = field("Bunga total (%) contoh: 5");
        interest.setInputType(8194);
        EditText tenor = field("Tenor (bulan)");
        tenor.setInputType(2);
        form.addView(name); form.addView(principal); form.addView(interest); form.addView(tenor);
        new AlertDialog.Builder(this).setTitle("Tambah Pinjaman")
                .setView(form)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    try {
                        String n = name.getText().toString().trim();
                        double p = Double.parseDouble(principal.getText().toString().trim());
                        double i = Double.parseDouble(interest.getText().toString().trim());
                        int t = Integer.parseInt(tenor.getText().toString().trim());
                        if (n.isEmpty() || p <= 0 || i < 0 || t <= 0 || t > 120) throw new Exception();
                        db.addLoan(n, p, i, t);
                        currentPage = "Pinjaman";
                        render();
                        Toast.makeText(this, "Pinjaman dan jadwal cicilan berhasil disimpan", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Periksa kembali isian. Semua angka harus valid.", Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    private EditText field(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setSingleLine(true);
        e.setTextColor(fg);
        e.setHintTextColor(muted);
        return e;
    }

    private TextView sectionTitle(String s) {
        TextView t = text(s, 20, true, fg);
        t.setPadding(2, 12, 2, 12);
        return t;
    }

    private View statCard(String title, String value) {
        LinearLayout box = cardLayout();
        box.addView(text(title, 13, false, muted));
        box.addView(text(value, 22, true, fg));
        return box;
    }

    private View infoCard(String title, String detail) {
        LinearLayout box = cardLayout();
        box.addView(text(title, 16, true, fg));
        box.addView(text(detail, 13, false, muted));
        return box;
    }

    private LinearLayout cardLayout() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(16, 15, 16, 15);
        box.setBackgroundColor(card);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 6, 0, 10);
        box.setLayoutParams(lp);
        return box;
    }

    private TextView note(String s) {
        TextView t = text(s, 13, false, muted);
        t.setPadding(12, 16, 12, 16);
        return t;
    }

    private Button actionButton(String label, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label);
        b.setOnClickListener(listener);
        return b;
    }

    private TextView text(String s, int size, boolean bold, int color) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private String money(double value) {
        rupiah.setMaximumFractionDigits(0);
        return rupiah.format(value);
    }
}
