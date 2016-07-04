package me.drakeet.transformer.entity;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * @author drakeet
 */
public class YouDao {

    /**
     * translation : ["Youdao translation is so good, 233"]
     * query : 有道翻译真不错，233
     * errorCode : 0
     */

    @SerializedName("query") public String query;
    @SerializedName("errorCode") public int errorCode;
    @SerializedName("translation") public List<String> translation;


    public boolean isSuccessful() {
        return errorCode == 0;
    }
}
