一些java的反序列化链子的分析

Commoncollections:

- cc1:TransformedMap
- cc2:LazyMap
- cc3:TrAXFilter
- cc4:PriorityQueue
- cc5:TemplatesImpl
- cc6HashMap
- cc7Hashtable
- cc11(cc6变种)

shiro-shiro-root-1.2.4:

- shiro550的测试环境和代码

大家在测试debug的时候,有时候idea的调试器会造成干扰,比如调试cc6的时候,lazyMap的get方法会被调试器提前执行,导致map中有缓存,后面调试到get方法的时候就不会进入到漏洞触发点了

修改以下设置即可:

![](https://github.com/v1f18/SerializerChain/blob/main/img/631355-20190429141045413-173558177.png)

把这两个选项关掉