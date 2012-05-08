package org.protobee.gnutella.extension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.protobee.gnutella.util.NameValue;
import org.protobee.util.ByteUtils;

import com.google.common.base.Preconditions;



/**
 * A mutable GGEP extension block. A GGEP block can be thought of as a collection of key/value
 * pairs. A key (extension header) cannot be greater than 15 bytes. The value (extension data) can
 * be 0 to 2^24-1 bytes. Values can be formatted as a number, boolean, or generic blob of binary
 * data. If necessary (e.g., for query replies), GGEP will COBS-encode values to remove null bytes.
 * The order of the extensions is immaterial. Extensions supported by LimeWire have keys specified
 * in this class (prefixed by GGEP_HEADER...)
 */
public class GGEP {

  /** The maximum size of a extension header (key). */
  public static final int MAX_KEY_SIZE_IN_BYTES = 15;

  /** The maximum size of a extension data (value). */
  public static final int MAX_VALUE_SIZE_IN_BYTES = 262143;

  /**
   * The GGEP prefix. A GGEP block will start with this byte value.
   */
  public static final byte GGEP_PREFIX_MAGIC_NUMBER = (byte) 0xC3;

  /**
   * The collection of key/value pairs. Rep. rationale: arrays of bytes are convenient for values
   * since they're easy to convert to numbers or strings. But strings are conventient for keys since
   * they define hashCode and equals.
   */
  private final Map<String, Object> properties = new TreeMap<String, Object>();

  /**
   * Cached hash code value to avoid calculating the hash code from the map each time.
   */
  private volatile int hashCode = 0;


  // ////////////////// Encoding/Decoding (Map <==> byte[]) ///////////////////


  public GGEP() {}

  public GGEP(Map<String, Object> properties) {
    Preconditions.checkNotNull(properties);
    this.properties.putAll(properties);
  }

  public GGEP(GGEP other) {
    Preconditions.checkNotNull(other);
    this.properties.putAll(other.properties);
    this.hashCode = other.hashCode;
  }


  /**
   * Merges the other's GGEP with this' GGEP.
   */
  public void merge(GGEP other) {
    Preconditions.checkNotNull(other);
    properties.putAll(other.properties);
  }

  public int getNumKeys() {
    return properties.size();
  }

  /**
   * Returns the amount of overhead that will be added when the following key/value pair is written.
   * 
   * This does *NOT* work for non-ASCII headers, or compressed data.
   */
  public int getHeaderOverhead(String key) {
    byte[] data = get(key);
    Preconditions.checkNotNull(data, "no data for key " + key);

    return 1 + // flags
        key.length() + // header
        data.length + // data
        1 + // required data length
        (data.length > 0x3F ? 1 : 0) + // optional data
        (data.length > 0xFFF ? 1 : 0); // more option data
  }

  // //////////////////////// Key/Value Mutators and Accessors ////////////////

  /**
   * Adds all the specified key/value pairs. TODO: Allow a value to be compressed.
   */
  public void putAll(List<? extends NameValue<?>> fields) throws IllegalArgumentException {
    for (NameValue<?> next : fields) {
      String key = next.getName();
      Object value = next.getValue();
      if (value == null)
        put(key);
      else if (value instanceof byte[])
        put(key, (byte[]) value);
      else if (value instanceof String)
        put(key, (String) value);
      else if (value instanceof Integer)
        put(key, ((Integer) value).intValue());
      else if (value instanceof Long)
        put(key, ((Long) value).longValue());
      else if (value instanceof Byte)
        put(key, ((Byte) value).byteValue());
      else
        throw new IllegalArgumentException("Unknown value: " + value);
    }
  }

  /**
   * Adds a key with data that should be compressed.
   */
  public void putCompressed(String key, byte[] value) throws IllegalArgumentException {
    validateKey(key);
    Preconditions.checkNotNull(value, "Null value for key " + key);
    // validateValue(value); // done when writing. TODO: do here?
    properties.put(key, new NeedsCompression(value));
  }

  /**
   * Adds a key with byte value.
   * 
   * @param key the name of the GGEP extension, whose length should be between 1 and 15, inclusive
   * @param value the GGEP extension data.
   */
  public void put(String key, byte value) throws IllegalArgumentException {
    put(key, new byte[] {value});
  }

  /**
   * Adds a key with raw byte value.
   * 
   * @param key the name of the GGEP extension, whose length should be between 1 and 15, inclusive
   * @param value the GGEP extension data
   * @exception IllegalArgumentException key is of an illegal length or if value is null.
   */
  public void put(String key, byte[] value) throws IllegalArgumentException {
    validateKey(key);
    validateValue(value, key);
    properties.put(key, value);
  }

  /**
   * Adds a key with string value, using the default character encoding.
   * 
   * @param key the name of the GGEP extension, whose length should be between 1 and 15, inclusive
   * @param value the GGEP extension data
   * @exception IllegalArgumentException key is of an illegal length or if value is null
   */
  public void put(String key, String value) throws IllegalArgumentException {
    put(key, value == null ? null : value.getBytes());
  }

  /**
   * Adds a key with integer value.
   * 
   * @param key the name of the GGEP extension, whose length should be between 1 and 15, inclusive
   * @param value the GGEP extension data, which should be an unsigned integer
   * @exception IllegalArgumentException key is of an illegal length or if value is negative
   */
  public void put(String key, int value) throws IllegalArgumentException {
    if (value < 0) // int2minLeb doesn't work on negative values
      throw new IllegalArgumentException("Negative value: " + value + " for key: " + key);
    put(key, ByteUtils.int2minLeb(value));
  }

  /**
   * Adds a key with long value.
   * 
   * @param key the name of the GGEP extension, whose length should be between 1 and 15, inclusive
   * @param value the GGEP extension data, which should be an unsigned long
   * @exception IllegalArgumentException key is of an illegal length of ir value is negative
   */
  public void put(String key, long value) throws IllegalArgumentException {
    if (value < 0) // long2minLeb doesn't work on negative values
      throw new IllegalArgumentException("Negative value: " + value + " for key: " + key);
    put(key, ByteUtils.long2minLeb(value));
  }

  /**
   * Adds a key without any value.
   * 
   * @param key the name of the GGEP extension, whose length should be between 1 and 15, inclusive
   * @exception IllegalArgumentException key is of an illegal length.
   */
  public void put(String key) throws IllegalArgumentException {
    validateKey(key);
    properties.put(key, null);
  }

  /**
   * Returns the value for a key, as raw bytes.
   * 
   * @param key the name of the GGEP extension
   * @return the GGEP extension data associated with the key
   * @exception BadGGEPPropertyException extension not found, was corrupt, or has no associated
   *            data. Note that BadGGEPPropertyException is is always thrown for extensions with no
   *            data; use hasKey instead.
   */
  public byte[] getBytes(String key) throws BadGGEPPropertyException {
    byte[] ret = get(key);
    if (ret == null) throw new BadGGEPPropertyException();
    return ret;
  }

  /**
   * Returns the value for a key, as a string.
   * 
   * @param key the name of the GGEP extension
   * @return the GGEP extension data associated with the key
   * @exception BadGGEPPropertyException extension not found, was corrupt, or has no associated
   *            data. Note that BadGGEPPropertyException is is always thrown for extensions with no
   *            data; use hasKey instead.
   */
  public String getString(String key) throws BadGGEPPropertyException {
    return new String(getBytes(key));
  }

  /**
   * Returns the value for a key, as an integer
   * 
   * @param key the name of the GGEP extension
   * @return the GGEP extension data associated with the key
   * @exception BadGGEPPropertyException extension not found, was corrupt, or has no associated
   *            data. Note that BadGGEPPropertyException is is always thrown for extensions with no
   *            data; use hasKey instead.
   */
  public int getInt(String key) throws BadGGEPPropertyException {
    byte[] bytes = getBytes(key);
    if (bytes.length < 1) throw new BadGGEPPropertyException("No bytes");
    if (bytes.length > 4) throw new BadGGEPPropertyException("Integer too big");
    return ByteUtils.leb2int(bytes, 0, bytes.length);
  }

  /**
   * Returns the value for a key as a long.
   * 
   * @param key the name of the GGEP extension
   * @return the GGEP extension data associated with the key
   * @exception BadGGEPPropertyException extension not found, was corrupt, or has no associated
   *            data. Note that BadGGEPPropertyException is is always thrown for extensions with no
   *            data; use hasKey instead.
   */
  public long getLong(String key) throws BadGGEPPropertyException {
    byte[] bytes = getBytes(key);
    if (bytes.length < 1) throw new BadGGEPPropertyException("No bytes");
    if (bytes.length > 8) throw new BadGGEPPropertyException("Integer too big");
    return ByteUtils.leb2long(bytes, 0, bytes.length);
  }

  /**
   * Returns whether this has the given key.
   * 
   * @param key the name of the GGEP extension
   * @return true if this has a key
   */
  public boolean hasKey(String key) {
    return properties.containsKey(key);
  }

  /**
   * Returns the set of keys.
   * 
   * @return a set of all the GGEP extension header name in this, each as a String.
   */
  public Set<String> getHeaders() {
    return properties.keySet();
  }

  /**
   * Returns whether this GGEP is empty or not.
   */
  public boolean isEmpty() {
    return properties.isEmpty();
  }

  /**
   * Gets the byte[] data from props.
   */
  public byte[] get(String key) {
    Object value = properties.get(key);
    if (value instanceof NeedsCompression)
      return ((NeedsCompression) value).data;
    else
      return (byte[]) value;
  }

  public boolean isCompressed(String key) {
    return properties.get(key) instanceof NeedsCompression;
  }

  private void validateKey(String key) throws IllegalArgumentException {
    byte[] bytes = key.getBytes();
    if (key.equals("") || (bytes.length > MAX_KEY_SIZE_IN_BYTES) || containsNull(bytes))
      throw new IllegalArgumentException();
  }

  private void validateValue(byte[] value, String key) throws IllegalArgumentException {
    if (value == null) throw new IllegalArgumentException("null value for key: " + key);
    if (value.length > MAX_VALUE_SIZE_IN_BYTES)
      throw new IllegalArgumentException("value (" + value + ") too large for key: " + key);
  }

  private boolean containsNull(byte[] bytes) {
    if (bytes != null) {
      for (int i = 0; i < bytes.length; i++)
        if (bytes[i] == 0x0) return true;
    }
    return false;
  }

  // ////////////////////////////// Miscellany ///////////////////////////////

  /**
   * @return True if the two Maps that represent header/data pairs are equivalent.
   */
  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof GGEP)) return false;
    // This is O(n lg n) time with n keys. It would be great if we could
    // just check that the trees are isomorphic. I don't think this code is
    // really used anywhere, however.
    return this.subset((GGEP) o) && ((GGEP) o).subset(this);
  }

  /**
   * Returns true if this is a subset of other, e.g., all of this' keys can be found in OTHER with
   * the same value.
   */
  private boolean subset(GGEP other) {
    for (String key : properties.keySet()) {
      byte[] v1 = this.get(key);
      byte[] v2 = other.get(key);
      // Remember that v1 and v2 can be null.
      if ((v1 == null) != (v2 == null)) return false;
      if (v1 != null && !Arrays.equals(v1, v2)) return false;
    }
    return true;
  }

  // overrides Object.hashCode to be consistent with equals
  @Override
  public int hashCode() {
    if (hashCode == 0) {
      hashCode = 37 * properties.hashCode();
    }
    return hashCode;
  }

  /**
   * Marker class that wraps a byte[] value, if that value is going to require compression upon
   * write. TODO: don't use this, and instead keep a set of keys that are flagged for compression
   */
  public static class NeedsCompression {
    final byte[] data;

    public NeedsCompression(byte[] data) {
      this.data = data;
    }
  }
}
