package com.example.readspread.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

class BookActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BookTest()
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun BookTest(){
    val pages = listOf("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at enim orci. Fusce bibendum iaculis sem, vitae tincidunt ipsum molestie ut. Vivamus sit amet pellentesque arcu, a commodo tortor. Vivamus ipsum massa, ultricies id luctus at, posuere sit amet lorem. Donec sed placerat nisl. Proin quam tortor, pulvinar a rutrum eget, sodales sit amet mi. Vivamus venenatis sapien sem, ut tempor mauris vulputate sed. Sed gravida sem quis porta malesuada. Vestibulum vehicula congue aliquam. Etiam tincidunt egestas mauris quis accumsan. Sed pulvinar, risus rutrum vehicula semper, magna justo eleifend neque, eu ornare arcu quam a turpis. Cras gravida libero pretium aliquam eleifend. Nulla id laoreet ipsum. Aliquam erat volutpat. Suspendisse id suscipit ex, id varius arcu. ",
        "Sed sollicitudin euismod nunc, ut efficitur augue ornare at. Aliquam et consectetur nulla. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Sed a vulputate augue. Sed sodales ipsum felis, et pharetra magna feugiat et. Duis sed suscipit leo, quis imperdiet magna. Suspendisse tempor dui vel est ultrices eleifend. Sed scelerisque consectetur rhoncus. Aliquam dapibus efficitur porttitor. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Quisque est urna, ullamcorper et velit sit amet, commodo pellentesque nibh. Etiam mollis elit sem, sit amet faucibus est ultrices sodales. Vestibulum malesuada auctor purus sed efficitur. Quisque ut orci aliquet, vulputate enim quis, sodales nisi. ",
        "Morbi vitae consectetur metus, vitae finibus ex. Nulla quis volutpat nulla. Integer elementum ligula magna, sit amet mollis mi rhoncus sit amet. Duis molestie odio ac tempus porta. Curabitur accumsan eleifend ullamcorper. Sed quis neque venenatis, sollicitudin mauris viverra, interdum neque. Suspendisse egestas pharetra dolor ac suscipit. ",
        "Cras posuere id augue et posuere. Nam blandit augue est, nec bibendum urna ultrices eget. Quisque mollis nisi ac mauris consectetur mollis. Etiam consequat dolor blandit ante laoreet commodo. Donec in risus nec augue aliquet tincidunt ut eget mi. Sed bibendum, ex et consequat pretium, lectus nunc sagittis mauris, eu venenatis justo massa vel libero. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Donec interdum elit at mollis consectetur. Proin eget nisi malesuada, sodales ligula pellentesque, tristique urna. Suspendisse sed orci justo. Suspendisse suscipit odio id dapibus condimentum. Nullam dapibus massa et enim hendrerit mollis. Ut volutpat tincidunt lacus sit amet volutpat. Vestibulum volutpat rhoncus eleifend. ")

    var pageNumber by remember { mutableIntStateOf(0) }
    var page by remember { mutableStateOf(pages[pageNumber]) }
    var fontSize by remember { mutableIntStateOf(20) }
    var selectedFont by remember { mutableStateOf(FontFamily.Default) }
    var fontMenuExpanded by remember { mutableStateOf(false) }

    // List of available system fonts
    val fontOptions = listOf(
        "Default" to FontFamily.Default,
        "Serif" to FontFamily.Serif,
        "Sans Serif" to FontFamily.SansSerif,
        "Monospace" to FontFamily.Monospace,
        "Cursive" to FontFamily.Cursive
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Row for page decrementer, font size text field, and page incrementer
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Page decrementer
            Box(
                modifier = Modifier
                    .size(60.dp, 45.dp)
                    .background(Color.Gray)
                    .clickable(onClick = {
                        pageNumber = max(pageNumber - 1, 0)
                        page = pages[pageNumber]
                    })
            )

            // Font size text field
            TextField(
                value = fontSize.toString(),
                onValueChange = { newValue ->
                    fontSize = max((newValue.toIntOrNull() ?: fontSize), 0)
                },
                modifier = Modifier
                    .size(80.dp, 45.dp),
                textStyle = TextStyle(fontSize = 14.sp)
            )

            // Page incrementer
            Box(
                modifier = Modifier
                    .size(60.dp, 45.dp)
                    .background(Color.Gray)
                    .clickable(onClick = {
                        pageNumber = min(pageNumber + 1, pages.size - 1)
                        page = pages[pageNumber]
                    })
            )
        }

        // Font picker dropdown
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
        ) {
            TextField(
                value = fontOptions.find { it.second == selectedFont }?.first ?: "Select Font",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable { fontMenuExpanded = true },
                enabled = false,
                textStyle = TextStyle(fontSize = 14.sp)
            )

            DropdownMenu(
                expanded = fontMenuExpanded,
                onDismissRequest = { fontMenuExpanded = false },
                modifier = Modifier.wrapContentWidth()
            ) {
                fontOptions.forEach { (fontName, fontFamily) ->
                    DropdownMenuItem(
                        text = { Text(fontName) },
                        onClick = {
                            selectedFont = fontFamily
                            fontMenuExpanded = false
                        }
                    )
                }
            }
        }

        Text(
            page,
            fontSize = fontSize.sp,
            modifier = Modifier.padding(10.dp).padding(vertical = 20.dp),
            style = TextStyle(fontFamily = selectedFont)
        )

        // Page counter aligned to bottom center
        Text(
            "${pageNumber + 1}/${pages.size}",
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp)
                .wrapContentSize(Alignment.BottomCenter)
        )
    }
}