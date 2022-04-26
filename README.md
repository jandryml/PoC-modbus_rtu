

record
    maven - config local repo, generate path for external jar
    add rxtx (ddl, jar) to jdk
    cmd args 



use

java -jar modbus-rtu-tool-jar-with-dependencies.jar -p COM12 -t REGISTER -a 1 2


mvn install:install-file -Dfile=EasyModbusJava.jar -DgroupId=de.re -DartifactId=easymodbus.modbusclient -Dversion=1.0 -Dpackaging=jar -DlocalRepositoryPath=libs -DcreateChecksum=true