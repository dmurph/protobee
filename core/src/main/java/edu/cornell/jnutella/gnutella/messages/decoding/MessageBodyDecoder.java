package edu.cornell.jnutella.gnutella.messages.decoding;

import edu.cornell.jnutella.gnutella.messages.MessageBody;

public interface MessageBodyDecoder<T extends MessageBody> extends PartDecoder<T> {
}
