import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.descriptors.Elasticsearch;
import org.apache.flink.table.descriptors.Json;
import org.apache.flink.table.descriptors.Schema;



public class flinktest {
    public static void main(String args[]) throws Exception{
//        init table environment
        EnvironmentSettings fsSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env,fsSettings);

//        Schema schema = new Schema()
//            .field("tag", DataTypes.STRING())
//            .field("num", DataTypes.INT())
//            .field("timed", DataTypes.STRING())
//            .field("name", DataTypes.STRING());

//        connect to kafka
        final String KAFKA_SQL = "CREATE TABLE instance_obj_tb ( " +
                "tag STRING, " +
                "num INT, " +
                "timed STRING, " +
                "name STRING" +
                ") WITH ( " +
                "'connector' = 'kafka-0.10', " +
                "'topic' = 'flinktest', " +
                "'properties.bootstrap.servers' = 'localhost:9092', " +
//                "'properties.group.id' = 'testGroup', " +
                "'format' = 'json', " +
                "'scan.startup.mode' = 'latest-offset')";

        tableEnv.executeSql(KAFKA_SQL);

        // {"tag": "12345", "num":1715, "name": "pv", "timed": "2017-11-26T01:00:00Z"}
        //{"tag": "12346", "num":1720, "name": "ab", "timed": "2017-11-27T01:00:00Z"}
        //{"tag": "12347", "num":1820, "name": "cd", "timed": "2017-11-28T01:00:00Z"}

//        connect to elastic search
        final String ES_SQL = "CREATE TABLE esflink ( " +
                "tag STRING, " +
                "num INT, " +
                "timed STRING, " +
                "name STRING" +
                ") WITH ( " +
                "'connector' = 'elasticsearch-6', " +
                "'hosts' = 'http://localhost:9200', " +
                "'index' = 'flink', " +
                "'document-type' = 'user', " +
//                "'sink.flush-on-checkpoint' = 'false'," +
//                "'sink.bulk-flush.max-actions' = '20'," +
//                "'sink.bulk-flush.max-size' = '20mb'" +
                "'format' = 'json')";

        tableEnv.executeSql(ES_SQL);

//        query executed in flink
        tableEnv.executeSql(
                "INSERT INTO esflink SELECT tag, num, timed, name FROM instance_obj_tb");
    }
}
