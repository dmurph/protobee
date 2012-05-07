package org.protobee.gnutella.messages;

import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.extension.HUGEExtension;
import org.protobee.gnutella.util.URN;

public class TestUtils {

  public static GGEP[] getGGEPArray(){
    GGEP[] ggepArr = {null, new GGEP(), new GGEP()};
    ggepArr[2].put("key");
    ggepArr[2].put("byte=1", 1);
    ggepArr[2].put("int=2", 2);
    ggepArr[2].put("long=432", 432);
    ggepArr[2].put("string=hello", "hello");
    return ggepArr;
  }

  public static ResponseBody[][] getResponsesArray(){
    HUGEExtension[] hugeArr = getHUGEArray();
    GGEP[] ggepArr = getGGEPArray();
    ResponseBody[] responses = new ResponseBody[hugeArr.length * ggepArr.length];
    int count = 0;

    for (GGEP ggep : ggepArr){
      for (HUGEExtension huge : hugeArr){
        ResponseBody response = new ResponseBody((long) Integer.MAX_VALUE + 1l, 
          (long) Integer.MAX_VALUE + 1l,"FILENAME", huge, ggep);
        responses[count] = response;
        count++;
      }
    }
    ResponseBody[][] responseArr = {null, responses};
    return responseArr;
  }

  public static HUGEExtension[] getHUGEArray(){
    HUGEExtension[] hugeArr = {null, new HUGEExtension(new URN[0]), getHUGE()};
    return hugeArr;
  }
  
  public static HUGEExtension getHUGE(){
    URN[] urns = new URN[5];
    urns[0] = new URN("urn:sha1:ANCKHTQCWBTRNJIV4WNAE52SJUQCZO5C");
    urns[1] = new URN("urn:sha1:BBCKHTQCWBTRNJIV4WNAE52SJUQCZO5F");
    urns[2] = new URN("urn:sha1:CNCKHTQABCTRNJIV4WNAE52SJUQCZO5G");
    urns[3] = new URN("urn:sha1:DNCKHTQCWBTRNJIV4WNAE52SJUQCZO5H");
    urns[4] = new URN("urn:sha1:ENCKHTQCWABCNJIV4WNAE52SJUQCZO5J");
    return new HUGEExtension(urns);
  }

}
