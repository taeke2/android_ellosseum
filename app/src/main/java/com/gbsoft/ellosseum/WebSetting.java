package com.gbsoft.ellosseum;

import android.content.Context;
import android.net.http.SslError;
import android.os.Build;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebSetting extends WebViewClient {
//    public SslWebViewConnect sslWebViewConnect;
    private final Context mContext;
    public WebSetting(Context mContext) {
//        sslWebViewConnect = new SslWebViewConnect();
        this.mContext = mContext;
    }

    // @RequiresApi(api = Build.VERSION_CODES.O)
    public WebView setWebSettings(WebView webView) {
        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(false);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            webSettings.setSafeBrowsingEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        /*
            필요한 설정은 참고
            setJavaScriptEnabled(true);
            // javascript를 실행할 수 있도록 설정
            setJavaScriptCanOpenWindowsAutomatically (true);
            // javascript가 window.open()을 사용할 수 있도록 설정
            setBuiltInZoomControls(false);
            // 안드로이드에서 제공하는 줌 아이콘을 사용할 수 있도록 설정
            setPluginState(WebSettings.PluginState.ON_DEMAND);
            // 플러그인을 사용할 수 있도록 설정
            setSupportMultipleWindows(false);
            // 여러개의 윈도우를 사용할 수 있도록 설정
            setSupportZoom(false);
            // 확대,축소 기능을 사용할 수 있도록 설정
            setBlockNetworkImage(false);
            // 네트워크의 이미지의 리소스를 로드하지않음
            setLoadsImagesAutomatically(true);
            // 웹뷰가 앱에 등록되어 있는 이미지 리소스를 자동으로 로드하도록 설정
            setUseWideViewPort(true);
            // wide viewport를 사용하도록 설정
            setCacheMode(WebSettings.LOAD_NO_CACHE);
            // 웹뷰가 캐시를 사용하지 않도록 설정 매번 새로 로딩
        */
        webView.setWebViewClient(new SslWebViewConnect(mContext));
//        ssl 인증이 없는 경우 해결을 위한 부분
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        return webView;
    }

    private static class SslWebViewConnect extends WebViewClient {

        public SslWebViewConnect(Context mContext) {
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//            super.onReceivedSslError(view, handler, error);

//            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//            String message = "SSL 인증서 오류.";
//            switch (error.getPrimaryError()) {
//                case SslError.SSL_UNTRUSTED:
//                    message = "신뢰할 수 없는 인증기관입니다.";
//                    break;
//                case SslError.SSL_EXPIRED:
//                    message = "인증서가 만료되었습니다.";
//                    break;
//                case SslError.SSL_IDMISMATCH:
//                    message = "인증서 호스트 이름이 일치하지 않습니다.";
//                    break;
//                case SslError.SSL_NOTYETVALID:
//                    message = "인증서가 유효하지 않습니다.";
//                    break;
//            }
//            message += " 계속 진행하시겠습니까?";
//
//            builder.setTitle("SSL 인증서 오류");
//            builder.setMessage(message);
//            builder.setPositiveButton("계속", (dialog, which) -> handler.proceed());
//            builder.setNegativeButton("취소", (dialog, which) -> handler.cancel());
//            final AlertDialog dialog = builder.create();
//            dialog.show();

            handler.proceed(); // SSL 인증서 무시
        }

        // 페이지 내에서만 url 이동하게끔 만듬
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}


