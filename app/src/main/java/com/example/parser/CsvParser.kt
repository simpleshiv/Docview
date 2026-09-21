package com.example.parser

object CsvParser {
    fun parse(content: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val lines = content.lines()
        for (line in lines) {
            if (line.isBlank()) continue
            val tokens = mutableListOf<String>()
            val sb = StringBuilder()
            var inQuotes = false

            for (i in line.indices) {
                val c = line[i]
                if (c == '\"') {
                    inQuotes = !inQuotes
                } else if (c == ',' && !inQuotes) {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                } else {
                    sb.append(c)
                }
            }
            tokens.add(sb.toString().trim())
            rows.add(tokens)
        }
        return rows
    }
}
