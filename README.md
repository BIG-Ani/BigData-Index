## Advanced Big Data Applications and Indexing Techniques

![NEU INFO7255 - BigData&Indexinbg](https://img.shields.io/badge/info7255--v1.0-passing-bri)

![NEU INFO7255 - BigData&Indexinbg](https://img.shields.io/badge/info7255--v2.0-passing-bri)

![NEU INFO7255 - BigData&Indexinbg](https://img.shields.io/badge/info7255--v3.0-passing-bri)

### [Final System Architecture Diagram](diagram.png)



### Northeastern University - 2020

repo of INFO-7255 course project



### Prerequisite

For this course, I developed the backend REST API, by utilizing

- **Spring Boot**
- **Redis**
- **Json Schema Validator**
- **Google Oauth 2.0**
- **ElasticSearch**
- **Kibana**
- **Kafka**
- **Zookeeper**

### Introduction

This project contains two services, which integrated Kafka to decouple restapi service and elastic service

- First is producer, the RESTful API service, which can handle the requests from url, persist data in Redis
- Kafka, the message queue, can receive the message from producer and consumer can retrieve message and excute
- Second is consumer, which is mainly responsible for the elastic search side

### How to Use

1. start the services, including redis, elastic search, kibana, zookeeper, kafka;
2. start the SpringBoot applitions, the restful api service and elastic search ops service;
3. test the work flow, through Postman and Kibana Dev Tools
   1. Postman
      - E-tag test
      - Token test, choose Auth 2.0 and send request to Google Token Server; then, use the id_token to replace the evn var {google-id_token}
      - CRUD/ GET/ POST/ PATCH/ PUT/ DELETE 
   2. Elastic Search/ Kibana
      - CRUD
      - Get object with specific conditions

**** Run the [start.sh](start.sh) to start all the necessary services

### Tools

#### [Elastic Search](https://www.elastic.co/guide/en/kibana/current/index.html)

To install with Homebrew, you first need to tap the Elastic Homebrew repository:

```sh
brew tap elastic/tap
```

Once you’ve tapped the Elastic Homebrew repo, you can use `brew install` to install the default distribution of Elasticsearch:

```sh
brew install elastic/tap/elasticsearch-full

## start elastic search
$./elasticsearch-full/bin/elasticsearch

## stop elastic search
$ps
$kill -9 PID(elastic search)
```

[**Java REST Client**](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/index.html)



#### [Kibana](https://www.elastic.co/guide/en/kibana/current/index.html)

To install with Homebrew, you first need to tap the Elastic Homebrew repository:

```sh
brew tap elastic/tap
```

Once you’ve tapped the Elastic Homebrew repo, you can use `brew install` to install the default distribution of Kibana:

```sh
brew install elastic/tap/kibana-full

## start kibana
$./kibana/bin/kibana

## stop kibana
$CTRL + C
```



#### [Kafka](https://kafka.apache.org/quickstart)

install Java: `brew cask install java`

```sh
brew install kafka
```

revise kafka config for standalone mode - `/usr/local/etc/kafka/server.properties`

```
############################# Socket Server Settings #############################
# The address the socket server listens on. It will get the value returned from 
# java.net.InetAddress.getCanonicalHostName() if not configured.
#   FORMAT:
#     listeners = listener_name://host_name:port
#   EXAMPLE:
#     listeners = PLAINTEXT://your.host.name:9092
listeners=PLAINTEXT://localhost:9092
```

##### kafka command

```sh
brew services start kafka

brew services stop kafka		
```



#### [Zookeeper](https://zookeeper.apache.org/) 

```sh
brew install zookeeper		
```

**note: zookeeper's jetty takes up the 8080 port, you may config the `/usr/local/etc/zookeeper/zoo.cfg`*， by adding `admin.serverPort=9000`



##### zoo command

```sh
brew services start zookeeper

brew services stop zookeeper	

## check zookeeper status
$ zkSever status
```



### Features

1. ##### Demo 1

   - Develop a Spring Boot Web service and implement ***GET***, ***POST***, ***DELET*** method
   - Configure the noSQL, validate the post json schema and save content in Redis

2. ##### Demo 2

   - Create the token service to authorize the REST API operation, by utilizing ***Google Oauth 2.0*** as the token server
   - Add the ***PUT*** and ***PATCH*** to implement the resource update and json-patch update

3. **Demo 3**
   - Add the search API by utilizing the **elastic search** - additional data index for the main restAPI-Database flow
   - Add the **kafka** **breaker** for the elastic search requests' asynchnouse processing