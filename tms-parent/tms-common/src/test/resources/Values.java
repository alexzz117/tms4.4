// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Values.proto

public final class Values {
  private Values() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface VMapOrBuilder extends
      // @@protoc_insertion_point(interface_extends:VMap)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>map&lt;string, string&gt; myMap = 1;</code>
     */
    int getMyMapCount();
    /**
     * <code>map&lt;string, string&gt; myMap = 1;</code>
     */
    boolean containsMyMap(
        java.lang.String key);
    /**
     * Use {@link #getMyMapMap()} instead.
     */
    @java.lang.Deprecated
    java.util.Map<java.lang.String, java.lang.String>
    getMyMap();
    /**
     * <code>map&lt;string, string&gt; myMap = 1;</code>
     */
    java.util.Map<java.lang.String, java.lang.String>
    getMyMapMap();
    /**
     * <code>map&lt;string, string&gt; myMap = 1;</code>
     */

    java.lang.String getMyMapOrDefault(
        java.lang.String key,
        java.lang.String defaultValue);
    /**
     * <code>map&lt;string, string&gt; myMap = 1;</code>
     */

    java.lang.String getMyMapOrThrow(
        java.lang.String key);
  }
  /**
   * Protobuf type {@code VMap}
   */
  public  static final class VMap extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:VMap)
      VMapOrBuilder {
    // Use VMap.newBuilder() to construct.
    private VMap(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private VMap() {
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
    }
    private VMap(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!input.skipField(tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                myMap_ = com.google.protobuf.MapField.newMapField(
                    MyMapDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000001;
              }
              com.google.protobuf.MapEntry<java.lang.String, java.lang.String>
              myMap__ = input.readMessage(
                  MyMapDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
              myMap_.getMutableMap().put(
                  myMap__.getKey(), myMap__.getValue());
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return Values.internal_static_VMap_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(
        int number) {
      switch (number) {
        case 1:
          return internalGetMyMap();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return Values.internal_static_VMap_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              Values.VMap.class, Values.VMap.Builder.class);
    }

    public static final int MYMAP_FIELD_NUMBER = 1;
    private static final class MyMapDefaultEntryHolder {
      static final com.google.protobuf.MapEntry<
          java.lang.String, java.lang.String> defaultEntry =
              com.google.protobuf.MapEntry
              .<java.lang.String, java.lang.String>newDefaultInstance(
                  Values.internal_static_VMap_MyMapEntry_descriptor, 
                  com.google.protobuf.WireFormat.FieldType.STRING,
                  "",
                  com.google.protobuf.WireFormat.FieldType.STRING,
                  "");
    }
    private com.google.protobuf.MapField<
        java.lang.String, java.lang.String> myMap_;
    private com.google.protobuf.MapField<java.lang.String, java.lang.String>
    internalGetMyMap() {
      if (myMap_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            MyMapDefaultEntryHolder.defaultEntry);
      }
      return myMap_;
    }

    public int getMyMapCount() {
      return internalGetMyMap().getMap().size();
    }
    /**
     * <code>map&lt;string, string&gt; myMap = 1;</code>
     */

    public boolean containsMyMap(
        java.lang.String key) {
      if (key == null) { throw new java.lang.NullPointerException(); }
      return internalGetMyMap().getMap().containsKey(key);
    }
    /**
     * Use {@link #getMyMapMap()} instead.
     */
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, java.lang.String> getMyMap() {
      return getMyMapMap();
    }
    /**
     * <code>map&lt;string, string&gt; myMap = 1;</code>
     */

    public java.util.Map<java.lang.String, java.lang.String> getMyMapMap() {
      return internalGetMyMap().getMap();
    }
    /**
     * <code>map&lt;string, string&gt; myMap = 1;</code>
     */

    public java.lang.String getMyMapOrDefault(
        java.lang.String key,
        java.lang.String defaultValue) {
      if (key == null) { throw new java.lang.NullPointerException(); }
      java.util.Map<java.lang.String, java.lang.String> map =
          internalGetMyMap().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /**
     * <code>map&lt;string, string&gt; myMap = 1;</code>
     */

    public java.lang.String getMyMapOrThrow(
        java.lang.String key) {
      if (key == null) { throw new java.lang.NullPointerException(); }
      java.util.Map<java.lang.String, java.lang.String> map =
          internalGetMyMap().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      com.google.protobuf.GeneratedMessageV3
        .serializeStringMapTo(
          output,
          internalGetMyMap(),
          MyMapDefaultEntryHolder.defaultEntry,
          1);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      for (java.util.Map.Entry<java.lang.String, java.lang.String> entry
           : internalGetMyMap().getMap().entrySet()) {
        com.google.protobuf.MapEntry<java.lang.String, java.lang.String>
        myMap__ = MyMapDefaultEntryHolder.defaultEntry.newBuilderForType()
            .setKey(entry.getKey())
            .setValue(entry.getValue())
            .build();
        size += com.google.protobuf.CodedOutputStream
            .computeMessageSize(1, myMap__);
      }
      memoizedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof Values.VMap)) {
        return super.equals(obj);
      }
      Values.VMap other = (Values.VMap) obj;

      boolean result = true;
      result = result && internalGetMyMap().equals(
          other.internalGetMyMap());
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      if (!internalGetMyMap().getMap().isEmpty()) {
        hash = (37 * hash) + MYMAP_FIELD_NUMBER;
        hash = (53 * hash) + internalGetMyMap().hashCode();
      }
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static Values.VMap parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static Values.VMap parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static Values.VMap parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static Values.VMap parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static Values.VMap parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static Values.VMap parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static Values.VMap parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static Values.VMap parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static Values.VMap parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static Values.VMap parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static Values.VMap parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static Values.VMap parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(Values.VMap prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code VMap}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:VMap)
        Values.VMapOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return Values.internal_static_VMap_descriptor;
      }

      @SuppressWarnings({"rawtypes"})
      protected com.google.protobuf.MapField internalGetMapField(
          int number) {
        switch (number) {
          case 1:
            return internalGetMyMap();
          default:
            throw new RuntimeException(
                "Invalid map field number: " + number);
        }
      }
      @SuppressWarnings({"rawtypes"})
      protected com.google.protobuf.MapField internalGetMutableMapField(
          int number) {
        switch (number) {
          case 1:
            return internalGetMutableMyMap();
          default:
            throw new RuntimeException(
                "Invalid map field number: " + number);
        }
      }
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return Values.internal_static_VMap_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                Values.VMap.class, Values.VMap.Builder.class);
      }

      // Construct using Values.VMap.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        internalGetMutableMyMap().clear();
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return Values.internal_static_VMap_descriptor;
      }

      public Values.VMap getDefaultInstanceForType() {
        return Values.VMap.getDefaultInstance();
      }

      public Values.VMap build() {
        Values.VMap result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public Values.VMap buildPartial() {
        Values.VMap result = new Values.VMap(this);
        int from_bitField0_ = bitField0_;
        result.myMap_ = internalGetMyMap();
        result.myMap_.makeImmutable();
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof Values.VMap) {
          return mergeFrom((Values.VMap)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(Values.VMap other) {
        if (other == Values.VMap.getDefaultInstance()) return this;
        internalGetMutableMyMap().mergeFrom(
            other.internalGetMyMap());
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        Values.VMap parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (Values.VMap) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private com.google.protobuf.MapField<
          java.lang.String, java.lang.String> myMap_;
      private com.google.protobuf.MapField<java.lang.String, java.lang.String>
      internalGetMyMap() {
        if (myMap_ == null) {
          return com.google.protobuf.MapField.emptyMapField(
              MyMapDefaultEntryHolder.defaultEntry);
        }
        return myMap_;
      }
      private com.google.protobuf.MapField<java.lang.String, java.lang.String>
      internalGetMutableMyMap() {
        onChanged();;
        if (myMap_ == null) {
          myMap_ = com.google.protobuf.MapField.newMapField(
              MyMapDefaultEntryHolder.defaultEntry);
        }
        if (!myMap_.isMutable()) {
          myMap_ = myMap_.copy();
        }
        return myMap_;
      }

      public int getMyMapCount() {
        return internalGetMyMap().getMap().size();
      }
      /**
       * <code>map&lt;string, string&gt; myMap = 1;</code>
       */

      public boolean containsMyMap(
          java.lang.String key) {
        if (key == null) { throw new java.lang.NullPointerException(); }
        return internalGetMyMap().getMap().containsKey(key);
      }
      /**
       * Use {@link #getMyMapMap()} instead.
       */
      @java.lang.Deprecated
      public java.util.Map<java.lang.String, java.lang.String> getMyMap() {
        return getMyMapMap();
      }
      /**
       * <code>map&lt;string, string&gt; myMap = 1;</code>
       */

      public java.util.Map<java.lang.String, java.lang.String> getMyMapMap() {
        return internalGetMyMap().getMap();
      }
      /**
       * <code>map&lt;string, string&gt; myMap = 1;</code>
       */

      public java.lang.String getMyMapOrDefault(
          java.lang.String key,
          java.lang.String defaultValue) {
        if (key == null) { throw new java.lang.NullPointerException(); }
        java.util.Map<java.lang.String, java.lang.String> map =
            internalGetMyMap().getMap();
        return map.containsKey(key) ? map.get(key) : defaultValue;
      }
      /**
       * <code>map&lt;string, string&gt; myMap = 1;</code>
       */

      public java.lang.String getMyMapOrThrow(
          java.lang.String key) {
        if (key == null) { throw new java.lang.NullPointerException(); }
        java.util.Map<java.lang.String, java.lang.String> map =
            internalGetMyMap().getMap();
        if (!map.containsKey(key)) {
          throw new java.lang.IllegalArgumentException();
        }
        return map.get(key);
      }

      public Builder clearMyMap() {
        internalGetMutableMyMap().getMutableMap()
            .clear();
        return this;
      }
      /**
       * <code>map&lt;string, string&gt; myMap = 1;</code>
       */

      public Builder removeMyMap(
          java.lang.String key) {
        if (key == null) { throw new java.lang.NullPointerException(); }
        internalGetMutableMyMap().getMutableMap()
            .remove(key);
        return this;
      }
      /**
       * Use alternate mutation accessors instead.
       */
      @java.lang.Deprecated
      public java.util.Map<java.lang.String, java.lang.String>
      getMutableMyMap() {
        return internalGetMutableMyMap().getMutableMap();
      }
      /**
       * <code>map&lt;string, string&gt; myMap = 1;</code>
       */
      public Builder putMyMap(
          java.lang.String key,
          java.lang.String value) {
        if (key == null) { throw new java.lang.NullPointerException(); }
        if (value == null) { throw new java.lang.NullPointerException(); }
        internalGetMutableMyMap().getMutableMap()
            .put(key, value);
        return this;
      }
      /**
       * <code>map&lt;string, string&gt; myMap = 1;</code>
       */

      public Builder putAllMyMap(
          java.util.Map<java.lang.String, java.lang.String> values) {
        internalGetMutableMyMap().getMutableMap()
            .putAll(values);
        return this;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return this;
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return this;
      }


      // @@protoc_insertion_point(builder_scope:VMap)
    }

    // @@protoc_insertion_point(class_scope:VMap)
    private static final Values.VMap DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new Values.VMap();
    }

    public static Values.VMap getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<VMap>
        PARSER = new com.google.protobuf.AbstractParser<VMap>() {
      public VMap parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new VMap(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<VMap> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<VMap> getParserForType() {
      return PARSER;
    }

    public Values.VMap getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_VMap_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_VMap_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_VMap_MyMapEntry_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_VMap_MyMapEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\014Values.proto\"U\n\004VMap\022\037\n\005myMap\030\001 \003(\0132\020." +
      "VMap.MyMapEntry\032,\n\nMyMapEntry\022\013\n\003key\030\001 \001" +
      "(\t\022\r\n\005value\030\002 \001(\t:\0028\001b\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_VMap_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_VMap_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_VMap_descriptor,
        new java.lang.String[] { "MyMap", });
    internal_static_VMap_MyMapEntry_descriptor =
      internal_static_VMap_descriptor.getNestedTypes().get(0);
    internal_static_VMap_MyMapEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_VMap_MyMapEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}