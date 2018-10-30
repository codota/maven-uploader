# Codota maven plugin
Maven plugin for [indexing java/scala code in Codota](https://www.codota.com/index-your-repo)

You can easily send your Java code for indexing by adding this simple maven plugin which collects your artifacts and uploads them to your secure private Codota account.

### Uploading your artifacts for analysis
To submit your code follow these simple steps:
* [Create your private account at Codota](https://www.codota.com/submit-repo/maven)
* Build your project including sources and send to Codota for indexing by running the following command in your project directory
    
    ```
    mvn clean source:jar source:test-jar jar:test-jar package com.codota:codota-uploader:1.1.0:upload -Dcodota.token={your_token_here} -Dcodota.endpoint=https://upload.codota.com/artifacts -DskipAssembly -DskipTests -Dmaven.javadoc.skip=true -Dgpg.skip
    ```
    Also, you have the following optional parameter:
    ```
   -Dcodota.projectPrefix={prefix}
    ```
    Make sure to use the secure token you receive from Codota. Please make sure to keep your token private and secure.
    Indexing usually takes less than an hour - you'll get an email once it's ready

* To permanently add upload to Codota in your build, paste the following in your pom.xml under <build><plugins> - make sure to use the secure token you receive from Codota. Please make sure to keep your token private and secure.
    ```
    <plugin>    
         <groupId>com.codota</groupId>
         <artifactId>uploader</artifactId>
         <version>1.1.0</version>
         <configuration>
             <endpoint>https://upload.codota.com/artifacts</endpoint>
             <token>----------- *put your secure token here* -----------</token>       
         </configuration>      
         <executions>      
             <execution>       
                 <id>codota-upload</id>        
                 <phase>package</phase>        
                 <!-- bind to the packaging phase -->      
                 <goals>       
                     <goal>upload</goal>       
                 </goals>      
             </execution>      
         </executions>     
     </plugin>
     ```

    Build your project including sources and send to Codota for indexing by running the following command in your project directory

    ```
    mvn clean source:jar source:test-jar jar:test-jar package -DskipAssembly -DskipTests
    ```
