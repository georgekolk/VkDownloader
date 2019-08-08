import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class VkHelper {
    private String requestURL = null;
	private static String charset = "UTF-8";
	private long overallPhotoPostCount;
	private ArrayList<String> list = new ArrayList<String>();
	private List<String> allTagsMatches = new ArrayList<String>();
	private String finallyGatheredTags  = "";
	private String finallyGatheredForTumblrTags  = "";


    public List<String> uploadPhoto(String uploadPage, String charset, File fileToProcess)throws Exception {

        List<String> serverHashPhoto = new ArrayList<String>();

        MultipartUtility multipart = new MultipartUtility(uploadPage, charset);

        multipart.addHeaderField("User-Agent", "jHateSMM");
        multipart.addFormField("description", "Cool Pictures");

					/*int z = 1;
					for (File fileToProcess : uploadFile) {
						multipart.addFilePart("file" + Integer.toString(z), fileToProcess);
						z++;
					}*/

        multipart.addFilePart("file", fileToProcess);
        List<String> response2 = multipart.finish();

					/*for (String line : response2) {
						System.out.println(line);
					}*/

        JSONParser pars = new JSONParser();
        Object obj = null;
        try {
            obj = pars.parse(response2.toString());

            //System.out.println("Response after upload: " + response2.toString());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        JSONArray uploadedPhotoArray = (JSONArray) obj;

        for (int i = 0; i < uploadedPhotoArray.size(); i++) {
            JSONObject uploadedPhoto = (JSONObject) uploadedPhotoArray.get(i);

            System.out.println("Server: " + uploadedPhoto.get("server"));
            System.out.println("Hash: " + uploadedPhoto.get("hash"));
            System.out.println("Photo: " + uploadedPhoto.get("photo"));

            serverHashPhoto.add(uploadedPhoto.get("server").toString());
            serverHashPhoto.add(uploadedPhoto.get("hash").toString());
            serverHashPhoto.add(uploadedPhoto.get("photo").toString());

        }

            return serverHashPhoto;
    }

	public long getOverallPhotoPosts(String OWNER_ID, String ACCESS_TOKEN)throws Exception {
		//URL getPhotos = new URL("https://api.vk.com/method/wall.get?owner_id=" + OWNER_ID + "&count=1&v=5.64");
		URL getPhotos = new URL("https://api.vk.com/method/wall.get?owner_id=" + OWNER_ID + "&access_token=" + ACCESS_TOKEN + "&count=1&v=5.64");
		HttpURLConnection connection = (HttpURLConnection) getPhotos.openConnection();
		StringBuilder content = new StringBuilder();

		connection.setRequestProperty("Accept-Charset", charset);
		connection.setUseCaches(false);
		connection.setRequestProperty("User-Agent", "jHateSMM");
		//--------------------------------------------------test--------------------------------------------------
		//connection.setConnectTimeout(5000);
		//connection.setReadTimeout(10000);
		//connection.setRequestProperty("Connection","close");
		//System.out.println(connection.getResponseCode());
		//--------------------------------------------------test--------------------------------------------------

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		String line;

		while ((line = bufferedReader.readLine()) != null)
		{
			content.append(line + "\n");
		}
		bufferedReader.close();
		//-------connection.disconnect();

		//System.out.println(content.toString());

		JSONParser pars = new JSONParser();

		try {

			JSONObject obj = (JSONObject) pars.parse(content.toString());
			System.out.println("obj to string looks like: " + obj.toString());
			JSONObject jsonResponse = (JSONObject) obj.get("response");
			//System.out.println("response: " + jsonResponse.toString());
			this.overallPhotoPostCount = (long) jsonResponse.get("count");

			//this.overallPhotoPostCount = (long) blogInfo.get("posts");
			System.out.println("photo posts count: " + overallPhotoPostCount);

		}catch (ParseException e) {
			e.printStackTrace();
		}catch (NullPointerException npe){
			npe.printStackTrace();

			JSONObject obj = (JSONObject) pars.parse(content.toString());
			//System.out.println("RAW parsed content: " + obj.toString());
			JSONObject jsonError = (JSONObject) obj.get("error");
			//System.out.println("jsonError: " + jsonError);
			//= (String) jsonError.get("error_code");
			String errorMsg = (String) jsonError.get("error_msg");
			System.out.println("errorMsg: " + errorMsg);

			throw new Exception(errorMsg);
		}

		return overallPhotoPostCount;
	}

	public ArrayList getPhotosListOffset(String OWNER_ID, String ACCESS_TOKEN, long offset)throws Exception{

		allTagsMatches.clear(); //очистить теги перед сбором в посте, ибо будет куча левых тагов из прошлых постов в которых небыло фоточек

		//URL getPhotos = new URL("https://api.vk.com/method/wall.get?owner_id=" + OWNER_ID + "&count=1&offset=" + offset + "&v=5.62");
		URL getPhotos = new URL("https://api.vk.com/method/wall.get?owner_id=" + OWNER_ID + "&access_token=" + ACCESS_TOKEN + "&count=1&offset=" + offset + "&v=5.62");

		//try {
			HttpURLConnection connection = (HttpURLConnection) getPhotos.openConnection();
			StringBuilder content = new StringBuilder();

			connection.setRequestProperty("Accept-Charset", charset);
			connection.setUseCaches(false);
			connection.setRequestProperty("User-Agent", "jHateSMM");
			//--------------------------------------------------test--------------------------------------------------
			//connection.setConnectTimeout(5000);
			//connection.setReadTimeout(10000);
			//System.out.println(connection.getResponseCode());
			//connection.setRequestProperty("Connection","close");
			//--------------------------------------------------test--------------------------------------------------

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;

			while ((line = bufferedReader.readLine()) != null)
			{
				content.append(line + "\n");
			}
			bufferedReader.close();

			//-------connection.disconnect();

			//System.out.println(content.toString());

			JSONParser pars = new JSONParser();

			try {

				JSONObject obj = (JSONObject) pars.parse(content.toString());
				//System.out.println("obj to string looks like: " + obj.toString());
				JSONObject jsonResponse = (JSONObject) obj.get("response");
				JSONArray jsonItems = (JSONArray) jsonResponse.get("items");
				//TODO: вот тут должен быть обработчки если нету вообще ключа Items
				//System.out.println("jsonItems: " + jsonItems);
				for (int i = 0; i < jsonItems.size(); i++) {

					JSONObject jsonItemStep = (JSONObject) jsonItems.get(i);
					//System.out.println("jsonItemStep: " + jsonItemStep);

					String jsonItemsText = (String) jsonItemStep.get("text");
					System.out.println("jsonItemsText: " + jsonItemsText);

 					Matcher m = Pattern.compile("#[A-Za-zА-Яа-я_!0-9]*").matcher(jsonItemsText);

 					while (m.find()) {
						allTagsMatches.add(m.group());
 					}

					JSONArray jsonAttachments = (JSONArray) jsonItemStep.get("attachments");
					//System.out.println("jsonAttachments: " + jsonAttachments);

					if (jsonAttachments != null) {
						for (int z = 0; z < jsonAttachments.size(); z++) {

							JSONObject jsonAttachmentsStep = (JSONObject) jsonAttachments.get(z);
							//System.out.println("jsonAttachmentsStep: " + jsonAttachmentsStep);

								if (jsonAttachmentsStep.get("photo") != null) {

									JSONObject jsonAttachmentsStepGetPhoto = (JSONObject) jsonAttachmentsStep.get("photo");
									//System.out.println("jsonAttachmentsStepGetPhoto: " + jsonAttachmentsStepGetPhoto);

									if (jsonAttachmentsStepGetPhoto.containsKey("photo_1280")) {
										//System.out.println("1280: " + jsonAttachmentsStepGetPhoto.get("photo_1280"));
										list.add(jsonAttachmentsStepGetPhoto.get("photo_1280").toString());
									} else if (jsonAttachmentsStepGetPhoto.containsKey("photo_807")) {
										//System.out.println("807: " + jsonAttachmentsStepGetPhoto.get("photo_807"));
										list.add(jsonAttachmentsStepGetPhoto.get("photo_807").toString());
									} else if (jsonAttachmentsStepGetPhoto.containsKey("photo_604")) {
										//System.out.println("604: " + jsonAttachmentsStepGetPhoto.get("photo_604"));
										list.add(jsonAttachmentsStepGetPhoto.get("photo_604").toString());
									} else if (jsonAttachmentsStepGetPhoto.containsKey("photo_130")) {
										System.out.println("130: " + jsonAttachmentsStepGetPhoto.get("photo_130"));
										//list.add(jsonAttachmentsStepGetPhoto.get("photo_130").toString());
									} else if (jsonAttachmentsStepGetPhoto.containsKey("photo_75")) {
										//System.out.println("75: " + jsonAttachmentsStepGetPhoto.get("photo_75"));
										list.add(jsonAttachmentsStepGetPhoto.get("photo_75").toString());
									}
								}else{
									TimeUnit.SECONDS.sleep(1);
									return null;
								}

							/*JSONObject jsonAttachmentsStepGetPhoto = (JSONObject) jsonAttachmentsStep.get("photo");
							System.out.println("jsonAttachmentsStepGetPhoto: " + jsonAttachmentsStepGetPhoto);

							JSONObject jsonAttachmentsStepGetDoc = (JSONObject) jsonAttachmentsStep.get("doc");
							System.out.println("jsonAttachmentsStepGetPhoto: " + jsonAttachmentsStepGetPhoto);*/

						}
					}else{
						return null;
					}

				}

			} catch (ParseException e) {
				e.printStackTrace();
			}catch (NullPointerException npe){
				npe.printStackTrace();

				JSONObject obj = (JSONObject) pars.parse(content.toString());
				//System.out.println("RAW parsed content: " + obj.toString());
				JSONObject jsonError = (JSONObject) obj.get("error");
				//System.out.println("jsonError: " + jsonError);
				//= (String) jsonError.get("error_code");
				String errorMsg = (String) jsonError.get("error_msg");
				System.out.println("errorMsg: " + errorMsg);

				throw new Exception(errorMsg);
			}

		//}catch (Exception E){
		//	E.printStackTrace();
		//}

		return list;
	}

	public String saveWallPhoto(String ACCESS_TOKEN, String GROUP_ID, String server, String hash, String photo)throws Exception{
		//System.out.println("==NEW saveWallPhoto Without apache HTTP client==");

		URL saveWallPhoto = new URL("https://api.vk.com/method/photos.saveWallPhoto?group_id=" + GROUP_ID + "&server=" + server + "&hash=" + hash + "&photo=" + photo + "&access_token=" + ACCESS_TOKEN + "&v=5.52");
		String multipleAttachments = "";

		//try {
			HttpURLConnection connection = (HttpURLConnection) saveWallPhoto.openConnection();
			StringBuilder content = new StringBuilder();

			connection.setRequestProperty("Accept-Charset", charset);
			connection.setUseCaches(false);
			connection.setRequestProperty("User-Agent", "jHateSMM");
			//--------------------------------------------------test--------------------------------------------------
			//connection.setConnectTimeout(5000);
			//connection.setReadTimeout(10000);
			//connection.setRequestProperty("Connection","close");
			//System.out.println(connection.getResponseCode());
			//--------------------------------------------------test--------------------------------------------------

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;

			while ((line = bufferedReader.readLine()) != null)
			{
				content.append(line + "\n");
			}
			bufferedReader.close();
            //-------connection.disconnect();

			//System.out.println("RAW Content: " + content.toString());

			JSONParser pars = new JSONParser();

			try {

				JSONObject obj = (JSONObject) pars.parse(content.toString());
				//System.out.println("RAW parsed content: " + obj.toString());
				JSONArray jsonResponse = (JSONArray) obj.get("response");
				//System.out.println("parsed response after saveWallPhoto response: " + jsonResponse);

				for(int i = 0; i < jsonResponse.size(); i++){
					JSONObject jsonObj1 = (JSONObject) jsonResponse.get(i);
					System.out.println("PHOTO_ID: "+ jsonObj1.get("id").toString());
					multipleAttachments = multipleAttachments + "photo" + jsonObj1.get("owner_id").toString() + "_" + jsonObj1.get("id").toString()+",";
				}

			} catch (NullPointerException e) {
				e.printStackTrace();

				JSONObject obj = (JSONObject) pars.parse(content.toString());
				//System.out.println("RAW parsed content: " + obj.toString());
				JSONObject jsonError = (JSONObject) obj.get("error");
				System.out.println("jsonError: " + jsonError);
				//= (String) jsonError.get("error_code");
				String errorMsg = (String) jsonError.get("error_msg");
				System.out.println("errorMsg: " + errorMsg);

				throw new Exception(errorMsg);
			}

		//}catch (Exception E){
		//	E.printStackTrace();
		//}

		return multipleAttachments;
	}

	public Boolean postWallPhoto(String ACCESS_TOKEN, String GROUP_ID, String multipleAttachments, String message)throws Exception{
		URL postWallPhoto = new URL("https://api.vk.com/method/wall.post?owner_id=-" + GROUP_ID  + "&attachments=" + multipleAttachments + "&access_token=" + ACCESS_TOKEN + "&message="
				+ message + "&v=5.62");

		System.out.println("postWallPhoto: " + postWallPhoto);

		boolean savePhotoResult = false;

		//try {
			HttpURLConnection connection = (HttpURLConnection) postWallPhoto.openConnection();
			StringBuilder content = new StringBuilder();

			connection.setRequestProperty("Accept-Charset", charset);
			connection.setUseCaches(false);
			connection.setRequestProperty("User-Agent", "jHateSMM");
			//--------------------------------------------------test--------------------------------------------------
			//connection.setConnectTimeout(5000);
			//connection.setReadTimeout(10000);
			//System.out.println(connection.getResponseCode());
			connection.setRequestProperty("Connection","close"); //!!!! последний вызов работы с HttpURLConnection
			//--------------------------------------------------test--------------------------------------------------

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String line;

			while ((line = bufferedReader.readLine()) != null)
			{
				content.append(line + "\n");
			}
			bufferedReader.close();
            //-------connection.disconnect();

			System.out.println("raw response after photo post: " + content.toString());


			savePhotoResult = content.toString().contains("post_id");
			System.out.println("Post result: " + savePhotoResult);


		//}catch (Exception E){
		//	E.printStackTrace();
		//}

		return savePhotoResult;
	}

	public String getWallUploadServer(String ACCESS_TOKEN, String GROUP_ID)throws Exception{
		URL getWallUploadServer = new URL("https://api.vk.com/method/photos.getWallUploadServer?group_id=" + GROUP_ID + "&access_token=" + ACCESS_TOKEN + "&count=1&v=5.62");

		//System.out.println("\n ==========NEW getWallUploadServer2 without apache=========== \n");

		//try {
			HttpURLConnection connection = (HttpURLConnection) getWallUploadServer.openConnection();
			StringBuilder content = new StringBuilder();

			connection.setRequestProperty("Accept-Charset", charset);
			connection.setUseCaches(false);
			connection.setRequestProperty("User-Agent", "jHateSMM");
			//--------------------------------------------------test--------------------------------------------------
			//connection.setConnectTimeout(5000);
			//connection.setReadTimeout(10000);
			//connection.setRequestProperty("Connection","close");
			//System.out.println(connection.getResponseCode());
			//--------------------------------------------------test--------------------------------------------------

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String line;

			while ((line = bufferedReader.readLine()) != null)
			{
				content.append(line + "\n");
			}
			bufferedReader.close();
            //-------connection.disconnect();

			//System.out.println("RAW Content: " + content.toString());

			JSONParser pars = new JSONParser();

			try {
				JSONObject obj = (JSONObject) pars.parse(content.toString());
				//System.out.println("RAW parsed content: " + obj.toString());
				JSONObject jsonResponse = (JSONObject) obj.get("response");
				//System.out.println("parsed response after getWallUploadServer: " + jsonResponse);
				this.requestURL = (String) jsonResponse.get("upload_url");
				System.out.println("Upload server: " + requestURL);

			}catch (NullPointerException e) {
				e.printStackTrace();

				JSONObject obj = (JSONObject) pars.parse(content.toString());
				//System.out.println("RAW parsed content: " + obj.toString());
				JSONObject jsonError = (JSONObject) obj.get("error");
				System.out.println("jsonError: " + jsonError);
				 //= (String) jsonError.get("error_code");
				String errorMsg = (String) jsonError.get("error_msg");
				System.out.println("errorMsg: " + errorMsg);

				throw new Exception(errorMsg);
			}

		//}catch (Exception E){
		//	E.printStackTrace();
		//}

		return requestURL;

	}

	public String rerurnTags(){

		for (String item : allTagsMatches) {
			this.finallyGatheredTags = this.finallyGatheredTags + item + " " ;
		}

		return this.finallyGatheredTags;
	}

	public String returnTumblrTags(){

		for (String item : allTagsMatches) {
			this.finallyGatheredForTumblrTags = this.finallyGatheredForTumblrTags + item.replace("#","") + ",";
		}

		return this.finallyGatheredForTumblrTags;
	}

    public String docsGetWallUploadServer(String ACCESS_TOKEN, String GROUP_ID)throws Exception{
        URL getWallUploadServer = new URL("https://api.vk.com/method/docs.getWallUploadServer?group_id=" + GROUP_ID + "&access_token=" + ACCESS_TOKEN + "&v=5.62");

        //try {
            HttpURLConnection connection = (HttpURLConnection) getWallUploadServer.openConnection();
            StringBuilder content = new StringBuilder();

            connection.setRequestProperty("Accept-Charset", charset);
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent", "jHateSMM");
			//--------------------------------------------------test--------------------------------------------------
			//connection.setConnectTimeout(5000);
			//connection.setReadTimeout(10000);
			//connection.setRequestProperty("Connection","close");
			//--------------------------------------------------test--------------------------------------------------
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;

            while ((line = bufferedReader.readLine()) != null)
            {
                content.append(line + "\n");
            }
            bufferedReader.close();
            //connection.disconnect();

            System.out.println("RAW Content: " + content.toString());

            JSONParser pars = new JSONParser();

            try {
                JSONObject obj = (JSONObject) pars.parse(content.toString());
                //System.out.println("RAW parsed content: " + obj.toString());
                JSONObject jsonResponse = (JSONObject) obj.get("response");
                //System.out.println("parsed response after getWallUploadServer: " + jsonResponse);
                this.requestURL = (String) jsonResponse.get("upload_url");
                System.out.println("Upload server: " + requestURL);

			}catch (NullPointerException e) {
				e.printStackTrace();

				JSONObject obj = (JSONObject) pars.parse(content.toString());
				//System.out.println("RAW parsed content: " + obj.toString());
				JSONObject jsonError = (JSONObject) obj.get("error");
				System.out.println("jsonError: " + jsonError);
				//= (String) jsonError.get("error_code");
				String errorMsg = (String) jsonError.get("error_msg");
				System.out.println("errorMsg: " + errorMsg);

				throw new Exception(errorMsg);
			}

        //}catch (Exception E){
        //    E.printStackTrace();
        //}

        return requestURL;

    }

    public String uploadDoc(String uploadPage, String charset, File fileToProcess)throws Exception {
        MultipartUtility multipart = new MultipartUtility(uploadPage, charset);

        multipart.addHeaderField("User-Agent", "jHateSMM");
        multipart.addFormField("description", "Cool Pictures");

        multipart.addFilePart("file", fileToProcess);
        List<String> response2 = multipart.finish();

        JSONParser pars = new JSONParser();

        try {

            JSONArray obj = (JSONArray) pars.parse(response2.toString());

            System.out.println("Response after upload: " + response2.toString());

            for (int i = 0; i < obj.size(); i++) {
                JSONObject uploadedFile = (JSONObject) obj.get(i);

                System.out.println("Server: " + uploadedFile.get("file"));
                return uploadedFile.get("file").toString();

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String docsSave (String ACCESS_TOKEN, String uploadFile)throws Exception{
        URL getWallUploadServer = new URL("https://api.vk.com/method/docs.save?file=" + uploadFile + "&access_token=" + ACCESS_TOKEN +"&v=5.63");

        //try {
            HttpURLConnection connection = (HttpURLConnection) getWallUploadServer.openConnection();
            StringBuilder content = new StringBuilder();

            connection.setRequestProperty("Accept-Charset", charset);
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent", "jHateSMM");
			//--------------------------------------------------test--------------------------------------------------
			//connection.setConnectTimeout(5000);
			//connection.setReadTimeout(10000);
			//connection.setRequestProperty("Connection","close");
			//--------------------------------------------------test--------------------------------------------------
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;

            while ((line = bufferedReader.readLine()) != null)
            {
                content.append(line + "\n");
            }
            bufferedReader.close();
            //connection.disconnect();

            //System.out.println("raw response docs.Save: " + content.toString());

            JSONParser pars = new JSONParser();

            try {
                JSONObject obj = (JSONObject) pars.parse(content.toString());


				JSONArray jsonResponse = (JSONArray) obj.get("response");
				System.out.println("parsed response: " + jsonResponse);

				for (int i = 0; i < obj.size(); i++) {
					JSONObject uploadedDoc = (JSONObject) jsonResponse.get(i);
					return "doc" + uploadedDoc.get("owner_id") + "_" + uploadedDoc.get("id") + ",";
				}


			} catch (ParseException e) {
                e.printStackTrace();

				JSONObject obj = (JSONObject) pars.parse(content.toString());
				//System.out.println("RAW parsed content: " + obj.toString());
				JSONObject jsonError = (JSONObject) obj.get("error");
				System.out.println("jsonError: " + jsonError);
				//= (String) jsonError.get("error_code");
				String errorMsg = (String) jsonError.get("error_msg");
				System.out.println("errorMsg: " + errorMsg);

				throw new Exception(errorMsg);

			}

        //}catch (Exception E){
        //    E.printStackTrace();
        //}

        return null;

    }



	public static List<PhotoAlbum> photosGetAlbums(String ACCESS_TOKEN, String OWNER_ID)throws Exception{
		List<PhotoAlbum> albums = new ArrayList<>();

		//URL getWallUploadServer = new URL("https://api.vk.com/method/docs.getWallUploadServer?group_id=" + GROUP_ID + "&access_token=" + ACCESS_TOKEN + "&v=5.62");

		URL photosGetAlbums = new URL("https://api.vk.com/method/photos.getAlbums?owner_id=" + OWNER_ID + "&need_system=1&access_token=" + ACCESS_TOKEN + "&v=5.69");
//https://api.vk.com/method/photos.getAlbums?owner_id=13519709&access_token=94f858182b87c71152f290c93ddfeee7ca31aff50ca96fada0fc67472e3b4da6e18dfcc5cec4189ae50e2&v=5.69

		//try {
		HttpURLConnection connection = (HttpURLConnection) photosGetAlbums.openConnection();
		StringBuilder content = new StringBuilder();

		connection.setRequestProperty("Accept-Charset", charset);
		connection.setUseCaches(false);
		connection.setRequestProperty("User-Agent", "jHateSMM");
		//--------------------------------------------------test--------------------------------------------------
		//connection.setConnectTimeout(5000);
		//connection.setReadTimeout(10000);
		//connection.setRequestProperty("Connection","close");
		//--------------------------------------------------test--------------------------------------------------
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		String line;

		while ((line = bufferedReader.readLine()) != null)
		{
			content.append(line + "\n");
		}
		bufferedReader.close();
		//connection.disconnect();

		System.out.println("RAW Content: " + content.toString());

		JSONParser pars = new JSONParser();

		try {
			JSONObject obj = (JSONObject) pars.parse(content.toString());
			//System.out.println("RAW parsed content: " + obj.toString());
			JSONObject jsonResponse = (JSONObject) obj.get("response");
			//System.out.println("parsed response after getWallUploadServer: " + jsonResponse);
			//this.requestURL = (String) jsonResponse.get("upload_url");

			JSONArray items = (JSONArray) jsonResponse.get("items");

			for (int i = 0; i < items.size(); i++) {

				JSONObject id = (JSONObject) items.get(i);

				//System.out.println(items.get(i));

				//albums.add(new PhotoAlbum(id.get("title").toString(), id.get("id").toString(), (long)id.get("size"), id.get("description").toString(), id.get("updated").toString()));
				albums.add(new PhotoAlbum(id.get("title").toString(), id.get("id").toString(), (long)id.get("size")));

				//public PhotoAlbum(String albumName, String albumId, String albumDescription, String albumUpdated) {
				//{"thumb_id":435215245,"size":16,"owner_id":-40462459,"created":1449564528,"description":"наши девушки","can_upload":0,"id":225208328,"title":"Wrapa's Models","updated":1511724496}

			}

			//System.out.println("Upload server: " + requestURL);

		}catch (NullPointerException e) {
			e.printStackTrace();

			JSONObject obj = (JSONObject) pars.parse(content.toString());
			//System.out.println("RAW parsed content: " + obj.toString());
			JSONObject jsonError = (JSONObject) obj.get("error");
			System.out.println("jsonError: " + jsonError);
			//= (String) jsonError.get("error_code");
			String errorMsg = (String) jsonError.get("error_msg");
			System.out.println("errorMsg: " + errorMsg);

			throw new Exception(errorMsg);
		}

		return albums;

	}

	public static List<String> photosGet (String ACCESS_TOKEN,String OWNER_ID,String ALBUM_ID,long OFFSET, List<String> photos)throws Exception{
		URL photosGet = new URL("https://api.vk.com/method/photos.get?owner_id=" + OWNER_ID + "&album_id=" + ALBUM_ID + "&offset=" + OFFSET + "&access_token=" + ACCESS_TOKEN + "&v=5.69");

		HttpURLConnection connection = (HttpURLConnection) photosGet.openConnection();
		StringBuilder content = new StringBuilder();

		connection.setRequestProperty("Accept-Charset", charset);
		connection.setUseCaches(false);
		connection.setRequestProperty("User-Agent", "jHateSMM");
		//--------------------------------------------------test--------------------------------------------------
		//connection.setConnectTimeout(5000);
		//connection.setReadTimeout(10000);
		//connection.setRequestProperty("Connection","close");
		//--------------------------------------------------test--------------------------------------------------
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		String line;

		while ((line = bufferedReader.readLine()) != null)
		{
			content.append(line + "\n");
		}
		bufferedReader.close();
		//connection.disconnect();

		System.out.println("RAW Content: " + content.toString());

		JSONParser pars = new JSONParser();

		try {
			JSONObject obj = (JSONObject) pars.parse(content.toString());
			//System.out.println("RAW parsed content: " + obj.toString());
			JSONObject jsonResponse = (JSONObject) obj.get("response");
			//System.out.println("parsed response after getWallUploadServer: " + jsonResponse);
			//this.requestURL = (String) jsonResponse.get("upload_url");

			JSONArray items = (JSONArray) jsonResponse.get("items");

			//System.out.println("items array: " + items.toString());

			for (int i = 0; i < items.size(); i++) {

				JSONObject id = (JSONObject) items.get(i);

				if (id.containsKey("photo_1280")){
					photos.add(id.get("photo_1280").toString());
				}else if (id.containsKey("photo_807")){
					photos.add(id.get("photo_807").toString());
				}else if (id.containsKey("photo_604")){
					photos.add(id.get("photo_604").toString());
				}else if (id.containsKey("photo_130")){
					photos.add(id.get("photo_130").toString());
				}else if (id.containsKey("photo_75")){
					photos.add(id.get("photo_75").toString());
				}

			}
			//System.out.println("Upload server: " + requestURL);

		}catch (NullPointerException e) {
			e.printStackTrace();

			JSONObject obj = (JSONObject) pars.parse(content.toString());
			//System.out.println("RAW parsed content: " + obj.toString());
			JSONObject jsonError = (JSONObject) obj.get("error");
			System.out.println("jsonError: " + jsonError);
			//= (String) jsonError.get("error_code");
			String errorMsg = (String) jsonError.get("error_msg");
			System.out.println("errorMsg: " + errorMsg);

			throw new Exception(errorMsg);
		}

		return photos;

	}

/*public List<String> photosGet (String ACCESS_TOKEN,String OWNER_ID,String ALBUM_ID)throws URISyntaxException, IOException, ParseException    {
		//long OFFSET_URI = 0;
		long OFFSET = 1000;
		List<String> photos = new ArrayList<>();

		VkHelper vkHater = new VkHelper();

		long count = vkHater.photosGetCount(ACCESS_TOKEN,OWNER_ID,ALBUM_ID);

		for(long i = 0; i <= count; i = i + OFFSET){

			photos = vkHater.photosGet(ACCESS_TOKEN,OWNER_ID,ALBUM_ID,OFFSET,photos);
		}

		return photos;
	}*/

	/*public long photosGetCount (String ACCESS_TOKEN,String OWNER_ID,String ALBUM_ID)throws URISyntaxException, IOException, ParseException{

		long itemsCount = 0;

		builder.setScheme("https").setHost("api.vk.com").setPath("/method/photos.get")
				.setParameter("owner_id", OWNER_ID)
				.setParameter("album_id", ALBUM_ID)
				.setParameter("access_token", ACCESS_TOKEN)
				.setParameter("v", "5.60");

		URI uri = builder.build();
		HttpGet httpget = new HttpGet(uri);

		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			InputStream instream = null;
			try {
				instream = entity.getContent();
				String responseAsString = IOUtils.toString(instream);

				JSONParser pars = new JSONParser();

				try {
					JSONObject obj = (JSONObject) pars.parse(responseAsString);
					JSONObject jsonResponse = (JSONObject) obj.get("response");

					itemsCount = (long) jsonResponse.get("count");

					System.out.println("items count: "+itemsCount);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					//e.getMessage();
				}

			} finally {
				if (instream != null)
					instream.close();
			}
		}

		return itemsCount;
	}
*/
}






