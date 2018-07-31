(English instruction please see [README-ENGLISH.md](README-ENGLISH.md) )  
## jDbPro  
开源协议: [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)  

jDbPro是一个建立于Apache Commons DbUtils上，并对其增强了动态SQL功能的JDBC持久层工具，它是一个承上(包装JDBC，支持多种SQL写法)启下(作为ORM项目内核)的项目，但它本身也是一个独立的工具，可以单独使用，其运行环境为Java6或以上。  
作为ORM项目的内核，jDbPro仅关注于改进JDBC操作的易用性，它不考虑对象映射、关联映射、跨数据库、分页等高级功能，这些高级功能属于ORM工具如jSqlBox负责的范畴。jSqlBox的设计理念是尽量将每个功能点设计成独立的小项目，隔离它们的相互依赖性，每个小项目都可以单独使用，整合在一起就成了jSqlBox，这与Hibernate之类将所有功能都捆绑在一起、不能单独使用的持久层工具是不同的。目前在这一理念下已经开发的工具项目有：  
1)jDialects，这是一个支持70多种方言的SQL分页、DDL支持、JPA支持工具，用于解决利用JDBC工具进行跨数据库开发的问题。  
2)jTransactions，这是一个将声明式事务作为单独的项目提供的小工具，目前包含一个微型实现TinyTx，并支持配置成使用Spring的声明式事务。  
3)jBeanBox，这是一个微型IOC/AOP工具，如果不想用笨重的Spring-Ioc,可以用它来替代。
4)jDbPro，即本项目，支持多种SQL风格，即可单独使用，也作为ORM项目jSqlBox的内核存在。  
5)jSqlBox，这是一个整合了上述子项目的ORM工具，除了拥有jDbPro的所有功能并与DbUtils兼容之外，主要增加了实体的CURD存取、实体关联查询功能。得益于jDbPro做了大量底层工作，jSqlBox项目只用了区区30多个类就构成了一个完整的ORM工具。

### 如何引入jDbPro到项目? 
在项目的pom.xml文件中加入如下行：  
```
   <dependency>  
      <groupId>com.github.drinkjava2</groupId>  
      <artifactId>jdbpro</artifactId>  
      <version>2.0.2</version> <!--或Maven最新版-->
   </dependency>
``` 
如果没有特殊理由（例如想开发自已的ORM工具)，一般不建议直接使用jDbPro，而应使用jSqlBox，因为后者功能更全。
jDbPro依赖于DbUtils, 如果使用Maven还将会自动下载对应其主版本号的DbUtils包commons-dbutils-1.7.jar。   

### 使用 
从2.0.2版本开始，jDbPro将不再有自已的说明文档，因为它的功能已在jSqlBox中的用户手册中有介绍，其中n、i、p、t、INLINE系列方法皆为jSqlBox从DbPro中继承过去的。
以下两行是个简短的示例，更多使用方式见jSqlBox用户手册。
```
DbPro dbPro = new DbPro(someDataSource);  
dbPro.nExecute("update users set name=?, address=?", "Sam", "Canada");
```