//package org.dhis2.ethiopiancalendar.components
//
//@Composable
//fun EthiopianDayCell(
//    date: EthiopianDate,
//    isSelected: Boolean,
//    onClick: () -> Unit
//) {
//    val gregDate = EthiopianDateConverter.ethiopianToGregorian(date.year, date.month, date.day)
//
//    Column(
//        modifier = Modifier
//            .padding(2.dp)
//            .clip(CircleShape)
//            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
//            .clickable { onClick() }
//            .padding(8.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = date.day.toString(),
//            style = MaterialTheme.typography.bodySmall,
//            color = if (isSelected) Color.White else Color.Black
//        )
//        Text(
//            text = SimpleDateFormat("MMM d", Locale.ENGLISH).format(gregDate),
//            style = MaterialTheme.typography.labelSmall,
//            color = if (isSelected) Color.White else Color.Gray
//        )
//    }
//}
