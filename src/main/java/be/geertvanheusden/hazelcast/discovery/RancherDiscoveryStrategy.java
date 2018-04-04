package be.geertvanheusden.hazelcast.discovery;

import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;

import java.util.Map;

public class RancherDiscoveryStrategy extends AbstractDiscoveryStrategy {

	public RancherDiscoveryStrategy(ILogger logger, Map<String, Comparable> properties) {
		super(logger, properties);
	}

	public Iterable<DiscoveryNode> discoverNodes() {
		return new RancherMetadataResolver().requestNodes();
	}


}
