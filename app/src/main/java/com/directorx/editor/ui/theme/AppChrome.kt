package com.directorx.editor.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DirectorXScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LocalDirectorXColors.current
    Box(modifier = modifier.background(colors.background), content = content)
}

@Composable
fun DirectorXHeroCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Radius.xxl),
    accent: Color = Color.Unspecified,
    contentPadding: PaddingValues = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.xl),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalDirectorXColors.current
    val resolvedAccent = if (accent == Color.Unspecified) colors.accent else accent
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.panel,
        shape = shape,
        border = BorderStroke(
            1.dp,
            if (colors.highContrast) colors.cardStrokeStrong else resolvedAccent.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            content = content
        )
    }
}

@Composable
fun DirectorXPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = Spacing.lg, vertical = 16.dp)
) {
    val colors = LocalDirectorXColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.disabledSurface
            pressed -> colors.accentSecondary
            else -> colors.accent
        },
        animationSpec = tween(durationMillis = Motion.DurationFast, easing = Motion.StandardEasing),
        label = "primaryButtonContainer"
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (enabled && pressed) 0.985f else 1f,
        animationSpec = tween(durationMillis = Motion.DurationFast, easing = Motion.StandardEasing),
        label = "primaryButtonScale"
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(Radius.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = colors.onAccent,
            disabledContainerColor = containerColor,
            disabledContentColor = colors.disabledText
        ),
        contentPadding = contentPadding,
        modifier = modifier
            .graphicsLayer {
                scaleX = buttonScale
                scaleY = buttonScale
            }
            .semantics { contentDescription = text }
            .defaultMinSize(minWidth = TouchTarget.minimum, minHeight = TouchTarget.minimum)
            // Material buttons can be measured below the accessibility minimum when
            // their parent has a tight height constraint. Require the actual clickable
            // surface to stay at least 48dp tall, not just its preferred minimum.
            .requiredSizeIn(minHeight = TouchTarget.minimum + 4.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (!enabled) colors.disabledText else colors.onAccent,
                modifier = Modifier.size(18.dp)
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(Spacing.sm))
        }
        Text(
            text = text,
            color = if (!enabled) colors.disabledText else colors.onAccent,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun DirectorXSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    contentColor: Color = Color.Unspecified,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = Spacing.lg, vertical = 16.dp)
) {
    val colors = LocalDirectorXColors.current
    val resolvedContentColor = if (contentColor == Color.Unspecified) colors.text else contentColor
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.disabledSurface.copy(alpha = 0.28f)
            pressed -> colors.panelHighest.copy(alpha = if (colors.highContrast) 0.96f else 0.66f)
            else -> colors.panelHighest.copy(alpha = if (colors.highContrast) 0.88f else 0.42f)
        },
        animationSpec = tween(durationMillis = Motion.DurationFast, easing = Motion.StandardEasing),
        label = "secondaryButtonContainer"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.cardStroke.copy(alpha = 0.55f)
            pressed -> resolvedContentColor.copy(alpha = if (colors.highContrast) 0.86f else 0.42f)
            else -> colors.cardStrokeStrong
        },
        animationSpec = tween(durationMillis = Motion.DurationFast, easing = Motion.StandardEasing),
        label = "secondaryButtonBorder"
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (enabled && pressed) 0.985f else 1f,
        animationSpec = Motion.fast(),
        label = "secondaryButtonScale"
    )
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(Radius.md),
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = resolvedContentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = colors.disabledText
        ),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = 16.dp),
        modifier = modifier
            .graphicsLayer {
                scaleX = buttonScale
                scaleY = buttonScale
            }
            .semantics(mergeDescendants = true) { contentDescription = text }
            .defaultMinSize(minWidth = TouchTarget.minimum, minHeight = TouchTarget.minimum)
            .requiredSizeIn(minWidth = TouchTarget.minimum + 4.dp, minHeight = TouchTarget.minimum + 4.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) resolvedContentColor else colors.disabledText,
                modifier = Modifier.size(18.dp)
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(Spacing.sm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun DirectorXStatusTag(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val colors = LocalDirectorXColors.current
    val controlColors = DirectorXThemeDefaults.accentControlColors(colors, accent)
    Surface(
        modifier = modifier,
        color = controlColors.container,
        shape = RoundedCornerShape(Radius.xs),
        border = BorderStroke(1.dp, accent.copy(alpha = if (colors.highContrast) 0.95f else 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = controlColors.content,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text = text,
                color = controlColors.content,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DirectorXMetricPill(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) = DirectorXStatusTag(text = text, accent = accent, modifier = modifier, icon = icon)

@Composable
fun DirectorXFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = Color.Unspecified,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val colors = LocalDirectorXColors.current
    val resolvedAccent = if (accent == Color.Unspecified) colors.accent else accent
    val controlColors = DirectorXThemeDefaults.accentControlColors(colors, resolvedAccent)
    FilterChip(
        selected = selected,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = TouchTarget.minimum, minHeight = TouchTarget.minimum),
        label = {
            // Keep the minimum inside FilterChip's clickable surface. Applying
            // it only to the outer modifier can enlarge layout without enlarging
            // the accessibility hit target.
            Box(
                modifier = Modifier.requiredSizeIn(
                    minWidth = TouchTarget.minimum,
                    minHeight = TouchTarget.minimum,
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        leadingIcon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            null
        },
        shape = RoundedCornerShape(Radius.sm),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.panelHighest,
            labelColor = colors.subtext,
            selectedContainerColor = controlColors.container,
            selectedLabelColor = controlColors.content,
            selectedLeadingIconColor = controlColors.content
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = colors.cardStroke,
            selectedBorderColor = if (colors.highContrast) colors.cardStrokeStrong else resolvedAccent.copy(alpha = 0.34f)
        )
    )
}

@Composable
fun DirectorXChromeIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    containerColor: Color = Color.Unspecified,
    borderColor: Color = Color.Unspecified,
    shape: Shape = RoundedCornerShape(Radius.md),
    size: Dp = TouchTarget.minimum,
    iconSize: Dp = 18.dp,
    enabled: Boolean = true,
    buttonModifier: Modifier = Modifier
) {
    val colors = LocalDirectorXColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val resolvedContainer = if (containerColor == Color.Unspecified) colors.panelHighest else containerColor
    val resolvedBorder = if (borderColor == Color.Unspecified) colors.cardStroke else borderColor
    val resolvedTint = if (tint == Color.Unspecified) colors.subtext else tint
    val animatedContainer by animateColorAsState(
        targetValue = when {
            !enabled -> colors.panelRaised.copy(alpha = 0.46f)
            pressed -> resolvedContainer.copy(alpha = if (colors.highContrast) 1f else 0.88f)
            else -> resolvedContainer
        },
        animationSpec = tween(durationMillis = Motion.DurationFast, easing = Motion.StandardEasing),
        label = "chromeIconButtonContainer"
    )
    val animatedBorder by animateColorAsState(
        targetValue = when {
            !enabled -> colors.cardStroke.copy(alpha = 0.46f)
            pressed -> if (colors.highContrast) colors.cardStrokeStrong else resolvedTint.copy(alpha = 0.48f)
            colors.highContrast && borderColor == Color.Unspecified -> colors.cardStrokeStrong
            else -> resolvedBorder
        },
        animationSpec = tween(durationMillis = Motion.DurationFast, easing = Motion.StandardEasing),
        label = "chromeIconButtonBorder"
    )
    val animatedTint by animateColorAsState(
        targetValue = if (enabled) resolvedTint else colors.disabledText.copy(alpha = 0.72f),
        animationSpec = tween(durationMillis = Motion.DurationFast, easing = Motion.StandardEasing),
        label = "chromeIconButtonTint"
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (enabled && pressed) 0.965f else 1f,
        animationSpec = Motion.fast(),
        label = "chromeIconButtonScale"
    )
    Box(
        modifier = modifier
            .then(buttonModifier)
            // Constrain the clickable node itself. Putting the size modifier after
            // clickable only enlarges its parent layout, leaving a smaller hit target.
            .requiredSizeIn(
                minWidth = TouchTarget.minimum + 4.dp,
                minHeight = TouchTarget.minimum + 4.dp,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                }
                .clearAndSetSemantics { },
            color = animatedContainer,
            shape = shape,
            border = BorderStroke(1.dp, animatedBorder)
        ) {
            Box(Modifier.size(size))
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = animatedTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun DirectorXDialogIcon(
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalDirectorXColors.current
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = if (colors.highContrast) 0.26f else 0.14f),
        shape = RoundedCornerShape(Radius.md),
        border = BorderStroke(1.dp, accent.copy(alpha = if (colors.highContrast) 0.95f else 0.24f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier
                .padding(Spacing.md)
                .size(22.dp)
        )
    }
}

@Composable
fun DirectorXSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    val colors = LocalDirectorXColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Text(
                text = title,
                color = colors.text,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    color = colors.subtext,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(
            modifier = Modifier.defaultMinSize(minHeight = TouchTarget.minimum),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            content = trailing
        )
    }
}
