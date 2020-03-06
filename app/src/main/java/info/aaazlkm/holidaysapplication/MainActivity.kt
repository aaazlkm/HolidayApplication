package info.aaazlkm.holidaysapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        holidayButton.setOnClickListener {
            val permissions =
                listOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)

            checkPermissionOf(permissions) {
                holidayText.text = ""
                val holidays = HolidayProvider.loadHolidays(this)
                holidays.forEach {
                    var textForLog = "countryName: ${it.countryName}---------------------\n"
                    it.holidays.forEach { holiday ->
                        val dateStart = Date(holiday.dateStart)
                        val dateEnd = Date(holiday.dateEnd)
                        val simpleDateFormat =
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").apply {
                                timeZone = TimeZone.getTimeZone(holiday.timeZone)
                            }

                        textForLog = "$textForLog" +
                                "countryName: ${holiday.name}\n" +
                                "dateStart: ${simpleDateFormat.format(dateStart)}\n" +
                                "dateEnd: ${simpleDateFormat.format(dateEnd)}\n" +
                                "timezone: ${holiday.timeZone}\n"

                    }
                    holidayText.text = "${holidayText.text}\n$textForLog"
                }
            }
        }
    }

    private fun checkPermissionOf(permissions: List<String>, doOnPermissionChecked: () -> Unit) {
        val permissionsNotGranted =
            permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

        if (permissionsNotGranted.isEmpty()) {
            doOnPermissionChecked()
        } else {
            ActivityCompat.requestPermissions(
                this,
                permissionsNotGranted.toTypedArray(),
                1000
            )
        }
    }
}
