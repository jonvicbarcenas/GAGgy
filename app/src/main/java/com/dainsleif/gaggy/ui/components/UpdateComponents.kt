package com.dainsleif.gaggy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dainsleif.gaggy.model.VersionData

@Composable
fun UpdateDialog(
    versionData: VersionData?,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onForceUpdate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "App Update",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp)
                        )
                        Text(
                            text = "Checking for updates...",
                            textAlign = TextAlign.Center
                        )
                    }
                    error != null -> {
                        Text(
                            text = "Error: $error",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                    versionData != null -> {
                        Text(
                            text = "New version available: ${versionData.version}",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Would you like to update now?",
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        Text(
                            text = "No update information available",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                if (versionData != null && !isLoading) {
                    Button(
                        onClick = onForceUpdate,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Update")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Dismiss")
                }
            }
        }
    )
} 