/**
 * Created by user on 04.12.2017.
 */
public class PhotoAlbum {

    private String albumName;
    private String albumDescription;
    private String albumId;
    private String albumUpdated;
    private long count;


    public PhotoAlbum(String albumName, String albumId, long count, String albumDescription, String albumUpdated) {
        this.albumName = albumName;
        this.albumId = albumId;
        this.albumDescription = albumDescription;
        this.albumUpdated = albumUpdated;
        this.count = count;
    }

    public PhotoAlbum(String albumName, String albumId, long count, String albumDescription) {
        this.albumName = albumName;
        this.albumId = albumId;
        this.albumDescription = albumDescription;
        this.count = count;
    }

    public PhotoAlbum(String albumName, String albumId, long count) {
        this.albumName = albumName;
        this.albumId = albumId;
        this.count = count;
    }

    public String albumName() {
        return albumName;
    }
    public String albumDescription() {
        return albumDescription;
    }
    public String albumId() {
        return albumId;
    }
    public String albumUpdated() {
        return albumUpdated;
    }
    public long count() {
        return count;
    }

    public void showAll() {

        System.out.println("albumName: " + albumName + ", albumDescription: " + albumDescription + ", albumId: " + albumId + ", albumUpdated: " + albumUpdated + ", count: " + String.valueOf(count));

    }
}
