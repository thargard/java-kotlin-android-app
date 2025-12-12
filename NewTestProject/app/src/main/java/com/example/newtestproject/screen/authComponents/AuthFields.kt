package com.example.newtestproject.screen.authComponents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.newtestproject.R

@Composable
fun LoginField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(id = R.string.login)) },
        modifier = modifier,
        singleLine = true
    )
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    labelRes: Int,
    modifier: Modifier = Modifier,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(id = labelRes)) },
        modifier = modifier,
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        colors = colors
    )
}

@Composable
fun RepeatPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(id = R.string.second_password)) },
        modifier = modifier,
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        colors = colors
    )
}

@Composable
fun FullNameField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(id = R.string.full_name)) },
        modifier = modifier,
        singleLine = true
    )
}

@Composable
fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(id = R.string.email)) },
        modifier = modifier,
        singleLine = true
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RoleSelectionField(
    selectedRole: String?,
    onRoleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf(
        "PRODUCER" to R.string.role_freelancer,
        "CUSTOMER" to R.string.role_customer,
        "ADMIN" to R.string.role_admin
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedRole?.let { role ->
                roles.find { it.first == role }?.let { 
                    stringResource(id = it.second) 
                } ?: role
            } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(id = R.string.role)) },
            placeholder = { Text(stringResource(id = R.string.select_role)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            roles.forEach { (roleValue, roleLabelRes) ->
                DropdownMenuItem(
                    text = { Text(stringResource(id = roleLabelRes)) },
                    onClick = {
                        onRoleSelected(roleValue)
                        expanded = false
                    }
                )
            }
        }
    }
}

