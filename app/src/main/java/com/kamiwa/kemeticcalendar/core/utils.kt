package com.kamiwa.kemeticcalendar.core

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.net.toUri
import com.kamiwa.kemeticcalendar.CheckActivity
import com.kamiwa.kemeticcalendar.CustomActivity
import com.kamiwa.kemeticcalendar.MainActivity
import com.kamiwa.kemeticcalendar.MonthActivity
import com.kamiwa.kemeticcalendar.R
import com.kamiwa.kemeticcalendar.ui.theme.BackgroundColor
import com.kamiwa.kemeticcalendar.ui.theme.TextColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(context: Context) {
    return TopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = TextColor,
            titleContentColor = BackgroundColor
        ),
        modifier = Modifier.clickable {
            val intent = Intent(Intent.ACTION_VIEW, "https://kemetyzm.pl".toUri())
            context.startActivity(intent)
        }
    )
}

@Composable
fun CustomBottomBar(context: Context, currentActivityId: Int) {
    return NavigationBar(
        containerColor = TextColor
    ) {
        CustomNavigationBarItem(
            Icons.Default.CalendarToday,
            R.string.menu_bottom_navigation_day,
            context,
            MainActivity::class.java,
            0,
            currentActivityId
        )
        CustomNavigationBarItem(
            Icons.Default.CalendarMonth,
            R.string.menu_bottom_navigation_month,
            context,
            MonthActivity::class.java,
            1,
            currentActivityId
        )
        CustomNavigationBarItem(
            Icons.Default.EditCalendar,
            R.string.menu_bottom_navigation_custom,
            context,
            CustomActivity::class.java,
            2,
            currentActivityId
        )
        CustomNavigationBarItem(
            Icons.Default.Search,
            R.string.menu_bottom_navigation_check,
            context,
            CheckActivity::class.java,
            3,
            currentActivityId
        )
    }
}

@Composable
fun RowScope.CustomNavigationBarItem(
    icon: ImageVector,
    stringId: Int,
    context: Context,
    activity: Class<*>,
    targetActivityId: Int,
    currentActivityId: Int
) {
    NavigationBarItem(
        selected = targetActivityId == currentActivityId,
        onClick = {
            if (targetActivityId != currentActivityId) {
                context.startActivity(Intent(context, activity).apply {
                    flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                })
            }
        },
        icon = {
            Icon(
                icon,
                contentDescription = null,
            )
        },
        label = {
            Text(
                stringResource(stringId),
                color = BackgroundColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = BackgroundColor,
            selectedIconColor = TextColor,
            unselectedIconColor = BackgroundColor
        )
    )

}