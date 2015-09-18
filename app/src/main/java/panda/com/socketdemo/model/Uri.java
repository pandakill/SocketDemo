package panda.com.socketdemo.model;

import panda.com.socketdemo.utils.UrlUtil;

/**
 * URI类,存有协议、服务器地址、端口、url等信息
 *
 * Created by Administrator on 2015/9/18:12:53.
 */
public class Uri {
    String uriStr;
    String agreement;
    String host;
    int port;
    String url;

    public Uri (String uri) {
        uriStr = uri;
        resolveUri();
    }

    public String getAgreement() {
        return agreement;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUrl() {
        return url;
    }

    /**
     * 针对传入的uri进行处理,从uri中获取相应的服务器地址等信息
     */
    private void resolveUri() {
        UrlUtil util = new UrlUtil(uriStr);
        agreement = util.getAgreement();
        host = util.getHost();
        url = util.getAddress();
        port = util.getPort();
    }
}
