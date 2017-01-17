package com.example.diana.vrec;

/**
 * Created by Diana on 08.01.2017.
 */

import android.os.Environment;
import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MergedAudio {
    public static void mergeAudio(List<String> audioFiles,String ext) throws IOException {
        Log.e("----------------", String.valueOf(audioFiles.size()));
        List<Movie> inMovies = new ArrayList<Movie>();
        for (String audio : audioFiles) {
            File audioFile = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.getExternalStorageDirectory().getAbsolutePath()), "MyRecordings"+"/" +audio);
            if(!audioFile.exists())
                continue;
            inMovies.add(MovieCreator.build( audioFile.getAbsolutePath()));

        }

        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();

        for (Movie m : inMovies) {
            for (Track t : m.getTracks()) {
                if (t.getHandler().equals("soun")) {
                    audioTracks.add(t);
                }
                if (t.getHandler().equals("vide")) {
                    videoTracks.add(t);
                }
            }
        }

        Movie result = new Movie();

        if (!audioTracks.isEmpty()) {
            result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }
        if (!videoTracks.isEmpty()) {
            result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
        }

        Container out = new DefaultMp4Builder().build(result);

        File mergedAudio = new File(Environment.getExternalStoragePublicDirectory(
                Environment.getExternalStorageDirectory().getAbsolutePath()), "MyRecordings/"+"audio"+ext);

        FileOutputStream fos = null;
        fos = new FileOutputStream(mergedAudio);

        FileChannel fc = new RandomAccessFile(String.format(mergedAudio.getAbsolutePath()), "rw").getChannel();
        out.writeContainer(fc);
        fc.close();

        for (String audio : audioFiles) {
            File audioFile = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.getExternalStorageDirectory().getAbsolutePath()), "MyRecordings"+"/" +audio);
            if(audioFile.exists()){
                audioFile.delete();
            }
        }

        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.getExternalStorageDirectory().getAbsolutePath()), "MyRecordings");
        File from = new File(file,  "audio" + ext);
        File to = new File(file, audioFiles.get(audioFiles.size()-1));
        from.renameTo(to);

    }


}