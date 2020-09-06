# flink

## Summary

simple flink v1.11.1 demo

### Main Techs

zookeeper:3.4.6

kafka:0.10.0.0

flink:1.11.1

elasticsearch:6.4.3



## How to Run

### pull kafka image

```shell
sudo docker pull wurstmeister/zookeeper:3.4.6
sudo docker pull wurstmeister/kafka:0.10.0.0
```

### boot kafka

#### 1. boot zookeeper:3.4.6

```shell
sudo docker run -d --name zookeeper --publish 2181:2181 --volume /etc/localtime:/etc/localtime wurstmeister/zookeeper:3.4.6
```

#### 2. boot kafka:0.10.0.0

```shell
sudo docker run -d --name kafka --publish 9092:9092 --link zookeeper --env KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 --env KAFKA_ADVERTISED_HOST_NAME=10.0.2.15 --env KAFKA_ADVERTISED_PORT=9092 --volume /etc/localtime:/etc/localtime wurstmeister/kafka:0.10.0.0
```

### test kafka

#### 1. producer

```shell
sudo docker exec -it <container_id> /bin/bash
cd /opt/kafka_version_num/bin
./kafka-console-producer.sh --broker-list 10.0.2.15:9092 --topic flinktest
```

#### 2. consumer

```shell
sudo docker exec -it <container_id> /bin/bash
cd /opt/kafka_version_num/bin
./kafka-console-consumer.sh --zookeeper 172.17.0.2:2181 --bootstrap-server 10.0.2.15:9092 --topic flinktest --from-beginning
```

### run flink standalone

```shell
sudo docker network create flink
sudo docker run -itd --rm --name flink-jm -h flink-jm -p 8081:8081  --network flink flink jobmanager
sudo docker run -itd --rm --name flink-tm -h flink-tm -e JOB_MANAGER_RPC_ADDRESS=flink-jm --network flink flink taskmanager
```

#### test flink

```shell
access url:localhost:8081 in web browser, you'll see apache flink dashboard
```

### elasticsearch

```shell
sudo docker run -d  --name es -e "discovery.type=single-node" -p 9200:9200 -p 9300:9300 --network flink elasticsearch:6.4.3
```

#### test elasticsearch

```url
http://localhost:9200/flink/_search
flink：es index
_search：api
```

### Make Jar file

```
mvn -X clean package assembly:single
```

### Submit Jar file



## Problems encountered

### 1. StreamTableEnvironment moved to a different package

```shell
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
```

### 2.import org.apache.flink.table.descriptors.Kafka no longer exist

Used the new way provided on the offical website to establish connection

### 3.--zookeeper is a required field when starting a consumer in Kafka 0.10.0.0

```
./kafka-console-consumer.sh --zookeeper 172.17.0.2:2181
```

### 4.flink-table-planner conflict

```
<dependency>
  <groupId>org.apache.flink</groupId>
  <artifactId>flink-table-planner_${flink.scala.version}</artifactId>
  <version>1.11.1</version>
  <scope>provided</scope>
</dependency>
```

### 5.Flink web UI not displaying records(unsolved)



## Main References

### 1.https://github.com/muyangcaiwei/flink

### 2.https://ci.apache.org/projects/flink/flink-docs-release-1.11/



## Reference Code from Flink v1.11 offical website

### 1. init (https://ci.apache.org/projects/flink/flink-docs-release-1.11/dev/table/common.html#create-a-tableenvironment)

```shell
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

StreamExecutionEnvironment bsEnv = StreamExecutionEnvironment.getExecutionEnvironment();
EnvironmentSettings bsSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
StreamTableEnvironment bsTableEnv = StreamTableEnvironment.create(bsEnv, bsSettings);
```

### 2.Kafka connector (https://ci.apache.org/projects/flink/flink-docs-release-1.11/dev/table/connectors/kafka.html)

```java
CREATE TABLE kafkaTable (
 user_id BIGINT,
 item_id BIGINT,
 category_id BIGINT,
 behavior STRING,
 ts TIMESTAMP(3)
) WITH (
 'connector' = 'kafka',
 'topic' = 'user_behavior',
 'properties.bootstrap.servers' = 'localhost:9092',
 'properties.group.id' = 'testGroup',
 'format' = 'csv',
 'scan.startup.mode' = 'earliest-offset'
)
```

### 3.Elastic Search connector (https://ci.apache.org/projects/flink/flink-docs-release-1.11/dev/table/connectors/elasticsearch.html)

```java
CREATE TABLE myUserTable (
  user_id STRING,
  user_name STRING
  uv BIGINT,
  pv BIGINT,
  PRIMARY KEY (user_id) NOT ENFORCED
) WITH (
  'connector' = 'elasticsearch-7',
  'hosts' = 'http://localhost:9200',
  'index' = 'users'
);
```

### 4.Execute Query (https://ci.apache.org/projects/flink/flink-docs-release-1.11/dev/table/sql/queries.html#execute-a-query)

```java
tableEnv.executeSql("CREATE TABLE Orders (`user` BIGINT, product STRING, amount INT) WITH (...)");
// or
TableResult tableResult2 = tableEnv.sqlQuery("SELECT * FROM Orders").execute();
tableResult2.print();
```

