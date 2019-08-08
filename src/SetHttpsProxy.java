public class SetHttpsProxy {
    private String httpsProxyHost, httpsProxyPort;

    public SetHttpsProxy(String httpsProxyHost, String httpsProxyPort){
        this.httpsProxyHost = httpsProxyHost;
        this.httpsProxyPort = httpsProxyPort;
    }

    public void on(){
        if (httpsProxyHost != null && httpsProxyPort != null){
            System.setProperty("https.proxyHost", httpsProxyHost);
            System.setProperty("https.proxyPort", httpsProxyPort);
        }
    }

    public void off(){
        if (httpsProxyHost != null && httpsProxyPort != null){
            System.setProperty("https.proxyHost", "");
            System.setProperty("https.proxyPort", "");
        }
    }
}
