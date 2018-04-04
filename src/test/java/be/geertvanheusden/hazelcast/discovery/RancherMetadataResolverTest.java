package be.geertvanheusden.hazelcast.discovery;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.nio.IOUtil;
import com.hazelcast.spi.discovery.DiscoveryNode;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class RancherMetadataResolverTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8089);

	@Test
	public void serviceWithOneContainerShouldReturnAddress() throws IOException {
		stubFor(get(urlEqualTo("/2015-12-19/self/service/containers"))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(IOUtil.toByteArray(this.getClass().getResourceAsStream("/one-container.json")))));

		Iterable<DiscoveryNode> discoveryNodes = new RancherMetadataResolver("localhost:8089").requestNodes();

		assertEquals(1, discoveryNodes.spliterator().getExactSizeIfKnown());

		DiscoveryNode discoveryNode = discoveryNodes.iterator().next();

		assertEquals(NetworkConfig.DEFAULT_PORT, discoveryNode.getPrivateAddress().getPort());
		assertEquals("10.42.63.40", discoveryNode.getPrivateAddress().getHost());
	}

	@Test
	public void serviceWithTwoContainerShouldReturnAddresses() throws IOException {
		stubFor(get(urlEqualTo("/2015-12-19/self/service/containers"))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(IOUtil.toByteArray(this.getClass().getResourceAsStream("/two-containers.json")))));

		Iterable<DiscoveryNode> discoveryNodes = new RancherMetadataResolver("localhost:8089").requestNodes();

		assertEquals(2, discoveryNodes.spliterator().getExactSizeIfKnown());

		Iterator<DiscoveryNode> discoveryNodeIterator = discoveryNodes.iterator();
		DiscoveryNode discoveryNode = discoveryNodeIterator.next();

		assertEquals(NetworkConfig.DEFAULT_PORT, discoveryNode.getPrivateAddress().getPort());
		assertEquals("10.42.63.40", discoveryNode.getPrivateAddress().getHost());

		DiscoveryNode discoveryNode2 = discoveryNodeIterator.next();
		assertEquals(NetworkConfig.DEFAULT_PORT, discoveryNode2.getPrivateAddress().getPort());
		assertEquals("10.42.63.50", discoveryNode2.getPrivateAddress().getHost());
	}

}