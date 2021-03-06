// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/java;

# Represents a single network sender connection to the Asb broker.
public client class SenderConnection {

    handle asbSenderConnection = JAVA_NULL;

    private string connectionString;
    private string entityPath;

    # Initiates an Asb Sender Connection using the given connection configuration.
    # 
    # + connectionConfiguration - Configurations used to create a `asb:SenderConnection`
    # + return - An `asb:Error` if failed to create connection or else `()`
    public isolated function init(ConnectionConfiguration connectionConfiguration) returns Error? {
        self.connectionString = connectionConfiguration.connectionString;
        self.entityPath = connectionConfiguration.entityPath;
        var connectionResult = createSenderConnection(java:fromString(self.connectionString),
            java:fromString(self.entityPath));
        if (connectionResult is handle) {
            self.asbSenderConnection = <handle> connectionResult;
            return;
        } else {
            return connectionResult;
        }        
    }

    # Creates a Asb Sender Connection using the given connection parameters.
    # 
    # + connectionConfiguration - Configurations used to create a `asb:SenderConnection`
    # + return - An `asb:Error` if failed to create connection or else `()`
    public isolated function createSenderConnection(ConnectionConfiguration connectionConfiguration) 
        returns Error? {
        self.connectionString = connectionConfiguration.connectionString;
        self.entityPath = connectionConfiguration.entityPath;
        var connectionResult = createSenderConnection(java:fromString(self.connectionString),
            java:fromString(self.entityPath));
        if (connectionResult is handle) {
            self.asbSenderConnection = <handle> connectionResult;
            return;
        } else {
            return connectionResult;
        }
    }

    # Closes the Asb Sender Connection using the given connection parameters.
    #
    # + return - An `asb:Error` if failed to close connection or else `()`
    public isolated function closeSenderConnection() returns Error? {
        var connectionResult = closeSenderConnection(self.asbSenderConnection);
        if (connectionResult is ()) {
            return;
        } else {
            return connectionResult;
        }
    }

    # Send message to queue with a content and optional parameters
    #
    # + content - MessageBody content
    # + parameters - Optional Message parameters 
    # + properties - Message properties
    # + return - An `asb:Error` if failed to send message or else `()`
    isolated remote function sendMessageWithConfigurableParameters(byte[] content = [], map<string> parameters = {},
        map<string> properties = {}) returns Error? {
        return sendMessageWithConfigurableParameters(self.asbSenderConnection, content, parameters, properties);
    }

    # Send message to queue with a content
    #
    # + content - MessageBody content
    # + contentType - Content type of the message content
    # + messageId - This is a user-defined value that Service Bus can use to identify duplicate messages, if enabled
    # + to - Send to address
    # + replyTo - Address of the queue to reply to
    # + replyToSessionId - Identifier of the session to reply to
    # + label - Application specific label
    # + sessionId - Identifier of the session
    # + correlationId - Identifier of the correlation
    # + properties - Message properties
    # + timeToLive - This is the duration, in ticks, that a message is valid. The duration starts from when the 
    #                message is sent to the Service Bus.
    # + return - An `asb:Error` if failed to send message or else `()`
    isolated remote function sendMessage(byte[] content = [], string? contentType = (), string? messageId = (), 
        string? to = (), string? replyTo = (), string? replyToSessionId = (), string? label = (), 
        string? sessionId = (), string? correlationId = (), map<string> properties = {}, 
            int? timeToLive = DEFAULT_TIME_TO_LIVE) returns Error? {
        return sendMessage(self.asbSenderConnection, content, contentType, messageId, to, replyTo, replyToSessionId, 
            label, sessionId, correlationId, properties, timeToLive);
    }

    # Send batch of messages to queue with a content and optional parameters
    #
    # + content - MessageBody content
    # + parameters - Optional Message parameters 
    # + properties - Message properties
    # + maxMessageCount - Maximum no. of messages in a batch
    # + return - An `asb:Error` if failed to send message or else `()`
    isolated remote function sendBatchMessage(string[] content = [], map<string> parameters = {}, 
        map<string> properties = {}, int? maxMessageCount = DEFAULT_MAX_MESSAGE_COUNT) returns Error? {
        return sendBatchMessage(self.asbSenderConnection, content, parameters, properties, maxMessageCount);
    }
}

isolated function createSenderConnection(handle connectionString, handle entityPath) 
    returns handle|Error? = @java:Method {
    name: "createSenderConnection",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function closeSenderConnection(handle imessageSender) returns Error? = @java:Method {
    name: "closeSenderConnection",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function sendMessageWithConfigurableParameters(handle imessageSender, byte[] content, map<string> parameters, 
    map<string> properties) returns Error? = @java:Method {
    name: "sendMessageWithConfigurableParameters",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function sendMessage(handle imessageSender, byte[] content, string? contentType, string? messageId, string? to, 
    string? replyTo, string? replyToSessionId, string? label, string? sessionId, string? correlationId, 
    map<string> properties, int? timeToLive) returns Error? = @java:Method {
    name: "sendMessage",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function sendBatchMessage(handle imessageSender, string[] content, map<string> parameters, 
    map<string> properties, int? maxMessageCount) returns Error? = @java:Method {
    name: "sendBatchMessage",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;
