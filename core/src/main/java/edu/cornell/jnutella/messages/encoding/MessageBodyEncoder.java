package edu.cornell.jnutella.messages.encoding;

import edu.cornell.jnutella.messages.MessageBody;

public interface MessageBodyEncoder<T extends MessageBody> extends PartEncoder<MessageBody> {}
