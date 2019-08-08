import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class Main {

    public static void main(String[] args) {
	// write your code here

        String httpsProxyHost = "";
        String httpsProxyPort = "";


        File postJson = new File("default.json");

        try {
            Scanner s = new Scanner(postJson);
            StringBuilder builder = new StringBuilder();

            while (s.hasNextLine()) builder.append(s.nextLine());

            JSONParser pars = new JSONParser();

            try {
                Object obj = pars.parse(builder.toString());
                JSONObject overallConfig = (JSONObject) obj;

                httpsProxyHost = (String) overallConfig.get("httpsProxyHost");
                httpsProxyPort = (String) overallConfig.get("httpsProxyPort");

            } catch (ParseException e) {
                e.printStackTrace();
            }

            s.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        SetHttpsProxy setHttpsProxy = new SetHttpsProxy(httpsProxyHost, httpsProxyPort);

        setHttpsProxy.on();



        //вспомнить чего это я наделал раньше и как это заново заставить работать

        //получить альбомы
        //получить стену


        //VkHelper vkhelper = new VkHelper();


        //https://oauth.vk.com/authorize?client_id=748256837&redirect_uri=https://oauth.vk.com/blank.html&display=page&scope=friends,groups,wall,photos,offline,docs&response_type=token&v=5.69
        String ACCESS_TOKEN = "tkn";
        //String OWNER_ID = "-40462459"; //"-13519709";
        String OWNER_ID = "ID";
        String saveDir = "C://jHateVKdownloader//";


        List<PhotoAlbum> albums = null;
        //albums.add("-15");

        final long albumOffset = 1000;
        List<String> photos = new ArrayList<>();

        try {
            albums = VkHelper.photosGetAlbums(ACCESS_TOKEN, OWNER_ID);
        }catch (Exception e){
            e.printStackTrace();
        }


        /*try{
            VkHelper.photosGet(ACCESS_TOKEN,OWNER_ID,albums.get(2).albumId(),0,photos);
        }catch (Exception e){
            e.printStackTrace();
        }*/


        /*for (String photosCounter:photos) {
            System.out.println(photosCounter);
        }*/

        for (PhotoAlbum albumsCounter:albums) {

            //albumsCounter.showAll();


            /*long count = vkhelper.photosGetCount(ACCESS_TOKEN, OWNER_ID, albumsCounter);*/

            for(long i = 0; i <= albumsCounter.count(); i = i + albumOffset ){
                System.out.println("offset summary: "+i);

                try{
                    photos = VkHelper.photosGet(ACCESS_TOKEN, OWNER_ID, albumsCounter.albumId(), albumOffset, photos);
                    TimeUnit.SECONDS.sleep(5);
                }catch (Exception e){
                    e.printStackTrace();
                }

                //albumOffset =+ 1000;
            }
            //albumOffset = 0;

            /*File file = new File(saveDir + "//" + albumsCounter);
            if (!file.exists()) {
                if (file.mkdir()) {
                    System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }

            for (String line : photos) {
                try {
                    HttpDownloadUtility.downloadFile(line, saveDir + "//" + albumsCounter);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }*/
        }



    }
}
