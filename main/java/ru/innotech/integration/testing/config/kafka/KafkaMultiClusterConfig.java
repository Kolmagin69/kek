package ru.innotech.integration.testing.config.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.innotech.integration.testing.config.props.KafkaSslProperties;
import ru.innotech.integration.testing.config.props.MirionKafkaProperties;
import ru.innotech.integration.testing.config.props.PimKafkaProperties;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@EnableConfigurationProperties({KafkaSslProperties.class, PimKafkaProperties.class, MirionKafkaProperties.class})
@RequiredArgsConstructor
public class KafkaMultiClusterConfig {

  private final PimKafkaProperties pimProps;

  private final MirionKafkaProperties mirionProps;

  private final KafkaSslProperties sslProps;

  @Primary
  @Bean(name = "consumerFactory")
  public ConsumerFactory<String, String> consumerFactory() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, mirionProps.getBootstrapServers());
    configs.put(ConsumerConfig.GROUP_ID_CONFIG, mirionProps.getConsumer().getGroupId());
    configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, mirionProps.getConsumer().getAutoOffsetReset());
    configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

    applySsl(configs, sslProps);

    return new DefaultKafkaConsumerFactory<>(configs);
  }

  @Primary
  @Bean(name = "containerFactory")
  public ConcurrentKafkaListenerContainerFactory<String, String> containerFactory() {
    var factory = new ConcurrentKafkaListenerContainerFactory<String,String>();
    factory.setConsumerFactory(consumerFactory());
    return factory;
  }

  @Primary
  @Bean(name = "producerFactory")
  public ProducerFactory<String, String> producerFactory() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, mirionProps.getBootstrapServers());
    configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringDeserializer.class);

    applySsl(configs, sslProps);

    return new DefaultKafkaProducerFactory<>(configs);
  }

  @Primary
  @Bean(name = "kafkaTemplate")
  public KafkaTemplate<String, String> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  @Bean(name = "pimConsumerFactory")
  public ConsumerFactory<String, byte[]> pimConsumerFactory() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, pimProps.getBootstrapServers());
    configs.put(ConsumerConfig.GROUP_ID_CONFIG, pimProps.getConsumer().getGroupId());
    configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, pimProps.getConsumer().getAutoOffsetReset());
    configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

    applySsl(configs, sslProps);

    return new DefaultKafkaConsumerFactory<>(configs);
  }

  @Bean(name = "pimContainerFactory")
  public ConcurrentKafkaListenerContainerFactory<String, byte[]> pimContainerFactory() {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, byte[]>();
    factory.setConsumerFactory(pimConsumerFactory());
    return factory;
  }

  @Bean(name = "pimProducerFactory")
  public ProducerFactory<String, byte[]> pimProducerFactory() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, pimProps.getBootstrapServers());
    configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

    applySsl(configs, sslProps);

    return new DefaultKafkaProducerFactory<>(configs);
  }

  @Bean(name = "pimKafkaTemplate")
  public KafkaTemplate<String, byte[]> pimKafkaTemplate() {
    return new KafkaTemplate<>(pimProducerFactory());
  }

  private void applySsl(Map<String, Object> target, KafkaSslProperties ssl) {
    if (ssl.isEnabled()) {
      target.put("security.protocol", ssl.getProtocol());
      target.put("ssl.truststore.location", ssl.getTrustStoreLocation());
      target.put("ssl.truststore.password", ssl.getTrustStorePassword());
      target.put("ssl.keystore.location", ssl.getKeyStoreLocation());
      target.put("ssl.keystore.password", ssl.getKeyStorePassword());
      target.put("ssl.keystore.type", ssl.getKeyStoreType());
      target.put("ssl.truststore.type", ssl.getTrustStoreType());
    }
  }

}