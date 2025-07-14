package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import javax.crypto.Cipher;

/**
 * Channel handler that handles protocol encryption.
 *
 * @see Connection#setEncryptionKey
 */
public class CipherEncoder extends MessageToByteEncoder<ByteBuf> {
    private final CipherBase cipher;

    public CipherEncoder(Cipher cipher) {
        this.cipher = new CipherBase(cipher);
    }

    protected void encode(ChannelHandlerContext context, ByteBuf message, ByteBuf out) throws Exception {
        this.cipher.encipher(message, out);
    }
}
