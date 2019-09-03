(English instruction please see [README-ENGLISH.md](README-ENGLISH.md) )
## jDbPro
Open source agreement: [Apache 2.0] (http://www.apache.org/licenses/LICENSE-2.0)

jDbPro is a JDBC persistence layer tool built on Apache Commons DbUtils and enhanced with dynamic SQL functionality. It is a project (packaging JDBC, supporting multiple SQL scripts) (as the ORM project kernel). But it is also a stand-alone tool that can be used alone, and its operating environment is Java6 or above.
As the kernel of the ORM project, jDbPro only focuses on improving the ease of use of JDBC operations. It does not consider advanced functions such as object mapping, association mapping, cross-database, paging, etc. These advanced functions belong to the scope of ORM tools such as jSqlBox. jSqlBox is designed to design each function point as a separate small project, to isolate their interdependence, each small project can be used separately, integrated into jSqlBox, which will be all functions like Hibernate Persistence layer tools that are bundled together and cannot be used separately are different. The tool projects that have been developed under this concept are:  

1. jDialects, which is a SQL paging, DDL support, and JPA support tool that supports more than 70 dialects. It is used to solve the problem of cross-database development using JDBC tools.  
2. jTransactions, a small tool that provides declarative transactions as a separate project, currently includes a tiny implementation of TinyTx and supports declarative transactions configured to use Spring.  
3. jBeanBox, this is a miniature IOC/AOP tool, if you don't want to use the bulky Spring-Ioc, you can use it instead.  
4. jDbPro, the project, supports a variety of SQL styles, can be used alone, also as the kernel of the ORM project jSqlBox.  
5. jSqlBox, which is an ORM tool that integrates the above sub-projects. In addition to having all the functions of jDbPro and being compatible with DbUtils, it mainly adds the CURD access and entity association query functions of the entity. Thanks to jDbPro doing a lot of low-level work, the jSqlBox project uses only more than 30 classes to form a complete ORM tool.  

### How do I introduce jDbPro to a project?
Add the following line to the project's pom.xml file:
```
   <dependency>
      <groupId>com.github.drinkjava2</groupId>
      <artifactId>jdbpro</artifactId>
      <version>3.0.0</version> <!-- or Maven latest version -->
   </dependency>
```
If there is no special reason (for example, if you want to develop your own ORM tool), it is generally not recommended to use jDbPro directly, but instead use jSqlBox because the latter is more complete.
jDbPro relies on DbUtils. If you use Maven, the DbUtils package commons-dbutils-1.7.jar corresponding to its major version number will be automatically downloaded.

### Use
Starting with version 3.0.0, jDbPro will no longer have its own documentation, as its functionality has been introduced in the user manual of jSqlBox, where n, i, p, t, and INLINE series methods are all inherited from DbPro。
The following two lines are short examples. For more usuages, see jSqlBox user manual.
```
DbPro dbPro = new DbPro(someDataSource);
dbPro.nExecute("update users set name=?, address=?", "Sam", "Canada");
```