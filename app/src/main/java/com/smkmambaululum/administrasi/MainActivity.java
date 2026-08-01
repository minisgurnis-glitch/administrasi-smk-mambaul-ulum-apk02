package com.smkmambaululum.administrasi;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
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

    // ============================================================
    // URL GOOGLE APPS SCRIPT
    // ============================================================

    private static final String APP_URL =
            "https://script.google.com/macros/s/AKfycbyUFOlAbGQBZIhDNUKljStXzisIqv1WOSe4OY3H2JjSejKZ7kKw8DCIkLGaqxWI9eS9MQ/exec";

    private static final int FILE_CHOOSER_REQUEST = 1001;

    private WebView webView;
    private SwipeRefreshLayout swipeRefresh;
    private ValueCallback<Uri[]> fileCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // ========================================================
        // HUBUNGKAN KOMPONEN
        // ========================================================

        swipeRefresh = findViewById(R.id.swipeRefresh);
        webView = findViewById(R.id.webView);

        // ========================================================
        // KONFIGURASI WEBVIEW
        // ========================================================

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        settings.setUserAgentString(
                settings.getUserAgentString()
                        + " SMK-MU-AndroidApp/1.0"
        );

        // ========================================================
        // COOKIE
        // ========================================================

        CookieManager cookieManager =
                CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);

        cookieManager.setAcceptThirdPartyCookies(
                webView,
                true
        );

        // ========================================================
        // WEBVIEW CLIENT
        // ========================================================

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {

                Uri uri = request.getUrl();

                String scheme = uri.getScheme();

                // HTTP / HTTPS tetap dibuka di WebView
                if (scheme != null &&
                        (scheme.equals("http")
                                || scheme.equals("https"))) {

                    return false;
                }

                // Link selain HTTP/HTTPS
                // dibuka menggunakan aplikasi Android
                try {

                    Intent intent =
                            new Intent(
                                    Intent.ACTION_VIEW,
                                    uri
                            );

                    startActivity(intent);

                } catch (Exception ignored) {
                }

                return true;
            }
        });

        // ========================================================
        // WEB CHROME CLIENT
        // ========================================================

        webView.setWebChromeClient(new WebChromeClient() {

            // ====================================================
            // UPLOAD FILE
            // ====================================================

            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams fileChooserParams
            ) {

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

            // ====================================================
            // PERMISSION WEBVIEW
            // ====================================================

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

        // ========================================================
        // DOWNLOAD FILE KE HP ANDROID
        // ========================================================
        //
        // Ini bagian BARU.
        //
        // Ketika website mengirim file untuk didownload,
        // WebView akan menyerahkannya kepada Android DownloadManager.
        //
        // File akan masuk ke:
        //
        // Penyimpanan Internal
        //     └── Download
        //
        // ========================================================

        webView.setDownloadListener(
                (url, userAgent, contentDisposition, mimeType, contentLength) -> {

                    try {

                        // Ambil cookie WebView agar file yang membutuhkan
                        // session/login tetap dapat diakses.
                        String cookies =
                                CookieManager
                                        .getInstance()
                                        .getCookie(url);

                        // Menentukan nama file
                        String fileName =
                                URLUtil.guessFileName(
                                        url,
                                        contentDisposition,
                                        mimeType
                                );

                        // Jika nama file kosong
                        if (fileName == null
                                || fileName.trim().isEmpty()) {

                            String extension = "";

                            if (mimeType != null) {

                                String ext =
                                        MimeTypeMap
                                                .getSingleton()
                                                .getExtensionFromMimeType(
                                                        mimeType
                                                );

                                if (ext != null
                                        && !ext.isEmpty()) {

                                    extension = "." + ext;
                                }
                            }

                            fileName =
                                    "download"
                                            + extension;
                        }

                        // =================================================
                        // DOWNLOAD MANAGER ANDROID
                        // =================================================

                        DownloadManager.Request request =
                                new DownloadManager.Request(
                                        Uri.parse(url)
                                );

                        // Header penting agar Apps Script/Google
                        // mengenali session WebView.
                        if (userAgent != null) {

                            request.addRequestHeader(
                                    "User-Agent",
                                    userAgent
                            );
                        }

                        if (cookies != null
                                && !cookies.isEmpty()) {

                            request.addRequestHeader(
                                    "Cookie",
                                    cookies
                            );
                        }

                        // Jenis file
                        if (mimeType != null
                                && !mimeType.isEmpty()) {

                            request.setMimeType(mimeType);
                        }

                        // Judul notifikasi
                        request.setTitle(fileName);

                        request.setDescription(
                                "Mengunduh file dari Administrasi SMK Mambaul Ulum"
                        );

                        // Tampilkan notifikasi proses download
                        request.setNotificationVisibility(
                                DownloadManager
                                        .Request
                                        .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                        );

                        // =================================================
                        // SIMPAN KE FOLDER DOWNLOAD ANDROID
                        // =================================================

                        request.setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                fileName
                        );

                        // Masukkan ke Android Download Manager
                        DownloadManager downloadManager =
                                (DownloadManager)
                                        getSystemService(
                                                Context.DOWNLOAD_SERVICE
                                        );

                        if (downloadManager != null) {

                            downloadManager.enqueue(request);

                            Toast.makeText(
                                    MainActivity.this,
                                    "Download dimulai...",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Toast.makeText(
                                    MainActivity.this,
                                    "Download Manager Android tidak tersedia",
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                    } catch (Exception e) {

                        Toast.makeText(
                                MainActivity.this,
                                "Gagal mengunduh file: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

        // ========================================================
        // MATIKAN PULL-TO-REFRESH
        // ========================================================

        swipeRefresh.setEnabled(false);

        // Hilangkan efek overscroll
        webView.setOverScrollMode(
                View.OVER_SCROLL_NEVER
        );

        // ========================================================
        // BUKA GOOGLE APPS SCRIPT
        // ========================================================

        webView.loadUrl(APP_URL);

        // ========================================================
        // TOMBOL BACK ANDROID
        // ========================================================

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (webView.canGoBack()) {

                            webView.goBack();

                        } else {

                            finish();
                        }
                    }
                }
        );
    }

    // ============================================================
    // HASIL PEMILIHAN FILE
    // ============================================================

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

        if (requestCode == FILE_CHOOSER_REQUEST
                && fileCallback != null) {

            Uri[] results = null;

            if (resultCode == Activity.RESULT_OK
                    && data != null) {

                // MULTIPLE FILE
                if (data.getClipData() != null) {

                    int count =
                            data.getClipData()
                                    .getItemCount();

                    results = new Uri[count];

                    for (int i = 0; i < count; i++) {

                        results[i] =
                                data.getClipData()
                                        .getItemAt(i)
                                        .getUri();
                    }

                }

                // SINGLE FILE
                else if (data.getData() != null) {

                    results = new Uri[]{
                            data.getData()
                    };
                }
            }

            fileCallback.onReceiveValue(results);

            fileCallback = null;
        }
    }

    // ============================================================
    // HANCURKAN WEBVIEW
    // ============================================================

    @Override
    protected void onDestroy() {

        if (webView != null) {

            webView.stopLoading();

            webView.destroy();
        }

        super.onDestroy();
    }
}
