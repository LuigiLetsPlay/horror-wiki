package org.luigilp.horrorwiki.wiki.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class WikiModEntry {
    @SerializedName(value = "name", alternate = {"displayName"})
    private String displayName;
    @SerializedName(value = "modName", alternate = {"mod-name"})
    private String modName;
    @SerializedName(value = "fileName", alternate = {"file-name"})
    private String fileName;
    private String icon;
    private String image;
    private String description;
    private List<WikiEntityEntry> entities;

    public String getDisplayName() {
        return displayName == null ? "" : displayName;
    }

    public String getModName() {
        return modName == null ? "" : modName;
    }

    public String getFileName() {
        return fileName == null ? "" : fileName;
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

    public List<WikiEntityEntry> getEntities() {
        return entities == null ? List.of() : entities;
    }

    public boolean isValid() {
        return !getDisplayName().isBlank() && !getModName().isBlank();
    }

    public WikiModEntry withEntities(List<WikiEntityEntry> sortedEntities) {
        WikiModEntry entry = new WikiModEntry();
        entry.displayName = this.displayName;
        entry.modName = this.modName;
        entry.fileName = this.fileName;
        entry.icon = this.icon;
        entry.image = this.image;
        entry.description = this.description;
        entry.entities = new ArrayList<>(sortedEntities);
        return entry;
    }
}
