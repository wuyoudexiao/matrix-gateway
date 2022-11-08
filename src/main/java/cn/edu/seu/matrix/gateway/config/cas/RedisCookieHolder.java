package cn.edu.seu.matrix.gateway.config.cas;

public class RedisCookieHolder implements CookieHolder{

    @Override
    public Object getAttr(String userKey, String key) {
        return null;
    }

    @Override
    public void setAttr(String userKey, String key, Object attr) {

    }
}
