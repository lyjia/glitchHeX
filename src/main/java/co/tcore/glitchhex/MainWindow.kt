package co.tcore.glitchhex

import java.awt.EventQueue
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.*

import org.exbin.deltahex.swing.CodeArea
import org.exbin.utils.binary_data.ByteArrayEditableData
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File


class MainWindow() : JFrame() {

    private val WINDOW_TITLE_PREFIX = "glitchHeX"

    private var fileCurrentPath = ""
    private var fileCurrent: File? = null
    private lateinit var paneHexEditor: CodeArea
    private lateinit var paneImageOuter: JScrollPane

    /* =================================================
    UI SET UP
    ================================================= */

    init {
        createUI()
    }

    private fun createUI() {

        createMenuBar()
        createMainBody()

        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setSize(900, 550)
        setLocationRelativeTo(null)

        setNewWindowTitle(null)

    }

    private fun createMenuBar() {
        val menuMain = JMenuBar()

        /* FILE MENU */

        val menuFile = JMenu("File")
        menuFile.mnemonic = KeyEvent.VK_F

        val menuFileOpen = JMenuItem("Open...")
        menuFileOpen.mnemonic = KeyEvent.VK_O
        menuFileOpen.toolTipText = "Open an image file for editing..."
        menuFileOpen.addActionListener { _: ActionEvent -> onMenuFileOpen() }
        menuFile.add(menuFileOpen)

        val menuFileSaveNewVersion = JMenuItem("Save new version")
        menuFileSaveNewVersion.mnemonic = KeyEvent.VK_S
        menuFileSaveNewVersion.toolTipText = "Saves the current file with a new, automatically-generated filename"
        menuFileSaveNewVersion.addActionListener { _: ActionEvent -> onMenuFileSaveNewVersion() }
        menuFile.add(menuFileSaveNewVersion)

        val menuFileSaveAs = JMenuItem("Save as...")
        menuFileSaveAs.mnemonic = KeyEvent.VK_A
        menuFileSaveAs.toolTipText = "Saves the current file with a new filename of your choosing, and sets that to be the current file."
        menuFileSaveAs.addActionListener { _: ActionEvent -> onMenuFileSaveAs() }
        menuFile.add(menuFileSaveAs)

        menuFile.add(JSeparator())

        val menuFileExit = JMenuItem("Quit")
        menuFileExit.mnemonic = KeyEvent.VK_Q
        menuFileExit.toolTipText = "Exit application"
        menuFileExit.addActionListener { _: ActionEvent -> System.exit(0) }
        menuFile.add(menuFileExit)

        menuMain.add(menuFile)
        jMenuBar = menuMain //adds it to the JFrame

    }

    private fun createMainBody() {
        this.minimumSize = Dimension(750, 300)

        //// the left pane -- hex editor
        this.paneHexEditor = CodeArea()
        this.paneHexEditor.minimumSize = Dimension(680, 300)

        val theData = ByteArrayEditableData("".toByteArray()) //in class header
        paneHexEditor.data = theData

        //// the right pane -- image preview
        this.paneImageOuter = JScrollPane()

        //// main splitter
        val paneSplitMain = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, paneHexEditor, paneImageOuter)
        //paneSplitMain.isOneTouchExpandable = true
        paneSplitMain.dividerLocation = 680
        paneSplitMain.dividerSize = 1

        add(paneSplitMain, BorderLayout.CENTER)


    }



    /* =================================================
    Event Handlers
    ================================================= */
    private fun onMenuFileOpen() {

        val fileChooser = JFileChooser()
        val ret = fileChooser.showOpenDialog(this)

        if (ret == JFileChooser.APPROVE_OPTION) {
            loadFile(fileChooser.selectedFile)
        }

    }

    private fun onMenuFileSaveNewVersion() {

    }

    private fun onMenuFileSaveAs() {

    }

    /* =================================================
        ByteEditor Manipulation
    ================================================= */
    private fun setCurrentFile(theFile: File?, bytes: ByteArray?) {
        if (theFile != null && bytes != null) {
            this.fileCurrentPath = theFile.absolutePath
            this.fileCurrent = theFile
            setNewWindowTitle(theFile)
            val theNewData = ByteArrayEditableData(bytes)
            this.paneHexEditor.data = theNewData
        }
    }

    /* =================================================
        File I/O
    ================================================= */
    private fun loadFile(theFile: File) {
        var failures = 0

        // check that the file is valid
        if (!theFile.exists()) {
            JOptionPane.showMessageDialog(this, "Error: File does not exist!")
            return
        }

        if (!theFile.canRead()) {
            JOptionPane.showMessageDialog(this, "Error: File cannot be read!")
            return
        }

        if (!theFile.isFile) {
            JOptionPane.showMessageDialog(this, "Error: File does not appear to be a valid file!")
            return
        }

        val fStream = theFile.inputStream()
        var bytes = ByteArray(size = theFile.length().toInt())

        try {
            fStream.read(bytes, 0, bytes.size)
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(this, "An error occurred while trying to read the file you supplied: "+e.message)
            failures++
        } finally {
            fStream.close()
        }

        if (failures==0) {
            setCurrentFile(theFile, bytes)
        }

    }

    private fun saveFileNewVersionAuto() {

    }

    private fun saveFileAs(filenameWithPath: String) {

    }

    private fun saveFileOverwrite() {

    }

    private fun flushCurrentBuffer() {

    }

    /* =================================================
        Other
    ================================================= */

    private fun setNewWindowTitle(file: File?) {
        val version = this.javaClass.`package`.implementationVersion
        var filen = "(Nothing loaded)"
        val prefix = this.WINDOW_TITLE_PREFIX

        if (file != null) {
            filen = file.absolutePath
        }

        val final = "$prefix v$version - $filen"
        setTitle(final)
    }

}

private fun createAndShowGUI() {
    val frame = MainWindow()
    frame.isVisible = true
}

fun main(args: Array<String>) {
    EventQueue.invokeLater(::createAndShowGUI)
}