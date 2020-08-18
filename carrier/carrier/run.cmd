set JAVA_HOME="C:\Program Files\RedHat\java-11-openjdk-11.0.8-2"
%JAVA_HOME%\bin\java -jar --add-opens java.base/jdk.internal.misc=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true -Dserver=Server1 carrier-0.0.1-SNAPSHOT.jar
%JAVA_HOME%\bin\java -jar --add-opens java.base/jdk.internal.misc=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true -Dserver.port=8171 -Dserver=Server2 carrier-0.0.1-SNAPSHOT.jar
java -jar -Dio.netty.tryReflectionSetAccessible=true --add-opens java.base/jdk.internal.misc=ALL-UNNAMED --illegal-access=warn carrier-0.0.1-SNAPSHOT.jar
