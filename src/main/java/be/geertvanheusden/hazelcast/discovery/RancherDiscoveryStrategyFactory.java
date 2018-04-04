package be.geertvanheusden.hazelcast.discovery;

import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class RancherDiscoveryStrategyFactory implements DiscoveryStrategyFactory {

	public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
		return RancherDiscoveryStrategy.class;
	}

	public DiscoveryStrategy newDiscoveryStrategy(DiscoveryNode discoveryNode, ILogger logger, Map<String, Comparable> properties) {
		return new RancherDiscoveryStrategy(logger, properties);
	}

	public Collection<PropertyDefinition> getConfigurationProperties() {
		return Collections.EMPTY_LIST;
	}
}
