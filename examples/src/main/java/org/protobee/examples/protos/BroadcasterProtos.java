// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: broadcaster.proto

package org.protobee.examples.protos;

public final class BroadcasterProtos {
  private BroadcasterProtos() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface BroadcastMessageOrBuilder
      extends com.google.protobuf.MessageOrBuilder {
    
    // required .protobee.Header header = 1;
    boolean hasHeader();
    org.protobee.examples.protos.Common.Header getHeader();
    org.protobee.examples.protos.Common.HeaderOrBuilder getHeaderOrBuilder();
    
    // optional string message = 2;
    boolean hasMessage();
    String getMessage();
    
    // optional int64 sendTimeMillis = 3;
    boolean hasSendTimeMillis();
    long getSendTimeMillis();
    
    // optional .protobee.SourceAddress sourceAddress = 4;
    boolean hasSourceAddress();
    org.protobee.examples.protos.Common.SourceAddress getSourceAddress();
    org.protobee.examples.protos.Common.SourceAddressOrBuilder getSourceAddressOrBuilder();
  }
  public static final class BroadcastMessage extends
      com.google.protobuf.GeneratedMessage
      implements BroadcastMessageOrBuilder {
    // Use BroadcastMessage.newBuilder() to construct.
    private BroadcastMessage(Builder builder) {
      super(builder);
    }
    private BroadcastMessage(boolean noInit) {}
    
    private static final BroadcastMessage defaultInstance;
    public static BroadcastMessage getDefaultInstance() {
      return defaultInstance;
    }
    
    public BroadcastMessage getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.protobee.examples.protos.BroadcasterProtos.internal_static_protobee_BroadcastMessage_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.protobee.examples.protos.BroadcasterProtos.internal_static_protobee_BroadcastMessage_fieldAccessorTable;
    }
    
    private int bitField0_;
    // required .protobee.Header header = 1;
    public static final int HEADER_FIELD_NUMBER = 1;
    private org.protobee.examples.protos.Common.Header header_;
    public boolean hasHeader() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    public org.protobee.examples.protos.Common.Header getHeader() {
      return header_;
    }
    public org.protobee.examples.protos.Common.HeaderOrBuilder getHeaderOrBuilder() {
      return header_;
    }
    
    // optional string message = 2;
    public static final int MESSAGE_FIELD_NUMBER = 2;
    private java.lang.Object message_;
    public boolean hasMessage() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    public String getMessage() {
      java.lang.Object ref = message_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (com.google.protobuf.Internal.isValidUtf8(bs)) {
          message_ = s;
        }
        return s;
      }
    }
    private com.google.protobuf.ByteString getMessageBytes() {
      java.lang.Object ref = message_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8((String) ref);
        message_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    
    // optional int64 sendTimeMillis = 3;
    public static final int SENDTIMEMILLIS_FIELD_NUMBER = 3;
    private long sendTimeMillis_;
    public boolean hasSendTimeMillis() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    public long getSendTimeMillis() {
      return sendTimeMillis_;
    }
    
    // optional .protobee.SourceAddress sourceAddress = 4;
    public static final int SOURCEADDRESS_FIELD_NUMBER = 4;
    private org.protobee.examples.protos.Common.SourceAddress sourceAddress_;
    public boolean hasSourceAddress() {
      return ((bitField0_ & 0x00000008) == 0x00000008);
    }
    public org.protobee.examples.protos.Common.SourceAddress getSourceAddress() {
      return sourceAddress_;
    }
    public org.protobee.examples.protos.Common.SourceAddressOrBuilder getSourceAddressOrBuilder() {
      return sourceAddress_;
    }
    
    private void initFields() {
      header_ = org.protobee.examples.protos.Common.Header.getDefaultInstance();
      message_ = "";
      sendTimeMillis_ = 0L;
      sourceAddress_ = org.protobee.examples.protos.Common.SourceAddress.getDefaultInstance();
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      if (!hasHeader()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!getHeader().isInitialized()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (hasSourceAddress()) {
        if (!getSourceAddress().isInitialized()) {
          memoizedIsInitialized = 0;
          return false;
        }
      }
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeMessage(1, header_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeBytes(2, getMessageBytes());
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeInt64(3, sendTimeMillis_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        output.writeMessage(4, sourceAddress_);
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, header_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, getMessageBytes());
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(3, sendTimeMillis_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(4, sourceAddress_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }
    
    public static org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements org.protobee.examples.protos.BroadcasterProtos.BroadcastMessageOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.protobee.examples.protos.BroadcasterProtos.internal_static_protobee_BroadcastMessage_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.protobee.examples.protos.BroadcasterProtos.internal_static_protobee_BroadcastMessage_fieldAccessorTable;
      }
      
      // Construct using org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }
      
      private Builder(BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
          getHeaderFieldBuilder();
          getSourceAddressFieldBuilder();
        }
      }
      private static Builder create() {
        return new Builder();
      }
      
      public Builder clear() {
        super.clear();
        if (headerBuilder_ == null) {
          header_ = org.protobee.examples.protos.Common.Header.getDefaultInstance();
        } else {
          headerBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000001);
        message_ = "";
        bitField0_ = (bitField0_ & ~0x00000002);
        sendTimeMillis_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000004);
        if (sourceAddressBuilder_ == null) {
          sourceAddress_ = org.protobee.examples.protos.Common.SourceAddress.getDefaultInstance();
        } else {
          sourceAddressBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000008);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage.getDescriptor();
      }
      
      public org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage getDefaultInstanceForType() {
        return org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage.getDefaultInstance();
      }
      
      public org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage build() {
        org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage buildPartial() {
        org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage result = new org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        if (headerBuilder_ == null) {
          result.header_ = header_;
        } else {
          result.header_ = headerBuilder_.build();
        }
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.message_ = message_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.sendTimeMillis_ = sendTimeMillis_;
        if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
          to_bitField0_ |= 0x00000008;
        }
        if (sourceAddressBuilder_ == null) {
          result.sourceAddress_ = sourceAddress_;
        } else {
          result.sourceAddress_ = sourceAddressBuilder_.build();
        }
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage) {
          return mergeFrom((org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage other) {
        if (other == org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage.getDefaultInstance()) return this;
        if (other.hasHeader()) {
          mergeHeader(other.getHeader());
        }
        if (other.hasMessage()) {
          setMessage(other.getMessage());
        }
        if (other.hasSendTimeMillis()) {
          setSendTimeMillis(other.getSendTimeMillis());
        }
        if (other.hasSourceAddress()) {
          mergeSourceAddress(other.getSourceAddress());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public final boolean isInitialized() {
        if (!hasHeader()) {
          
          return false;
        }
        if (!getHeader().isInitialized()) {
          
          return false;
        }
        if (hasSourceAddress()) {
          if (!getSourceAddress().isInitialized()) {
            
            return false;
          }
        }
        return true;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              onChanged();
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                onChanged();
                return this;
              }
              break;
            }
            case 10: {
              org.protobee.examples.protos.Common.Header.Builder subBuilder = org.protobee.examples.protos.Common.Header.newBuilder();
              if (hasHeader()) {
                subBuilder.mergeFrom(getHeader());
              }
              input.readMessage(subBuilder, extensionRegistry);
              setHeader(subBuilder.buildPartial());
              break;
            }
            case 18: {
              bitField0_ |= 0x00000002;
              message_ = input.readBytes();
              break;
            }
            case 24: {
              bitField0_ |= 0x00000004;
              sendTimeMillis_ = input.readInt64();
              break;
            }
            case 34: {
              org.protobee.examples.protos.Common.SourceAddress.Builder subBuilder = org.protobee.examples.protos.Common.SourceAddress.newBuilder();
              if (hasSourceAddress()) {
                subBuilder.mergeFrom(getSourceAddress());
              }
              input.readMessage(subBuilder, extensionRegistry);
              setSourceAddress(subBuilder.buildPartial());
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // required .protobee.Header header = 1;
      private org.protobee.examples.protos.Common.Header header_ = org.protobee.examples.protos.Common.Header.getDefaultInstance();
      private com.google.protobuf.SingleFieldBuilder<
          org.protobee.examples.protos.Common.Header, org.protobee.examples.protos.Common.Header.Builder, org.protobee.examples.protos.Common.HeaderOrBuilder> headerBuilder_;
      public boolean hasHeader() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      public org.protobee.examples.protos.Common.Header getHeader() {
        if (headerBuilder_ == null) {
          return header_;
        } else {
          return headerBuilder_.getMessage();
        }
      }
      public Builder setHeader(org.protobee.examples.protos.Common.Header value) {
        if (headerBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          header_ = value;
          onChanged();
        } else {
          headerBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      public Builder setHeader(
          org.protobee.examples.protos.Common.Header.Builder builderForValue) {
        if (headerBuilder_ == null) {
          header_ = builderForValue.build();
          onChanged();
        } else {
          headerBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      public Builder mergeHeader(org.protobee.examples.protos.Common.Header value) {
        if (headerBuilder_ == null) {
          if (((bitField0_ & 0x00000001) == 0x00000001) &&
              header_ != org.protobee.examples.protos.Common.Header.getDefaultInstance()) {
            header_ =
              org.protobee.examples.protos.Common.Header.newBuilder(header_).mergeFrom(value).buildPartial();
          } else {
            header_ = value;
          }
          onChanged();
        } else {
          headerBuilder_.mergeFrom(value);
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      public Builder clearHeader() {
        if (headerBuilder_ == null) {
          header_ = org.protobee.examples.protos.Common.Header.getDefaultInstance();
          onChanged();
        } else {
          headerBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }
      public org.protobee.examples.protos.Common.Header.Builder getHeaderBuilder() {
        bitField0_ |= 0x00000001;
        onChanged();
        return getHeaderFieldBuilder().getBuilder();
      }
      public org.protobee.examples.protos.Common.HeaderOrBuilder getHeaderOrBuilder() {
        if (headerBuilder_ != null) {
          return headerBuilder_.getMessageOrBuilder();
        } else {
          return header_;
        }
      }
      private com.google.protobuf.SingleFieldBuilder<
          org.protobee.examples.protos.Common.Header, org.protobee.examples.protos.Common.Header.Builder, org.protobee.examples.protos.Common.HeaderOrBuilder> 
          getHeaderFieldBuilder() {
        if (headerBuilder_ == null) {
          headerBuilder_ = new com.google.protobuf.SingleFieldBuilder<
              org.protobee.examples.protos.Common.Header, org.protobee.examples.protos.Common.Header.Builder, org.protobee.examples.protos.Common.HeaderOrBuilder>(
                  header_,
                  getParentForChildren(),
                  isClean());
          header_ = null;
        }
        return headerBuilder_;
      }
      
      // optional string message = 2;
      private java.lang.Object message_ = "";
      public boolean hasMessage() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      public String getMessage() {
        java.lang.Object ref = message_;
        if (!(ref instanceof String)) {
          String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
          message_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      public Builder setMessage(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        message_ = value;
        onChanged();
        return this;
      }
      public Builder clearMessage() {
        bitField0_ = (bitField0_ & ~0x00000002);
        message_ = getDefaultInstance().getMessage();
        onChanged();
        return this;
      }
      void setMessage(com.google.protobuf.ByteString value) {
        bitField0_ |= 0x00000002;
        message_ = value;
        onChanged();
      }
      
      // optional int64 sendTimeMillis = 3;
      private long sendTimeMillis_ ;
      public boolean hasSendTimeMillis() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      public long getSendTimeMillis() {
        return sendTimeMillis_;
      }
      public Builder setSendTimeMillis(long value) {
        bitField0_ |= 0x00000004;
        sendTimeMillis_ = value;
        onChanged();
        return this;
      }
      public Builder clearSendTimeMillis() {
        bitField0_ = (bitField0_ & ~0x00000004);
        sendTimeMillis_ = 0L;
        onChanged();
        return this;
      }
      
      // optional .protobee.SourceAddress sourceAddress = 4;
      private org.protobee.examples.protos.Common.SourceAddress sourceAddress_ = org.protobee.examples.protos.Common.SourceAddress.getDefaultInstance();
      private com.google.protobuf.SingleFieldBuilder<
          org.protobee.examples.protos.Common.SourceAddress, org.protobee.examples.protos.Common.SourceAddress.Builder, org.protobee.examples.protos.Common.SourceAddressOrBuilder> sourceAddressBuilder_;
      public boolean hasSourceAddress() {
        return ((bitField0_ & 0x00000008) == 0x00000008);
      }
      public org.protobee.examples.protos.Common.SourceAddress getSourceAddress() {
        if (sourceAddressBuilder_ == null) {
          return sourceAddress_;
        } else {
          return sourceAddressBuilder_.getMessage();
        }
      }
      public Builder setSourceAddress(org.protobee.examples.protos.Common.SourceAddress value) {
        if (sourceAddressBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          sourceAddress_ = value;
          onChanged();
        } else {
          sourceAddressBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000008;
        return this;
      }
      public Builder setSourceAddress(
          org.protobee.examples.protos.Common.SourceAddress.Builder builderForValue) {
        if (sourceAddressBuilder_ == null) {
          sourceAddress_ = builderForValue.build();
          onChanged();
        } else {
          sourceAddressBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000008;
        return this;
      }
      public Builder mergeSourceAddress(org.protobee.examples.protos.Common.SourceAddress value) {
        if (sourceAddressBuilder_ == null) {
          if (((bitField0_ & 0x00000008) == 0x00000008) &&
              sourceAddress_ != org.protobee.examples.protos.Common.SourceAddress.getDefaultInstance()) {
            sourceAddress_ =
              org.protobee.examples.protos.Common.SourceAddress.newBuilder(sourceAddress_).mergeFrom(value).buildPartial();
          } else {
            sourceAddress_ = value;
          }
          onChanged();
        } else {
          sourceAddressBuilder_.mergeFrom(value);
        }
        bitField0_ |= 0x00000008;
        return this;
      }
      public Builder clearSourceAddress() {
        if (sourceAddressBuilder_ == null) {
          sourceAddress_ = org.protobee.examples.protos.Common.SourceAddress.getDefaultInstance();
          onChanged();
        } else {
          sourceAddressBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000008);
        return this;
      }
      public org.protobee.examples.protos.Common.SourceAddress.Builder getSourceAddressBuilder() {
        bitField0_ |= 0x00000008;
        onChanged();
        return getSourceAddressFieldBuilder().getBuilder();
      }
      public org.protobee.examples.protos.Common.SourceAddressOrBuilder getSourceAddressOrBuilder() {
        if (sourceAddressBuilder_ != null) {
          return sourceAddressBuilder_.getMessageOrBuilder();
        } else {
          return sourceAddress_;
        }
      }
      private com.google.protobuf.SingleFieldBuilder<
          org.protobee.examples.protos.Common.SourceAddress, org.protobee.examples.protos.Common.SourceAddress.Builder, org.protobee.examples.protos.Common.SourceAddressOrBuilder> 
          getSourceAddressFieldBuilder() {
        if (sourceAddressBuilder_ == null) {
          sourceAddressBuilder_ = new com.google.protobuf.SingleFieldBuilder<
              org.protobee.examples.protos.Common.SourceAddress, org.protobee.examples.protos.Common.SourceAddress.Builder, org.protobee.examples.protos.Common.SourceAddressOrBuilder>(
                  sourceAddress_,
                  getParentForChildren(),
                  isClean());
          sourceAddress_ = null;
        }
        return sourceAddressBuilder_;
      }
      
      // @@protoc_insertion_point(builder_scope:protobee.BroadcastMessage)
    }
    
    static {
      defaultInstance = new BroadcastMessage(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:protobee.BroadcastMessage)
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_protobee_BroadcastMessage_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_protobee_BroadcastMessage_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\021broadcaster.proto\022\010protobee\032\014common.pr" +
      "oto\"\215\001\n\020BroadcastMessage\022 \n\006header\030\001 \002(\013" +
      "2\020.protobee.Header\022\017\n\007message\030\002 \001(\t\022\026\n\016s" +
      "endTimeMillis\030\003 \001(\003\022.\n\rsourceAddress\030\004 \001" +
      "(\0132\027.protobee.SourceAddressB1\n\034org.proto" +
      "bee.examples.protosB\021BroadcasterProtos"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_protobee_BroadcastMessage_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_protobee_BroadcastMessage_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_protobee_BroadcastMessage_descriptor,
              new java.lang.String[] { "Header", "Message", "SendTimeMillis", "SourceAddress", },
              org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage.class,
              org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          org.protobee.examples.protos.Common.getDescriptor(),
        }, assigner);
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}
