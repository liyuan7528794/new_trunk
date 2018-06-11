package com.travel.video.tools;

import com.ctsmedia.hltravel.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
public class RandomSetColor {
	private final static  int[] colors = {R.color.red_FAA,R.color.red_FFD9AD,R.color.blue_7FA9FF,R.color.green_B2FFAC,R.color.blue_A3FFF1,R.color.blue_A8CBFF,R.color.purple_ECB8FF};
	private static Map<String, Integer> colorMap = new HashMap<String, Integer>();
	public static void randomColorAdapter(){
		colorMap.clear();
		new Thread(new Runnable() {
			@Override
			public void run() {
				Random random = new Random();
				colorMap.put("0", colors[random.nextInt(7)]);
				colorMap.put("1", colors[random.nextInt(7)]);
				colorMap.put("2", colors[random.nextInt(7)]);
				colorMap.put("3", colors[random.nextInt(7)]);
				colorMap.put("4", colors[random.nextInt(7)]);
				colorMap.put("5", colors[random.nextInt(7)]);
				colorMap.put("6", colors[random.nextInt(7)]);
				colorMap.put("7", colors[random.nextInt(7)]);
				colorMap.put("8", colors[random.nextInt(7)]);
				colorMap.put("9", colors[random.nextInt(7)]);
			}
		}).start();
	}
	public static Map<String, Integer> getColorMap() {
		return colorMap;
	}
}