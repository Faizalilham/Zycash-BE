package com.zycash.bereport.controller


import com.zycash.bereport.dto.PieChartResponse
import com.zycash.bereport.service.ReportService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.YearMonth

@RestController
@RequestMapping("/api/reports")
class ReportController(
    private val reportService: ReportService
) {

    @GetMapping("/pie-chart/daily")
    fun getDailyPieChart(
        @RequestParam userId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<PieChartResponse> {
        val data = reportService.getDailyPieChart(userId, date)
        return ResponseEntity.ok(data)
    }

    @GetMapping("/pie-chart/monthly")
    fun getMonthlyPieChart(
        @RequestParam userId: String,
        @RequestParam year: Int,
        @RequestParam month: Int
    ): ResponseEntity<PieChartResponse> {
        val yearMonth = YearMonth.of(year, month)
        val data = reportService.getMonthlyPieChart(userId, yearMonth)
        return ResponseEntity.ok(data)
    }

    @GetMapping("/pie-chart/yearly")
    fun getYearlyPieChart(
        @RequestParam userId: String,
        @RequestParam year: Int
    ): ResponseEntity<PieChartResponse> {
        val data = reportService.getYearlyPieChart(userId, year)
        return ResponseEntity.ok(data)
    }
}