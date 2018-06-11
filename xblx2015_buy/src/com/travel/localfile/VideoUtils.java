package com.travel.localfile;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.travel.localfile.dao.LocalFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/1.
 */

public class VideoUtils {
    private VideoListener listener;
    public VideoUtils(VideoListener listener){
        this.listener = listener;
    }

    public interface VideoListener{
        void spliceVideo(String filePath, boolean isSccess);
    }

    private String outName;//输出的视频名称

    public void merge(List<LocalFile> list, String filePath){
        int count = list.size();
        try {
            Movie[] inMovies = new Movie[count];
            for (int i = 0; i < count; i++) {
                inMovies[i] = MovieCreator.build(list.get(i).getLocalPath());
            }

            List<Track> videoTracks = new LinkedList<>();
            List<Track> audioTracks = new LinkedList<>();

            //提取所有视频和音频的通道
            for (Movie m : inMovies) {
                for (Track t : m.getTracks()) {
                    if (t.getHandler().equals("soun")) {
                        audioTracks.add(t);
                    }
                    if (t.getHandler().equals("vide")) {
                        videoTracks.add(t);
                    }
                    if (t.getHandler().equals("")) {

                    }
                }
            }

            //添加通道到新的视频里
            Movie result = new Movie();
            if (audioTracks.size() > 0) {
                result.addTrack(new AppendTrack(audioTracks
                        .toArray(new Track[audioTracks.size()])));
            }
            if (videoTracks.size() > 0) {
                result.addTrack(new AppendTrack(videoTracks
                        .toArray(new Track[videoTracks.size()])));
            }
            Container mp4file = new DefaultMp4Builder()
                    .build(result);


            //开始生产mp4文件
            File storagePath = new File(filePath);
            //            storagePath.mkdirs();
//            FileOutputStream fos =  new FileOutputStream(new File(storagePath,outName));

            if (!storagePath.exists()) {
                try {
                    storagePath.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileOutputStream fos =  new FileOutputStream(storagePath);
            FileChannel fco = fos.getChannel();
            mp4file.writeContainer(fco);
            fco.close();
            fos.close();
            listener.spliceVideo(filePath, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
