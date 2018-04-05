package be.geertvanheusden.hazelcast.discovery;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import com.hazelcast.util.ExceptionUtil;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.json.JsonParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class RancherMetadataResolver {

	// API Specs: http://rancher.com/docs/rancher/v1.6/en/rancher-services/metadata-service/
	private static final String DEFAULT_RANCHER_METADATA_HOSTNAME = "rancher-metadata";
	private static final String API_V2 = "/2015-12-19";
	private static final String HTTP = "http://";
	private static final String CONTAINER_SERVICE = "/self/service/containers";
	private static final String APPLICATION_JSON_ACCEPT = "application/json";

	private static final String SERVICE_NAME = "serviceName";
	private static final String CONTAINER_NAME = "containerName";

	private static final ILogger LOGGER = Logger.getLogger(RancherMetadataResolver.class);
	
	private String metadataServiceHostname;

	public RancherMetadataResolver() {
		this.metadataServiceHostname = DEFAULT_RANCHER_METADATA_HOSTNAME;
	}

	public RancherMetadataResolver(String metadataServiceHostname) {
		this.metadataServiceHostname = metadataServiceHostname;
	}

	public Iterable<DiscoveryNode> requestNodes() {
		String fullUrl = HTTP + metadataServiceHostname + API_V2 + CONTAINER_SERVICE;
		HttpResponse httpResponse = null;
		
		try {
			httpResponse = HttpRequest
					.get(fullUrl)
					.accept(APPLICATION_JSON_ACCEPT)
					.send();
		} catch (Exception e) {
			LOGGER.severe("Unable to connect to the Rancher metadata service on " + fullUrl + ". Make sure the container is running within a Rancher Service.", e);
			ExceptionUtil.rethrow(e);
		}

		String jsonBody = httpResponse.body();

		List<Container> containers = new JsonParser().parseAsList(jsonBody, Container.class);
		
		List<DiscoveryNode> discoveryNodes = containers.stream()
				.filter(Container::isRunning)
				.map(container -> {
					Map<String, Object> properties = new HashMap() {{
						this.put(SERVICE_NAME, container.getServiceName());
						this.put(CONTAINER_NAME, container.getName());
					}};
					
					return new SimpleDiscoveryNode(container.getAddress(), properties);
				})
				.collect(Collectors.toList());
		
		if (discoveryNodes.isEmpty()) {
			LOGGER.warning("No running containers found for this service.");
			return Collections.EMPTY_LIST;
		}

		return discoveryNodes;
	}
}
