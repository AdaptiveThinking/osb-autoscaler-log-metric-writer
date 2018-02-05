package de.cf.autoscaler.prometheus.kafka;


import com.google.protobuf.InvalidProtocolBufferException;

import de.cf.autoscaler.kafka.AutoScalerConsumer;
import de.cf.autoscaler.kafka.ByteConsumerThread;
import de.cf.autoscaler.kafka.messages.ApplicationMetric;
import de.cf.autoscaler.kafka.protobuf.ProtobufApplicationMetricWrapper.ProtoApplicationMetric;

public class ApplicationMetricConsumer implements AutoScalerConsumer{
	
	private ByteConsumerThread consThread;
	private InfluxDBWriter writer;
	
	public ApplicationMetricConsumer(String topic, String groupId, String hostname, int port, InfluxDBWriter writer) {
		consThread = new ByteConsumerThread(topic, groupId, hostname, port, this);
		this.writer = writer;
		
	}
	
	public void startConsumer() {
		consThread.start();
	}
	
	public void stopConsumer() {
		consThread.getKafkaConsumer().wakeup();
	}
	
	public void consume(byte[] bytes) {
		try {
			ApplicationMetric metric = new ApplicationMetric(ProtoApplicationMetric.parseFrom(bytes));
			writer.writeApplicationContainerMetric(metric);

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
	
	public String getType() {
		return "metric_container_application";
	}

}
