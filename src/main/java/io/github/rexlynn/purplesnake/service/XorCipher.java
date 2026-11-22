package io.github.rexlynn.purplesnake.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * XOR with a repeating key.
 *
 * NOTE: this is obfuscation, NOT secure encryption. A short repeating key can be
 * recovered with simple analysis. The useful property of XOR: (a ^ k) ^ k == a,
 * so the same operation both "encrypts" and "decrypts".
 */
public class XorCipher {

    private static final int BUFFER_SIZE = 8192; // read and write in 8 KB blocks

    private final byte[] key;

    public XorCipher(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key must not be empty.");
        }
        this.key = key.getBytes(StandardCharsets.UTF_8);
    }

    /** XORs a whole byte array in memory (used for small data and in tests). */
    public byte[] apply(byte[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = xorByte(data[i], i);
        }
        return result;
    }

    /** XORs a file block by block, so even big files need only a small amount of memory. */
    public void processFile(Path input, Path output) throws IOException {
        Path in = input.toAbsolutePath().normalize();
        Path out = output.toAbsolutePath().normalize();

        // If both paths point to the same file, opening the output would empty the input
        // before we read it. normalize() removes things like "./", isSameFile() follows links.
        if (in.equals(out) || (Files.exists(out) && Files.isSameFile(in, out))) {
            throw new IllegalArgumentException("Input and output files must be different.");
        }

        try (InputStream fin = new BufferedInputStream(Files.newInputStream(in));
             OutputStream fout = new BufferedOutputStream(Files.newOutputStream(out))) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long position = 0; // position in the whole file, decides which key byte is used

            // read(buffer) returns how many bytes it read, or -1 at the end of the file
            while ((bytesRead = fin.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    buffer[i] = xorByte(buffer[i], position);
                    position++;
                }
                fout.write(buffer, 0, bytesRead); // write only what was read
            }
        }
    }

    private byte xorByte(byte data, long position) {
        byte keyByte = key[(int) (position % key.length)];
        // Java promotes both bytes to int for the XOR. The (byte) cast then keeps only the
        // lowest 8 bits, which are exactly the result of XOR-ing the two bytes.
        return (byte) (data ^ keyByte);
    }
}
