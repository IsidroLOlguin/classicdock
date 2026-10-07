package cu.axel.smartdock.components

import cu.axel.smartdock.models.WINDOWING_MODE_FREEFORM
import cu.axel.smartdock.models.WINDOWING_MODE_FULLSCREEN
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WindowControlsPolicyTest {
    private val launcher = "com.sec.android.app.launcher"
    private val own = "dev.isidro.classicdock"

    private fun task(
        pkg: String,
        mode: Int = WINDOWING_MODE_FULLSCREEN,
        id: Int = 7,
        display: Int = 0,
        activity: String = ""
    ) = ForegroundTask(id, pkg, mode, display, activity)

    private fun target(vararg tasks: ForegroundTask, display: Int = 0, launcherPkg: String = launcher) =
        WindowControlsPolicy.targetTask(tasks.toList(), display, launcherPkg, own)

    @Test
    fun appEnPantallaCompletaEsObjetivo() {
        val t = task("com.brave.browser")
        assertEquals(t, target(t))
    }

    @Test
    fun sinTareaNoHayObjetivo() {
        assertNull(target())
    }

    @Test
    fun launcherSystemUiYClassicDockNoSonObjetivo() {
        assertNull(target(task(launcher)))
        assertNull(target(task("com.android.systemui")))
        assertNull(target(task(own)))
    }

    @Test
    fun ventanaFlotanteOcultaLaBarra() {
        assertNull(target(task("com.brave.browser", WINDOWING_MODE_FREEFORM)))
    }

    @Test
    fun otrosModosMuestranLaBarra() {
        val t = task("com.brave.browser", mode = 6)
        assertEquals(t, target(t))
    }

    @Test
    fun tareaDeOtroDisplayNoEsObjetivo() {
        val otro = task("com.brave.browser", display = 2)
        assertNull(target(otro, display = 0))
    }

    @Test
    fun objetivoEsLaPrimeraTareaDeSuPropioDisplay() {
        val otro = task("com.brave.browser", id = 3, display = 2)
        val propia = task("com.google.android.youtube", id = 4, display = 0)
        assertEquals(propia, target(otro, propia, display = 0))
        assertEquals(otro, target(otro, propia, display = 2))
    }

    @Test
    fun launcherDeSuDisplayOcultaLaBarraAunqueOtroDisplayTengaApp() {
        val enOtroDisplay = task("com.brave.browser", id = 3, display = 2)
        val launcherPropio = task(launcher, id = 4, display = 0)
        assertNull(target(enOtroDisplay, launcherPropio, display = 0))
    }

    @Test
    fun displayIlegibleNuncaCoincide() {
        val t = task("com.brave.browser", display = WindowControlsPolicy.INVALID_DISPLAY_ID)
        assertNull(target(t, display = 0))
    }

    @Test
    fun recentsNoEsObjetivoAunqueSuPaqueteNoSeaElLauncher() {
        val recents = task(launcher, activity = WindowControlsPolicy.RECENTS_ACTIVITY)
        assertNull(target(recents, launcherPkg = "com.teslacoilsw.launcher"))
    }

    @Test
    fun otraActividadDelMismoPaqueteSiEsObjetivo() {
        val t = task("com.brave.browser", activity = "com.google.android.apps.chrome.Main")
        assertEquals(t, target(t))
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
        WindowControlsPolicy.runOnTask(target(top)) { llamadas++ }
        assertEquals(0, llamadas)
    }

    @Test
    fun cerrarSinGestorDeActividadesAvisaYNoQuitaNada() {
        var llamadas = 0
        val r = WindowControlsPolicy.close(false, task("a", id = 5)) { llamadas++ }
        assertEquals(CloseResult.UNAVAILABLE, r)
        assertEquals(0, llamadas)
    }

    @Test
    fun cerrarSinGestorYSinTareaTambienAvisa() {
        assertEquals(CloseResult.UNAVAILABLE, WindowControlsPolicy.close(false, null) {})
    }

    @Test
    fun cerrarConGestorQuitaLaTareaConSuId() {
        var recibido = -1
        val r = WindowControlsPolicy.close(true, task("a", id = 42)) { recibido = it }
        assertEquals(CloseResult.CLOSED, r)
        assertEquals(42, recibido)
    }

    @Test
    fun cerrarConGestorSinTareaOIdInvalidoNoHaceNadaNiAvisa() {
        var llamadas = 0
        assertEquals(CloseResult.NO_TARGET, WindowControlsPolicy.close(true, null) { llamadas++ })
        assertEquals(CloseResult.NO_TARGET, WindowControlsPolicy.close(true, task("a", id = -1)) { llamadas++ })
        assertEquals(CloseResult.NO_TARGET, WindowControlsPolicy.close(true, task("a", id = 0)) { llamadas++ })
        assertEquals(0, llamadas)
    }

    @Test
    fun cerrarConBinderMuertoAvisaEnVezDeFallarEnSilencio() {
        val r = WindowControlsPolicy.close(true, task("a", id = 5)) { throw IllegalStateException("binder muerto") }
        assertEquals(CloseResult.UNAVAILABLE, r)
    }
}
