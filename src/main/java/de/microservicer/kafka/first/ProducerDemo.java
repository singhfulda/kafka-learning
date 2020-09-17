package de.microservicer.kafka.first;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class ProducerDemo {

	public static void main(String[] args) {

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
		ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic",
				"Hello world from app");

		// send data
		producer.send(record);
		producer.flush();
		producer.close();

	}

}
