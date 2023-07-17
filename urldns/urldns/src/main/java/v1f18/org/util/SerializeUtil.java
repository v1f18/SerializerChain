package v1f18.org.util;

import java.io.*;
import java.lang.reflect.Field;

public class SerializeUtil {
    public SerializeUtil(String file_name) {
        this.file_name = file_name;
    }

    private static String file_name = "obj";

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public SerializeUtil() {
    }

    public static void writeObjectToFile(Object data) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file_name);
        ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
        outputStream.writeObject(data);
        outputStream.close();
    }

    public static void readObjectToFile() throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(file_name);
        ObjectInputStream inputStream = new ObjectInputStream(fileInputStream);
        inputStream.readObject();
    }

    public static ByteArrayOutputStream writeObjectToByteArray(Object data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
        outputStream.writeObject(data);
        outputStream.close();
        return byteArrayOutputStream;
    }
    public static void  readObjectToByteArray(ByteArrayOutputStream byteArrayOutputStream) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream);
        inputStream.readObject();

    }
    public static void setFieldValue(Object obj,String name,Object value) throws NoSuchFieldException, IllegalAccessException {
        Field iTransformers = obj.getClass().getDeclaredField(name);
        iTransformers.setAccessible(true);
        iTransformers.set(obj,value);
    }
}