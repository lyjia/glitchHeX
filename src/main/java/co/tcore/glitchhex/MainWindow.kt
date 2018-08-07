package co.tcore.glitchhex

import co.tcore.glitchhex.controls.ImagePanel
import org.exbin.deltahex.DataChangedListener
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.*

import org.exbin.deltahex.swing.CodeArea
import org.exbin.utils.binary_data.ByteArrayEditableData
import java.awt.*
import java.awt.event.ComponentAdapter
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import javax.swing.UIManager


class MainWindow() : JFrame() {

    private val WINDOW_TITLE_PREFIX = "glitchHeX"

    val DATESTAMP_STRFTIME = "_yyyyMMdd-HHmmss"
    val DATESTAMP_REGEX = Regex("""_\d{8}\-\d{6}${'$'}""")


    private var fileCurrentPath = ""
    private var fileCurrent: File? = null
    private var fileCurrentChanged = false
    private lateinit var paneHexEditor: CodeArea
    private lateinit var paneImageOuter: JScrollPane
    private lateinit var paneImagePreview: ImagePanel

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
        menuFileOpen.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK)
        menuFile.add(menuFileOpen)

        val menuFileSaveNewVersion = JMenuItem("Save a copy as a new version")
        menuFileSaveNewVersion.mnemonic = KeyEvent.VK_S
        menuFileSaveNewVersion.toolTipText = "Saves the current file with a new, automatically-generated filename"
        menuFileSaveNewVersion.addActionListener { _: ActionEvent -> onMenuFileSaveNewVersion() }
        menuFileSaveNewVersion.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK)
        menuFile.add(menuFileSaveNewVersion)

        val menuFileSaveAs = JMenuItem("Save as...")
        menuFileSaveAs.mnemonic = KeyEvent.VK_A
        menuFileSaveAs.toolTipText = "Saves the current file with a new filename of your choosing, and sets that to be the current file."
        menuFileSaveAs.addActionListener { _: ActionEvent -> onMenuFileSaveAs() }
        menuFileSaveAs.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK + KeyEvent.CTRL_DOWN_MASK)
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

        UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName() )

        this.minimumSize = Dimension(750, 300)

        //// the left pane -- hex editor
        this.paneHexEditor = CodeArea()
        this.paneHexEditor.minimumSize = Dimension(680, 300)
        this.paneHexEditor.font = Font("Fixedsys Excelsior 3.01", 0, 16)

        val theData = ByteArrayEditableData("".toByteArray()) //in class header
        paneHexEditor.data = theData

        //// the right pane -- image preview
        this.paneImagePreview = ImagePanel( paneHexEditor.data.dataInputStream )
        this.paneImagePreview.layout = GridBagLayout()
        this.paneImageOuter = JScrollPane()
        val gbc = GridBagConstraints()
        this.paneImageOuter.add(this.paneImagePreview, gbc)

        //// main splitter
        val paneSplitMain = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, paneHexEditor, paneImageOuter)
        //paneSplitMain.isOneTouchExpandable = true
        paneSplitMain.dividerLocation = 680
        paneSplitMain.dividerSize = 1

        contentPane.add(paneSplitMain, BorderLayout.CENTER)

        // set event handlers last (because of lateinit)
        this.paneHexEditor.addDataChangedListener { onDataChanged() }

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
        saveFileNewVersionAuto()
    }

    private fun onMenuFileSaveAs() {

        val choose = JFileChooser()
        val ret = choose.showSaveDialog(this)

        if (ret == JFileChooser.APPROVE_OPTION) {
            saveFileAs(choose.selectedFile)
        }
    }

    private fun onDataChanged() {
        //System.out.println("on data change")
        val o = ByteArrayOutputStream()
        this.paneHexEditor.data.saveToStream( o )
        this.paneImagePreview.readImage( ByteArrayInputStream( o.toByteArray() ) )
        fileCurrentChanged = true
    }

    /* =================================================
        ByteEditor Manipulation
    ================================================= */
    private fun setCurrentFile(theFile: File?, bytes: ByteArray?) {

        if (theFile != null && bytes != null) {

            this.fileCurrentPath = theFile.absolutePath
            this.fileCurrent = theFile
            setNewWindowTitle(theFile)

            // set hex editor
            val theNewData = ByteArrayEditableData(bytes)
            this.paneHexEditor.data = theNewData

//            // set image preview
//            val i = ByteArrayInputStream(bytes)
//            paneImagePreview.readImage( i )

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
        val bytes = ByteArray(size = theFile.length().toInt())

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

    // autogenerates a filename from the current datetime
    private fun saveFileNewVersionAuto() {

        if (fileCurrent != null) {
            var fname = fileCurrent!!.nameWithoutExtension
            val ext = fileCurrent!!.extension

            val curtime = LocalDateTime.now()

            // build date string
            val fmter = DateTimeFormatter.ofPattern(DATESTAMP_STRFTIME)
            val fmted = curtime.format(fmter)

            fname = fname.replace(DATESTAMP_REGEX, "")

            val newFilename = "${fname}${fmted}.${ext}"
            val newFileWithPath = File("${fileCurrent!!.parent}\\${newFilename}")

            //assemble file object and pass to saveAsInternal()
            saveFileInternal(newFileWithPath)


        } else {
            //do nothing
            return
        }


    }

    private fun saveFileAs(filenameWithPath:  File) {
        System.out.println("About to save to ${filenameWithPath.toPath()}")

        if (filenameWithPath.exists()) {
            val ret = JOptionPane.showConfirmDialog(this, "File ${filenameWithPath.toPath()} already exists. Are you sure you want to overwrite it?", "Confirm file overwrite", JOptionPane.YES_NO_OPTION)

            if (ret == JOptionPane.NO_OPTION) {
                return
            }

        }

        saveFileInternal(filenameWithPath)

    }

    private fun saveFileInternal(filenameWithPath: File) {
//
//        if (!filenameWithPath.canWrite()) {
//            JOptionPane.showMessageDialog(this, "Error: Cannot write to designated file: ${filenameWithPath.toPath()}")
//            return
//        }

        val fStream = filenameWithPath.outputStream()
        //System.out.println("on data change")
        val o = ByteArrayOutputStream()
        this.paneHexEditor.data.saveToStream(o)
        fStream.write(o.toByteArray())
        fStream.close()

        fileCurrentChanged = false

        System.out.println("Buffer flushed to ${filenameWithPath.toPath()} successfully")
    }

    private fun flushCurrentBuffer() {

    }

    /* =================================================
        Other
    ================================================= */

    private fun setNewWindowTitle(file: File?) {
        val version = this.javaClass.`package`.implementationVersion
        var filen = "Untitled"
        var ftype : String? = "no format"
        val prefix = this.WINDOW_TITLE_PREFIX

        if (file != null) {
            filen = file.absolutePath
            ftype = FileUtil.identifyFileType(file)
        }

        val final = "$prefix - $filen ($ftype)"
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