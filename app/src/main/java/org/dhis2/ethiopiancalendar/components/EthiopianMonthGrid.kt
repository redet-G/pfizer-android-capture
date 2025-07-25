//package org.dhis2.ethiopiancalendar.components
//
//@Composable
//fun EthiopianMonthGrid(
//    year: Int,
//    month: Int,
//    selectedDate: EthiopianDate,
//    onDateClick: (EthiopianDate) -> Unit
//) {
//    val dates = remember(year, month) {
//        EthiopianCalendarUtils.getMonthDays(year, month)
//    }
//
//    LazyVerticalGrid(
//        columns = GridCells.Fixed(7),
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 4.dp)
//    ) {
//        items(dates) { ethDate ->
//            EthiopianDayCell(
//                date = ethDate,
//                isSelected = ethDate == selectedDate,
//                onClick = { onDateClick(ethDate) }
//            )
//        }
//    }
//}
