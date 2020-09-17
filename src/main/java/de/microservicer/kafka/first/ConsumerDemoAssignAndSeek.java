package de.microservicer.kafka.first;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerDemoAssignAndSeek {

	public static void main(String[] args) {

		Logger logger = LoggerFactory.getLogger(ConsumerDemoAssignAndSeek.class);

		String bootstrapServerString = "127.0.0.1:9092";
		String groupIdString = "my-fourth-application";
		String topic = "second_topic";

		// create producer properties
		Properties properties = new Properties();

		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServerString);
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		// no need to use group
		// properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupIdString);
		properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		// create consumer
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

		// no subscribe consumer
		// consumer.subscribe(Collections.singleton(topic));

		// Assign and seek mostly used to replay data or fetch a specific message
		TopicPartition partitionToReadFromPartition = new TopicPartition(topic, 0);
		long offsetToReadFrom = 15L;
		consumer.assign(Arrays.asList(partitionToReadFromPartition));

		// seek
		consumer.seek(partitionToReadFromPartition, offsetToReadFrom);

		int numberOfMessagesToRead = 5;
		boolean keepOnReading = true;
		int numberOfMessagesReadSoFar = 0;

		// poll new data
		while (keepOnReading) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
			for (ConsumerRecord<String, String> record : records) {
				numberOfMessagesReadSoFar += 1;
				logger.info("Key: " + record.key() + ", value: " + record.value());
				logger.info("Partition: " + record.partition() + ", Offset: " + record.offset());

				if (numberOfMessagesReadSoFar >= numberOfMessagesToRead) {
					keepOnReading = false; // to exit while loop
					break; // to exit the for loop

				}

			}
		}

		logger.info("exiting app");

	}

}
