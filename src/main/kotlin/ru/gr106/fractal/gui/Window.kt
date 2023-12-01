package ru.gr106.fractal.gui

import ru.smak.drawing.Converter
import ru.smak.drawing.Plane
import math.Mandelbrot
import org.jcodec.common.model.Plane
import ru.gr106.fractal.main
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.MenuBar
import java.awt.event.*
import java.util.*
import javax.swing.GroupLayout
import javax.swing.GroupLayout.PREFERRED_SIZE
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.event.MenuListener
import kotlin.math.*

class Window : JFrame() {

    private val mainPanel: DrawingPanel
    private val fp: FractalPainter
    private var cancelAction: Stack<Map<Pair<Double, Double>, Pair<Double, Double>>>
    private val xyCorr: Double
    private var xMin = 0.0
    private var xMax = 0.0
    private var yMin = 0.0
    private var yMax = 0.0

    init{
        fp = FractalPainter(Mandelbrot)
        val menuBar = createMenuBar()
        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = Dimension(600, 550)
        mainPanel = DrawingPanel(fp)
        val p = Plane(-2.0, 1.0, -1.0, 1.0, mainPanel.width, mainPanel.height)
        xMin = p.xMin
        yMin = p.yMin
        xMax = p.xMax
        yMax = p.yMax
        xyCorr = abs(xMax - xMin) / abs(yMax - yMin)
        cancelAction = Stack<Map<Pair<Double, Double>, Pair<Double, Double>>>()


        mainPanel.addSelectedListener {rect ->
            fp.plane?.let {
                val xMin = Converter.xScr2Crt(rect.x, it)
                val yMax = Converter.yScr2Crt(rect.y, it)
                val xMax = Converter.xScr2Crt(rect.x + rect.width, it)
                val yMin = Converter.yScr2Crt(rect.y + rect.height, it)
                it.xMin = xMin
                it.yMin = yMin
                it.xMax = xMax
                it.yMax = yMax
                val mapOfCoord = mutableMapOf<Pair<Double, Double>, Pair<Double, Double>>()
                val pairX = Pair(xMin, xMax)
                val pairY = Pair(yMin, yMax)
                mapOfCoord.put(pairX, pairY)
                cancelAction.push(mapOfCoord)
                fp.previous_img = null
                mainPanel.repaint()
            }
        }
        mainPanel.background = Color.WHITE
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
        pack()
        fp.plane = Plane(-2.0, 1.0, -1.0, 1.0, mainPanel.width, mainPanel.height)
        fp.pointColor = {
            if (it == 1f) Color.BLACK else
            Color(
                0.5f*(1-cos(16f*it*it)).absoluteValue,
                sin(5f*it).absoluteValue,
                log10(1f + 5*it).absoluteValue
            )
        }
    }




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

        val edit = JMenu("Изменить")
        edit.setMnemonic('И')
        menuBar.add(edit)

        val undo = JMenuItem("Назад")
        edit.add(undo)
        undo.addActionListener { _: ActionEvent -> undoFunc() }
        undo.addMouseListener(object: MouseAdapter(){
            override fun mouseClicked(e: MouseEvent?) {
                if (cancelAction.size != 1){
                    cancelAction.pop()
                    var afterRemoval = mutableMapOf<Pair<Double, Double>, Pair<Double, Double>>()
                    afterRemoval = cancelAction.peek() as MutableMap<Pair<Double, Double>, Pair<Double, Double>>
                    val xPair = afterRemoval.keys.toList()
                    val yPair = afterRemoval.values.toList()
                    val xMin = xPair[0].first
                    val xMax = xPair[0].second
                    val yMin = yPair[0].first
                    val yMax = yPair[0].second
                    fp.plane?.xMax = xMax
                    fp.plane?.xMin = xMin
                    fp.plane?.yMax = yMax
                    fp.plane?.yMin = yMin
                }
                mainPanel.repaint()
            }
        })

        val redo = JMenuItem("Вперёд")
        edit.add(redo)
        redo.addActionListener { _: ActionEvent -> redoFunc() }

        val theme = JMenuItem("Тема")
        edit.add(theme)
        theme.setMnemonic('Т')
        theme.addActionListener { _: ActionEvent -> themeFunc()}

        val observe = JMenu("Обозреть")
        observe.setMnemonic('О')
        menuBar.add(observe)
        observe.addActionListener { _: ActionEvent -> joulbertFunc() }

        val joulbert = JMenuItem("Отрисовать множество Жюльберта")
        joulbert.setMnemonic('Ж')
        joulbert.addActionListener { _: ActionEvent -> joulbertFunc()}
        observe.add(joulbert)

        val view = JMenuItem("Экскурсия")
        view.setMnemonic('Э')
        view.addActionListener { _: ActionEvent -> viewFunc()}
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



    private fun joulbertFunc() {

    }

    private fun themeFunc() {

    }

    private fun redoFunc() {

    }
    private fun saveJPGFunc(){

    }
    private fun saveFunc(){

    }

    private fun viewFunc() {

    }

    private fun undoFunc() {

    }

}