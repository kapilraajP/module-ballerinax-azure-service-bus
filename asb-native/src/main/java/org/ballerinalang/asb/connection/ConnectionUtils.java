/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.asb.connection;

import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BArrayType;
import org.ballerinalang.asb.ASBConstants;
import org.ballerinalang.asb.ASBUtils;

import java.time.Duration;
import java.util.*;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.ballerinalang.asb.ASBConstants.*;

/**
 * Util class used to bridge the Asb connector's native code and the Ballerina API.
 */
public class ConnectionUtils {
    private static final Logger log = Logger.getLogger(ConnectionUtils.class.getName());

    private String connectionString;

    /**
     * Creates a Asb Sender Connection using the given connection parameters.
     *
     * @param connectionString Azure Service Bus Primary key string used to initialize the connection.
     * @param entityPath Resource entity path.
     * @return Asb Sender Connection object.
     */
    public static IMessageSender createSenderConnection(String connectionString, String entityPath) throws Exception {
        try {
            IMessageSender sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(
                    new ConnectionStringBuilder(connectionString, entityPath));
            return sender;
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting: "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting: "
                    + e.getMessage());
        } catch (Exception e) {
            throw ASBUtils.returnErrorValue("Sender Connection Creation Failed: " + e.getMessage());
        }
    }

    /**
     * Closes the Asb Sender Connection using the given connection parameters.
     *
     * @param sender Created IMessageSender instance used to close the connection.
     */
    public static void closeSenderConnection(IMessageSender sender) throws Exception {
        try {
            sender.close();
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("object cannot be properly closed " + e.getMessage());
        }
    }

    /**
     * Creates a Asb Receiver Connection using the given connection parameters.
     *
     * @param connectionString Primary key string used to initialize the connection.
     * @param entityPath Resource entity path.
     * @return Asb Receiver Connection object.
     */
    public static IMessageReceiver createReceiverConnection(String connectionString, String entityPath)
            throws Exception {
        try {
            IMessageReceiver receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                    new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);
            return receiver;
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting: "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting: "
                    + e.getMessage());
        } catch (Exception e) {
            throw ASBUtils.returnErrorValue("Receiver Connection Creation Failed: " + e.getMessage());
        }
    }

    /**
     * Closes the Asb Receiver Connection using the given connection parameters.
     *
     * @param receiver Created IMessageReceiver instance used to close the connection.
     */
    public static void closeReceiverConnection(IMessageReceiver receiver) throws Exception {
        try {
            receiver.close();
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("object cannot be properly closed " + e.getMessage());
        }
    }

    /**
     * Convert BMap to Map.
     *
     * @param map Input BMap used to convert to Map.
     * @return Converted Map object.
     */
    public static Map<String, String> toStringMap(BMap map) {
        Map<String, String> returnMap = new HashMap<>();
        if (map != null) {
            for (Object aKey : map.getKeys()) {
                returnMap.put(aKey.toString(), map.get(aKey).toString());
            }
        }
        return returnMap;
    }

    /**
     * Convert Map to BMap.
     *
     * @param map Input Map used to convert to BMap.
     * @return Converted BMap object.
     */
    public static BMap<BString, Object> toBMap(Map map) {
        BMap<BString, Object> returnMap = ValueCreator.createMapValue();
        if (map != null) {
            for (Object aKey : map.keySet().toArray()) {
                returnMap.put(StringUtils.fromString(aKey.toString()),
                        StringUtils.fromString(map.get(aKey).toString()));
            }
        }
        return returnMap;
    }

    /**
     * Send Message with configurable parameters when Sender Connection is given as a parameter and
     * message content as a byte array.
     *
     * @param sender Input Sender connection.
     * @param content Input message content as byte array
     * @param contentType Input message content type
     * @param messageId Input Message Id
     * @param to Input Message to
     * @param replyTo Input Message reply to
     * @param replyToSessionId Identifier of the session to reply to
     * @param label Input Message label
     * @param sessionId Input Message session Id
     * @param correlationId Input Message correlationId
     * @param properties Input Message properties
     * @param timeToLive Input Message time to live in minutes
     */
    public static void sendMessage(IMessageSender sender, BArray content, Object contentType, Object messageId,
                                   Object to, Object replyTo, Object replyToSessionId, Object label, Object sessionId,
                                   Object correlationId, BMap<String, String> properties, Object timeToLive)
            throws Exception {
        try {
            // Send messages to queue
            log.info("\tSending messages to ...\n" + sender.getEntityPath());
            IMessage message = new Message();
            String msgId = (messageId == null || messageId.toString() == "") ?
                    UUID.randomUUID().toString() : messageId.toString();
            message.setMessageId(msgId);
            message.setTimeToLive(Duration.ofMinutes((long) timeToLive));
            byte[] byteArray = content.getBytes();
            message.setBody(byteArray);
            message.setContentType(valueToEmptyOrToString(contentType));
            message.setTo(valueToEmptyOrToString(to));
            message.setReplyTo(valueToEmptyOrToString(replyTo));
            message.setReplyToSessionId(valueToEmptyOrToString(replyToSessionId));
            message.setLabel(valueToEmptyOrToString(label));
            message.setSessionId(valueToEmptyOrToString(sessionId));
            message.setCorrelationId(valueToEmptyOrToString(correlationId));
            Map<String,String> map = toStringMap(properties);
            message.setProperties(map);

            sender.send(message);
            log.info("\t=> Sent a message with messageId \n" + message.getMessageId());
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Send Message with configurable parameters when Sender Connection is given as a parameter and
     * message content as a byte array and optional parameters as a BMap.
     *
     * @param sender Input Sender connection.
     * @param content Input message content as byte array
     * @param parameters Input message optional parameters specified as a BMap
     * @param properties Input Message properties
     */
    public static void sendMessageWithConfigurableParameters(IMessageSender sender, BArray content,
                                                             BMap<String, String> parameters,
                                                             BMap<String, String> properties) throws Exception {
        Map<String,String> map = toStringMap(parameters);

        String contentType = valueToStringOrEmpty(map, CONTENT_TYPE);
        String messageId = map.get(MESSAGE_ID) != null ? map.get(MESSAGE_ID) : UUID.randomUUID().toString();
        String to = valueToStringOrEmpty(map, TO);
        String replyTo = valueToStringOrEmpty(map, REPLY_TO);
        String replyToSessionId = valueToStringOrEmpty(map, REPLY_TO_SESSION_ID);
        String label = valueToStringOrEmpty(map,LABEL);
        String sessionId = valueToStringOrEmpty(map, SESSION_ID);
        String correlationId = valueToStringOrEmpty(map, CORRELATION_ID);
        int timeToLive = map.get(TIME_TO_LIVE) != null ? Integer.parseInt(map.get(TIME_TO_LIVE)) : DEFAULT_TIME_TO_LIVE;

        try {
            // Send messages to queue
            log.info("\tSending messages to ...\n" + sender.getEntityPath());
            IMessage message = new Message();
            message.setMessageId(messageId);
            message.setTimeToLive(Duration.ofMinutes(timeToLive));
            byte[] byteArray = content.getBytes();
            message.setBody(byteArray);
            message.setContentType(contentType);
            message.setMessageId(messageId);
            message.setTo(to);
            message.setReplyTo(replyTo);
            message.setReplyToSessionId(replyToSessionId);
            message.setLabel(label);
            message.setSessionId(sessionId);
            message.setCorrelationId(correlationId);
            Map<String,String> propertiesMap = toStringMap(properties);
            message.setProperties(propertiesMap);

            sender.send(message);
            log.info("\t=> Sent a message with messageId \n" + message.getMessageId());
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Receive Message with configurable parameters as Map when Receiver Connection is given as a parameter and
     * message content as a byte array and return Message object.
     *
     * @param receiver Output Receiver connection.
     * @param serverWaitTime Specified server wait time in seconds to receive message.
     * @return Message Object of the received message.
     */
    public static Object receiveMessage(IMessageReceiver receiver, Object serverWaitTime) throws Exception {
        try {
            log.info("\n\tWaiting up to 'serverWaitTime' seconds for messages from\n" + receiver.getEntityPath());

            IMessage receivedMessage = receiver.receive(Duration.ofSeconds((long) serverWaitTime));

            if (receivedMessage == null) {
                return null;
            }
            log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
            log.info("\t<= Received a message with messageBody \n" +
                    new String(receivedMessage.getBody(), UTF_8));
            receiver.complete(receivedMessage.getLockToken());

            log.info("\tDone receiving messages from \n" + receiver.getEntityPath());

            BObject messageBObject = ValueCreator.createObjectValue(ASBConstants.PACKAGE_ID_ASB,
                    ASBConstants.MESSAGE_OBJECT);
            messageBObject.set(ASBConstants.MESSAGE_CONTENT, ValueCreator.createArrayValue(receivedMessage.getBody()));
            messageBObject.set(MESSAGE_CONTENT_TYPE, StringUtils.fromString(receivedMessage.getContentType()));
            messageBObject.set(BMESSAGE_ID, StringUtils.fromString(receivedMessage.getMessageId()));
            messageBObject.set(BTO, StringUtils.fromString(receivedMessage.getTo()));
            messageBObject.set(BREPLY_TO, StringUtils.fromString(receivedMessage.getReplyTo()));
            messageBObject.set(BREPLY_TO_SESSION_ID, StringUtils.fromString(receivedMessage.getReplyToSessionId()));
            messageBObject.set(BLABEL, StringUtils.fromString(receivedMessage.getLabel()));
            messageBObject.set(BSESSION_ID, StringUtils.fromString(receivedMessage.getSessionId()));
            messageBObject.set(BCORRELATION_ID, StringUtils.fromString(receivedMessage.getCorrelationId()));
            messageBObject.set(BTIME_TO_LIVE, receivedMessage.getTimeToLive().getSeconds());
            BMap<BString, Object> optionalProperties =
                    ValueCreator.createRecordValue(PACKAGE_ID_ASB, OPTIONAL_PROPERTIES);
            Object[] values = new Object[1];
            values[0] = toBMap(receivedMessage.getProperties());
            messageBObject.set(BPROPERTIES, ValueCreator.createRecordValue(optionalProperties, values));

            return messageBObject;
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Receive Messages with configurable parameters as Map when Receiver Connection is given as a parameter and
     * message content as a byte array and return Messages object.
     *
     * @param receiver Output Receiver connection.
     * @param serverWaitTime Specified server wait time in seconds to receive message.
     * @param maxMessageCount Maximum no. of messages in a batch
     * @return Message Object of the received message.
     */
    public static Object receiveMessages(IMessageReceiver receiver, Object serverWaitTime, Object maxMessageCount)
            throws Exception {
        try {
            // receive messages from queue or subscription
            String receivedMessageId = "";
            BArrayType sourceArrayType = null;
            BObject[] bObjectArray = new BObject[Long.valueOf(maxMessageCount.toString()).intValue()];
            int i = 0;

            BObject messagesBObject = ValueCreator.createObjectValue(ASBConstants.PACKAGE_ID_ASB,
                    ASBConstants.MESSAGES_OBJECT);

            log.info("\n\tWaiting up to 'serverWaitTime' seconds for messages from " + receiver.getEntityPath());
            while (true) {
                IMessage receivedMessage = receiver.receive(Duration.ofSeconds((long) serverWaitTime));

                if (receivedMessage == null || i == (long) maxMessageCount) {
                    break;
                }
                log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
                log.info("\t<= Received a message with messageBody \n" +
                        new String(receivedMessage.getBody(), UTF_8));
                receiver.complete(receivedMessage.getLockToken());

                if (receivedMessageId.contentEquals(receivedMessage.getMessageId())) {
                    return ASBUtils.returnErrorValue("Received a duplicate message!");
                }
                receivedMessageId = receivedMessage.getMessageId();

                BObject messageBObject = ValueCreator.createObjectValue(ASBConstants.PACKAGE_ID_ASB,
                        ASBConstants.MESSAGE_OBJECT);
                messageBObject.set(ASBConstants.MESSAGE_CONTENT,
                        ValueCreator.createArrayValue(receivedMessage.getBody()));
                messageBObject.set(MESSAGE_CONTENT_TYPE, StringUtils.fromString(receivedMessage.getContentType()));
                messageBObject.set(BMESSAGE_ID, StringUtils.fromString(receivedMessage.getMessageId()));
                messageBObject.set(BTO, StringUtils.fromString(receivedMessage.getTo()));
                messageBObject.set(BREPLY_TO, StringUtils.fromString(receivedMessage.getReplyTo()));
                messageBObject.set(BREPLY_TO_SESSION_ID,
                        StringUtils.fromString(receivedMessage.getReplyToSessionId()));
                messageBObject.set(BLABEL, StringUtils.fromString(receivedMessage.getLabel()));
                messageBObject.set(BSESSION_ID, StringUtils.fromString(receivedMessage.getSessionId()));
                messageBObject.set(BCORRELATION_ID, StringUtils.fromString(receivedMessage.getCorrelationId()));
                messageBObject.set(BTIME_TO_LIVE, receivedMessage.getTimeToLive().getSeconds());
                BMap<BString, Object> optionalProperties =
                        ValueCreator.createRecordValue(PACKAGE_ID_ASB, OPTIONAL_PROPERTIES);
                Object[] values = new Object[1];
                values[0] = toBMap(receivedMessage.getProperties());
                messageBObject.set(BPROPERTIES, ValueCreator.createRecordValue(optionalProperties, values));
                bObjectArray[i] = messageBObject;
                i = i + 1;
                sourceArrayType = new BArrayType(messageBObject.getType());
            }
            log.info("\tDone receiving messages from \n" + receiver.getEntityPath());
            if(sourceArrayType != null) {
                messagesBObject.set(ASBConstants.MESSAGES_CONTENT,
                        ValueCreator.createArrayValue(bObjectArray, sourceArrayType));
                messagesBObject.set(ASBConstants.MESSAGE_COUNT, i);
            }
            return messagesBObject;
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (Exception e) {
            throw ASBUtils.returnErrorValue("Receiving Messages Failed: " + e.getMessage());
        }
    }

    /**
     * Send Batch of Messages with configurable parameters when Sender Connection is given as a parameter and
     * message content as a byte array, optional parameters as a BMap and maximum message count in a batch as int.
     *
     * @param sender Input Sender connection.
     * @param content Input message content as byte array
     * @param parameters Input message optional parameters specified as a BMap
     * @param properties Input Message properties
     * @param maxMessageCount Maximum no. of messages in a batch
     */
    public static void sendBatchMessage(IMessageSender sender, BArray content, BMap<String, String> parameters,
                                        BMap<String, String> properties, Object maxMessageCount) throws Exception {
        Map<String,String> map = toStringMap(parameters);

        String contentType = valueToStringOrEmpty(map, CONTENT_TYPE);
        String messageId = map.get(MESSAGE_ID) != null ? map.get(MESSAGE_ID) : UUID.randomUUID().toString();
        String to = valueToStringOrEmpty(map, TO);
        String replyTo = valueToStringOrEmpty(map, REPLY_TO);
        String replyToSessionId = valueToStringOrEmpty(map, REPLY_TO_SESSION_ID);
        String label = valueToStringOrEmpty(map,LABEL);
        String sessionId = valueToStringOrEmpty(map, SESSION_ID);
        String correlationId = valueToStringOrEmpty(map, CORRELATION_ID);
        int timeToLive = map.get(TIME_TO_LIVE) != null ? Integer.parseInt(map.get(TIME_TO_LIVE)) : DEFAULT_TIME_TO_LIVE;

        try {
            List<IMessage> messages = new ArrayList<>();

            for(int i = 0; i < (long) maxMessageCount; i++) {
                IMessage message = new Message();
                messageId = map.get(MESSAGE_ID) != null ? map.get(MESSAGE_ID) : UUID.randomUUID().toString();
                message.setMessageId(messageId);
                message.setTimeToLive(Duration.ofMinutes(timeToLive));
                byte[] byteArray = content.get(i).toString().getBytes();
                message.setBody(byteArray);
                message.setContentType(contentType);
                message.setMessageId(messageId);
                message.setTo(to);
                message.setReplyTo(replyTo);
                message.setReplyToSessionId(replyToSessionId);
                message.setLabel(label);
                message.setSessionId(sessionId);
                message.setCorrelationId(correlationId);
                Map<String,String> propertiesMap = toStringMap(properties);
                message.setProperties(propertiesMap);

                messages.add(message);
                log.info("\t=> Sending a message with messageId \n" + message.getMessageId());
            }

            // Send messages to queue or topic
            log.info("\tSending messages to  ...\n" + sender.getEntityPath());
            sender.sendBatch(messages);
            log.info("\t=> Sent  messages\n" + messages.size());
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Receive Batch of Messages with configurable parameters as Map when Receiver Connection is given as a parameter,
     * message content as a byte array, maximum message count in a batch as int and return Messages object.
     *
     * @param receiver Output Receiver connection.
     * @param maxMessageCount Maximum no. of messages in a batch
     * @return Message Object of the received message.
     */
    public static Object receiveBatchMessage(IMessageReceiver receiver, Object maxMessageCount) throws Exception {
        try {
            // receive messages from queue or subscription
            String receivedMessageId = "";
            BArrayType sourceArrayType = null;
            BObject[] bObjectArray = new BObject[Long.valueOf(maxMessageCount.toString()).intValue()];
            int i = 0;

            BObject messagesBObject = ValueCreator.createObjectValue(ASBConstants.PACKAGE_ID_ASB,
                    ASBConstants.MESSAGES_OBJECT);

            log.info("\n\tWaiting up to default server time for messages from  ...\n" + receiver.getEntityPath());
            for(int j=0; j < (long) maxMessageCount; j++) {
                IMessage receivedMessage = receiver.receive();

                if (receivedMessage == null) {
                    continue;
                }
                log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
                log.info("\t<= Received a message with messageBody \n" +
                        new String(receivedMessage.getBody(), UTF_8));
                receiver.complete(receivedMessage.getLockToken());

                if (receivedMessageId.contentEquals(receivedMessage.getMessageId())) {
                    return ASBUtils.returnErrorValue("Received a duplicate message!");
                }
                receivedMessageId = receivedMessage.getMessageId();

                BObject messageBObject = ValueCreator.createObjectValue(ASBConstants.PACKAGE_ID_ASB,
                        ASBConstants.MESSAGE_OBJECT);
                messageBObject.set(ASBConstants.MESSAGE_CONTENT,
                        ValueCreator.createArrayValue(receivedMessage.getBody()));
                messageBObject.set(MESSAGE_CONTENT_TYPE, StringUtils.fromString(receivedMessage.getContentType()));
                messageBObject.set(BMESSAGE_ID, StringUtils.fromString(receivedMessage.getMessageId()));
                messageBObject.set(BTO, StringUtils.fromString(receivedMessage.getTo()));
                messageBObject.set(BREPLY_TO, StringUtils.fromString(receivedMessage.getReplyTo()));
                messageBObject.set(BREPLY_TO_SESSION_ID,
                        StringUtils.fromString(receivedMessage.getReplyToSessionId()));
                messageBObject.set(BLABEL, StringUtils.fromString(receivedMessage.getLabel()));
                messageBObject.set(BSESSION_ID, StringUtils.fromString(receivedMessage.getSessionId()));
                messageBObject.set(BCORRELATION_ID, StringUtils.fromString(receivedMessage.getCorrelationId()));
                messageBObject.set(BTIME_TO_LIVE, receivedMessage.getTimeToLive().getSeconds());
                BMap<BString, Object> optionalProperties =
                        ValueCreator.createRecordValue(PACKAGE_ID_ASB, OPTIONAL_PROPERTIES);
                Object[] values = new Object[1];
                values[0] = toBMap(receivedMessage.getProperties());
                messageBObject.set(BPROPERTIES, ValueCreator.createRecordValue(optionalProperties, values));
                bObjectArray[i] = messageBObject;
                i = i + 1;
                sourceArrayType = new BArrayType(messageBObject.getType());
            }
            log.info("\tDone receiving messages from \n" + receiver.getEntityPath());
            if(sourceArrayType != null) {
                messagesBObject.set(ASBConstants.MESSAGES_CONTENT,
                        ValueCreator.createArrayValue(bObjectArray, sourceArrayType));
                messagesBObject.set(ASBConstants.MESSAGE_COUNT, i);
            }
            return messagesBObject;
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Complete Messages from Queue or Subscription based on messageLockToken
     *
     * @param receiver Output Receiver connection.
     */
    public static void completeMessages(IMessageReceiver receiver)
            throws Exception {
        try {
            // receive messages from queue
            String receivedMessageId = "";

            log.info("\n\tWaiting up to default server time for messages from  ...\n" + receiver.getEntityPath());
            while (true) {
                IMessage receivedMessage = receiver.receive();

                if (receivedMessage == null) {
                    break;
                }
                log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
                log.info("\t<= Completes a message with messageLockToken \n" +
                        receivedMessage.getLockToken());
                receiver.complete(receivedMessage.getLockToken());
                if (receivedMessageId.contentEquals(receivedMessage.getMessageId())) {
                    throw ASBUtils.returnErrorValue("Received a duplicate message!");
                }
                receivedMessageId = receivedMessage.getMessageId();
            }
            log.info("\tDone completing a message using its lock token from \n" + receiver.getEntityPath());
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Completes a single message from Queue or Subscription based on messageLockToken
     *
     * @param receiver Output Receiver connection.
     */
    public static void completeOneMessage(IMessageReceiver receiver) throws Exception {
        try {
            log.info("\nWaiting up to default server wait time for messages from  ...\n" +
                    receiver.getEntityPath());

            IMessage receivedMessage = receiver.receive();

            if (receivedMessage != null) {
                log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
                log.info("\t<= Completes a message with messageLockToken \n" +
                        receivedMessage.getLockToken());
                receiver.complete(receivedMessage.getLockToken());

                log.info("\tDone completing a message using its lock token from \n" +
                        receiver.getEntityPath());
            } else {
                log.info("\tNo message in the queue\n");
            }
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Abandon message & make available again for processing from Queue or Subscription based on messageLockToken
     *
     * @param receiver Output Receiver connection.
     */
    public static void abandonMessage(IMessageReceiver receiver) throws Exception {
        try {
            log.info("\n\tWaiting up to default server wait time for messages from  ...\n" +
                    receiver.getEntityPath());
            IMessage receivedMessage = receiver.receive();

            if (receivedMessage != null) {
                log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
                log.info("\t<= Abandon a message with messageLockToken \n" + receivedMessage.getLockToken());
                receiver.abandon(receivedMessage.getLockToken());

                log.info("\tDone abandoning a message using its lock token from \n" +
                        receiver.getEntityPath());
            } else {
                log.info("\t<= No message in the queue \n");
            }
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Dead-Letter the message & moves the message to the Dead-Letter Queue based on messageLockToken
     *
     * @param receiver Output Receiver connection.
     * @param deadLetterReason The dead letter reason.
     * @param deadLetterErrorDescription The dead letter error description.
     */
    public static void deadLetterMessage(IMessageReceiver receiver, Object deadLetterReason,
                                         Object deadLetterErrorDescription) throws Exception {
        try {
            log.info("\n\tWaiting up to default server wait time for messages from  ...\n" +
                    receiver.getEntityPath());
            IMessage receivedMessage = receiver.receive();

            if (receivedMessage != null) {
                log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
                log.info("\t<= Dead-Letter a message with messageLockToken \n" + receivedMessage.getLockToken());
                receiver.deadLetter(receivedMessage.getLockToken(), valueToEmptyOrToString(deadLetterReason),
                        valueToEmptyOrToString(deadLetterErrorDescription));

                log.info("\tDone dead-lettering a message using its lock token from \n" +
                        receiver.getEntityPath());
            } else {
                log.info("\t<= No message in the queue \n");
            }
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Defer the message in a Queue or Subscription based on messageLockToken
     *
     * @param receiver Output Receiver connection.
     */
    public static long deferMessage(IMessageReceiver receiver) throws Exception {
        try {
            log.info("\n\tWaiting up to default server wait time for messages from  ...\n" +
                    receiver.getEntityPath());
            IMessage receivedMessage = receiver.receive();

            if (receivedMessage != null) {
                log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
                log.info("\t<= Defer a message with messageLockToken \n" + receivedMessage.getLockToken());
                long sequenceNumber = receivedMessage.getSequenceNumber();
                receiver.defer(receivedMessage.getLockToken());

                log.info("\tDone deferring a message using its lock token from \n" +
                        receiver.getEntityPath());
                return sequenceNumber;
            } else {
                log.info("\t<= No message in the queue \n");
                return 0;
            }
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Receives a deferred Message. Deferred messages can only be received by using sequence number and return
     * Message object.
     *
     * @param receiver Output Receiver connection.
     * @param sequenceNumber Unique number assigned to a message by Service Bus. The sequence number is a unique 64-bit
     *                       integer assigned to a message as it is accepted and stored by the broker and functions as
     *                       its true identifier.
     * @return The received Message or null if there is no message for given sequence number.
     */
    public static Object receiveDeferredMessage(IMessageReceiver receiver, int sequenceNumber) throws Exception {
        try {
            log.info("\n\tWaiting up to default server Wait Time for messages from\n" + receiver.getEntityPath());

            IMessage receivedMessage = receiver.receiveDeferredMessage(sequenceNumber);

            if (receivedMessage == null) {
                return null;
            }
            log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
            log.info("\t<= Received a message with messageBody \n" +
                    new String(receivedMessage.getBody(), UTF_8));

            log.info("\tDone receiving messages from \n" + receiver.getEntityPath());

            BObject messageBObject = ValueCreator.createObjectValue(ASBConstants.PACKAGE_ID_ASB,
                    ASBConstants.MESSAGE_OBJECT);
            messageBObject.set(ASBConstants.MESSAGE_CONTENT, ValueCreator.createArrayValue(receivedMessage.getBody()));
            return messageBObject;
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * The operation renews lock on a message in a queue or subscription based on messageLockToken.
     *
     * @param receiver Output Receiver connection.
     */
    public static void renewLockOnMessage(IMessageReceiver receiver) throws Exception {
        try {
            log.info("\n\tWaiting up to default server wait time for messages from  ...\n" +
                    receiver.getEntityPath());
            IMessage receivedMessage = receiver.receive();

            if (receivedMessage != null) {
                log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
                log.info("\t<= Renew message with messageLockToken \n" + receivedMessage.getLockToken());
                receiver.renewMessageLock(receivedMessage);

                log.info("\tDone renewing a message using its lock token from \n" +
                        receiver.getEntityPath());
            } else {
                log.info("\t<= No message in the queue \n");
            }
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Set the prefetch count of the receiver.
     * Prefetch speeds up the message flow by aiming to have a message readily available for local retrieval when and
     * before the application asks for one using Receive. Setting a non-zero value prefetches PrefetchCount
     * number of messages. Setting the value to zero turns prefetch off. For both PEEKLOCK mode and
     * RECEIVEANDDELETE mode, the default value is 0.
     *
     * @param receiver Output Receiver connection.
     * @param prefetchCount The desired prefetch count.
     */
    public static void setPrefetchCount(IMessageReceiver receiver, int prefetchCount) throws Exception {
        try {
            receiver.setPrefetchCount(prefetchCount);
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Setting the prefetch value failed" + e.getMessage());
        }
    }

    /**
     * Get the map value as string or as empty based on the key.
     *
     * @param map Input map.
     * @param key Input key.
     * @return map value as a string or empty.
     */
    private static String valueToStringOrEmpty(Map<String, ?> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : value.toString();
    }

    /**
     * Get the value as string or as empty based on the object value.
     *
     * @param value Input value.
     * @return value as a string or empty.
     */
    private static String valueToEmptyOrToString(Object value) {
        return (value == null) ? "" : value.toString();
    }

    public ConnectionUtils() {
    }

    public ConnectionUtils(String connectionString) {
        this.connectionString = connectionString;
    }
}
