package be.geertvanheusden.hazelcast.discovery;

import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.json.JsonParser;

import java.util.List;
import java.util.stream.Collectors;

public final class RancherMetadataResolver {

	// API Specs: http://rancher.com/docs/rancher/v1.6/en/rancher-services/metadata-service/
	private static final String DEFAULT_RANCHER_METADATA_HOSTNAME = "rancher-metadata";
	private static final String API_V2 = "/2015-12-19";
	private static final String HTTP = "http://";
	private static final String CONTAINER_SERVICE = "/self/service/containers";
	private static final String APPLICATION_JSON_ACCEPT = "application/json";
	private String metadataServiceHostname;

	public RancherMetadataResolver() {
		this.metadataServiceHostname = DEFAULT_RANCHER_METADATA_HOSTNAME;
	}

	public RancherMetadataResolver(String metadataServiceHostname) {
		this.metadataServiceHostname = metadataServiceHostname;
	}

	public Iterable<DiscoveryNode> requestNodes() {
		HttpResponse httpResponse = HttpRequest
				.get(HTTP + metadataServiceHostname + API_V2 + CONTAINER_SERVICE)
				.accept(APPLICATION_JSON_ACCEPT)
				.send();

		String jsonBody = httpResponse.body();

		List<Container> containers = new JsonParser().parseAsList(jsonBody, Container.class);

		List<DiscoveryNode> discoveryNodes = containers.stream()
				.filter(Container::isRunning)
				.map(container -> new SimpleDiscoveryNode(container.getAddress()))
				.collect(Collectors.toList());

		return discoveryNodes;
	}
}
