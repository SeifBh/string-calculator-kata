diff --git a/pom.xml b/pom.xml
index aeda49e..31cc441 100644



--- a/pom.xml
+++ b/pom.xml
@@ -15,6 +15,12 @@
             <version>4.13</version>



             <scope>test</scope>



        </dependency>



+        <dependency>



+            <groupId>org.projectlombok</groupId>



+            <artifactId>lombok</artifactId>



+            <version>1.18.30</version>



+            <scope>provided</scope>



+        </dependency>



        </dependencies>




     <properties>



diff --git a/src/main/java/ChatRequest.java b/src/main/java/ChatRequest.java
new file mode 100644



index 0000000..c01b71d
--- /dev/null



+++ b/src/main/java/ChatRequest.java
@@ -0,0 +1,27 @@
+import lombok.*;



+



+import java.util.List;



+



+/**
 


 + * @author madhankumar
 


 + */



+@Setter



+@Getter



+@ToString



+@Data



+@Builder



+@NoArgsConstructor



+@AllArgsConstructor



+public class ChatRequest {
    


+    private String model;
    


+    private List<?> messages;
    


+    private int n;
    


+    private double temperature;
    


+
    


+    public boolean newMethod(String st1, String st2) {
                +        if (st1 == st2) {
            


                    +            return true;
            


                    +        }
                +        return false;
                +    }
    


+}