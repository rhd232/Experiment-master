package com.anitoa.util;

public class Utils {


    public static int getChannelIndexByName(String channel){

        int index = -1;
        if (channel.contains("Chip#")){
            index=Integer.parseInt(channel.split("Chip#")[1])-1;
        }
        return index;

        /*switch (channel)
        {
            case "Chip#1":
                index = 0;
                break;
            case "Chip#2":
                index = 1;
                break;
            case "Chip#3":
                index = 2;
                break;
            case "Chip#4":
                index = 3;
                break;
            default:
                break;
        }
        return index;*/
    }
}
