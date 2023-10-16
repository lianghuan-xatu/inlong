/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.inlong.sort.tubemq.table;

import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.configuration.description.Description;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import static org.apache.flink.table.factories.FactoryUtil.FORMAT;

/**
 * Option utils for tubeMQ table source and sink.
 */
public class TubeMQOptions {

    // --------------------------------------------------------------------------------------------
    // Option enumerations
    // --------------------------------------------------------------------------------------------

    public static final String PROPERTIES_PREFIX = "properties.";

    // Start up offset.
    // Always start from the max consume position.
    public static final String CONSUMER_FROM_MAX_OFFSET_ALWAYS = "max";
    // Start from the latest position for the first time. Otherwise start from last consume position.
    public static final String CONSUMER_FROM_LATEST_OFFSET = "latest";
    // Start from 0 for the first time. Otherwise start from last consume position.
    public static final String CONSUMER_FROM_FIRST_OFFSET = "earliest";

    // --------------------------------------------------------------------------------------------
    // Format options
    // --------------------------------------------------------------------------------------------

    public static final ConfigOption<String> KEY_FORMAT = ConfigOptions
            .key("key." + FORMAT.key())
            .stringType()
            .noDefaultValue()
            .withDescription("Defines the format identifier for encoding key data. "
                    + "The identifier is used to discover a suitable format factory.");

    public static final ConfigOption<List<String>> KEY_FIELDS =
            ConfigOptions.key("key.fields")
                    .stringType()
                    .asList()
                    .defaultValues()
                    .withDescription(
                            "Defines an explicit list of physical columns from the table schema "
                                    + "that configure the data type for the key format. By default, this list is "
                                    + "empty and thus a key is undefined.");

    // --------------------------------------------------------------------------------------------
    // TubeMQ specific options
    // --------------------------------------------------------------------------------------------

    public static final ConfigOption<String> TOPIC =
            ConfigOptions.key("topic")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "Topic names from which the table is read. Either 'topic' "
                                    + "or 'topic-pattern' must be set for source.");

    public static final ConfigOption<String> TOPIC_PATTERN =
            ConfigOptions.key("topic-pattern")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "Optional topic pattern from which the table is read for source."
                                    + " Either 'topic' or 'topic-pattern' must be set.");

    public static final ConfigOption<String> MASTER_RPC =
            ConfigOptions.key("master.rpc")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Required TubeMQ master connection string");

    public static final ConfigOption<String> GROUP_NAME =
            ConfigOptions.key("group.name")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "Required consumer group in TubeMQ consumer");

    public static final ConfigOption<String> TUBE_MESSAGE_NOT_FOUND_WAIT_PERIOD =
            ConfigOptions.key("tubemq.message.not.found.wait.period")
                    .stringType()
                    .defaultValue("350ms")
                    .withDescription("The time of waiting period if "
                            + "tubeMQ broker return message not found.");

    public static final ConfigOption<Long> TUBE_SUBSCRIBE_RETRY_TIMEOUT =
            ConfigOptions.key("tubemq.subscribe.retry.timeout")
                    .longType()
                    .defaultValue(300000L)
                    .withDescription("The time of subscribing tubeMQ timeout, in millisecond");

    public static final ConfigOption<Integer> SOURCE_EVENT_QUEUE_CAPACITY =
            ConfigOptions.key("source.event.queue.capacity")
                    .intType()
                    .defaultValue(1024);

    public static final ConfigOption<String> SESSION_KEY =
            ConfigOptions.key("session.key")
                    .stringType()
                    .defaultValue("default_session_key")
                    .withDescription("The session key for this consumer group at startup.");

    public static final ConfigOption<List<String>> STREAMID =
            ConfigOptions.key("topic.streamId")
                    .stringType()
                    .asList()
                    .noDefaultValue()
                    .withDescription("The streamId owned this topic.");

    public static final ConfigOption<Integer> MAX_RETRIES =
            ConfigOptions.key("max.retries")
                    .intType()
                    .defaultValue(5)
                    .withDescription("The maximum number of retries when an "
                            + "exception is caught.");

    public static final ConfigOption<Boolean> BOOTSTRAP_FROM_MAX =
            ConfigOptions.key("bootstrap.from.max")
                    .booleanType()
                    .defaultValue(true)
                    .withDescription("True if consuming from the most recent "
                            + "position when the tubemq source starts.. It only takes "
                            + "effect when the tubemq source does not recover from "
                            + "checkpoints.");

    public static final ConfigOption<String> SOURCE_MAX_IDLE_TIME =
            ConfigOptions.key("source.task.max.idle.time")
                    .stringType()
                    .defaultValue("5min")
                    .withDescription("The max time of the source marked as temporarily idle.");

    public static final ConfigOption<String> MESSAGE_NOT_FOUND_WAIT_PERIOD =
            ConfigOptions.key("message.not.found.wait.period")
                    .stringType()
                    .defaultValue("500ms")
                    .withDescription("The time of waiting period if tubemq broker return message not found.");

    public static final ConfigOption<ValueFieldsStrategy> VALUE_FIELDS_INCLUDE =
            ConfigOptions.key("value.fields-include")
                    .enumType(ValueFieldsStrategy.class)
                    .defaultValue(ValueFieldsStrategy.ALL)
                    .withDescription(
                            String.format(
                                    "Defines a strategy how to deal with key columns in the data type "
                                            + "of the value format. By default, '%s' physical columns "
                                            + "of the table schema will be included in the value "
                                            + "format which means that the key columns "
                                            + "appear in the data type for both the key and value format.",
                                    ValueFieldsStrategy.ALL));

    public static final ConfigOption<String> KEY_FIELDS_PREFIX =
            ConfigOptions.key("key.fields-prefix")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            Description.builder()
                                    .text(
                                            "Defines a custom prefix for all fields of the key format to avoid "
                                                    + "name clashes with fields of the value format. "
                                                    + "By default, the prefix is empty.")
                                    .linebreak()
                                    .text(
                                            String.format(
                                                    "If a custom prefix is defined, both the table schema "
                                                            + "and '%s' will work with prefixed names.",
                                                    KEY_FIELDS.key()))
                                    .linebreak()
                                    .text(
                                            "When constructing the data type of the key format, "
                                                    + "the prefix will be removed and the "
                                                    + "non-prefixed names will be used within the key format.")
                                    .linebreak()
                                    .text(
                                            String.format(
                                                    "Please note that this option requires that '%s' must be '%s'.",
                                                    VALUE_FIELDS_INCLUDE.key(),
                                                    ValueFieldsStrategy.EXCEPT_KEY))
                                    .build());

    // --------------------------------------------------------------------------------------------
    // Validation
    // --------------------------------------------------------------------------------------------
    private static final Set<String> CONSUMER_STARTUP_MODE_ENUMS = new HashSet<>(Arrays.asList(
            CONSUMER_FROM_MAX_OFFSET_ALWAYS,
            CONSUMER_FROM_LATEST_OFFSET,
            CONSUMER_FROM_FIRST_OFFSET));

    public static Configuration getTubeMQProperties(Map<String, String> tableOptions) {
        final Configuration tubeMQProperties = new Configuration();

        if (hasTubeMQClientProperties(tableOptions)) {
            tableOptions.keySet().stream()
                    .filter(key -> key.startsWith(PROPERTIES_PREFIX))
                    .forEach(
                            key -> {
                                final String value = tableOptions.get(key);
                                final String subKey = key.substring((PROPERTIES_PREFIX).length());
                                tubeMQProperties.toMap().put(subKey, value);
                            });
        }
        return tubeMQProperties;
    }

    // --------------------------------------------------------------------------------------------
    // Scan specific options
    // --------------------------------------------------------------------------------------------

    /**
     * Decides if the table options contains TubeMQ client properties that start with prefix
     * 'properties'.
     */
    private static boolean hasTubeMQClientProperties(Map<String, String> tableOptions) {
        return tableOptions.keySet().stream().anyMatch(k -> k.startsWith(PROPERTIES_PREFIX));
    }

    public static String getSourceTopics(ReadableConfig tableOptions) {
        return tableOptions.getOptional(TOPIC).orElse(null);
    }

    public static String getSinkTopics(ReadableConfig tableOptions) {
        return tableOptions.getOptional(TOPIC).orElse(null);
    }

    public static String getMasterRpcAddress(ReadableConfig tableOptions) {
        return tableOptions.getOptional(MASTER_RPC).orElse(null);
    }

    public static TreeSet<String> getTiSet(ReadableConfig tableOptions) {
        TreeSet<String> set = new TreeSet<>();
        tableOptions.getOptional(STREAMID).ifPresent(new Consumer<List<String>>() {

            @Override
            public void accept(List<String> strings) {
                set.addAll(strings);
            }
        });
        return set;
    }

    public static String getConsumerGroup(ReadableConfig tableOptions) {
        return tableOptions.getOptional(GROUP_NAME).orElse(null);
    }

    public static String getSessionKey(ReadableConfig tableOptions) {
        return tableOptions.getOptional(SESSION_KEY).orElse(SESSION_KEY.defaultValue());
    }

    /**
     * Strategies to derive the data type of a value format by considering a key format.
     */
    public enum ValueFieldsStrategy {

        ALL,

        EXCEPT_KEY

    }

}
