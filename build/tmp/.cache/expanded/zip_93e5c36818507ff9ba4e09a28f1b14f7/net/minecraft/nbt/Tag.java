package net.minecraft.nbt;

import java.io.DataOutput;
import java.io.IOException;

public interface Tag {
    int OBJECT_HEADER = 8;
    int ARRAY_HEADER = 12;
    int OBJECT_REFERENCE = 4;
    int STRING_SIZE = 28;
    byte TAG_END = 0;
    byte TAG_BYTE = 1;
    byte TAG_SHORT = 2;
    byte TAG_INT = 3;
    byte TAG_LONG = 4;
    byte TAG_FLOAT = 5;
    byte TAG_DOUBLE = 6;
    byte TAG_BYTE_ARRAY = 7;
    byte TAG_STRING = 8;
    byte TAG_LIST = 9;
    byte TAG_COMPOUND = 10;
    byte TAG_INT_ARRAY = 11;
    byte TAG_LONG_ARRAY = 12;
    byte TAG_ANY_NUMERIC = 99;
    int MAX_DEPTH = 512;

    void write(DataOutput output) throws IOException;

    @Override
    String toString();

    byte getId();

    TagType<?> getType();

    Tag copy();

    int sizeInBytes();

    default String getAsString() {
        return new StringTagVisitor().visit(this);
    }

    void accept(TagVisitor visitor);

    StreamTagVisitor.ValueResult accept(StreamTagVisitor visitor);

    default void acceptAsRoot(StreamTagVisitor visitor) {
        StreamTagVisitor.ValueResult streamtagvisitor$valueresult = visitor.visitRootEntry(this.getType());
        if (streamtagvisitor$valueresult == StreamTagVisitor.ValueResult.CONTINUE) {
            this.accept(visitor);
        }
    }
}
