package com.oxygen.weather.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oxygen.weather.app.ui.theme.LocalOxygenHomeDesign

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable () -> Unit,
) {
    val roles = LocalOxygenHomeDesign.current
    Surface(
        modifier = modifier,
        color = roles.strongGlassSurface,
        shape = RoundedCornerShape(roles.homeCardCorner),
        border = BorderStroke(1.dp, roles.outlineAccent),
        shadowElevation = 0.dp,
    ) {
        androidx.compose.foundation.layout.Box(Modifier.padding(contentPadding)) {
            content()
        }
    }
}
