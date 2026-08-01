package com.smkmambaululum.administrasi;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    // URL Google Apps Script
    private static final String APP_URL =
            "https://script.google.com/macros/s/AKfycbyUFOlAbGQBZIhDNUKljStXzisIqv1WOSe4OY3H2JjSejKZ7kKw8DCIkLGaqxWI9eS9MQ/exec";

    // Request code
    private static final int FILE_CHOOSER_REQUEST = 1001;
    private static final int CAMERA_REQUEST = 1002;

    // WebView
    private WebView webView;

    // SwipeRefreshLayout tetap digunakan pada layout,
    // tetapi fitur swipe-to-refresh DINONAKTIFKAN.
    private SwipeRefreshLayout swipeRefresh;

    // Callback untuk upload file
    private ValueCallback<Uri[]> fileCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Memuat layout utama
        setContentView(R.layout.activity_main);

        // Menghubungkan komponen dari XML
        swipeRefresh = findViewById(R.id.swipeRefresh);
        webView = findViewById(R.id.webView);

        // =========================================================
        // KONFIGURASI WEBVIEW
        // =========================================================

        WebSettings settings = webView.getSettings();

        // Aktifkan JavaScript
        settings.setJavaScriptEnabled(true);

        // DOM Storage diperlukan oleh banyak aplikasi web modern
        settings.setDomStorageEnabled(true);

        // Database WebView
        settings.setDatabaseEnabled(true);

        // Izinkan akses file
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        // Nonaktifkan zoom
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        // Izinkan JavaScript membuka window
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        // Izinkan media berjalan tanpa gesture tambahan
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Tambahkan identitas aplikasi ke User Agent
        settings.setUserAgentString(
                settings.getUserAgentString()
                        + " SMK-MU-AndroidApp/1.0"
        );

        // =========================================================
        // COOKIE
        // =========================================================

        CookieManager cookieManager = CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);

        cookieManager.setAcceptThirdPartyCookies(
                webView,
                true
        );

        // =========================================================
        // WEBVIEW CLIENT
        // =========================================================

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {

                Uri uri = request.getUrl();

                String scheme = uri.getScheme();

                // Link HTTP/HTTPS tetap dibuka di dalam WebView
                if (scheme != null &&
                        (scheme.equals("http")
                                || scheme.equals("https"))) {

                    return false;
                }

                // Link selain HTTP/HTTPS dibuka menggunakan aplikasi
                // yang sesuai di Android.
                try {

                    Intent intent =
                            new Intent(Intent.ACTION_VIEW, uri);

                    startActivity(intent);

                } catch (Exception ignored) {
                    // Tidak melakukan apa-apa jika tidak ada
                    // aplikasi yang dapat menangani link tersebut.
                }

                return true;
            }
        });

        // =========================================================
        // WEB CHROME CLIENT
        // =========================================================

        webView.setWebChromeClient(new WebChromeClient() {

            // -----------------------------------------------------
            // UPLOAD FILE
            // -----------------------------------------------------

            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams fileChooserParams
            ) {

                // Jika masih ada callback sebelumnya,
                // batalkan callback tersebut.
                if (fileCallback != null) {
                    fileCallback.onReceiveValue(null);
                }

                fileCallback = callback;

                Intent intent =
                        fileChooserParams.createIntent();

                try {

                    startActivityForResult(
                            intent,
                            FILE_CHOOSER_REQUEST
                    );

                } catch (Exception e) {

                    fileCallback = null;

                    Toast.makeText(
                            MainActivity.this,
                            "Pemilih file tidak tersedia",
                            Toast.LENGTH_SHORT
                    ).show();

                    return false;
                }

                return true;
            }

            // -----------------------------------------------------
            // PERMISSION WEBVIEW
            // -----------------------------------------------------

            @Override
            public void onPermissionRequest(
                    final PermissionRequest request
            ) {

                runOnUiThread(() -> {

                    if (request.getResources() != null) {

                        request.grant(
                                request.getResources()
                        );
                    }
                });
            }
        });

        // =========================================================
        // PULL-TO-REFRESH DINONAKTIFKAN
        // =========================================================
        //
        // Sebelumnya terdapat:
        //
        // swipeRefresh.setOnRefreshListener(
        //     () -> webView.reload()
        // );
        //
        // dan:
        //
        // webView.setOnScrollChangeListener(...);
        //
        // Kedua kode tersebut DIHAPUS agar ketika pengguna
        // melakukan scroll, halaman tidak melakukan refresh.
        //
        // =========================================================

        swipeRefresh.setEnabled(false);

        // Hilangkan efek overscroll WebView
        webView.setOverScrollMode(
                View.OVER_SCROLL_NEVER
        );

        // =========================================================
        // LOAD GOOGLE APPS SCRIPT
        // =========================================================

        webView.loadUrl(APP_URL);

        // =========================================================
        // TOMBOL BACK ANDROID
        // =========================================================

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        // Jika WebView memiliki history,
                        // kembali ke halaman sebelumnya.
                        if (webView.canGoBack()) {

                            webView.goBack();

                        } else {

                            // Jika tidak ada history,
                            // tutup aplikasi.
                            finish();
                        }
                    }
                }
        );
    }

    // =============================================================
    // HASIL PEMILIHAN FILE
    // =============================================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        // Pastikan ini merupakan hasil dari file chooser
        if (requestCode == FILE_CHOOSER_REQUEST
                && fileCallback != null) {

            Uri[] results = null;

            // Jika pengguna berhasil memilih file
            if (resultCode == Activity.RESULT_OK
                    && data != null) {

                // -------------------------------------------------
                // MULTIPLE FILE
                // -------------------------------------------------

                if (data.getClipData() != null) {

                    int count =
                            data.getClipData().getItemCount();

                    results = new Uri[count];

                    for (int i = 0; i < count; i++) {

                        results[i] =
                                data.getClipData()
                                        .getItemAt(i)
                                        .getUri();
                    }

                }

                // -------------------------------------------------
                // SINGLE FILE
                // -------------------------------------------------

                else if (data.getData() != null) {

                    results = new Uri[]{
                            data.getData()
                    };
                }
            }

            // Kirim hasil file ke WebView
            fileCallback.onReceiveValue(results);

            // Reset callback
            fileCallback = null;
        }
    }

    // =============================================================
    // KETIKA ACTIVITY DIHANCURKAN
    // =============================================================

    @Override
    protected void onDestroy() {

        if (webView != null) {

            // Hentikan proses loading
            webView.stopLoading();

            // Hancurkan WebView
            webView.destroy();
        }

        super.onDestroy();
    }
}
