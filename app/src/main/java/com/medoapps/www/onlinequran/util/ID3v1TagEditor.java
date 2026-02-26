package com.medoapps.www.onlinequran.util;

import com.medoapps.www.onlinequran.models.ID3v1Tags;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

//https://id3.org/ID3v1
//https://en.wikipedia.org/wiki/ID3
public class ID3v1TagEditor {

    public ID3v1Tags readID3v1Tags(File file) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        FileChannel channel = randomAccessFile.getChannel();

        boolean hasTag = hasID3v1Tag(channel);
        if (!hasTag) {
            channel.close();
            return null;
        }

        String title = removeTrailingNullBytes(readTag(channel, 30));
        String artist = removeTrailingNullBytes(readTag(channel, 30));
        String album = removeTrailingNullBytes(readTag(channel, 30));
        String year = removeTrailingNullBytes(readTag(channel, 4));
        String comment = removeTrailingNullBytes(readTag(channel, 30));
        int genreId = readTag(channel, 1).charAt(0);

        channel.close();

        return new ID3v1Tags(title, artist, album, year, comment, genreId);
    }

    private String readTag(FileChannel channel, int length) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
        channel.read(byteBuffer);
        return new String(byteBuffer.array());
    }

    private String removeTrailingNullBytes(String s) {
        int firstZeroByteIndex = s.indexOf(0);
        if (firstZeroByteIndex != -1) {
            s = s.substring(0, firstZeroByteIndex);
        }
        return s;
    }

    public void writeID3v1Tags(File file, ID3v1Tags tags,FileChannel fileChannel) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        FileChannel channel = randomAccessFile.getChannel();

        if (!hasID3v1Tag(channel)) {
            channel.position(channel.size());
            writeTag(channel, "TAG", 3);
        }

        writeTag(channel, tags.getTitle(), 30);
        writeTag(channel, tags.getArtist(), 30);
        writeTag(channel, tags.getAlbum(), 30);
        writeTag(channel, tags.getYear(), 4);
        writeTag(channel, tags.getComment(), 30);
        writeTag(channel, String.valueOf((char) tags.getGenreId()), 1);

        channel.close();
    }

    private void writeTag(FileChannel channel, String tagValue, int maxLength) throws IOException {
        int length = tagValue.length();
        if (length > maxLength) {
            tagValue = tagValue.substring(0, maxLength);
            length = tagValue.length();
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(tagValue.getBytes());
        channel.write(byteBuffer);

        byteBuffer = ByteBuffer.allocate(maxLength - length);
        channel.write(byteBuffer);
    }

    private boolean hasID3v1Tag(FileChannel channel) throws IOException {
        channel.position(channel.size() - 128);
        String prefix = readTag(channel, 3);
        return "TAG".equals(prefix);
    }
}
