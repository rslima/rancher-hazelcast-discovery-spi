package be.geertvanheusden.hazelcast.discovery;

import com.hazelcast.config.NetworkConfig;
import com.hazelcast.nio.Address;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class Container {

	private static final String RUNNING_STATE = "running";


	private String service_name;
	private Integer service_index;
	private String primary_ip;
	private String state;


	public String getName() {
		return service_name + "_" + service_index;
	}

	public String getServiceName() {
		return service_name;
	}

	public Integer getServiceIndex() {
		return service_index;
	}

	public Address getAddress() {
		try {
			return new Address(InetAddress.getByName(primary_ip), NetworkConfig.DEFAULT_PORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public boolean isRunning() {
		return RUNNING_STATE.equals(state);
	}
}
