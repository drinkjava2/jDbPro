package text;

import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.jdbpro.Handler;
import com.github.drinkjava2.jdbpro.Pretreat;
import com.github.drinkjava2.jdbpro.SQL;

/*-
 * This is a sample to show let Java support multiple lines String, this is a
 * Java file, and also is a resource file, so can read the source code as normal
 * resource file. only thing need do is to put a build-helper-maven-plugin
 * plugin in pom.xml: 
 
<build> 
     <plugins>
      ......

      <plugin>
       <groupId>org.codehaus.mojo</groupId>
       <artifactId>build-helper-maven-plugin</artifactId>
       <version>1.7</version>
       <executions>
         <execution>
           <id>add-source</id>
           <phase>generate-sources</phase>
           <goals>
             <goal>add-source</goal>
           </goals>
           <configuration>
             <sources>
               <source>src/main/resources</source>
               <source>src/test/resources</source>
             </sources>
           </configuration>
         </execution>
       </executions>
     </plugin> 
     ......

     </plugins>
</build>    

* @author Yong Zhu
* @since 1.7.0
*/
public interface TextSample1 {
	@SQL("select * from User")
	public <T> T retrieveAllUsers();

	@Handler(MapListHandler.class)
	@Pretreat(MapListHandler.class)
	@SQL("select * from User where id=#{id} and age>#{age}")
	public <T> T retrieveUserById(int id, int age);
	/*-
	 SELECT P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME 
	 P.LAST_NAME,P.CREATED_ON, P.UPDATED_ON  
	 FROM PERSON P, ACCOUNT A 
	 INNER JOIN DEPARTMENT D on D.ID = P.DEPARTMENT_ID  
	 INNER JOIN COMPANY C on D.COMPANY_ID = C.ID 
	 WHERE (P.ID = A.ID AND P.FIRST_NAME like ?) 
	 OR (P.LAST_NAME like ?)  
	 GROUP BY P.ID  
	 HAVING (P.LAST_NAME like ?)  
	 OR (P.FIRST_NAME like ?)  
	 ORDER BY P.ID, P.FULL_NAME
	 */
}
