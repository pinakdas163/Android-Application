package com.pinak.currenttrends;

/**
 * Created by Pinak on 22-07-2016.
 */
public class ParseApplication {
    private String name;
    private String releaseDate;
    private String artist;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public String toString() {

        return "Name: " + getName() +"\n" +
                "Artist: " + getArtist() + "\n" +
                        "Release Date: " + getReleaseDate() + "\n";
    }
}
