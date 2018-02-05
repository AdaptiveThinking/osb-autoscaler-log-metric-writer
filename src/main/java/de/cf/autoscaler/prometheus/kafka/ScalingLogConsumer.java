package de.cf.autoscaler.prometheus.kafka;


import com.google.protobuf.InvalidProtocolBufferException;

import de.cf.autoscaler.kafka.AutoScalerConsumer;
import de.cf.autoscaler.kafka.ByteConsumerThread;
import de.cf.autoscaler.kafka.messages.ScalingLog;
import de.cf.autoscaler.kafka.protobuf.ProtobufScalingWrapper.ProtoScaling;

public class ScalingLogConsumer implements AutoScalerConsumer {
	
	private ByteConsumerThread consThread;
	private InfluxDBWriter writer;
	
	public ScalingLogConsumer(String topic, String groupId, String hostname, int port, InfluxDBWriter writer) {
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
			ScalingLog log = new ScalingLog(ProtoScaling.parseFrom(bytes));
			writer.writeScalingLog(log);

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
	
	public String getType() {
		return "quotient";
	}

}
