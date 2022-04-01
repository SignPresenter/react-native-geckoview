package cn.reactnative;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebExtension;
import org.mozilla.geckoview.WebExtensionController;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

public class GeckoViewManager extends SimpleViewManager<View> {
    private static WebExtension.Port mPort;
    public static final String REACT_CLASS = "GeckoView";
    private static GeckoRuntime mGeckoRuntime = null;

    protected static final String HTML_ENCODING = "UTF-8";
    protected static final String HTML_MIME_TYPE = "text/html";
    protected static final String JAVASCRIPT_INTERFACE = "ReactNativeWebView";
    protected static final String HTTP_METHOD_POST = "POST";
    // Use `webView.loadUrl("about:blank")` to reliably reset the view
    // state and release page resources (including any running JavaScript).
    protected static final String BLANK_URL = "about:blank";

    /*
    if (sRuntime == null) {
        GeckoRuntimeSettings settings =
                new GeckoRuntimeSettings.Builder().remoteDebuggingEnabled(true).build();
        sRuntime = GeckoRuntime.create(this, settings);
    }*/

    @Override
    public String getName() {
        return REACT_CLASS;
    }


    @Override
    public View createViewInstance(ThemedReactContext c) {
        GeckoView view = new GeckoView(c);
        GeckoSession session = new GeckoSession();
        session.setPermissionDelegate(new GeckoPermissionDelegate());
        if (mGeckoRuntime == null) {
            GeckoRuntimeSettings.Builder builder = new GeckoRuntimeSettings.Builder();
            builder.autoplayDefault(GeckoRuntimeSettings.AUTOPLAY_DEFAULT_ALLOWED);
            builder.javaScriptEnabled(true);
            //builder.configFilePath("./geckoview2.yaml");


            mGeckoRuntime = GeckoRuntime.create(c, builder.build());
            mGeckoRuntime.getSettings().setRemoteDebuggingEnabled(true);
            installExtension();

            //mGeckoRuntime.setWebNotificationDelegate();
        }
        session.open(mGeckoRuntime);
        view.setSession(session);
        view.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));


        return view;
    }

    @ReactProp(name = "injectedJS")
    public void setInjectedJS(GeckoView view, @Nullable ReadableMap injectedJS) {
        if (injectedJS.hasKey("js")) {
            String js = injectedJS.getString("js");
            evaluateJavascript(js);
        }
    }

    @ReactProp(name = "source")
    public void setSource(GeckoView view, @Nullable ReadableMap source) {
        GeckoSession session = view.getSession();
        if (source != null) {

            if (source.hasKey("html")) {
                String html = source.getString("html");
//                String baseUrl = source.hasKey("baseUrl") ? source.getString("baseUrl") : "";
                session.loadString(html, HTML_MIME_TYPE);
                return;
            }
            if (source.hasKey("uri")) {
                String url = source.getString("uri");
                session.loadUri(url);
                return;
            }
        }
        session.loadUri(BLANK_URL);
    }

    void installExtension() {
        WebExtensionController controller = mGeckoRuntime.getWebExtensionController();
        WebExtension ext = new WebExtension("resource://android/assets/messaging/");
        GeckoResult result = mGeckoRuntime.registerWebExtension(ext);
        result.accept(
                extension -> {
                    Log.i("MessageDelegate", "Extension installed: " + extension);

                },
                e -> Log.e("MessageDelegate", "Error registering WebExtension")
        );
        if (ext!=null) ext.setMessageDelegate(mMessagingDelegate, "browser");
    }

    private final WebExtension.MessageDelegate mMessagingDelegate = new WebExtension.MessageDelegate() {

        @Nullable
        @Override
        public void onConnect(@NonNull WebExtension.Port port) {
            Log.e("MessageDelegate", "onConnect");
            mPort = port;
            mPort.setDelegate(mPortDelegate);
        }
    };

    private final WebExtension.PortDelegate mPortDelegate = new WebExtension.PortDelegate() {
        @Override
        public void onPortMessage(final @NonNull Object message,  final @NonNull WebExtension.Port port) {
            Log.e("MessageDelegate", "Received message from extension: "  + message);
            try {
                if (message instanceof JSONObject) {
                    Log.e("MessageDelegate", "Received JSONObject");
                    JSONObject jsonObject = (JSONObject) message;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnect(final @NonNull WebExtension.Port port) {
            Log.e("MessageDelegate:", "onDisconnect");
            if (port == mPort) {
                mPort = null;
            }
        }
    };

    public void evaluateJavascript(String javascriptString) {
        try {
            long id = System.currentTimeMillis();
            Log.e("evalJavascript:id:", id + "");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "evalJavascript");
            jsonObject.put("data", javascriptString);
            jsonObject.put("id", id);
            Log.e("evalJavascript:", jsonObject.toString());
            mPort.postMessage(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}
