package cu.axel.smartdock.components

import cu.axel.smartdock.models.WINDOWING_MODE_FREEFORM
import cu.axel.smartdock.models.WINDOWING_MODE_FULLSCREEN
import android.os.RemoteException
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
    fun cerrarConLauncherAlFrenteNoQuitaNada() {
        var llamadas = 0
        val r = WindowControlsPolicy.close(true, target(task(launcher, id = 9))) { llamadas++; true }
        assertEquals(CloseResult.NO_TARGET, r)
        assertEquals(0, llamadas)
    }

    @Test
    fun cerrarSinGestorDeActividadesAvisaYNoQuitaNada() {
        var llamadas = 0
        val r = WindowControlsPolicy.close(false, task("a", id = 5)) { llamadas++; true }
        assertEquals(CloseResult.UNAVAILABLE, r)
        assertEquals(0, llamadas)
    }

    @Test
    fun cerrarSinGestorYSinTareaTambienAvisa() {
        assertEquals(CloseResult.UNAVAILABLE, WindowControlsPolicy.close(false, null) { true })
    }

    @Test
    fun cerrarConGestorQuitaLaTareaConSuId() {
        var recibido = -1
        val r = WindowControlsPolicy.close(true, task("a", id = 42)) { recibido = it; true }
        assertEquals(CloseResult.CLOSED, r)
        assertEquals(42, recibido)
    }

    @Test
    fun cerrarConGestorSinTareaOIdInvalidoNoHaceNadaNiAvisa() {
        var llamadas = 0
        assertEquals(CloseResult.NO_TARGET, WindowControlsPolicy.close(true, null) { llamadas++; true })
        assertEquals(CloseResult.NO_TARGET, WindowControlsPolicy.close(true, task("a", id = 0)) { llamadas++; true })
        assertEquals(0, llamadas)
    }

    @Test
    fun removeTaskQueDevuelveFalseNoCuentaComoCerrada() {
        assertEquals(CloseResult.FAILED, WindowControlsPolicy.close(true, task("a", id = 5)) { false })
    }

    @Test
    fun binderMuertoOSinPermisoDeShizukuEsNoDisponible() {
        assertEquals(
            CloseResult.UNAVAILABLE,
            WindowControlsPolicy.close(true, task("a", id = 5)) { throw RemoteException() }
        )
        assertEquals(
            CloseResult.UNAVAILABLE,
            WindowControlsPolicy.close(true, task("a", id = 5)) { throw SecurityException("sin permiso") }
        )
    }

    @Test
    fun otraExcepcionDeRemoveTaskEsFalloDelSistema() {
        val r = WindowControlsPolicy.close(true, task("a", id = 5)) { throw IllegalStateException("raro") }
        assertEquals(CloseResult.FAILED, r)
    }

    @Test
    fun tareaDeFallbackNoSePuedeCerrarYAvisaAunConGestorVivo() {
        var llamadas = 0
        val r = WindowControlsPolicy.close(true, task("a", id = WindowControlsPolicy.FALLBACK_ID)) { llamadas++; true }
        assertEquals(CloseResult.UNAVAILABLE, r)
        assertEquals(0, llamadas)
    }

    private fun fallbackTarget(pkg: String?, display: Int = 0) =
        WindowControlsPolicy.targetTask(listOfNotNull(WindowControlsPolicy.fallbackTask(pkg, display)), display, launcher, own)

    @Test
    fun sinShizukuUnaAppEnPrimerPlanoSigueSiendoObjetivo() {
        assertEquals("com.brave.browser", fallbackTarget("com.brave.browser")?.packageName)
    }

    @Test
    fun sinShizukuLauncherSystemUiYClassicDockNoSonObjetivo() {
        assertNull(fallbackTarget(launcher))
        assertNull(fallbackTarget("com.android.systemui"))
        assertNull(fallbackTarget(own))
        assertNull(fallbackTarget(null))
    }

    @Test
    fun sinShizukuElObjetivoNoTieneIdYCerrarAvisa() {
        val t = fallbackTarget("com.brave.browser")
        assertEquals(-1, t?.id)
        var llamadas = 0
        assertEquals(CloseResult.UNAVAILABLE, WindowControlsPolicy.close(false, t) { llamadas++; true })
        assertEquals(0, llamadas)
    }
}
