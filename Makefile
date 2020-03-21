build:
	mvn package appassembler:assemble

json:
	./target/appassembler/bin/app -connection-check-url https://www.baidu.com/ \
		-mirror-json mirror/update-center.json -mirror-url https://mirrors.tuna.tsinghua.edu.cn/jenkins/ \
		-official-json update-center.json \
		-key /Users/rick/Workspace/GitHub/jenkins-zh/mirror-adapter/rootCA/mirror-adapter.key \
		-certificate /Users/rick/Workspace/GitHub/jenkins-zh/mirror-adapter/rootCA/mirror-adapter.crt \
		-root-certificate /Users/rick/Workspace/GitHub/jenkins-zh/mirror-adapter/rootCA/mirror-adapter.crt

toolJSON:
	chmod u+x ./target/appassembler/bin/app
	./target/appassembler/bin/app -tools -connection-check-url https://www.baidu.com/ \
		-mirror-json mirror/hudson.tools.JDKInstaller.json.html -mirror-url https://mirrors.tuna.tsinghua.edu.cn/jenkins/ \
		-official-json hudson.tools.JDKInstaller.json.html \
		-key /Users/rick/Workspace/GitHub/jenkins-zh/mirror-adapter/rootCA/mirror-adapter.key \
		-certificate /Users/rick/Workspace/GitHub/jenkins-zh/mirror-adapter/rootCA/mirror-adapter.crt \
		-root-certificate /Users/rick/Workspace/GitHub/jenkins-zh/mirror-adapter/rootCA/mirror-adapter.crt

get-json:
	rm -rfv update-center.json
	wget https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/update-center.json

get-tools-json:
	rm -rfv hudson.tools.JDKInstaller.json.html
	wget https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/current/updates/hudson.tools.JDKInstaller.json.html

debug:
	cp mirror/update-center.json /Users/rick/Downloads/apache-tomcat-7.0.96/webapps/test
	cp /Users/rick/Workspace/GitHub/jenkins-zh/mirror-adapter/rootCA/mirror-adapter.crt \
		/Users/rick/.jenkins/war/WEB-INF/update-center-rootCAs

image:
	docker build . -t docker.pkg.github.com/jenkins-zh/mirror-adapter/mirror-adapter:0.0.1

push-image:
	docker push docker.pkg.github.com/jenkins-zh/mirror-adapter/mirror-adapter:0.0.1