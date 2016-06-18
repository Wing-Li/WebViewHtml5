package com.lyl.cacheweb;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Html5Activity extends AppCompatActivity {

    private String mUrl;

    private LinearLayout mLayout;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Bundle bundle = getIntent().getBundleExtra("bundle");
        if (bundle != null) {
            mUrl = bundle.getString("url");
        } else {
            mUrl = "https://github.com/Wing-Li";
        }

        mLayout = (LinearLayout) findViewById(R.id.web_layout);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mWebView = new WebView(getApplicationContext());
        mWebView.setLayoutParams(params);
        mLayout.addView(mWebView);

        WebSettings mWebSettings = mWebView.getSettings();
        mWebSettings.setSupportZoom(true);//支持缩放，默认为true，是下面的那个的前提
        mWebSettings.setBuiltInZoomControls(true);//设置内置的缩放控件，若上面是false，则该WebView不可缩放，这个不管设置什么都不能缩放

//        mWebSettings.setDisplayZoomControls(false);//隐藏原生的缩放控件


        //设置自适应屏幕，两者合用
        mWebSettings.setLoadWithOverviewMode(true);//缩放到屏幕的大小
        mWebSettings.setUseWideViewPort(true);//将图片调整到适合WebView的大小

        mWebSettings.setDefaultTextEncodingName("utf-8");
        mWebSettings.setLoadsImagesAutomatically(true);

//        mWebSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);//提高渲染的优先级

//        mWebView.requestFocusFromTouch();//支持获取手势焦点，输入用户名、密码或其他


//        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//支持内容重新布局
//        mWebSettings.supportMultipleWindows();//多窗口
//        mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//关闭WebView中缓存
//        mWebSettings.setAllowFileAccess(true);//设置可以访问文件
//        mWebSettings.setNeedInitialFocus(true);//当WebView调用requestFocus时为WebView设置节点
//        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);//支持通过JS打开新窗口
//        mWebSettings.setLoadsImagesAutomatically(true);//支持自动加载图片
//        mWebSettings.setDefaultTextEncodingName("utf-8");//设置编码格式


        //调用JS方法.安卓版本大于17,加上注解 @JavascriptInterface
        mWebSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new InsertObj(), "jsObj");

        //缓存数据
        saveData(mWebSettings);

        newWin(mWebSettings);

        mWebView.setWebChromeClient(webChromeClient);
        //将自定义的WebViewClient设置给WebView
        mWebView.setWebViewClient(webViewClient);
        mWebView.loadUrl(mUrl);//加载一个网页
//        mWebView.loadUrl("file:///android_asset/test.html");//加载apk包中的一个html页面
//        mWebView.loadUrl("content://com.android.htmlfileprovider/sdcard/test.html");//加载手机本地的一个html页面的方法
    }

    class InsertObj extends Object {
        //给html提供的方法，js中可以通过：var str = window.jsObj.HtmlcallJava(); 获取到
        @JavascriptInterface
        public String HtmlcallJava() {
            return "Html call Java";
        }

        //给html提供的有参函数 ： window.jsObj.HtmlcallJava2("IT-homer blog");
        @JavascriptInterface
        public String HtmlcallJava2(final String param) {
            return "Html call Java : " + param;
        }

        //Html给我们提供的函数
        @JavascriptInterface
        public void JavacallHtml() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //这里是调用方法
                    mWebView.loadUrl("javascript: showFromHtml()");
                    Toast.makeText(Html5Activity.this, "clickBtn", Toast.LENGTH_SHORT).show();
                }
            });
        }

        //Html给我们提供的有参函数
        @JavascriptInterface
        public void JavacallHtml2(final String param) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript: showFromHtml2('IT-homer blog')");
                    Toast.makeText(Html5Activity.this, "clickBtn2", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 多窗口的问题
     */
    private void newWin(WebSettings mWebSettings) {
        //html中的_bank标签就是新建窗口打开，有时会打不开，需要加以下
        //然后 复写 WebChromeClient的onCreateWindow方法
        mWebSettings.setSupportMultipleWindows(true);
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    }

    /**
     * HTML5数据存储
     * 缓存模式
     LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
     LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
     LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
     LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
     */
    private void saveData(WebSettings mWebSettings) {
        //有时候网页需要自己保存一些关键数据,Android WebView 需要自己设置

        if (NetStatusUtil.isConnected(getApplicationContext())) {
            mWebSettings.setCacheMode(WebSettings.LOAD_DEFAULT);//根据cache-control决定是否从网络上取数据。
        } else {
            mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//没网，则从本地获取，即离线加载
        }

        mWebSettings.setDomStorageEnabled(true);//开发DOM storage API功能
        mWebSettings.setDatabaseEnabled(true);//开启database storage API功能
        mWebSettings.setAppCacheEnabled(true);//开发Application Caches功能

        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        mWebSettings.setAppCachePath(appCachePath);//设置Application Caches缓存目录
    }

    //WebViewClient就是帮助WebView处理各种通知，请求事件的
    WebViewClient webViewClient = new WebViewClient() {
        /**
         * 多页面在同一个WebView中打开，就是不新建activity或者调用系统浏览器打开
         *在网页上加载都经过这个方法
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            Log.d("Url:", url);
            return true;
        }

        /**
         *重写此方法才能够处理在浏览器中的按键事件
         * @param view
         * @param event
         * @return
         */
        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }

        /**
         * 这个事件就是开始载入页面调用的，我们可以设定一个loading的页面，告诉用户程序在等待网络响应
         * @param view
         * @param url
         * @param favicon
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        /**
         * 在页面加载结束时调用，同样道理，我们可以关闭loading条，切换程序动作
         * @param view
         * @param url
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        /**
         * 在加载页面资源时会调用，每一个资源（比如图片）的加载都会调用一次
         * @param view
         * @param url
         */
        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        /**
         * 报告错误信息
         * @param view
         * @param errorCode
         * @param description
         * @param failingUrl
         */
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        /**
         * 更新历史记录
         * @param view
         * @param url
         * @param isReload
         */
        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            super.doUpdateVisitedHistory(view, url, isReload);
        }

        /**
         * 应用程序重新请求网页数据
         * @param view
         * @param dontResend
         * @param resend
         */
        @Override
        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            super.onFormResubmission(view, dontResend, resend);
        }

        /**
         * 获取返回信息授权请求
         * @param view
         * @param handler
         * @param host
         * @param realm
         */
        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
        }

        /**
         * 重写此方法可以让WebView处理https请求
         * @param view
         * @param handler
         * @param error
         */
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
        }

        /**
         * WebView发生改变时调用
         * @param view
         * @param oldScale
         * @param newScale
         */
        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
        }

        /**
         * key事件未被加载时调用
         * @param view
         * @param event
         */
        @Override
        public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
            super.onUnhandledKeyEvent(view, event);
        }
    };
    /**
     * WebChromeClient是辅助WebView处理JavaScript的对话框，网站图标，网站title，加载进度等
     */
    WebChromeClient webChromeClient = new WebChromeClient() {
        /**
         * 获得网页的加载进度
         * @param view
         * @param newProgress
         */
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        /**
         * 获取Web页中的title用来设置自己界面中的title
         * 当加载出错的时候，比如无网络，这时onReceivedTitle中获取的标题为“找不到该网页”
         * 因此当触发onReceiveError时，不要使用获取到的title
         * @param view
         * @param title
         */
        @Override
        public void onReceivedTitle(WebView view, String title) {
//            super.onReceivedTitle(view, title);
            Html5Activity.this.setTitle(title);
        }

        /**
         * 处理alert弹出框，html弹框的一种方式
         * @param view
         * @param url
         * @param message
         * @param result
         * @return
         */
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }

        /**
         * 处理confirm弹出框
         * @param view
         * @param url
         * @param message
         * @param defaultValue
         * @param result
         * @return
         */
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        /**
         * 处理prompt弹出框
         * @param view
         * @param url
         * @param message
         * @param result
         * @return
         */
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            return super.onJsConfirm(view, url, message, result);
        }

        //=========HTML5定位==========================================================
        //需要先加入权限
        //<uses-permission android:name="android.permission.INTERNET"/>
        //<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
        //<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>

        /**
         * 获取到网站图标
         * @param view
         * @param icon
         */
        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
        }

        @Override
        public void onGeolocationPermissionsHidePrompt() {
            super.onGeolocationPermissionsHidePrompt();
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback
                callback) {
            callback.invoke(origin, true, false);//注意个函数，第二个参数就是是否同意定位权限，第三个是是否希望内核记住
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }
        //=========HTML5定位==========================================================


        //=========多窗口的问题==========================================================
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebView);
            resultMsg.sendToTarget();
            return true;
        }
        //=========多窗口的问题==========================================================


        @Override
        public void onCloseWindow(WebView window) {
            super.onCloseWindow(window);
        }
    };


    /**
     * goBack()//后退
     goForward()//前进
     goBackOrForward(intsteps) //以当前的index为起始点前进或者后退到历史记录中指定的steps，
     如果steps为负数则为后退，正数则为前进

     canGoForward()//是否可以前进
     canGoBack() //是否可以后退
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();
            //WebView调用destroy时，WebView仍绑定在Activity上，这时由于自定义WebView构建时传入了该Activity的context对象
            //,因此需要先从父容器中移除WebView，然后再销毁WebView
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    //清除缓存数据
    /**
     * clearCache(true);//清除网页访问留下的缓存，由于内核缓存是全局的因此这个方法不仅仅针对webview而是针对整个应用程序.
     clearHistory()//清除当前webview访问的历史记录，只会webview访问历史记录里的所有记录除了当前访问记录.
     clearFormData()//这个api仅仅清除自动完成填充的表单数据，并不会清除WebView存储到本地的数据。
     */

    //WebView的状态
    /**
     * onResume() //激活WebView为活跃状态，能正常执行网页的响应
     onPause()//当页面被失去焦点被切换到后台不可见状态，需要执行onPause动过， onPause动作通知内核暂停所有的动作，比如DOM的解析、plugin的执行、JavaScript执行。

     pauseTimers()//当应用程序被切换到后台我们使用了webview， 这个方法不仅仅针对当前的webview而是全局的全应用程序的webview，它会暂停所有webview的layout，parsing，javascripttimer。降低CPU功耗。
     resumeTimers()//恢复pauseTimers时的动作。

     destroy()//销毁，关闭了Activity时，音乐或视频，还在播放。就必须销毁。
     */


     //判断WebView是否已经滚动到页面底端 或者 顶端:
    /**
     getScrollY() //方法返回的是当前可见区域的顶端距整个页面顶端的距离,也就是当前内容滚动的距离.
     getHeight()或者getBottom() //方法都返回当前WebView这个容器的高度
     getContentHeight()返回的是整个html的高度,但并不等同于当前整个页面的高度,因为WebView有缩放功能,所以当前整个页面的高度实际上应该是原始html的高度再乘上缩放比例.因此,更正后的结果,准确的判断方法应该是：
     if (webView.getContentHeight() * webView.getScale() == (webView.getHeight() + webView.getScrollY())) {
     //已经处于底端
     }

     if(webView.getScrollY() == 0){
     //处于顶端
     }
     */

    //避免WebView内存泄露的一些方式
    /**
     * 1.可以将 Webview 的 Activity 新起一个进程，结束的时候直接System.exit(0);退出当前进程；
     启动新进程，主要代码： AndroidManifest.xml 配置文件代码如下

     <activity
     android:name=".ui.activity.Html5Activity"
     android:process=":lyl.boon.process.web">
     <intent-filter>
     <action android:name="com.lyl.boon.ui.activity.htmlactivity"/>
     <category android:name="android.intent.category.DEFAULT"/>
     </intent-filter>
     </activity>
     在新进程中启动 Activity ，里面传了 一个 Url：

     Intent intent = new Intent("com.lyl.boon.ui.activity.htmlactivity");
     Bundle bundle = new Bundle();
     bundle.putString("url", gankDataEntity.getUrl());
     intent.putExtra("bundle",bundle);
     startActivity(intent);
     然后在 Html5Activity 的onDestory() 最后加上 System.exit(0); 杀死当前进程。

     2.不能在xml中定义 Webview ，而是在需要的时候创建，并且Context使用 getApplicationgContext()，如下代码：

     LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
     mWebView = new WebView(getApplicationContext());
     mWebView.setLayoutParams(params);
     mLayout.addView(mWebView);
     3.在 Activity 销毁的时候，可以先让 WebView 加载null内容，然后移除 WebView，再销毁 WebView，最后置空。
     代码如下：

     @Override
     protected void onDestroy() {
     if (mWebView != null) {
     mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
     mWebView.clearHistory();

     ((ViewGroup) mWebView.getParent()).removeView(mWebView);
     mWebView.destroy();
     mWebView = null;
     }
     super.onDestroy();
     }

     文／Wing_Li（简书作者）
     原文链接：http://www.jianshu.com/p/3fcf8ba18d7f
     著作权归作者所有，转载请联系作者获得授权，并标注“简书作者”。
     */
}