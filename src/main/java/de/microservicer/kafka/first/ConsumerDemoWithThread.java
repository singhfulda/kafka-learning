package de.microservicer.kafka.first;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerDemoWithThread {

	public static void main(String[] args) {
		new ConsumerDemoWithThread().run();

	}

	private ConsumerDemoWithThread() {

	}

	private void run() {
		Logger logger = LoggerFactory.getLogger(ConsumerDemoWithThread.class.getName());

		String bootstrapServerString = "127.0.0.1:9092";
		String groupIdString = "my-ninth-application";
		String topic = "second_topic";

		// latch to deal with multiple Threads
		CountDownLatch latch = new CountDownLatch(1);

		logger.info("Creating a consumer");

		Runnable myConsumerThreadRunnable = new ConsumerRunnable(topic, bootstrapServerString, groupIdString, latch);

		// start the Thread
		Thread myThread = new Thread(myConsumerThreadRunnable);
		myThread.start();

		// add runtime hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("Caught shutdown hook");
			((ConsumerRunnable) myConsumerThreadRunnable).shutdown();
			try {
				latch.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info("Application has exited");
		}));

		try {
			latch.await();
		} catch (Exception e) {
			logger.error("Application got interruptted", e);
		} finally {
			logger.info("Application is closing");
		}

	}

	public class ConsumerRunnable implements Runnable {

		private Logger logger = LoggerFactory.getLogger(ConsumerRunnable.class);

		private CountDownLatch latch;
		private KafkaConsumer<String, String> consumer;
		String topic;
		String bootstrapServerString;
		String groupIdString;

		public ConsumerRunnable(String topic, String bootstrapServerString, String groupIdString,
				CountDownLatch latch) {
			this.latch = latch;
			// create producer properties
			Properties properties = new Properties();

			properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServerString);
			properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupIdString);
			properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

			// create consumer
			consumer = new KafkaConsumer<String, String>(properties);

			// subscribe consumer
			consumer.subscribe(Arrays.asList(topic));

		}

		@Override
		public void run() {

			try {
				// poll new data
				while (true) {
					ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
					for (ConsumerRecord<String, String> record : records) {
						logger.info("Key: " + record.key() + ", value: " + record.value());
						logger.info("Partition: " + record.partition() + ", Offset: " + record.offset());

					}
				}
			} catch (WakeupException e) {
				logger.info("Recieved shutdown signal!");

			} finally {
				consumer.close();
				// tell main code we are done
				latch.countDown();
			}

		}

		public void shutdown() {
			consumer.wakeup(); // interrupts consumer and throw wakeUpexception

		}
	}

}
