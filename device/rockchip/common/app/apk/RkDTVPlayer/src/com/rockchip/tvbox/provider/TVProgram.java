/********************************************************************************************************************/
/**
 *  @skip   $Id:$
 *  @file   TVProgram.java
 *  @brief  TV Programs table.
 *  @date   2011/11/16 ROCKCHIP) Chenqsh create.
 *
 *  ALL Rights Reserved, Copyright(C) ROCKCHIP LIMITED 2011
 */
/********************************************************************************************************************/
package com.rockchip.tvbox.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class TVProgram {
    public static final String AUTHORITY = "com.rockchip.tvbox.provider.TVProgram";

    // This class cannot be instantiated
    private TVProgram() {}
    
    /**
     * TV Programs table
     */
    public static final class Programs implements BaseColumns {
        // This class cannot be instantiated
        private Programs() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/programs");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.rockchip.programs";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.rockchip.programs";

        /**
         * The default sort order for this table
         */
        public static final String SORT_ORDER_BY_ID = "serviceid ASC";//"type ASC, servicename ASC";//"title DESC";
        public static final String SORT_ORDER_BY_NAME = "servicename ASC";//"type ASC, servicename ASC";//"title DESC";

        /**
         * service ID
         * <P>Type: TEXT</P>
         */
        public static final String SERVICEID = "serviceid";
        
        /**
         * The name of the service
         * <P>Type: TEXT</P>
         */
        public static final String SERVICENAME = "servicename";

        /**
         * The service frequency
         * <P>Type: TEXT</P>
         */
        public static final String FREQ = "freq";
        
        /**
         * The service bandwidth
         * <P>Type: TEXT</P>
         */
        public static final String BW = "bandwidth";

        /**
         * The service type: 1 - TV 2 - Radio
         * <P>Type: INTEGER</P>
         */
        public static final String TYPE = "type";
        
        /**
         * If the service is favorite : 0 - false 1 - true
         * <P>Type: INTEGER</P>
         */
        public static final String FAV = "favorite";
        
        /**
         * If the service is encrypt : 0 - false 1 - true
         * <P>Type: INTEGER</P>
         */
        public static final String ENCRYPT = "encrypt";

        /**
         * If scan LCN : 0 - false 1 - true
         * <P>Type: INTEGER</P>
         */
        public static final String LCN = "lcn";
        
    }
}
