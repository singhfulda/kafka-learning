package de.microservicer.kafka.first;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerDemoWithCallBackAndKeys {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		final Logger logger = LoggerFactory.getLogger(ProducerDemoWithCallBackAndKeys.class);

		// create producer properties
		Properties properties = new Properties();

		/*
		 * old way
		 * 
		 * properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
		 * properties.setProperty("key.serializer", StringSerializer.class.getName());
		 * properties.setProperty("value.serializer", StringSerializer.class.getName());
		 * 
		 */

		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		// create producer
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

		for (int i = 0; i < 10; i++) {

			String topic = "second_topic";

			String value = "Hello world from app: " + Integer.toString(i);
			String key = "id_" + Integer.toString(i);
			logger.info("Key: " + key);

			// id_0 is going to partition 1
			// id_1 is going to partition 0
			// id_2 is going to partition 2
			// id_3 is going to partition 0
			// 4 -> 2
			// 5 -> 2
			// 6 -> 0
			// 7 -> 2
			// 8 -> 1
			// 9 -> 2

			ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, key, value);

			// send data
			producer.send(record, new Callback() {

				public void onCompletion(RecordMetadata metadata, Exception exception) {
					// executes everytime record successfully or exception
					if (exception == null) {
						logger.info("Recieved metadata. \n" + "Topic:" + metadata.topic() + "\n" + "Partition: "
								+ metadata.partition() + "\n" + "Offset: " + metadata.offset() + "\n" + "Timstamp: "
								+ metadata.timestamp());
					} else {
						logger.error("Error while producing ", exception);
					}

				}
			}).get();// block the .send to make synchronous
		}

		producer.flush();
		producer.close();

	}

}
