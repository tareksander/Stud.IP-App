package org.studip.unofficial_app.api;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;

public class OAuthUtils
{
    public static final String request_token_url = "/dispatch.php/api/oauth/request_token";
    public static final String access_token_url = "/dispatch.php/api/oauth/access_token";
    public static final String authorize_url = "/dispatch.php/api/oauth/authorize";
    
    
    public static final Map<String, OAuthData> hosts;
    static {
        HashMap<String, OAuthData> h = new HashMap<>();
        h.put("studip.uni-osnabrueck.de", 
                new OAuthData("2a45f0453b48d7424e342ead5314ee80", "f73e979bb772707c4d4627458fb1ed89060b3cb64"));
        
        hosts = Collections.unmodifiableMap(h);
    }
    
    private static final SecureRandom noncegen = new SecureRandom();
    
    
    public static Call<String> requestToken(API api) {
        OAuthData o = hosts.get(api.getHostname());
        if (o == null) {
            return null;
        }
        return api.oauth.requestToken(getAuthHeader(api, o, null, request_token_url, "POST", null));
    }
    
    public static Call<String> accessToken(API api, OAuthToken tmp) {
        OAuthData o = hosts.get(api.getHostname());
        if (o == null) {
            return null;
        }
        return api.oauth.accessToken(getAuthHeader(api, o, tmp, access_token_url, "POST", null));
    }
    
    public static String getAuthHeader(API api, OAuthData o, OAuthToken t, String url, String method, TreeMap<String, String> query_params) {
        String timestamp = String.valueOf(System.currentTimeMillis()/1000);
        String nonce = getNonce(30);
    
        TreeMap<String, String> fields = new TreeMap<>();
        if (query_params != null) {
            fields.putAll(query_params);
        }
        fields.put("OAuth realm", "Stud.IP");
        fields.put("oauth_consumer_key", o.consumer_key);
        if (t != null) {
            fields.put("oauth_token", t.oauth_token);
        }
        fields.put("oauth_consumer_callback", "studip_app://unofficial_app/oauth_callback");
        fields.put("oauth_signature_method", "HMAC-SHA1");
        fields.put("oauth_timestamp", timestamp);
        fields.put("oauth_nonce", nonce);
        fields.put("oath_version", "1.0");
        fields.put("oauth_signature", Base64.encodeToString(getSignature(api, o, fields, t, url, method), Base64.DEFAULT));
        
        
        //System.out.println(fieldsToString(fields));
        
        return fieldsToString(fields);
    }
    
    public static String fieldsToString(TreeMap<String, String> fields) {
        try {
            StringBuilder params = new StringBuilder();
            for (Map.Entry<String, String> p : fields.entrySet()) {
                params.append(p.getKey());
                params.append("=\"");
                params.append(URLEncoder.encode(p.getValue(), "UTF-8"));
                params.append("\",");
            }
            params.deleteCharAt(params.length() - 1);
            return params.toString();
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    
    public static byte[] getSignature(API api, OAuthData o, TreeMap<String, String> fields, OAuthToken t, String url, String method) {
        try {
            Mac mac = Mac.getInstance("HMAC-SHA1");
            String key = URLEncoder.encode(o.consumer_secret, "UTF-8") + "&";
            if (t != null) {
                key += URLEncoder.encode(t.oauth_token_secret, "UTF-8");
            }
            mac.init(new SecretKeySpec(key.getBytes(), "HMAC-SHA1"));
            
            StringBuilder text = new StringBuilder();
            
            text.append(method);
            text.append("&");
            text.append(URLEncoder.encode("https://" + api.getHostname() + url, "UTF-8"));
            text.append("&");
            
            
            StringBuilder params = new StringBuilder();
            for (Map.Entry<String, String> p : fields.entrySet()) {
                if ("oauth_signature".equals(p.getKey()) || "OAuth realm".equals(p.getKey())) {
                    continue;
                }
                params.append(URLEncoder.encode(p.getKey(), "UTF-8"));
                params.append("=");
                params.append(URLEncoder.encode(p.getValue(), "UTF-8"));
                params.append("&");
            }
            params.deleteCharAt(params.length() - 1);
            
            text.append(URLEncoder.encode(params.toString(), "UTF-8"));
            //System.out.println("Signature base: "+text);
            return mac.doFinal(text.toString().getBytes());
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            return null;
        }
    }
    
    public static String getNonce(int length) {
        final String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder b = new StringBuilder();
        for (int i = 0;i<length;i++) {
            b.append(alphabet.charAt(noncegen.nextInt(alphabet.length())));
        }
        return b.toString();
    }
    
    public static OAuthToken getTokenFromResponse(String response, boolean temp) {
        String[] fields = response.split("&");
        String token = null, secret = null;
        for (String f : fields) {
            String[] parts = f.split("=");
            if (parts.length == 2 && "oauth_token".equals(parts[0])) {
                token = parts[1];
            }
            if (parts.length == 2 && "oauth_token_secret".equals(parts[0])) {
                secret = parts[1];
            }
        }
        if (token == null || secret == null) {
            return null;
        }
        return new OAuthToken(token, secret, temp);
    }
    
    public static class OAuthData {
        public final String consumer_secret;
        public final String consumer_key;
        public OAuthData(String consumer_secret, String consumer_key) {
            this.consumer_secret = consumer_secret;
            this.consumer_key = consumer_key;
        }
    }
    
    public static class OAuthToken {
        public final String oauth_token;
        public final String oauth_token_secret;
        public final boolean isTemp;
        public OAuthToken(String oauth_token, String oauth_token_secret, boolean isTemp) {
            this.oauth_token = oauth_token;
            this.oauth_token_secret = oauth_token_secret;
            this.isTemp = isTemp;
        }
    }
}