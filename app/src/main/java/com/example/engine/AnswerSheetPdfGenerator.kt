package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.model.Exam
import com.example.data.model.SchoolClass
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AnswerSheetConfig(
    val schoolName: String = "Escola / Instituição de Ensino",
    val className: String = "3º Ano A",
    val schoolYear: String = "2026",
    val subject: String = "Matemática",
    val examTitle: String = "Avaliação Bimestral",
    val teacherName: String = "Professor(a)",
    val dateText: String = "",
    val totalQuestions: Int = 20,
    val optionsPerQuestion: Int = 5, // 3, 4 or 5
    val sheetsPerPage: Int = 1, // 1, 2, or 4
    val showFiducials: Boolean = true,
    val showInstructions: Boolean = true,
    val showStudentNumber: Boolean = true,
    val showScoreBox: Boolean = true,
    val examId: Long? = null
) {
    companion object {
        fun fromExamAndClass(exam: Exam, schoolClass: SchoolClass, schoolName: String = "Escola / Instituição"): AnswerSheetConfig {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            return AnswerSheetConfig(
                schoolName = schoolName,
                className = schoolClass.name,
                schoolYear = schoolClass.schoolYear,
                subject = schoolClass.subject.ifEmpty { exam.subject },
                examTitle = exam.title,
                teacherName = "Professor(a)",
                dateText = dateFormat.format(Date(exam.createdAt)),
                totalQuestions = exam.totalQuestions,
                optionsPerQuestion = exam.optionsPerQuestion,
                sheetsPerPage = if (exam.totalQuestions <= 15) 2 else 1,
                showFiducials = true,
                showInstructions = true,
                showStudentNumber = true,
                showScoreBox = true,
                examId = exam.id
            )
        }
    }
}

object AnswerSheetPdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 standard height in points

    /**
     * Builds and saves a PDF document to app cache and returns the File.
     */
    fun createPdfDocument(context: Context, config: AnswerSheetConfig): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawPage(canvas, config)

        pdfDocument.finishPage(page)

        val outputDir = File(context.cacheDir, "answer_sheets")
        if (!outputDir.exists()) outputDir.mkdirs()

        val sanitizedClass = config.className.replace(Regex("[^a-zA-Z0-9_]"), "_")
        val sanitizedTitle = config.examTitle.replace(Regex("[^a-zA-Z0-9_]"), "_")
        val fileName = "Gabarito_${sanitizedClass}_${sanitizedTitle}_${config.totalQuestions}Q.pdf"
        val outputFile = File(outputDir, fileName)

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    /**
     * Saves the generated PDF directly to the user's Downloads or Documents folder on the device.
     */
    fun savePdfToDeviceStorage(context: Context, config: AnswerSheetConfig): Pair<Boolean, String> {
        return try {
            val sanitizedClass = config.className.replace(Regex("[^a-zA-Z0-9_]"), "_")
            val sanitizedTitle = config.examTitle.replace(Regex("[^a-zA-Z0-9_]"), "_")
            val fileName = "Folha_Respostas_${sanitizedClass}_${sanitizedTitle}_${config.totalQuestions}Q.pdf"

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            drawPage(page.canvas, config)
            pdfDocument.finishPage(page)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Gabaritos")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        pdfDocument.writeTo(out)
                    }
                    pdfDocument.close()
                    Pair(true, "Salvo em Downloads/Gabaritos/$fileName")
                } else {
                    pdfDocument.close()
                    Pair(false, "Falha ao criar arquivo no armazenamento")
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadsDir, "Gabaritos")
                if (!targetDir.exists()) targetDir.mkdirs()
                val targetFile = File(targetDir, fileName)
                FileOutputStream(targetFile).use { out ->
                    pdfDocument.writeTo(out)
                }
                pdfDocument.close()
                Pair(true, "Salvo em Downloads/Gabaritos/$fileName")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "Erro ao salvar PDF: ${e.localizedMessage}")
        }
    }

    /**
     * Directly prints or saves via Android native PrintManager.
     */
    fun printAnswerSheet(context: Context, config: AnswerSheetConfig) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val jobName = "Folha de Respostas - ${config.className} - ${config.examTitle}"

        printManager.print(
            jobName,
            object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: android.os.CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: android.os.Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder("answer_sheet.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: android.os.CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    if (destination == null) return
                    var out: OutputStream? = null
                    try {
                        val pdfDocument = PdfDocument()
                        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
                        val page = pdfDocument.startPage(pageInfo)
                        drawPage(page.canvas, config)
                        pdfDocument.finishPage(page)

                        out = FileOutputStream(destination.fileDescriptor)
                        pdfDocument.writeTo(out)
                        pdfDocument.close()
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback?.onWriteFailed(e.message)
                    } finally {
                        try {
                            out?.close()
                        } catch (ignored: Exception) {}
                    }
                }
            },
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()
        )
    }

    /**
     * Shares the PDF file with external apps (WhatsApp, Drive, Email, etc.).
     */
    fun sharePdf(context: Context, config: AnswerSheetConfig) {
        val file = createPdfDocument(context, config)
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Folha de Respostas - ${config.className} - ${config.examTitle}")
            putExtra(Intent.EXTRA_TEXT, "Segue a folha de respostas pronta para impressão para a turma ${config.className} (${config.subject}).")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartilhar Folha de Respostas"))
    }

    /**
     * Opens the generated PDF directly in the device's PDF viewer.
     */
    fun openPdf(context: Context, config: AnswerSheetConfig) {
        val file = createPdfDocument(context, config)
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            sharePdf(context, config)
        }
    }

    /**
     * Main drawing entrypoint for rendering on A4 Canvas.
     */
    private fun drawPage(canvas: Canvas, config: AnswerSheetConfig) {
        // Draw crisp white background
        canvas.drawColor(Color.WHITE)

        when (config.sheetsPerPage) {
            2 -> {
                // Two sheets per A4 page (top half and bottom half)
                val halfHeight = PAGE_HEIGHT / 2f
                val margin = 20f

                // Top sheet
                val topRect = RectF(margin, margin, PAGE_WIDTH - margin, halfHeight - 12f)
                drawSingleSheetUnit(canvas, config, topRect, isHalfPage = true)

                // Divider dashed cutting line
                val dashPaint = Paint().apply {
                    color = Color.DKGRAY
                    strokeWidth = 1f
                    style = Paint.Style.STROKE
                    pathEffect = DashPathEffect(floatArrayOf(6f, 4f), 0f)
                }
                canvas.drawLine(margin, halfHeight, PAGE_WIDTH - margin, halfHeight, dashPaint)

                // Cut label
                val textPaint = Paint().apply {
                    color = Color.GRAY
                    textSize = 8f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("✂  CORTE AQUI PARA SEPARAR OS GABARITOS  ✂", PAGE_WIDTH / 2f, halfHeight - 2f, textPaint)

                // Bottom sheet
                val bottomRect = RectF(margin, halfHeight + 12f, PAGE_WIDTH - margin, PAGE_HEIGHT - margin)
                drawSingleSheetUnit(canvas, config, bottomRect, isHalfPage = true)
            }
            4 -> {
                // Four sheets per A4 page (2x2 grid)
                val halfW = PAGE_WIDTH / 2f
                val halfH = PAGE_HEIGHT / 2f
                val margin = 14f

                val rects = listOf(
                    RectF(margin, margin, halfW - 8f, halfH - 8f),
                    RectF(halfW + 8f, margin, PAGE_WIDTH - margin, halfH - 8f),
                    RectF(margin, halfH + 8f, halfW - 8f, PAGE_HEIGHT - margin),
                    RectF(halfW + 8f, halfH + 8f, PAGE_WIDTH - margin, PAGE_HEIGHT - margin)
                )

                // Cut lines
                val dashPaint = Paint().apply {
                    color = Color.DKGRAY
                    strokeWidth = 1f
                    style = Paint.Style.STROKE
                    pathEffect = DashPathEffect(floatArrayOf(5f, 3f), 0f)
                }
                canvas.drawLine(halfW, margin, halfW, PAGE_HEIGHT - margin, dashPaint)
                canvas.drawLine(margin, halfH, PAGE_WIDTH - margin, halfH, dashPaint)

                for (rect in rects) {
                    drawSingleSheetUnit(canvas, config, rect, isQuarterPage = true)
                }
            }
            else -> {
                // Single Full Page
                val fullRect = RectF(28f, 28f, PAGE_WIDTH - 28f, PAGE_HEIGHT - 28f)
                drawSingleSheetUnit(canvas, config, fullRect, isHalfPage = false)
            }
        }
    }

    /**
     * Renders a single Answer Sheet block into the given bounding box rect.
     */
    private fun drawSingleSheetUnit(
        canvas: Canvas,
        config: AnswerSheetConfig,
        bounds: RectF,
        isHalfPage: Boolean = false,
        isQuarterPage: Boolean = false
    ) {
        val strokePaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }

        val fillPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val textBold = Paint().apply {
            color = Color.BLACK
            isFakeBoldText = true
            isAntiAlias = true
        }

        val textRegular = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }

        val textGray = Paint().apply {
            color = Color.DKGRAY
            isAntiAlias = true
        }

        // 1. Draw outer boundary box
        canvas.drawRoundRect(bounds, 6f, 6f, strokePaint)

        // 2. Optical Fiducial Marks (Corner Alignment Squares for OMR)
        if (config.showFiducials) {
            val fidSize = if (isQuarterPage) 10f else 14f
            val pad = 6f

            // Top-left
            canvas.drawRect(bounds.left + pad, bounds.top + pad, bounds.left + pad + fidSize, bounds.top + pad + fidSize, fillPaint)
            // Top-right
            canvas.drawRect(bounds.right - pad - fidSize, bounds.top + pad, bounds.right - pad, bounds.top + pad + fidSize, fillPaint)
            // Bottom-left
            canvas.drawRect(bounds.left + pad, bounds.bottom - pad - fidSize, bounds.left + pad + fidSize, bounds.bottom - pad, fillPaint)
            // Bottom-right
            canvas.drawRect(bounds.right - pad - fidSize, bounds.bottom - pad - fidSize, bounds.right - pad, bounds.bottom - pad, fillPaint)
        }

        var currentY = bounds.top + (if (isQuarterPage) 14f else 18f)
        val contentLeft = bounds.left + (if (isQuarterPage) 14f else 22f)
        val contentRight = bounds.right - (if (isQuarterPage) 14f else 22f)
        val contentWidth = contentRight - contentLeft

        // Header Title
        textBold.textSize = if (isQuarterPage) 9f else if (isHalfPage) 11f else 13f
        textBold.textAlign = Paint.Align.CENTER
        canvas.drawText("FOLHA DE RESPOSTAS — GABARITO OFICIAL", bounds.centerX(), currentY, textBold)
        currentY += if (isQuarterPage) 10f else 14f

        // Institution & Exam info
        textBold.textSize = if (isQuarterPage) 7.5f else if (isHalfPage) 8.5f else 10f
        textBold.textAlign = Paint.Align.CENTER
        canvas.drawText(config.schoolName.uppercase(Locale("pt", "BR")), bounds.centerX(), currentY, textBold)
        currentY += if (isQuarterPage) 9f else 12f

        // Horizontal separator
        canvas.drawLine(contentLeft, currentY, contentRight, currentY, strokePaint)
        currentY += 4f

        // Metadata grid (Turma, Disciplina, Avaliação, Data)
        textRegular.textSize = if (isQuarterPage) 6.5f else if (isHalfPage) 7.5f else 8.5f
        textBold.textSize = if (isQuarterPage) 6.5f else if (isHalfPage) 7.5f else 8.5f
        textRegular.textAlign = Paint.Align.LEFT
        textBold.textAlign = Paint.Align.LEFT

        // Line 1: Turma & Disciplina
        val col1X = contentLeft + 4f
        val col2X = contentLeft + contentWidth * 0.45f
        val col3X = contentLeft + contentWidth * 0.80f

        currentY += 10f
        canvas.drawText("TURMA: ", col1X, currentY, textBold)
        val turmaOffset = textBold.measureText("TURMA: ")
        canvas.drawText(config.className, col1X + turmaOffset, currentY, textRegular)

        canvas.drawText("DISCIPLINA: ", col2X, currentY, textBold)
        val discOffset = textBold.measureText("DISCIPLINA: ")
        canvas.drawText(config.subject, col2X + discOffset, currentY, textRegular)

        if (!isQuarterPage) {
            val dateLabel = if (config.dateText.isNotEmpty()) config.dateText else "___/___/______"
            canvas.drawText("DATA: $dateLabel", col3X, currentY, textRegular)
        }

        // Line 2: Avaliação / Prova
        currentY += if (isQuarterPage) 9f else 12f
        canvas.drawText("AVALIAÇÃO: ", col1X, currentY, textBold)
        val examOffset = textBold.measureText("AVALIAÇÃO: ")
        canvas.drawText(config.examTitle, col1X + examOffset, currentY, textRegular)

        if (!isQuarterPage && config.teacherName.isNotEmpty()) {
            canvas.drawText("PROF: ${config.teacherName}", col2X, currentY, textRegular)
        }

        currentY += if (isQuarterPage) 6f else 8f
        canvas.drawLine(contentLeft, currentY, contentRight, currentY, strokePaint)
        currentY += 6f

        // Student Info Box
        val studentBoxHeight = if (isQuarterPage) 22f else if (isHalfPage) 28f else 34f
        val scoreBoxWidth = if (config.showScoreBox) (if (isQuarterPage) 44f else 54f) else 0f
        val studentNameWidth = contentWidth - scoreBoxWidth - (if (scoreBoxWidth > 0) 6f else 0f)

        // Draw Student Box
        val studentBoxRect = RectF(contentLeft, currentY, contentLeft + studentNameWidth, currentY + studentBoxHeight)
        canvas.drawRoundRect(studentBoxRect, 3f, 3f, strokePaint)

        val innerTextY = currentY + (if (isQuarterPage) 10f else 13f)
        textBold.textSize = if (isQuarterPage) 6.5f else 8f
        canvas.drawText(" NOME DO ALUNO(A):", contentLeft + 4f, innerTextY, textBold)

        if (config.showStudentNumber) {
            val numY = innerTextY + (if (isQuarterPage) 9f else 12f)
            textRegular.textSize = if (isQuarterPage) 6.5f else 7.5f
            canvas.drawText(" Nº / MATRÍCULA: ________________________   ASSINATURA: ________________________", contentLeft + 4f, numY, textRegular)
        }

        // Score Box (Grade box for teacher)
        if (config.showScoreBox) {
            val scoreBoxRect = RectF(contentRight - scoreBoxWidth, currentY, contentRight, currentY + studentBoxHeight)
            canvas.drawRoundRect(scoreBoxRect, 3f, 3f, strokePaint)
            textBold.textSize = if (isQuarterPage) 6f else 7f
            textBold.textAlign = Paint.Align.CENTER
            canvas.drawText("NOTA", scoreBoxRect.centerX(), scoreBoxRect.top + (if (isQuarterPage) 8f else 10f), textBold)
            textRegular.textAlign = Paint.Align.LEFT
        }

        currentY += studentBoxHeight + (if (isQuarterPage) 4f else 8f)

        // Instructions banner
        if (config.showInstructions && !isQuarterPage) {
            textGray.textSize = if (isHalfPage) 6.5f else 7.5f
            val instr = "Instruções: Use caneta preta ou azul. Preencha completamente o círculo: (●) Correto  (×) Errado  (○) Não preenchido"
            canvas.drawText(instr, contentLeft, currentY + 4f, textGray)
            currentY += if (isHalfPage) 10f else 14f
        }

        // 3. Question Grid & Bubbles
        val availableHeight = bounds.bottom - currentY - (if (isQuarterPage) 14f else 20f)
        val totalQ = config.totalQuestions
        val optionsCount = config.optionsPerQuestion
        val optionLetters = listOf("A", "B", "C", "D", "E").take(optionsCount)

        // Determine number of columns based on question count and page size
        val numCols = when {
            isQuarterPage -> if (totalQ > 10) 2 else 1
            isHalfPage -> when {
                totalQ <= 15 -> 1
                totalQ <= 30 -> 2
                totalQ <= 45 -> 3
                else -> 4
            }
            else -> when {
                totalQ <= 15 -> 1
                totalQ <= 30 -> 2
                totalQ <= 50 -> 3
                else -> 4
            }
        }

        val questionsPerCol = (totalQ + numCols - 1) / numCols
        val colWidth = contentWidth / numCols
        val rowHeight = (availableHeight / questionsPerCol).coerceIn(
            if (isQuarterPage) 10f else 14f,
            if (isQuarterPage) 16f else if (isHalfPage) 22f else 28f
        )
        val bubbleRadius = if (isQuarterPage) 4.5f else if (isHalfPage) 6f else 7.5f
        val bubbleSpacing = if (isQuarterPage) 13f else if (isHalfPage) 17f else 21f

        val bubbleBorderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1.1f
            isAntiAlias = true
        }

        val bubbleLetterPaint = Paint().apply {
            color = Color.BLACK
            textSize = if (isQuarterPage) 5.5f else if (isHalfPage) 7f else 8.5f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val qNumberPaint = Paint().apply {
            color = Color.BLACK
            textSize = if (isQuarterPage) 6.5f else if (isHalfPage) 8f else 9.5f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        for (q in 1..totalQ) {
            val colIndex = (q - 1) / questionsPerCol
            val rowIndex = (q - 1) % questionsPerCol

            val cellLeft = contentLeft + (colIndex * colWidth)
            val cellY = currentY + (rowIndex * rowHeight) + (rowHeight / 2f)

            // Draw Question Number (e.g., "01", "02", ...)
            val qText = q.toString().padStart(2, '0')
            val qNumX = cellLeft + (if (isQuarterPage) 14f else 20f)
            val textMetric = qNumberPaint.fontMetrics
            val baseline = cellY - (textMetric.ascent + textMetric.descent) / 2f
            canvas.drawText(qText, qNumX, baseline, qNumberPaint)

            // Draw Bubbles
            val startBubbleX = qNumX + (if (isQuarterPage) 10f else 14f)
            for (i in optionLetters.indices) {
                val bubbleCenterX = startBubbleX + (i * bubbleSpacing)
                val bubbleCenterY = cellY

                // Draw circle outline
                canvas.drawCircle(bubbleCenterX, bubbleCenterY, bubbleRadius, bubbleBorderPaint)

                // Draw letter inside bubble
                val letter = optionLetters[i]
                val letterMetric = bubbleLetterPaint.fontMetrics
                val letterBaseline = bubbleCenterY - (letterMetric.ascent + letterMetric.descent) / 2f
                canvas.drawText(letter, bubbleCenterX, letterBaseline, bubbleLetterPaint)
            }

            // Draw light horizontal guideline between questions
            if (rowIndex < questionsPerCol - 1) {
                val guidePaint = Paint().apply {
                    color = Color.LTGRAY
                    strokeWidth = 0.5f
                }
                val lineY = cellY + (rowHeight / 2f)
                canvas.drawLine(cellLeft + 2f, lineY, cellLeft + colWidth - 6f, lineY, guidePaint)
            }
        }

        // Footer Exam Identification Code
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = if (isQuarterPage) 5.5f else 7f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val examCodeStr = if (config.examId != null) {
            "Gabarito Óptico • Código: [EXAM-${config.examId}-${config.totalQuestions}Q-${config.optionsPerQuestion}ALT]"
        } else {
            "Gabarito Óptico • ${config.className} • ${config.totalQuestions} Questões (${config.optionsPerQuestion} Alt)"
        }
        canvas.drawText(examCodeStr, bounds.centerX(), bounds.bottom - (if (isQuarterPage) 4f else 6f), footerPaint)
    }
}
