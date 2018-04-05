# Rancher Hazelcast discover SPI

This Hazelcast Discovery plugin for Rancher will automaticaly join containers from the same (Rancher) Service.
It will use the [Rancher Metadata Service](https://rancher.com/docs/rancher/v1.6/en/rancher-services/metadata-service/) to detect the other members (containers) that are part of the same Service.

The *rancher-metadata* host is registered by default to the Rancher DNS and is available from within the container. By calling the following endpoint: http://rancher-metadata/latest/self/service/containers it will get all the containers from the current Service.

Except for enabling the plugin, there is no further configuration needed.

## Configuration

Add the following dependency to the project:

```
<dependency>
    <groupId>be.geertvanheusden.hazelcast</groupId>
    <artifactId>rancher-hazelcast-discovery-spi</artifactId>
    <version>${rancher-hazelcast-discovery-spi.version}</version>
</dependency>
```

### hazelcast.xml

```
<hazelcast xsi:schemaLocation="http://www.hazelcast.com/schema/config hazelcast-config-3.9.xsd"
           xmlns="http://www.hazelcast.com/schema/config"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

  <properties>
    <property name="hazelcast.discovery.enabled">true</property>
  </properties>

  <network>
    <join>
      <multicast enabled="false"/>
      <tcp-ip enabled="false" />

      <discovery-strategies>
        <discovery-strategy enabled="true" class="be.geertvanheusden.hazelcast.discovery.RancherDiscoveryStrategyFactory">
        </discovery-strategy>
      </discovery-strategies>
    </join>
  </network>

</hazelcast>
```


### Programmatically 

```
Config config = new ClasspathXmlConfig("hazelcast.xml");
config.setProperty("hazelcast.discovery.enabled", "true");

JoinConfig joinConfig = config.getNetworkConfig().getJoin();
joinConfig.getMulticastConfig().setEnabled(false);
DiscoveryConfig discoveryConfig = joinConfig.getDiscoveryConfig();

DiscoveryStrategyFactory factory = new RancherDiscoveryStrategyFactory();
DiscoveryStrategyConfig strategyConfig = new DiscoveryStrategyConfig(factory);

discoveryConfig.addDiscoveryStrategyConfig(strategyConfig);

return Hazelcast.newHazelcastInstance(config);
```