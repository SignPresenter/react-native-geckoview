package cn.reactnative;
import org.mozilla.geckoview.GeckoSession;

public class GeckoPermissionDelegate implements GeckoSession.PermissionDelegate {
    @Override
    public void onContentPermissionRequest(final GeckoSession session, final String uri, final int type, final GeckoSession.PermissionDelegate.Callback callback) {
        callback.grant();
    }
}
