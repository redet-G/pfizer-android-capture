//package org.dhis2.ethiopiancalendar.components
//
//@Composable
//fun EthiopianCalendarHeader(
//    month: Int,
//    year: Int,
//    onPreviousMonth: () -> Unit,
//    onNextMonth: () -> Unit
//) {
//    val monthNameAmharic = ethiopianMonthNamesAmharic[month - 1]
//
//    Column {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(MaterialTheme.colorScheme.primary)
//                .padding(12.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            IconButton(onClick = onPreviousMonth) {
//                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month", tint = Color.White)
//            }
//
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                Text(
//                    text = "$monthNameAmharic $year",
//                    style = MaterialTheme.typography.titleMedium,
//                    color = Color.White
//                )
//                val gregorianRange = getGregorianDateRange(year, month)
//                Text(
//                    text = gregorianRange,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = Color.White
//                )
//            }
//
//            IconButton(onClick = onNextMonth) {
//                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month", tint = Color.White)
//            }
//        }
//
//        // Amharic weekday headers
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceAround
//        ) {
//            listOf("እሁን", "ሰኞ", "ማክሰ", "ረቡዕ", "ሐሙስ", "ዓርብ", "ቅዳሜ").forEach {
//                Text(text = it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
//            }
//        }
//    }
//}
