package de.cf.autoscaler.prometheus.kafka;


import com.google.protobuf.InvalidProtocolBufferException;

import de.cf.autoscaler.kafka.AutoScalerConsumer;
import de.cf.autoscaler.kafka.ByteConsumerThread;
import de.cf.autoscaler.kafka.messages.HttpMetric;
import de.cf.autoscaler.kafka.protobuf.ProtobufHttpMetricWrapper.ProtoHttpMetric;

public class HttpMetricConsumer implements AutoScalerConsumer{
	
	private ByteConsumerThread consThread;
	private InfluxDBWriter writer;
	
	public HttpMetricConsumer(String topic, String groupId, String hostname, int port, InfluxDBWriter writer) {
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
			HttpMetric metric = new HttpMetric(ProtoHttpMetric.parseFrom(bytes));
			writer.writeHttpMetric(metric);

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
	
	public String getType() {
		return "metric_http";
	}

}
