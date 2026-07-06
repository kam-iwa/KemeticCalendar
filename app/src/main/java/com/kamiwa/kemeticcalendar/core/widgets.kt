package com.kamiwa.kemeticcalendar.core

//import androidx.compose.runtime.Composable
//import androidx.glance.GlanceModifier
//import androidx.glance.Image
//import androidx.glance.ImageProvider
//import androidx.glance.appwidget.GlanceAppWidget
//import androidx.glance.layout.ContentScale
//import androidx.glance.layout.fillMaxSize


//
//class SimpleKemeticWidget : GlanceAppWidget() {
//    override suspend fun provideGlance(context: Context, id: GlanceId) {
//        provideContent {
//            val gregorianToday = LocalDate.now()
//            val kemeticToday = KemeticDate.today()
//            val lunarToday = LunarDate(gregorianToday)
//
//            val gregorianDayText = gregorianToday.format(
//                DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
//            )
//            val kemeticMonthText = context.getString(kemeticToday.getMonth().key)
//            val kemeticDayText = kemeticToday.day.toString()
//            val kemeticDayTypeText = context.getString(kemeticToday.getDayType())
//            val kemeticHolidaysText = kemeticToday.getHolidays().joinToString("; ") { context.getString(it.key) }
//
//            val lunarPhaseInfo = lunarToday.getLunarPhase()
//            val lunarPhaseText = context.getString(lunarPhaseInfo.first)
//            val lunarPhasePercent = lunarPhaseInfo.third * 100
//            val lunarHolidayText = context.getString(lunarToday.getHoliday())
//
//            Column(
//                modifier = GlanceModifier
//                    .fillMaxSize()
//                    .padding(6.dp)
//                    .clickable(actionStartActivity<MainActivity>()),
//            ) {
//
//            }
//            //Text(text = "Aktualizacja: ${LocalTime.now().toString().substring(0, 5)}")
//        }
//    }
//}
//
//// 2. WORKER - To on wykonuje pracę w tle
//class WidgetUpdateWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
//    override suspend fun doWork(): Result {
//        Log.d("WIDGET_TEST", "Worker właśnie odświeża widżet...")
//        SimpleKemeticWidget().updateAll(context)
//        return Result.success()
//    }
//}
//
//// 3. RECEIVER - Zarządza cyklem życia i WorkManagerem
//class SimpleWidgetReceiver : GlanceAppWidgetReceiver() {
//    override val glanceAppWidget: GlanceAppWidget = SimpleKemeticWidget()
//
//    override fun onEnabled(context: Context) {
//        super.onEnabled(context)
//        setupWork(context)
//    }
//
//    override fun onReceive(context: Context, intent: Intent) {
//        super.onReceive(context, intent)
//        // Reaguje na restart telefonu i aktualizację aplikacji
//        setupWork(context)
//    }
//
//    private fun setupWork(context: Context) {
//        val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
//            15, TimeUnit.MINUTES // Minimum to 15 minut
//        ).build()
//
//        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//            "widget_loop",
//            ExistingPeriodicWorkPolicy.KEEP, // Zachowaj istniejący, nie przerywaj odliczania
//            request
//        )
//        Log.d("WIDGET_TEST", "WorkManager zaplanowany.")
//    }
//}