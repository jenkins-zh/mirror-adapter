```shell script
openssl genrsa -out rootCA/mirror-adapter.key 1024
openssl req -new -x509 -days 1095 -key rootCA/mirror-adapter.key \
    -out rootCA/mirror-adapter.crt \
    -subj "/C=CN/ST=GD/L=SZ/O=vihoo/OU=dev/CN=demo.com/emailAddress=admin@jenkins-zh.com"
```

```shell script
mvn clean package appassembler:assemble
chmod u+x ./target/appassembler/bin/app
rm mirror/update-center.json
./target/appassembler/bin/app -connection-check-url https://www.baidu.com/ \
  -mirror-json mirror/update-center.json -mirror-url https://mirrors.tuna.tsinghua.edu.cn/jenkins/ \
  -official-json update-center.json \
  -key /Users/rick/Workspace/GitHub/jenkins-zh/mirror-adapter/rootCA/mirror-adapter.key \
  -certificate /Users/rick/Workspace/GitHub/jenkins-zh/mirror-adapter/rootCA/mirror-adapter.crt \
  -root-certificate /Users/rick/Workspace/GitHub/jenkins-zh/mirror-adapter/rootCA/mirror-adapter.crt
rm /Users/rick/Downloads/apache-tomcat-7.0.96/webapps/test/update-center.json
cp mirror/update-center.json /Users/rick/Downloads/apache-tomcat-7.0.96/webapps/test
cp /Users/rick/Workspace/GitHub/jenkins-zh/mirror-adapter/rootCA/mirror-adapter.crt \
  /Users/rick/.jenkins/war/WEB-INF/update-center-rootCAs
```