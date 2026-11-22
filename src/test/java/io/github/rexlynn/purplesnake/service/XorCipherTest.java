package io.github.rexlynn.purplesnake.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XorCipherTest {

    @TempDir
    Path tempDir; // JUnit creates a fresh temporary folder for every test and deletes it afterwards

    @Test
    void knownValueIsCalculatedCorrectly() {
        // 'A' = 0x41, key 'k' = 0x6B, 0x41 ^ 0x6B = 0x2A
        byte[] result = new XorCipher("k").apply(new byte[] {0x41});
        assertArrayEquals(new byte[] {0x2A}, result);
    }

    @Test
    void applyingTwiceRestoresTheOriginal() {
        XorCipher cipher = new XorCipher("secret");
        byte[] original = "Hello, PurpleSnake!".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(original, cipher.apply(cipher.apply(original)));
    }

    @Test
    void keyIsRepeatedOverLongData() {
        byte[] result = new XorCipher("ab").apply(new byte[] {0, 0, 0, 0});
        assertArrayEquals(new byte[] {'a', 'b', 'a', 'b'}, result);
    }

    @Test
    void keyBytesAbove127AreHandledCorrectly() {
        // "é" is two bytes in UTF-8 (0xC3 0xA9). Java bytes are signed, so both are negative
        // numbers; the result must still be the correct bit pattern.
        byte[] result = new XorCipher("é").apply(new byte[] {0, 0});
        assertArrayEquals(new byte[] {(byte) 0xC3, (byte) 0xA9}, result);
    }

    @Test
    void emptyKeyIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new XorCipher(""));
        assertThrows(IllegalArgumentException.class, () -> new XorCipher(null));
    }

    @Test
    void fileRoundTripRestoresTheOriginal() throws IOException {
        // Bigger than the 8 KB buffer, so the loop runs several times
        byte[] data = new byte[20000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i * 31);
        }
        Path original = tempDir.resolve("original.bin");
        Path encrypted = tempDir.resolve("encrypted.bin");
        Path restored = tempDir.resolve("restored.bin");
        Files.write(original, data);

        XorCipher cipher = new XorCipher("my key é");
        cipher.processFile(original, encrypted);
        cipher.processFile(encrypted, restored);

        assertEquals(20000, Files.size(encrypted));
        assertArrayEquals(data, Files.readAllBytes(restored));
    }

    @Test
    void sameFileAsInputAndOutputIsRejectedAndDataSurvives() throws IOException {
        Path file = tempDir.resolve("data.txt");
        Files.writeString(file, "important");
        XorCipher cipher = new XorCipher("k");

        assertThrows(IllegalArgumentException.class, () -> cipher.processFile(file, file));
        // "./" in the path points to the same file
        Path sameFileOtherSpelling = tempDir.resolve(".").resolve("data.txt");
        assertThrows(IllegalArgumentException.class, () -> cipher.processFile(file, sameFileOtherSpelling));

        assertEquals("important", Files.readString(file)); // not emptied
    }

    @Test
    void missingInputFileThrowsIOException() {
        XorCipher cipher = new XorCipher("k");
        assertThrows(IOException.class,
                () -> cipher.processFile(tempDir.resolve("missing.txt"), tempDir.resolve("out.txt")));
    }
}
