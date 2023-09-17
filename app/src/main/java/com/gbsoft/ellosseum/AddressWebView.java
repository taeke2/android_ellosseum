package com.gbsoft.ellosseum;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class AddressWebView extends AppCompatActivity {
    private WebView browser;

    class MyJavaScriptInterface { //해당코드를 server(.html)로 넘겨주어 processData함수를 서버에서 사용할 수 있게함.
        @JavascriptInterface //즉, 앱에서 해당 웹을 제어할 수 있도록 돕는 기능
        @SuppressWarnings("unused")
        public void processDATA(String data) { //서버에서 검색을 통해 선택한 주소 값을 앱으로 다시 보내줄때 사용하는 코드
            Intent intent = new Intent();
            intent.putExtra("data", data);
            setResult(RESULT_OK, intent);
            finish(); //다시 MainActivity로 돌아가게하는 코드임.
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_web_view);

        browser = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = browser.getSettings();

        /*
            WebView가 javascript를 사용할 수 있도록 허가해주는 코드
            WebView에 로드하려는 웹페이지가 자바스크립트를 사용하는 경우에 자바스크립트를 사용하도록 설정해야함.
            자바스크립트가 사용 설정되면 앱 코드와 자바스크립트 코드 간에 인터페이스를 만들 수도 있음.
         */
        webSettings.setJavaScriptEnabled(true);

        //다중 윈도우 허용 - web의 popup창이 보일 수 있도록 하는 기능
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //javascript에서 자동으로 창을 열도록 지시(widndow.open()에 해당함)

        //위에서 정의한 JavaScriptInterface를 Android라는 이름으로 추가함.
        browser.addJavascriptInterface(new MyJavaScriptInterface(), "Android");

        /*
            WebViewClient 역할 :
            탐색 오류 또는 양식 제출 오류 등 콘텐츠 렌더링에 영향을 미치는 이벤트 처리.
            이 서브클래스를 사용하여 URL 로드를 가로챌 수도 있음.
         */

        browser.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed(); //SSL 에러 발생해도 계속 진행
            }

            // 새로운 URL이 webview에 로드되려 할 경우 컨트롤을 대신할 기회를 줌
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true; //url 로드 중단
            }

            @Override
            public void onPageFinished(WebView view, String url) { //호스팅 앱에게 호출되는 페이지의 로딩이 끝났음을 알려줌.
                browser.loadUrl("javascript:sample2_execDaumPostcode();"); //서버(.html)의 함수호출
            }

//            @RequiresApi(api = Build.VERSION_CODES.O)
//            @Override
//            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
//                if (!detail.didCrash()) {
//                    /*
//                        시스템이 렌더기를 종료하거나 렌더기 프로세스 자체가 다운되어 WebView 객체의
//                        렌더기 프로세스가 사라지는 경우가 발생하면 종료 처리 API를 호출하는 코드
//                        이 API를 사용하면 렌더기 프로세스가 사라져도 앱은 계속 실행됨.
//                        (안드로이드 developers 상에서 사용을 권장하는 함수)
//                     */
//                    Log.e("MY_APP_TAG", "System killed the WebView rendering process " +
//                            "to reclaim memory. Recreating...");
//
//                    if (browser != null) {
//                        ViewGroup webViewContainer = (ViewGroup)view.getParent();
//                        webViewContainer.removeView(browser);
//                        browser.destroy();
//                        browser = null;
//                    }
//                    return true; // The app continues executing.
//                }
//                Log.e("MY_APP_TAG", "The WebView rendering process crashed!");
//                return false;
//            }
        });
        /*
            WebChromeClient 역할:
            전체 화면 지원 사용 설정.
            이 클래스는 WebView가 창을 만들거나 닫고 자바스크립트 대화상자를 사용자에게 전송하는 등
            호스트 앱의 UI를 변경하기 위한 권한을 필요로 할 때도 호출
         */
        browser.setWebChromeClient(new WebChromeClient() {
            //호스트 앱이 새창을 띄우도록 하는 코드
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView newWebView = new WebView(AddressWebView.this);
                WebSettings webSettings = newWebView.getSettings();
                webSettings.setJavaScriptEnabled(true);

                final Dialog dialog = new Dialog(AddressWebView.this);
                dialog.setContentView(newWebView);

                ViewGroup.LayoutParams params = dialog.getWindow().getAttributes();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                dialog.getWindow().setAttributes((WindowManager.LayoutParams) params);
                dialog.show();
                newWebView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onCloseWindow(WebView window) {
                        dialog.dismiss();
                    }
                });
                ((WebView.WebViewTransport) resultMsg.obj).setWebView(newWebView);
                resultMsg.sendToTarget();
                ;
                return true;
            }

            //ssl 인증이 없는 경우 해결을 위한 부분
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }
        });
        //dom storage 혀용
        browser.getSettings().setDomStorageEnabled(true);

//        String address = "http://115.85.183.231:3000/AddressAPI"; //서버의 html 주소
        String address = Common.URL + "/AddressAPI";
        browser.loadUrl(address);
    }
}
