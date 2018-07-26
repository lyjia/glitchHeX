package co.tcore.glitchhex.controls

import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.IOException
import java.io.InputStream
import java.lang.System

import javax.imageio.ImageIO
import javax.swing.JPanel

class ImagePanel(bytesToInit: InputStream) : JPanel() {

    private var image: BufferedImage? = null

    init {
        readImage(bytesToInit)
    }

    fun readImage(bytes: InputStream) {
        try {
            this.image = ImageIO.read(bytes)
            if (image != null) {
                setSize(image!!.width, image!!.height)
            }
            repaint()
        } catch (ex: IOException) {
            System.out.println("Exception rendering image: ${ex.message}")
            this.image = null
        } catch (ex: ArrayIndexOutOfBoundsException) {
            System.out.println("Exception rendering image: ${ex.message}")
            this.image = null
        }
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        if (image != null) {
            val posx = 0
            val posy = 0
            g?.drawImage(this.image, posx, posy, this.image!!.width, this.image!!.height, this)
        }

    }

}
