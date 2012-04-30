package org.protobee.gnutella.messages.decoding;

import org.protobee.gnutella.messages.MessageBody;

public interface MessageBodyDecoder<T extends MessageBody> extends PartDecoder<T> {
}
