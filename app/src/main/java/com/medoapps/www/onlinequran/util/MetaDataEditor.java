package com.medoapps.www.onlinequran.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.models.Metadata;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.images.AndroidArtwork;
import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MetaDataEditor {

    Context context;
    private static final String TAG = "MetaDataEditor";

    public MetaDataEditor(Context context) {
        this.context = context;
    }

    public void changeMetaData(String uri){
        Metadata metadata = null;
        try {
            metadata = readMetadata(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<String> kk =new ArrayList<String>();
        kk.add("Ahmed Hashim -- أحمد هاشم -- My Stream");

        Drawable d = context.getResources().getDrawable(R.drawable.mystream); // the drawable (Captain Obvious, to the rescue!!!)
        Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        try {
            metadata.title = metadata.title + " By Ahmed Hashim -- أحمد هاشم";
            metadata.album = "Ahmed Hashim -- أحمد هاشم -- My Stream";
            metadata.comment = "Ahmed Hashim -- أحمد هاشم -- My Stream";
            metadata.genreList =kk ;
            metadata.coverArtList = bitmapdata;
            writeMetadata(metadata);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Metadata readMetadata(String uri) throws TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException {
        File file = new File(uri);
        AudioFile audioFile = AudioFileIO.read(file);
        AudioHeader header = audioFile.getAudioHeader();
        Tag tag = audioFile.getTagAndConvertOrCreateAndSetDefault();

        //header metadata
        String format = header.getFormat();
        int sampleRate = header.getSampleRateAsNumber(),
                trackLength = header.getTrackLength();
        long fileSize = file.length();
        boolean isLossless = header.isLossless();

        //audio metadata
        String title = tag.getFirst(FieldKey.TITLE),
                album = tag.getFirst(FieldKey.ALBUM),
                track = tag.getFirst(FieldKey.TRACK),
                year = tag.getFirst(FieldKey.YEAR),
                rating = tag.getFirst(FieldKey.RATING),
                comment = tag.getFirst(FieldKey.COMMENT),
                lyrics = tag.getFirst(FieldKey.LYRICS);

        byte[] coverArt = tag.getFirstArtwork().getBinaryData();
        tag.getArtworkList();
        List artistList = tag.getAll(FieldKey.ARTIST),
                albumArtistList = tag.getAll(FieldKey.ALBUM_ARTIST),
                genreList = tag.getAll(FieldKey.GENRE);

        return new Metadata(uri,
                format,
                sampleRate,
                trackLength,
                fileSize,
                isLossless,
                title,
                album,
                track,
                year,
                rating,
                comment,
                lyrics,
                coverArt,
                artistList,
                albumArtistList,
                genreList);
    }

    public static void writeMetadata(Metadata metadata) throws TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException, CannotWriteException {
        AudioFile audioFile = AudioFileIO.read(new File(metadata.getUri()));

        ID3v24Tag tag = new ID3v24Tag();

        tag.setField(FieldKey.TITLE, metadata.getTitle());
        tag.setField(FieldKey.ALBUM, metadata.getAlbum());
        tag.setField(FieldKey.TRACK, metadata.getTrack());
        tag.setField(FieldKey.YEAR, metadata.getYear());
        tag.setField(FieldKey.RATING, metadata.getRating());
        tag.setField(FieldKey.COMMENT, metadata.getComment());
        tag.setField(FieldKey.LYRICS, metadata.getLyrics());
        Artwork artwork = new AndroidArtwork();
        artwork.setBinaryData(metadata.getCoverArtList());
        tag.setField(artwork);
        addAll(tag, FieldKey.ARTIST, metadata.getArtistList());
        addAll(tag, FieldKey.ALBUM_ARTIST, metadata.getAlbumArtistList());
        addAll(tag, FieldKey.GENRE, metadata.getGenreList());

        audioFile.setTag(tag);
        AudioFileIO.write(audioFile);

    }

    private static void addAll(Tag tag, FieldKey fieldKey, List dataList) throws FieldDataInvalidException {
        for (Object s : dataList) {
            tag.addField(fieldKey, s.toString());
        }
    }
}
