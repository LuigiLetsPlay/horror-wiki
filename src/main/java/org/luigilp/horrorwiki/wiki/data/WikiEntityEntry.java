package org.luigilp.horrorwiki.wiki.data;

import com.google.gson.annotations.SerializedName;

public class WikiEntityEntry {
    private String name;
    @SerializedName(value = "modName", alternate = {"mod-name"})
    private String modName;
    private String icon;
    private String image;
    private String description;
    private Integer danger;

    public String getName() {
        return name == null ? "" : name;
    }

    public String getModName() {
        return modName == null ? "" : modName;
    }

    public String getImage() {
        return image == null ? "" : image;
    }

    public String getIcon() {
        if (icon != null && !icon.isBlank()) {
            return icon;
        }
        return getImage();
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public int getDanger() {
        int raw = danger == null ? 0 : danger;
        if (raw < 0) {
            return 0;
        }
        return Math.min(5, raw);
    }

    public String getDangerLabel() {
        return Integer.toString(getDanger());
    }

    public boolean isValid() {
        return !getName().isBlank() && !getModName().isBlank();
    }
}
