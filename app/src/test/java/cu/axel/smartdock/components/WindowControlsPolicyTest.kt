package cu.axel.smartdock.components

import cu.axel.smartdock.models.WINDOWING_MODE_FREEFORM
import cu.axel.smartdock.models.WINDOWING_MODE_FULLSCREEN
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WindowControlsPolicyTest {
    private val launcher = "com.sec.android.app.launcher"
    private val own = "dev.isidro.classicdock"

    private fun task(pkg: String, mode: Int = WINDOWING_MODE_FULLSCREEN, id: Int = 7) =
        ForegroundTask(id, pkg, mode)

    @Test
    fun appEnPantallaCompletaEsObjetivo() {
        val t = task("com.brave.browser")
        assertEquals(t, WindowControlsPolicy.targetTask(t, launcher, own))
    }

    @Test
    fun sinTareaNoHayObjetivo() {
        assertNull(WindowControlsPolicy.targetTask(null, launcher, own))
    }

    @Test
    fun launcherSystemUiYClassicDockNoSonObjetivo() {
        assertNull(WindowControlsPolicy.targetTask(task(launcher), launcher, own))
        assertNull(WindowControlsPolicy.targetTask(task("com.android.systemui"), launcher, own))
        assertNull(WindowControlsPolicy.targetTask(task(own), launcher, own))
    }

    @Test
    fun ventanaFlotanteOcultaLaBarra() {
        val t = task("com.brave.browser", WINDOWING_MODE_FREEFORM)
        assertNull(WindowControlsPolicy.targetTask(t, launcher, own))
    }

    @Test
    fun otrosModosMuestranLaBarra() {
        val t = task("com.brave.browser", mode = 6)
        assertEquals(t, WindowControlsPolicy.targetTask(t, launcher, own))
    }

    @Test
    fun idInvalidoONuloNoEjecutaLaAccion() {
        var llamadas = 0
        WindowControlsPolicy.runOnTask(task("a", id = -1)) { llamadas++ }
        WindowControlsPolicy.runOnTask(task("a", id = 0)) { llamadas++ }
        WindowControlsPolicy.runOnTask(null) { llamadas++ }
        assertEquals(0, llamadas)
    }

    @Test
    fun idRealEjecutaLaAccionConEseId() {
        var recibido = -1
        WindowControlsPolicy.runOnTask(task("a", id = 42)) { recibido = it }
        assertEquals(42, recibido)
    }

    @Test
    fun cerrarConLauncherAlFrenteNoQuitaNada() {
        var llamadas = 0
        val top = task(launcher, id = 9)
        WindowControlsPolicy.runOnTask(WindowControlsPolicy.targetTask(top, launcher, own)) { llamadas++ }
        assertEquals(0, llamadas)
    }
}
