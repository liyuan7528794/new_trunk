package com.travel.lib.utils;

public class TypeIdToTYypeName {
	public static String idToTypeName(String typeId){
		String typeName = "";
		if(typeId.equals("1")){
			typeName = "历史人文";
		}else if(typeId.equals("2")){
			typeName = "极限运动";
		}else if(typeId.equals("3")){
			typeName = "自然风光";
		}else if(typeId.equals("4")){
			typeName = "休闲娱乐";
		}else if(typeId.equals("5")){
			typeName = "兴趣爱好";
		}else if(typeId.equals("6")){
			typeName = "达人展示";
		}
			
		return typeName;
	}
}
