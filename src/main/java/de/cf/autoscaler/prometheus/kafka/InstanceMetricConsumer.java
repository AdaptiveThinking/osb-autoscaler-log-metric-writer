package de.cf.autoscaler.prometheus.kafka;


import com.google.protobuf.InvalidProtocolBufferException;

import de.cf.autoscaler.kafka.AutoScalerConsumer;
import de.cf.autoscaler.kafka.ByteConsumerThread;
import de.cf.autoscaler.kafka.messages.ContainerMetric;
import de.cf.autoscaler.kafka.protobuf.ProtobufContainerMetricWrapper.ProtoContainerMetric;

public class InstanceMetricConsumer implements AutoScalerConsumer{
	
	private ByteConsumerThread consThread;
	private InfluxDBWriter writer;
	
	public InstanceMetricConsumer(String topic, String groupId, String hostname, int port, InfluxDBWriter writer) {
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
			ContainerMetric metric = new ContainerMetric(ProtoContainerMetric.parseFrom(bytes));
			writer.writeInstanceContainerMetric(metric);

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
	
	public String getType() {
		return "metric_container_instance";
	}

}
