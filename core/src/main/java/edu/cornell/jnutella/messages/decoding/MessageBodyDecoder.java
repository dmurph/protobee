package edu.cornell.jnutella.messages.decoding;

import edu.cornell.jnutella.messages.MessageBody;

public interface MessageBodyDecoder<T extends MessageBody> extends PartDecoder<T> {
}
