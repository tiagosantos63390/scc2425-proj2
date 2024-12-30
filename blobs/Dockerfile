FROM tomcat:10.0-jdk17-openjdk
WORKDIR /usr/local/tomcat
ADD target/tukano-1.war webapps/blobservice.war
EXPOSE 8081
ENV BLOB_STORAGE_ROOT=/data
# Mount a persistent volume at /data
VOLUME /data

