mvn -f ../pom.xml clean package

#cp ~/.m2/repository/com/cloopen/java-sms-sdk/1.0.3/java-sms-sdk-1.0.3.jar ../../build/
#cp ~/.m2/repository/com/squareup/okhttp3/okhttp/3.14.2/okhttp-3.14.2.jar ../../build/
#cp ~/.m2/repository/com/squareup/okio/okio/1.17.2/okio-1.17.2.jar ../../build/
#cp ../docker/docker/Dockerfile target/

docker build -t coopersoft/keycloak-phone-yuntongxun:snapshot ../target/

#run as remote server -pr?
scp -r keycloak root@192.168.1.21:/home/docker/
docker save -o ~/Downloads/keycloak-phone.tar coopersoft/keycloak-phone-yuntongxun:snapshot
scp ~/Downloads/keycloak-phone.tar root@192.168.1.21:/home/docker/keycloak/
scp remote-run.sh root@192.168.1.21:/home/docker/keycloak/run.sh

