package be.geertvanheusden.hazelcast.discovery;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.internal.nio.IOUtil;
import com.hazelcast.spi.discovery.DiscoveryNode;
import jodd.http.HttpException;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RancherMetadataResolverTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(0);

	@Test
	public void serviceWithUnavailableMetadataHostShouldThrowException() throws IOException {
		try {
			new RancherMetadataResolver("invalid-host").requestNodes();
			
			fail();
		} catch (Throwable e) {
			assertEquals("Can't connect to: http://invalid-host/2015-12-19/self/service/containers; <--- java.net.UnknownHostException: invalid-host", e.getMessage());
			assertEquals(HttpException.class, e.getClass());
			assertEquals(UnknownHostException.class, e.getCause().getClass());
		}
	}
	
	@Test
	public void serviceWithoutContainersShouldReturnZeroValues() throws IOException {
		stubContainersEndpoint("/zero-containers.json");

		Iterable<DiscoveryNode> discoveryNodes = new RancherMetadataResolver(getHostAndPort()).requestNodes();

		assertEquals(0, discoveryNodes.spliterator().getExactSizeIfKnown());
	}
	
	@Test
	public void serviceWithOneContainerShouldReturnAddress() throws IOException {
		stubContainersEndpoint("/one-container.json");

		Iterable<DiscoveryNode> discoveryNodes = new RancherMetadataResolver(getHostAndPort()).requestNodes();

		assertEquals(1, discoveryNodes.spliterator().getExactSizeIfKnown());

		DiscoveryNode discoveryNode = discoveryNodes.iterator().next();

		assertEquals(NetworkConfig.DEFAULT_PORT, discoveryNode.getPrivateAddress().getPort());
		assertEquals("10.42.63.40", discoveryNode.getPrivateAddress().getHost());
		assertEquals("temp", discoveryNode.getProperties().get("serviceName"));
		assertEquals("Testcluster-temp-1", discoveryNode.getProperties().get("containerName"));
	}

	@Test
	public void serviceWithTwoContainerShouldReturnAddresses() throws IOException {
		stubContainersEndpoint("/two-containers.json");

		Iterable<DiscoveryNode> discoveryNodes = new RancherMetadataResolver(getHostAndPort()).requestNodes();

		assertEquals(2, discoveryNodes.spliterator().getExactSizeIfKnown());

		Iterator<DiscoveryNode> discoveryNodeIterator = discoveryNodes.iterator();
		DiscoveryNode discoveryNode = discoveryNodeIterator.next();

		assertEquals(NetworkConfig.DEFAULT_PORT, discoveryNode.getPrivateAddress().getPort());
		assertEquals("10.42.63.40", discoveryNode.getPrivateAddress().getHost());
		assertEquals("temp", discoveryNode.getProperties().get("serviceName"));
		assertEquals("Testcluster-temp-1", discoveryNode.getProperties().get("containerName"));

		DiscoveryNode discoveryNode2 = discoveryNodeIterator.next();
		assertEquals(NetworkConfig.DEFAULT_PORT, discoveryNode2.getPrivateAddress().getPort());
		assertEquals("10.42.63.50", discoveryNode2.getPrivateAddress().getHost());
		assertEquals("temp", discoveryNode2.getProperties().get("serviceName"));
		assertEquals("Testcluster-temp-2", discoveryNode2.getProperties().get("containerName"));
	}
	
	private void stubContainersEndpoint(String returnFile) throws IOException {
		stubFor(get(urlEqualTo("/2015-12-19/self/service/containers"))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(IOUtil.toByteArray(this.getClass().getResourceAsStream(returnFile)))));
	}
	
	private String getHostAndPort() {
		return "localhost:" + wireMockRule.port();
	}

}