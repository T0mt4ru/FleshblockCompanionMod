package net.minecraft.util.eventlog;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import javax.annotation.Nullable;

public interface JsonEventLogReader<T> extends Closeable {
    static <T> JsonEventLogReader<T> create(final Codec<T> codec, Reader reader) {
        final JsonReader jsonreader = new JsonReader(reader);
        jsonreader.setLenient(true);
        return new JsonEventLogReader<T>() {
            @Nullable
            @Override
            public T next() throws IOException {
                try {
                    if (!jsonreader.hasNext()) {
                        return null;
                    } else {
                        JsonElement jsonelement = JsonParser.parseReader(jsonreader);
                        return codec.parse(JsonOps.INSTANCE, jsonelement).getOrThrow(IOException::new);
                    }
                } catch (JsonParseException jsonparseexception) {
                    throw new IOException(jsonparseexception);
                } catch (EOFException eofexception) {
                    return null;
                }
            }

            @Override
            public void close() throws IOException {
                jsonreader.close();
            }
        };
    }

    @Nullable
    T next() throws IOException;
}
