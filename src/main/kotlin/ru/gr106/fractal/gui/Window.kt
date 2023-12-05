package ru.gr106.fractal.gui

import drawing.Converter
import drawing.Plane
import math.AlgebraicFractal
import math.Julia
import math.Mandelbrot
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.ActionEvent
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.GroupLayout
import javax.swing.GroupLayout.PREFERRED_SIZE
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import kotlin.math.*

class Window(val f: AlgebraicFractal) : JFrame() {

    private val af = f
    private val const = ln(15.0)
    private val mainPanel: DrawingPanel
    private val fp: FractalPainter
    var themes: Map<String, (Float) -> Color> = mapOf()



    init {
        fp = FractalPainter(af)
        val menuBar = createMenuBar()
        if(af is Mandelbrot)
            defaultCloseOperation = EXIT_ON_CLOSE
        else
            defaultCloseOperation = DISPOSE_ON_CLOSE
        minimumSize = Dimension(600, 550)
        mainPanel = DrawingPanel(fp)

        themes = mapOf(
            "green" to {
                if (it == 1f) Color.BLACK else
                    Color(
                        0.5f * (1 - cos(16f * it * it)).absoluteValue,
                        sin(5f * it).absoluteValue,
                        log10(1f + 5 * it).absoluteValue
                    )
            },
            "red" to {
                if (it == 1f) Color.BLACK else
                    Color(
                        cos(it + PI * (0.5 + sin(it))).absoluteValue.toFloat(),
                        cos(it + PI * (0.5 + cos(it))).absoluteValue.toFloat(),
                        (0.1 * cos(it)).absoluteValue.toFloat(),
                    )

            },
            "red-blue" to {
                if (it == 1f) Color.BLACK else
                    Color(
                        (0.5*cos(it + PI * (0.5 + it))).absoluteValue.toFloat(),
                        (0.1*cos(it + PI * (0.5 + sin(it)))).absoluteValue.toFloat(),
                        (2 * atan(it*tan(it) + PI * (tan(it)*tan(it))) / PI).absoluteValue.toFloat(),
                    ).brighter()
            },
            "yellow-green" to {
                if (it == 1f) Color.BLACK else
                    Color(
                        (2 * asin(it + PI * (sin(it))) / PI).absoluteValue.toFloat(),
                        (2 * atan(it + PI * (tan(it))) / PI).absoluteValue.toFloat(),
                        (2 * acos(it + PI * (cos(it))) / PI).absoluteValue.toFloat()
                    )
            }
        )

        mainPanel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                fp.plane?.width = mainPanel.width
                fp.plane?.height = mainPanel.height

                fp.previous_img = null
                mainPanel.repaint()
            }
        })
        mainPanel.addSelectedListener { rect ->
            fp.plane?.let {
                val pxMin = it.xMin
                val pxMax = it.xMax
                val pyMin = it.yMin
                val pyMax = it.yMax
                val xMin = Converter.xScr2Crt(rect.x, it)
                val yMax = Converter.yScr2Crt(rect.y, it)
                val xMax = Converter.xScr2Crt(rect.x + rect.width, it)
                val yMin = Converter.yScr2Crt(rect.y + rect.height, it)
                it.xMin = xMin
                it.yMin = yMin
                it.xMax = xMax
                it.yMax = yMax
                fp.maxIteration = (fp.maxIteration*ln((pxMax-pxMin)*(pyMax-pyMin)/((it.xMax-it.xMin)*(it.yMax-it.yMin)))/const).toInt()
                fp.previous_img = null
                mainPanel.repaint()
            }
        }
        mainPanel.background = Color.WHITE
        if (af is Mandelbrot)
            layout = GroupLayout(contentPane).apply {
                setVerticalGroup(
                    createSequentialGroup()
                        .addComponent(menuBar, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
                        .addGap(4)
                        .addComponent(mainPanel)
                        .addGap(8)

                )
                setHorizontalGroup(
                    createParallelGroup()
                        .addComponent(menuBar)
                        .addGroup(
                            createSequentialGroup()
                                .addGap(8)
                                .addComponent(mainPanel)
                                .addGap(8)
                        )
                        .addGap(4)
                )
            }
        else {
            val lblXCord = JLabel("X: ")
            val lblYCord = JLabel("Y: ")
            val mdlXCord = SpinnerNumberModel((af as Julia).x, null, null, 0.00001)
            val mdlYCord = SpinnerNumberModel((af as Julia).y, null, null, 0.00001)
            val spnXCord = JSpinner(mdlXCord)
            val spnYCord = JSpinner(mdlYCord)

            mdlXCord.addChangeListener{
                (af as Julia).x = mdlXCord.value as Double
                fp.previous_img = null
                mainPanel.repaint()
            }
            mdlYCord.addChangeListener{
                (af as Julia).y = mdlYCord.value as Double
                fp.previous_img = null
                mainPanel.repaint()
            }

            spnXCord.setEditor(JSpinner.NumberEditor(spnXCord, "0.00000"));
            spnYCord.setEditor(JSpinner.NumberEditor(spnYCord, "0.00000"));

            layout = GroupLayout(contentPane).apply {
                setVerticalGroup(
                    createSequentialGroup()
                        .addComponent(menuBar, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
                        .addGap(4)
                        .addComponent(mainPanel)
                        .addGap(4)
                        .addGroup(
                            createParallelGroup()
                                .addComponent(lblXCord)
                                .addComponent(spnXCord, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
                                .addComponent(lblYCord)
                                .addComponent(spnYCord, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
                        )
                        .addGap(8)


                )
                setHorizontalGroup(
                    createParallelGroup()
                        .addComponent(menuBar)
                        .addGroup(
                            createSequentialGroup()
                                .addGap(8)
                                .addComponent(mainPanel)
                                .addGap(8)
                        )
                        .addGroup(
                            createSequentialGroup()
                                .addGap(8)
                                .addComponent(lblXCord)
                                .addGap(4)
                                .addComponent(spnXCord)
                                .addGap(4)
                                .addComponent(lblYCord)
                                .addGap(4)
                                .addComponent(spnYCord)
                                .addGap(8)
                        )
                        .addGap(4)
                )
            }

        }
        pack()
        fp.plane = Plane(-2.0, 1.0, -1.0, 1.0, mainPanel.width, mainPanel.height)
        fp.pointColor = themes["green"]!!

//        fp.pointColor = {
//            if (it == 1f) Color.BLACK else
//                Color(
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                    (2*asin(it + PI*(tan(it)))/PI).absoluteValue.toFloat(),
//                    (2* atan(it + PI*(1-cos(it))) / PI).absoluteValue.toFloat(),
//                    (2*acos(it+ PI*(1-sin(it)))/PI).absoluteValue.toFloat(),
//                )
//        }
        MovieMaker.fpp = fp
    }

/*
удачные темы

красная:
cos(it+PI*(0.5+sin(it))).absoluteValue.toFloat(),
cos(it + PI*(0.5+cos(it))).absoluteValue.toFloat(),
(0.1*cos(it)).absoluteValue.toFloat(),

сиреневенькое
cos(it + PI*(0.5 + it)).absoluteValue.toFloat(),
                    (2*atan(it + PI*(tan(it)))/ PI).absoluteValue.toFloat(),
                    cos(it+PI*(0.5+sin(it))).absoluteValue.toFloat(),

желто-зеленый
(2*asin(it + PI*(sin(it)))/PI).absoluteValue.toFloat(),
                    (2*atan(it + PI*(tan(it)))/ PI).absoluteValue.toFloat(),
                    (2*acos(it+ PI*(cos(it)))/PI).absoluteValue.toFloat(),
 */


    private fun createMenuBar(): JMenuBar {
        val menuBar = JMenuBar()
        this.add(menuBar)
        val file = JMenu("Файл")
        file.setMnemonic('Ф')
        menuBar.add(file)

        val saveJPG = JMenuItem("Сохранить картинку")
        file.add(saveJPG)
        saveJPG.addActionListener { _: ActionEvent -> saveJPGFunc() }

        val save = JMenuItem("Сохранить проект")
        file.add(save)
        save.addActionListener { _: ActionEvent -> saveFunc() }

        val load = JMenuItem("Загрузить проект")
        file.add(load)
        load.addActionListener { _: ActionEvent -> loadFunc() }

        val edit = JMenu("Изменить")
        edit.setMnemonic('И')
        menuBar.add(edit)

        val undo = JMenuItem("Назад")
        edit.add(undo)
        undo.addActionListener { _: ActionEvent -> undoFunc() }

        val redo = JMenuItem("Вперёд")
        edit.add(redo)
        redo.addActionListener { _: ActionEvent -> redoFunc() }

        val theme = JMenu("Тема")
        edit.add(theme)
        theme.setMnemonic('Т')

        val greenTheme = JMenuItem("Зелёная тема")
        theme.add(greenTheme)
        greenTheme.setMnemonic('З')
        greenTheme.addActionListener { _: ActionEvent ->
            fp.pointColor = themes["green"]!!
            fp.previous_img = null
            mainPanel.repaint()
        }

        val redTheme = JMenuItem("Красная тема")
        theme.add(redTheme)
        redTheme.setMnemonic('К')
        redTheme.addActionListener { _: ActionEvent ->
            fp.pointColor = themes["red"]!!
            fp.previous_img = null
            mainPanel.repaint()
        }

        val lilacTheme = JMenuItem("Красно-синяя тема")
        theme.add(lilacTheme)
        lilacTheme.setMnemonic('С')
        lilacTheme.addActionListener { _: ActionEvent ->
            fp.pointColor = themes["red-blue"]!!
            fp.previous_img = null
            mainPanel.repaint()
        }

        val yellowGreenTheme = JMenuItem("Желто-зелёная тема")
        theme.add(yellowGreenTheme)
        yellowGreenTheme.setMnemonic('Ж')
        yellowGreenTheme.addActionListener { _: ActionEvent ->
            fp.pointColor = themes["yellow-green"]!!
            fp.previous_img = null
            mainPanel.repaint()
        }

        val observe = JMenu("Обозреть")
        observe.setMnemonic('О')
        menuBar.add(observe)
        observe.addActionListener { _: ActionEvent -> joulbertFunc() }

        if(af !is Julia) {
            val joulbert = JMenuItem("Отрисовать множество Жюльберта")
            joulbert.setMnemonic('Ж')
            joulbert.addActionListener { _: ActionEvent -> joulbertFunc() }
            observe.add(joulbert)
        }

        val view = JMenuItem("Экскурсия")
        view.setMnemonic('Э')
        view.addActionListener { _: ActionEvent -> viewFunc() }
        observe.add(view)

        /*
        val joulbert = JMenuItem("Жюльберт")

        joulbert.setMnemonic('Ж')
        menuBar.add(joulbert)
        joulbert.addActionListener { _: ActionEvent -> joulbertFunc() }
        */
        /*
        val joulbertBtn = JButton("Отрисовать множество Жюльберта")
        joulbertBtn.addActionListener { joulbertFunc() }
        this.add(joulbertBtn)

        val viewBtn = JButton("Экскурсия по фракталу")
        viewBtn.addActionListener { viewFunc() }
        viewBtn.alignmentX = RIGHT_ALIGNMENT
        //viewBtn.alignmentY = RIGHT_ALIGNMENT
        this.add(viewBtn)
        * */

        return menuBar
    }

    private fun loadFunc() {

    }

    private fun joulbertFunc() {
        Window(Julia()).apply { isVisible = true
            title = "Множество Жюлиа"
        }
    }

    private fun redoFunc() {

    }
    private fun saveJPGFunc(){
        val fileChooser = JFileChooser()
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
        val ok = fileChooser.showSaveDialog(null)
        if (ok==0) {
            var path: String? = fileChooser.selectedFile.toString()
            if (path.isNullOrEmpty() ||
                path.length < 5
            ) path = null
            else if (!path.endsWith('\\')) path += "\\fractal.jpg"
            else if (!path.endsWith(".jpg")) path += ".jpg"

            var bufferedImage = BufferedImage(
                fp.width + 10,
                fp.height + 40,
                BufferedImage.TYPE_INT_RGB
            )
            val g: Graphics = bufferedImage.createGraphics().also {
                it.color = Color.WHITE
            }
            fp.previous_img?.let {
                g.drawImage(
                    it,
                    10,
                    0,
                    null
                )
                //g.drawLine(0, 0, 0, bufferedImage.height)

                fp.plane?.let { plane ->
                    val epsY = Converter.yScr2Crt(0, plane) - Converter.yScr2Crt(1, plane)
                    var step = (Converter.yScr2Crt(fp.height, plane) - Converter.yScr2Crt(0, plane)) / 8.0
                    for (yS in 0..fp.height) {
                        val y = Converter.yScr2Crt(yS, plane)
                        var h = 5
                        if (abs(y % step) < epsY) {
                            if (abs(y % (2 * step)) < epsY) {
                                h += 5
                            }
                            g.drawLine(0, yS, h, yS)
                        }
                    }

                    val string1 = "XMin = ${Converter.xScr2Crt(0, plane)}," +
                            " XMax = ${Converter.xScr2Crt(fp.width, plane)}"
                    val string2 = "YMin = ${Converter.yScr2Crt(0, plane)}," +
                            " YMax = ${Converter.yScr2Crt(fp.height, plane)}"
                    with(g.fontMetrics.getStringBounds(string1, g)) {
                        g.drawString(
                            string1,
                            ((fp.width / 2) - width / 2).toInt(),
                            (bufferedImage.height - height).toInt()
                        )
                        g.drawString(
                            string2,
                            ((fp.width / 2) - width / 2).toInt(),
                            (bufferedImage.height).toInt()
                        )

//                        g.drawLine(0,
//                            (bufferedImage.height - 2*height).toInt(),
//                            bufferedImage.width,
//                            (bufferedImage.height - 2*height).toInt()
//                        )

                        val epsX = Converter.xScr2Crt(1, plane) - Converter.xScr2Crt(0, plane)
                        step = (Converter.xScr2Crt(fp.width, plane) - Converter.xScr2Crt(0, plane)) / 8.0
                        for (xS in 0..fp.width) {
                            val x = Converter.xScr2Crt(xS, plane)
                            var h = 5
                            if (abs(x % step) < epsX) {
                                if (abs(x % (2 * step)) < epsX) {
                                    h += 5
                                }
                                g.drawLine(
                                    xS, (bufferedImage.height - 2 * height).toInt(),
                                    xS, (bufferedImage.height - 2 * height).toInt() - h
                                )
                            }
                        }
                    }
                }
            }

            path?.let {
                ImageIO.write(bufferedImage, "jpg", File(it))
            }
        }
    }

    private fun saveFunc() {

    }

    private fun viewFunc() {
        FractalTourMenu()
    }

    private fun undoFunc() {

    }

}