package org.protobee.examples.emotion;

import java.util.Random;

import org.protobee.examples.protos.EmotionProtos.EmotionMessage.Type;

import com.google.inject.Inject;
import com.google.protobuf.ByteString;

public class EmotionProvider {

  private final Random random;
  
  @Inject
  public EmotionProvider(){
    random = new Random();
  }
  
  public Type getEmotion(){
    return Type.values()[random.nextInt(Type.values().length)];
  }
  
  public ByteString getNewEmotionId(){
    byte[] id = new byte[16];
    random.nextBytes(id);
    return ByteString.copyFrom(id);
  }
  
}
