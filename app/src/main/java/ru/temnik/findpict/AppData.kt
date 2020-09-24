package ru.temnik.findpict

object AppData {
    const val BASE_URL = "https://pixabay.com"
    const val API_KEY = "18103189-f1ecfa687903d71fc75cc581f"


    //System
    const val APP_LOG_NAME = "[FindPict_APP]"
    const val DEBUG_TAG = "FindPict"
    const val REQUEST_CODE = 2703
    const val DEFAULT_SAVE_IMAGES_DIRECTORY = "FindPict/images"

    const val APP_SHARED_PREF = "FIND_PICT_SHARED_PREF"
    const val SHARED_PREF_LAST_CACHE_REMOVAL = "LAST_CACHE_REMOVAL"
    const val CACHE_FLUSH_PERIOD: Long = 1800000L // 30 min
}