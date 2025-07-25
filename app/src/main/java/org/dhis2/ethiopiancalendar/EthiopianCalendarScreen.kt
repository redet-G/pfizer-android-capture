//package org.dhis2.ethiopiancalendar
//
//@Composable
//fun EthiopianCalendarScreen(
//    onDateSelected: (EthiopianDate) -> Unit
//) {
//    val today = EthiopianDateConverter.gregorianToEthiopian(Calendar.getInstance().time)
//    var selectedDate by remember { mutableStateOf(today) }
//    var visibleMonth by remember { mutableStateOf(today.month) }
//    var visibleYear by remember { mutableStateOf(today.year) }
//
//    Column(modifier = Modifier.fillMaxSize()) {
//        EthiopianCalendarHeader(
//            month = visibleMonth,
//            year = visibleYear,
//            onPreviousMonth = {
//                if (visibleMonth == 1) {
//                    visibleMonth = 13
//                    visibleYear -= 1
//                } else {
//                    visibleMonth -= 1
//                }
//            },
//            onNextMonth = {
//                if (visibleMonth == 13) {
//                    visibleMonth = 1
//                    visibleYear += 1
//                } else {
//                    visibleMonth += 1
//                }
//            }
//        )
//
//        EthiopianMonthGrid(
//            year = visibleYear,
//            month = visibleMonth,
//            selectedDate = selectedDate,
//            onDateClick = {
//                selectedDate = it
//                onDateSelected(it)
//            }
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(
//            modifier = Modifier.align(Alignment.CenterHorizontally),
//            onClick = { /* handle open picker or confirm */ }
//        ) {
//            Text("Open Date Picker")
//        }
//    }
//}
