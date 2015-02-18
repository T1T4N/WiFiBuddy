package com.titantech.wifibuddy.models;

/**
 * Created by Robert on 24.01.2015.
 */
public class Constants {
    public static final String PREFS_NAME = "com.titantech.wifibuddy";
    public static final String PREFS_KEY_LAST_UPDATE = "last_update";

    public static final String TASK_STARTED = "com.titantech.wifibuddy.TASK_STARTED";
    public static final String TASK_COMPLETED = "com.titantech.wifibuddy.TASK_COMPLETED";
    public static final String TASK_URL_NOT_PROVIDED = "com.titantech.wifibuddy.TASK_URL_NOT_PROVIDED";

    public static final String TASK_EXCEPTION_OCCURRED = "com.titantech.wifibuddy.TASK_EXCEPTION_OCCURRED";
    public static final String SERVICE_BATCH_STARTED = "com.titantech.wifibuddy.SERVICE_BATCH_STARTED";
    public static final String SERVICE_BATCH_COMPLETED = "com.titantech.wifibuddy.SERVICE_BATCH_COMPLETED";
    public static final String SERVICE_UPDATE_STARTED = "com.titantech.wifibuddy.SERVICE_UPDATE_STARTED";
    public static final String SERVICE_UPDATE_RESULT_STATUS = "com.titantech.wifibuddy.SERVICE_UPDATE_RESULT_STATUS";
    public static final String SERVICE_UPDATE_RESULT_TASK = "com.titantech.wifibuddy.SERVICE_UPDATE_RESULT_TASK";
    public static final String SERVICE_UPDATE_COMPLETED = "com.titantech.wifibuddy.SERVICE_UPDATE_COMPLETED";
    public static final String FILENAME_UPDATES = "update_queue";

    public static final String ARG_SECTION_NUMBER = "section_number";
    public static final String EXTRA_ACTION_EDIT = "action_item";
    public static final String EXTRA_ACTION = "action";

    public static final int ACTION_EDIT = 0;
    public static final int ACTION_ADD = 1;

    public static final int SERVICE_ACTION_GET_PUBLIC = 317;
    public static final int SERVICE_ACTION_GET_PRIVATE = 318;
    public static final int SERVICE_ACTION_PUT = 319;
    public static final int SERVICE_ACTION_BATCH_TASKS = 320;

    public static final int SERVICE_RESULT_UNREACHABLE = -2;
    public static final int SERVICE_RESULT_UNAUTHORIZED = -1;

    public static final int LOADER_PRIVATE_ID = 347;
    public static final int LOADER_PUBLIC_ID = 348;
}
