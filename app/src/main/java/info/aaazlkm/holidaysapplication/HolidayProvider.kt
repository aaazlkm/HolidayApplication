package info.aaazlkm.holidaysapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat

data class CalendarInDevice(
    val id: Long,
    val name: String,
    val accountName: String,
    val calendarDisplayName: String,
    val ownerAccount: String
)

data class CalendarEvent(
    val name: String,
    val description: String,
    val dateStart: Long,
    val dateEnd: Long,
    val timeZone: String
)

data class CountryHoliday(
    val countryName: String,
    val holidays: List<Holiday>
)

data class Holiday(
    val name: String,
    val dateStart: Long,
    val dateEnd: Long,
    val timeZone: String
)

object HolidayProvider {

    /**
     * Google Calendar が提供する祝日が登録されてるカレンダーは`ownerAccount`のsuffixが以下の値で終わる
     * ドキュメントの記載などは見つけられなかった
     */
    private const val GOOGLE_HOLIDAY_CALENDAR_SUFFIX = "#holiday@group.v.calendar.google.com"

    /**
     * 端末に紐づいてるGoogleアカウントのカレンダーから祝日が登録されているカレンダーのみを取得し、祝日を抽出する
     *
     * @param context　Context
     * @return List<CountryHoliday>
     */
    fun loadHolidays(context: Context): List<CountryHoliday> {
        return loadAllCalendarsInDevice(context)
            .filter { it.ownerAccount.endsWith(GOOGLE_HOLIDAY_CALENDAR_SUFFIX) } // 祝日が登録されているカレンダーは`ownerAccount`が"#holiday@group.v.calendar.google.com"で終わる
            .map {
                val holidays = loadAllEventsOfCalendarId(context, it.id)
                    .map { event ->
                        Holiday(
                            event.name,
                            event.dateStart,
                            event.dateEnd,
                            event.timeZone
                        )
                    }
                CountryHoliday(it.name, holidays)
            }
            .filter { it.holidays.isNotEmpty() } // 祝日が取得できない場合ある
            .distinctBy { it.countryName } // 複数アカウントに祝日が紐づいてるとき重複する
    }

    /**
     * 端末に紐づいているカレンダーを全て取得する
     * カレンダーのプロパティとして他にも取得できる値あり
     *
     * @param context Context
     * @return List<CalendarInDevice>
     */
    private fun loadAllCalendarsInDevice(context: Context): List<CalendarInDevice> {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw Exception("Manifest.permission.READ_CALENDAR権限が与えられていません")
        }

        val calendarInDevices = mutableListOf<CalendarInDevice>()

        // クエリ条件を設定する
        val uri = CalendarContract.Calendars.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.OWNER_ACCOUNT
        )

        // クエリを発行してカーソルを取得する
        val cursor =
            context.contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            while (cursor.moveToNext()) {
                // カーソルから各プロパティを取得する
                val id = cursor.getLong(0)
                val name = cursor.getString(1) ?: ""
                val accountName = cursor.getString(2) ?: ""
                val calendarDisplayName = cursor.getString(3) ?: ""
                val ownerAccount = cursor.getString(4) ?: ""

                calendarInDevices.add(
                    CalendarInDevice(
                        id,
                        name,
                        accountName,
                        calendarDisplayName,
                        ownerAccount
                    )
                )
            }
        }

        return calendarInDevices
    }

    /**
     * カレンダーIDに紐づく全てのEventを取得する
     * イベントの値として他にも取得できる値あり
     *
     * @param context Context
     * @param id calendar id
     * @return List<CalendarEvent>
     */
    private fun loadAllEventsOfCalendarId(context: Context, id: Long): List<CalendarEvent> {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw Exception("Manifest.permission.READ_CALENDAR権限が与えられていません")
        }

        val events = mutableListOf<CalendarEvent>()

        // クエリ条件を設定する
        val uri = CalendarContract.Events.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_TIMEZONE

        )
        val selection = "(" + CalendarContract.Events.CALENDAR_ID + " = ?)"
        val selectionArgs = arrayOf(id.toString())

        // クエリを発行してカーソルを取得する
        val cursor =
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)

        cursor?.use {
            while (cursor.moveToNext()) {
                // カーソルから各プロパティを取得する
                val title = cursor.getString(0) ?: ""
                val description = cursor.getString(1) ?: ""
                val dateStart = cursor.getLong(2)
                val dateEnd = cursor.getLong(3)
                val eventTimeZone = cursor.getString(4) ?: ""

                events.add(
                    CalendarEvent(
                        title,
                        description,
                        dateStart,
                        dateEnd,
                        eventTimeZone
                    )
                )
            }
        }

        return events
    }
}

